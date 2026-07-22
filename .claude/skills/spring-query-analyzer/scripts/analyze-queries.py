#!/usr/bin/env python3
"""
Spring Boot SQL Query Analyzer - generic flow-driven version.

Usage:
    python3 analyze-queries.py                          # run the full flow
    python3 analyze-queries.py --endpoint "POST /api/v1/posts"   # single endpoint
    python3 analyze-queries.py --flow custom-flow.json   # use a different flow file

Requires:
    - test-flow.json (or --flow path) describing the ordered steps to run.
      This file is NOT hand-written by this script. It's proposed by reading
      openapi.json and reviewed/approved by you (see SKILL.md step 2), then
      reused across runs.
    - SQL query logging already enabled and writing to `logs/sql.log`
      (this script checks this precondition and halts with instructions
      if it's not on — it will never write app config files itself).

This script has zero knowledge of your specific domain (no "post", "comment",
"tag" anywhere). Everything endpoint-specific lives in test-flow.json.
"""

import argparse
import json
import re
import subprocess
import sys
import time
from pathlib import Path

# -- Config: the only things you should need to hand-edit here -------------------
BASE_URL = "http://localhost:8080"
SQL_LOG_REL = "logs/sql.log"
OUTPUT_REL = "docs/query_analysis.md"
CURL_TIMEOUT_SECS = 10

# Test credentials used by $CONST substitution in flow files (see SKILL.md).
# Keep these here (not in a separate config file) per project convention.
CREDENTIALS = {
    "USER_EMAIL": "testuser@example.com",
    "USER_PASSWORD": "password123",
    "ADMIN_EMAIL": "admin@blog.com",
    "ADMIN_PASSWORD": "admin123",
}

PAGINATION_QUERY_PARAMS = ("page=", "size=")  # used to detect paginated requests (fact only, no verdict)

# -- Project root discovery: search upward for a build marker, not fixed .parent hops
def find_project_root(start: Path) -> Path:
    cur = start.resolve()
    for _ in range(10):
        if (cur / "pom.xml").exists() or (cur / "build.gradle").exists() or (cur / "build.gradle.kts").exists():
            return cur
        if cur.parent == cur:
            break
        cur = cur.parent
    print("[!] Could not find pom.xml / build.gradle walking up from script location.")
    print("    Falling back to current working directory. If output paths look wrong,")
    print("    run this script from your project root.")
    return Path.cwd()


PROJECT_ROOT = find_project_root(Path(__file__).parent)
SQL_LOG = PROJECT_ROOT / SQL_LOG_REL
OUTPUT = PROJECT_ROOT / OUTPUT_REL


def log(msg):
    print(f"  [*] {msg}")


# -- HTTP -------------------------------------------------------------------------

def curl(method, path, token=None, body=None):
    args = [
        "curl", "-s", "-w", "\n%{http_code}",
        "--max-time", str(CURL_TIMEOUT_SECS),
        "-X", method, f"{BASE_URL}{path}",
    ]
    if token:
        args += ["-H", f"Authorization: Bearer {token}"]
    if body is not None:
        args += ["-H", "Content-Type: application/json", "-d", json.dumps(body)]
    try:
        result = subprocess.run(args, capture_output=True, text=True, timeout=CURL_TIMEOUT_SECS + 5)
    except subprocess.TimeoutExpired:
        return 0, "(request timed out)"
    parts = result.stdout.strip().rsplit("\n", 1)
    status = int(parts[-1]) if len(parts) > 1 and parts[-1].isdigit() else 0
    resp_body = parts[0] if len(parts) > 1 else result.stdout.strip()
    return status, resp_body


def read_sql_log():
    return SQL_LOG.read_text().strip() if SQL_LOG.exists() else ""


def clear_sql_log():
    SQL_LOG.parent.mkdir(parents=True, exist_ok=True)
    SQL_LOG.write_text("")


# -- SQL analysis (this used to be "read the raw dump and eyeball it" — now scripted)

def normalize_query(q: str) -> str:
    """Strip literal values so repeated shapes collapse to the same signature."""
    q = re.sub(r"'[^']*'", "?", q)          # quoted string literals
    q = re.sub(r"\b\d+\b", "?", q)          # numeric literals
    q = re.sub(r"\s+", " ", q).strip().lower()
    return q


