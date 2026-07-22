---
name: spring-query-analyzer
description: >
  Analyzes SQL query efficiency for Spring Boot applications by executing endpoints
  in a known-good order and inspecting the generated SQL. Use whenever the user wants
  to discover N+1 problems, missing pagination at the DB level, or inefficient JPA
  queries. Triggers on: "analyze my queries", "check for N+1", "review my SQL",
  "audit my JPA", "run the query analyzer", "check endpoint efficiency", "test all
  endpoints", "test endpoint X", or any request to inspect Hibernate query behavior
  in a Spring Boot project with an openapi.json present.
---

# Spring Boot SQL Query Analyzer

Two things drive this skill:
- `openapi.json` — the endpoint shapes (paths, methods, schemas). Copied over by the
  user after their app is fully started.
- `test-flow.json` — the call ORDER and data-chaining (e.g. create a post before
  commenting on it). openapi.json cannot express this; it has to be proposed and
  approved by a human once, then reused.

Never hardcode endpoint-specific logic (field names, business flow) into the script.
That knowledge belongs in `test-flow.json` only.

## Mode detection

- User says something like "test all endpoints" / "run the analyzer" / "analyze my
  queries" → **full-flow mode**.
- User names a specific endpoint ("test POST /posts", "check the publish endpoint")
  → **single-endpoint mode**. Pass it through as `--endpoint "METHOD /path"`.

## Step 1: Preconditions

1. Confirm `openapi.json` exists in the project root. If not, ask the user to copy
   it over (they do this manually after the app starts — don't try to fetch it).
2. Confirm the app responds on `localhost:8080` (or whatever `BASE_URL` is set to
   in the script).
3. Do NOT check or create logback/SQL-logging config yourself. The script checks
   this at runtime and halts with exact instructions if logging isn't producing
   output — that's intentional. Writing app config files is not this skill's job.

## Step 2: Get or propose test-flow.json

Check if `test-flow.json` exists in the project root.

**If it exists:** use it as-is. Skip to Step 3.

**If it doesn't exist (or the user asks to regenerate it):**
1. Read `openapi.json` and enumerate all paths/methods/schemas.
2. Propose an execution order as a draft `test-flow.json`, following the format in
   `reference/test-flow.example.json`. Use your judgment on dependencies — e.g. a
   `POST` that creates a resource should come before `GET`/`PATCH`/`DELETE` on
   `{id}` paths that reference it; auth/login steps come first; admin-only steps
   come after user-flow steps; destructive/cleanup steps (`DELETE`) come last so
   earlier steps in the same run still have valid data to work with.
3. For each step's `auth` field, reference the state key (not literal "user"/
   "admin") that a prior `login` step's `extract` produces.
4. Show the user the proposed flow (endpoint list + order, not necessarily the
   full JSON) and ask them to confirm or edit it.
5. Once approved, write it to `test-flow.json` in the project root. This file is
   reused on every future run.

**Drift is checked automatically, every run** — the script compares
`openapi.json` against `test-flow.json` before executing anything and prints
what's out of sync (new endpoints not yet in the flow, or flow entries that no
longer exist in the spec). It does not block the run or edit the flow file
itself. Read that output: if there's drift, tell the user what's missing/stale
and offer to propose additions (same process as above) before or after the
test run, their call — don't silently ignore it.

## Step 3: Run the script

Full flow:
```
python3 scripts/analyze-queries.py
```

Single endpoint:
```
python3 scripts/analyze-queries.py --endpoint "POST /api/v1/posts"
```

The script:
- Checks `openapi.json` vs `test-flow.json` for drift first, every run, and
  prints it — read this output and relay any drift to the user (see Step 2).
- Halts immediately with setup instructions if SQL logging isn't active — read
  that output and relay it to the user rather than trying to fix it yourself.
- In single-endpoint mode, resolves and silently runs only the setup steps needed
  (auth, required IDs) before hitting and fully analyzing the target endpoint.
- Writes `docs/query_analysis.md` with, per endpoint: request/response, exact
  query count, and **plain facts only** — no verdict, no "N+1_SUSPECTED" label,
  no judgment. Specifically:
    - the raw SQL statements
    - which normalized query shapes repeated (and how many times) — this is the
      one grouping the script does for you, purely to save you re-scanning a wall
      of SQL text for duplicates; it does not claim a repeat is a bug
    - whether the request had pagination params and whether LIMIT/OFFSET showed
      up in the SQL — again, a fact, not a verdict

  The script deliberately does NOT decide whether any of this is a problem.
  Whether a repeated query is a real N+1, or an intentional/acceptable pattern,
  or something else entirely (over-fetched columns, an unnecessary join, a
  cartesian product, a missing index) requires looking at the actual code —
  and that's not something a fixed set of Python `if` statements can keep up
  with as the codebase evolves. That judgment belongs to Step 4.

## Step 4: Judge using the real source, not a rule set

This is the step that actually decides whether something's wrong. Do this per
endpoint that has at least one query — don't skip straight from the facts to a
conclusion.

1. Map the endpoint back to its actual code: find the `@RestController` method
   for this path, then trace into the service method it calls, then the
   repository method(s) it uses. Read all of them.
2. With that code open, look at the facts the script gathered (query count,
   repeated shapes, pagination fact) and reason about *why* the SQL looks the
   way it does, grounded in what the code is actually doing — not a checklist.
   Things worth considering, not an exhaustive list:
    - A repeated query shape is likely a real N+1 if it comes from iterating a
      lazy-loaded collection in a loop; it might be entirely fine if it's, say,
      an idempotent check called deliberately for each item for a legitimate
      reason the code makes clear.
    - Over-fetching: does the query pull columns/associations the endpoint's
      response DTO doesn't even use?
    - Unnecessary joins, cartesian products from multiple to-many fetches in one
      query, in-memory filtering/pagination where a repository query with
      `Pageable`/`existsBy`/`countBy` would push the work to the DB instead.
    - Anything that doesn't fit a named pattern at all — that's fine, describe
      it in your own words. You're not limited to a fixed label set.
3. If nothing looks wrong, say so plainly — don't manufacture a finding because
   the report exists.
4. For real findings, name the fix concretely: `@EntityGraph`, `@BatchSize`,
   `JOIN FETCH`, a projection/DTO query instead of full entity load, a real
   `Pageable`-based query, etc. — whatever actually fits what you read in the
   code, not a fix picked because it matches a flag name.

## Step 5: Deliver

Summary ordered by your own judgment of severity (real problems first, minor
things last, clean endpoints just listed briefly), each finding pointing to the
exact file/method to fix, with a one-line suggested change. Endpoints you judged
clean don't need more than a line — don't pad the report to look thorough.