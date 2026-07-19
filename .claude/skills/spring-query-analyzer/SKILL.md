---
name: spring-query-analyzer
description: >
  Analyzes SQL query efficiency for Spring Boot applications. Use this skill whenever
  the user wants to discover N+1 problems, missing optimizations, or inefficient JPA
  queries across their endpoints. Triggers on: "analyze my queries", "check for N+1",
  "review my SQL", "audit my JPA", "run the query analyzer", "check endpoint efficiency",
  "hit endpoints and log queries", or any request to inspect Hibernate query behavior
  in a Spring Boot project. Always use this skill when the user has an openapi.json
  and wants automated query analysis.
---

# Spring Boot SQL Query Analyzer

## What to do

1. Read `openapi.json` from the project root — parse all endpoints, methods, path params, request bodies, schemas
2. Read `src/main/resources/data.sql` — find the seed user credentials (email + password)
3. Check if `src/main/resources/logback-spring.xml` exists with SQL logging configured. If not, create it and ask the user to restart the app.
4. Check if the app is running on `localhost:8080`
5. Run `scripts/analyze-queries.py` — this script hits every endpoint in logical order and captures SQL logs + responses into `docs/query_analysis.md`
6. Read `docs/query_analysis.md` and analyze each endpoint's SQL queries
7. Write inline analysis comments in the same file
8. Present findings to the user

## How the script works

The script at `scripts/analyze-queries.py` executes the following phases:

### Phase 1: Register a normal user
- `POST /auth/register` — creates a fresh test user
- `POST /auth/login` — logs in as that user, captures JWT_user

### Phase 2: Create data and hit read/write endpoints
All using JWT_user (normal user permissions):

| # | Endpoint | Purpose |
|---|----------|---------|
| 1 | `GET /tags` | Read tags (public) |
| 2 | `POST /posts` | Create post 1 (draft, with tags "java", "spring") |
| 3 | `POST /posts` | Create post 2 (to be published, tag "java") |
| 4 | `PATCH /posts/{id}/publish` | Publish post 2 |
| 5 | `GET /posts?page=0&size=10` | List posts (paginated) |
| 6 | `GET /posts/{id}` | Single post detail |
| 7 | `GET /posts/{id}/comments` | Get comments on published post |
| 8 | `POST /posts/{id}/comments` | Add a comment |
| 9 | `POST /posts/{id}/like` | Like the post |
| 10 | `GET /posts/{id}/likes/count` | Get like count |
| 11 | `GET /posts/me?page=0&size=10` | Authenticated user's posts |
| 12 | `GET /tags/{tagName}/posts` | Posts by tag name |
| 13 | `PUT /posts/{id}` | Update the post |
| 14 | `GET /test` | Basic auth check |
| 15 | `GET /test/user-dashboard` | ROLE_USER access |
| 16 | `GET /test/create-post` | POST_CREATE privilege |
| 17 | `GET /test/delete-user` | USER_MANAGE (expect 403) |

### Phase 3: Cleanup
| # | Endpoint | Purpose |
|---|----------|---------|
| 18 | `DELETE /posts/{postId}/comments/{commentId}` | Delete the comment |
| 19 | `PATCH /posts/{id}/unpublish` | Unpublish |
| 20 | `DELETE /posts/{id}` | Delete the post |

### Phase 4: Admin operations
- `POST /auth/login` as admin — captures JWT_admin
| # | Endpoint | Purpose |
|---|----------|---------|
| 21 | `GET /admin` | Admin dashboard |
| 22 | `GET /test/admin-dashboard` | ROLE_ADMIN access |
| 23 | `GET /posts` (re-list) | Find remaining posts |
| 24 | `DELETE /admin/posts/{id}` | Admin delete a post |

### Data flow between calls
The script maintains a `state` dict with `post_id`, `comment_id`, `comment_post_id`. When a POST/PATCH returns a JSON body with an `id` field, it's stored in state. Subsequent endpoints substitute `{post_id}` and `{comment_id}` in their URL paths with the real values.

This means every endpoint is hit regardless of seed data — the script creates its own data.

### For each individual endpoint call
1. Clear `logs/sql.log` (wipe previous queries)
2. Run `curl` with the proper method, URL, headers (JWT if needed), and request body
3. Read `logs/sql.log` (capture whatever Hibernate logged)
4. Append to `docs/query_analysis.md`:
   - Endpoint label
   - Actual HTTP request (method + URL)
   - Response status code + elapsed time
   - Pretty-printed response body
   - Raw SQL queries captured

## Analysis rules

After the script runs, read `docs/query_analysis.md`. For each endpoint's section, add an inline analysis comment:

```markdown
<!-- ANALYSIS:
  Queries: N
  Verdict: ✅ Clean | ⚠️ N+1 | ⚠️ Missing optimization | ❌ Bug
  Issues:
    - description with fix suggestion
-->
```

Verdict meanings:
- ✅ **Clean**: expected number of queries, no N+1, pagination at DB level
- ⚠️ **N+1**: repetitive SELECTs for lazy associations — suggest `@EntityGraph` or `@BatchSize`
- ⚠️ **Missing optimization**: full entity load when `existsBy`/`countBy`/`getReferenceById` would suffice
- ❌ **Bug**: in-memory pagination (no LIMIT/OFFSET in SQL), cartesian product from multiple to-many joins

## Deliver to user

Give a summary of findings ordered by severity, pointing to exact files and line numbers for each fix.