def _looks_like_fresh_log_line(line: str) -> bool:
    """Distinguishes a new log entry (timestamp, thread, log level — e.g. a
    bind-parameter TRACE line interleaved with SQL) from a genuine SQL
    continuation line (indented 'from', 'where', 'fetch first', etc. with no
    logger metadata). Used to avoid swallowing log noise into a statement."""
    if re.match(r"^\d{4}-\d{2}-\d{2}", line):          # e.g. "2026-07-19 10:00:00.123"
        return True
    if re.search(r"\b(TRACE|DEBUG|INFO|WARN|ERROR)\b", line):
        return True
    if "---" in line:                                    # Spring Boot's default log separator
        return True
    return False


def split_statements(sql_text: str) -> list:
    """Reassembles full SQL statements, including Hibernate's pretty-printed
    multi-line format (hibernate.format_sql=true), where a single logical
    statement is spread across many lines (e.g. 'select' / 'from' / 'offset' /
    'fetch first' each on their own line). Naively taking only the first
    matching line would silently drop everything after 'select'."""
    if not sql_text:
        return []
    lines = [l.strip() for l in sql_text.splitlines()]
    statements = []
    current = []
    for line in lines:
        if not line:
            continue
        line = re.sub(r"^Hibernate:\s*", "", line)
        if re.match(r"^(select|insert|update|delete)\b", line, re.IGNORECASE):
            if current:
                statements.append(" ".join(current))
            current = [line]
        elif current and not _looks_like_fresh_log_line(line):
            current.append(line)
        # else: stray log line (bind-parameter trace, etc.) before any
        # statement started, or clearly a fresh log entry — ignore it.
    if current:
        statements.append(" ".join(current))
    return statements or [l for l in lines if l]  # fall back to raw lines if nothing matched


def analyze_sql(statements: list, path_hint: str) -> dict:
    """Pure fact collection. No verdicts, no flags, no thresholds.

    This deliberately does NOT decide whether anything is wrong — that requires
    reading the actual controller/service/repository code behind the endpoint,
    which only happens in Step 4 of SKILL.md (Claude, with full context, not a
    fixed rule set). This function's only job is turning a wall of raw SQL text
    into a small, precise set of facts so that step doesn't have to re-derive
    them by eyeballing text.
    """
    normalized = [normalize_query(s) for s in statements]
    shape_counts = {}
    for n in normalized:
        shape_counts[n] = shape_counts.get(n, 0) + 1
    # Only keep shapes that repeated — that's the one grouping operation that's
    # unambiguous arithmetic (not judgment) and saves re-scanning raw text.
    repeated_shapes = {sig: c for sig, c in shape_counts.items() if c > 1}

    is_paginated_endpoint = any(p in path_hint for p in PAGINATION_QUERY_PARAMS)
    has_limit = any(re.search(r"\blimit\b|\btop\b|\bfetch first\b", s, re.IGNORECASE) for s in statements)

    return {
        "count": len(statements),
        "repeated_shapes": repeated_shapes,      # fact: {normalized_sql: occurrence_count}
        "is_paginated_endpoint": is_paginated_endpoint,  # fact: path has page=/size=
        "has_limit_in_sql": has_limit,            # fact: LIMIT/TOP/FETCH FIRST appeared
    }


# -- Templating: $CONST and {state_key} / {{state_key}} substitution --------------

def substitute(value, state: dict):
    if isinstance(value, str):
        for k, v in CREDENTIALS.items():
            value = value.replace(f"${k}", str(v))
        for k, v in state.items():
            if v is not None:
                value = value.replace(f"{{{{{k}}}}}", str(v)).replace(f"{{{k}}}", str(v))
        return value
    if isinstance(value, dict):
        return {k: substitute(v, state) for k, v in value.items()}
    if isinstance(value, list):
        return [substitute(v, state) for v in value]
    return value


def extract_value(resp_json, dotted_path: str):
    cur = resp_json
    for part in dotted_path.split("."):
        if isinstance(cur, dict):
            cur = cur.get(part)
        else:
            return None
    return cur


# -- Flow loading / dependency resolution ------------------------------------------

def load_flow(flow_path: Path) -> dict:
    if not flow_path.exists():
        print(f"[FATAL] Flow file not found: {flow_path}")
        print("        Run the SKILL.md 'propose a flow' step first, or pass --flow.")
        sys.exit(1)
    return json.loads(flow_path.read_text())


HTTP_METHODS = {"get", "post", "put", "patch", "delete", "head", "options"}


def normalize_path_template(path: str) -> str:
    """Strip query string and collapse any {param} to a generic token, so
    openapi's {id} and the flow file's {post_id} compare equal on shape."""
    path = path.split("?", 1)[0]
    return re.sub(r"\{[^}]+\}", "{}", path)


def diff_openapi_vs_flow(openapi_path: Path, flow: dict) -> dict:
    """Pure set comparison — no judgment. Tells you what's out of sync,
    doesn't decide whether that's a problem or block the run."""
    if not openapi_path.exists():
        return {"checked": False}

    try:
        spec = json.loads(openapi_path.read_text())
    except Exception as e:
        return {"checked": False, "error": f"could not parse openapi.json: {e}"}

    openapi_endpoints = set()
    for path, methods in (spec.get("paths") or {}).items():
        for method in methods:
            if method.lower() in HTTP_METHODS:
                openapi_endpoints.add((method.upper(), normalize_path_template(path)))

    flow_endpoints = {
        (s["method"].upper(), normalize_path_template(s["path"]))
        for s in flow["steps"]
    }

    return {
        "checked": True,
        "missing_from_flow": sorted(openapi_endpoints - flow_endpoints),
        "stale_in_flow": sorted(flow_endpoints - openapi_endpoints),
    }


def find_step(steps, method, path):
    method = method.upper()
    for s in steps:
        if s["method"].upper() == method and s["path"] == path:
            return s
    return None


def required_state_keys(step) -> set:
    keys = set(re.findall(r"\{\{?(\w+)\}?\}", step.get("path", "")))
    if step.get("auth"):
        keys.add(step["auth"])
    body = step.get("body")
    if body:
        keys |= set(re.findall(r"\{\{?(\w+)\}?\}", json.dumps(body)))
    return keys


def resolve_dependency_chain(steps, target_step):
    """For single-endpoint mode: find the minimal ordered subset of steps
    needed to satisfy target_step's state requirements."""
    producers = {}
    for s in steps:
        for key in (s.get("extract") or {}):
            producers.setdefault(key, s)

    needed_step_ids = set()
    queue = list(required_state_keys(target_step))
    seen_keys = set()
    while queue:
        key = queue.pop()
        if key in seen_keys:
            continue
        seen_keys.add(key)
        producer = producers.get(key)
        if producer is None:
            continue
        needed_step_ids.add(producer["id"])
        queue.extend(required_state_keys(producer))

    ordered = [s for s in steps if s["id"] in needed_step_ids]
    return ordered


# -- Execution ----------------------------------------------------------------------

def append_output(text):
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with open(OUTPUT, "a") as f:
        f.write(text)


def run_step(step, state, setup_only=False):
    method = step["method"]
    path = substitute(step["path"], state)
    body = substitute(step.get("body"), state)
    token = state.get(step["auth"]) if step.get("auth") else None

    label = step.get("id", f"{method} {path}")
    tag = "[setup] " if setup_only else ""
    log(f"{tag}{label} -> {method} {path}")

    clear_sql_log()
    t0 = time.time()
    status, resp_body = curl(method, path, token, body)
    elapsed = time.time() - t0
    statements = split_statements(read_sql_log())

    resp_json = None
    if resp_body:
        try:
            resp_json = json.loads(resp_body)
        except Exception:
            resp_json = None

    for key, dotted in (step.get("extract") or {}).items():
        if resp_json is not None:
            state[key] = extract_value(resp_json, dotted)

    if setup_only:
        return status, resp_json, {"count": len(statements), "repeated_shapes": {}, "is_paginated_endpoint": False, "has_limit_in_sql": True}

    analysis = analyze_sql(statements, step["path"])

    section = f"### {label}\n\n"
    section += f"**Request:** {method} {path}\n\n"
    section += f"**Response:** HTTP {status} ({elapsed:.2f}s)\n\n"
    if resp_json is not None:
        section += f"```json\n{json.dumps(resp_json, indent=2)[:3000]}\n```\n\n"
    elif resp_body:
        section += f"```\n{resp_body[:500]}\n```\n\n"
    section += f"**SQL Queries:** {analysis['count']}\n\n"
    if analysis["repeated_shapes"]:
        section += "**Repeated query shapes (fact, not a verdict — worth checking against the source):**\n"
        for sig, c in analysis["repeated_shapes"].items():
            section += f"- ran {c}x: `{sig[:150]}`\n"
        section += "\n"
    if analysis["is_paginated_endpoint"]:
        section += f"**Pagination params present in request:** yes — LIMIT/TOP/FETCH FIRST found in SQL: {'yes' if analysis['has_limit_in_sql'] else 'no'}\n\n"
    if statements:
        section += f"```sql\n{chr(10).join(statements)}\n```\n\n"
    section += "---\n\n"
    append_output(section)

    time.sleep(0.2)
    return status, resp_json, analysis


def check_sql_logging_enabled(steps):
    """Precondition check. Never writes app config — halts with instructions."""
    probe = next((s for s in steps if s["method"].upper() == "GET" and not s.get("auth")), steps[0] if steps else None)
    if probe is None:
        return True
    clear_sql_log()
    curl(probe["method"], probe["path"])
    return bool(read_sql_log().strip())


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--endpoint", help='e.g. "POST /api/v1/posts" — run just this one plus its dependencies')
    parser.add_argument("--flow", default="test-flow.json", help="path to the approved flow file")
    args = parser.parse_args()

    flow_path = Path(args.flow)
    if not flow_path.is_absolute():
        flow_path = PROJECT_ROOT / flow_path
    flow = load_flow(flow_path)
    steps = flow["steps"]

    diff = diff_openapi_vs_flow(PROJECT_ROOT / "openapi.json", flow)
    if diff.get("checked"):
        if diff["missing_from_flow"] or diff["stale_in_flow"]:
            print("[DRIFT] openapi.json and test-flow.json are out of sync:")
            for method, path in diff["missing_from_flow"]:
                print(f"        + in openapi.json but not in flow: {method} {path}")
            for method, path in diff["stale_in_flow"]:
                print(f"        - in flow but not in openapi.json (removed/renamed?): {method} {path}")
            print("        Run continuing with the current flow as-is. Update test-flow.json")
            print("        if these endpoints should be covered (see SKILL.md Step 2).")
        else:
            log("test-flow.json is in sync with openapi.json")
    elif diff.get("error"):
        print(f"[!] {diff['error']}")

    if not check_sql_logging_enabled(steps):
        print("[FATAL] SQL query logging isn't producing output in " + str(SQL_LOG))
        print("        This script will not modify your app config. Add a dedicated")
        print("        appender for the SQL logger, routed to " + SQL_LOG_REL + ", with a")
        print("        BARE message pattern (no timestamp/thread/logger prefix) — this")
        print("        script parses that file, and a normal logging pattern will break it:")
        print()
        print("        logger:  org.hibernate.SQL, level DEBUG")
        print("        pattern: %msg%n   (message only — nothing else)")
        print()
        print("        Don't enable org.hibernate.orm.jdbc.bind TRACE for this appender —")
        print("        it's not needed (the SQL logger already logs '?' placeholders) and")
        print("        just interleaves extra lines this script would have to filter out.")
        print()
        print("        Restart the app, confirm queries appear in the log on a manual")
        print("        request, then re-run this script.")
        sys.exit(1)

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(f"# SQL Query Analysis Report\n\nAnalyzed at: {time.strftime('%Y-%m-%d %H:%M:%S')}\n\n---\n\n")

    state = {}

    if args.endpoint:
        method, path = args.endpoint.split(" ", 1)
        target = find_step(steps, method, path)
        if target is None:
            print(f"[FATAL] No step matching '{args.endpoint}' found in {flow_path.name}.")
            print("        Add it to the flow file first (see SKILL.md).")
            sys.exit(1)
        setup_steps = resolve_dependency_chain(steps, target)
        log(f"Resolved {len(setup_steps)} setup step(s) required before target endpoint")
        for s in setup_steps:
            run_step(s, state, setup_only=True)
        run_step(target, state, setup_only=False)
    else:
        for s in steps:
            run_step(s, state, setup_only=False)

    log(f"Done. Report: {OUTPUT.relative_to(PROJECT_ROOT)}")


if __name__ == "__main__":
    main()