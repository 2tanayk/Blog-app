#!/usr/bin/env python3
"""
Spring Boot SQL Query Analyzer - hits all endpoints and captures SQL logs.
Usage:  python3 scripts/analyze-queries.py
"""

import json, re, sys, time, subprocess
from pathlib import Path

# -- Config ----------------------------------------------------------------------
BASE_URL = "http://localhost:8080"
SQL_LOG = "logs/sql.log"
OUTPUT = "docs/query_analysis.md"
ADMIN_EMAIL = "admin@blog.com"
ADMIN_PASSWORD = "admin123"
USER_EMAIL = "testuser@example.com"
USER_PASSWORD = "password123"

PROJECT_ROOT = Path(__file__).resolve().parent.parent.parent.parent.parent

# -- Helpers ---------------------------------------------------------------------

def log(msg):
    print(f"  [*] {msg}")

def curl(method, path, token=None, body=None):
    args = ["curl", "-s", "-w", "\n%{http_code}", "-X", method, f"{BASE_URL}{path}"]
    if token:
        args += ["-H", f"Authorization: Bearer {token}"]
    if body is not None:
        args += ["-H", "Content-Type: application/json", "-d", json.dumps(body)]
    result = subprocess.run(args, capture_output=True, text=True, cwd=PROJECT_ROOT)
    parts = result.stdout.strip().rsplit("\n", 1)
    status = int(parts[-1]) if len(parts) > 1 else 0
    resp_body = parts[0] if len(parts) > 1 else ""
    return status, resp_body

def read_sql_log():
    p = Path(PROJECT_ROOT) / SQL_LOG
    return p.read_text().strip() if p.exists() else ""

def clear_sql_log():
    Path(PROJECT_ROOT / SQL_LOG).write_text("")

def append_output(text):
    p = Path(PROJECT_ROOT) / OUTPUT
    p.write_text(p.read_text() + "\n" + text)

def extract_ids_from_page(resp_body):
    try:
        data = json.loads(resp_body)
        return [item.get("id") for item in data.get("content", []) if item.get("id")]
    except:
        return []

def login(email, password):
    log(f"Logging in as {email}...")
    status, resp = curl("POST", "/api/v1/auth/login", body={"email": email, "password": password})
    if status != 200:
        log(f"Login failed (HTTP {status}): {resp[:200]}")
        return None
    try:
        return json.loads(resp).get("token")
    except:
        m = re.search(r'"token"\s*:\s*"([^"]+)"', resp)
        return m.group(1) if m else None

# -- Main ------------------------------------------------------------------------

state = {}

def hit(method, path, desc=None, token=None, body=None):
    label = desc or f"{method} {path}"
    actual_path = path
    for key, val in state.items():
        if val is not None:
            actual_path = actual_path.replace(f"{{{key}}}", str(val))

    log(f"{label}...")
    clear_sql_log()
    t0 = time.time()
    status, resp_body = curl(method, actual_path, token, body)
    elapsed = time.time() - t0
    sql = read_sql_log()

    section = f"### {label}\n\n"
    section += f"**Request:** {method} {actual_path}\n\n"
    section += f"**Response:** HTTP {status} ({elapsed:.2f}s)\n\n"
    if resp_body:
        try:
            section += f"```json\n{json.dumps(json.loads(resp_body), indent=2)}\n```\n\n"
        except:
            section += f"```\n{resp_body[:500]}\n```\n\n"
    if sql:
        section += f"**SQL Queries ({len(sql.splitlines())} lines):**\n\n```sql\n{sql}\n```\n\n"
    else:
        section += "**SQL Queries:** (none captured)\n\n"
    section += "---\n\n"
    append_output(section)

    # Extract IDs from response for subsequent calls
    if status in (200, 201) and resp_body:
        try:
            data = json.loads(resp_body)
            if isinstance(data, dict) and data.get("id"):
                lbl = label.lower()
                if "comment" in lbl:
                    state["comment_id"] = data["id"]
                    state["comment_post_id"] = state.get("post_id")
                elif "post" in lbl:
                    state["post_id"] = data["id"]
        except:
            pass

    time.sleep(0.3)
    return status, resp_body


def main():
    print(f"\n{'='*60}")
    print(f"  Spring Boot SQL Query Analyzer")
    print(f"{'='*60}\n")

    # Init output file
    Path(PROJECT_ROOT / OUTPUT).write_text("# SQL Query Analysis Report\n\n")
    append_output(f"Analyzed at: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
    append_output(f"Base URL: {BASE_URL}\n\n---\n\n")

    # ============================================================
    # PHASE 1: Register a normal user
    # ============================================================
    log("=== PHASE 1: Register normal user ===")

    hit("POST", "/api/v1/auth/register", "POST /auth/register",
        body={"name": "Test User", "email": USER_EMAIL, "password": USER_PASSWORD})

    user_token = login(USER_EMAIL, USER_PASSWORD)
    if not user_token:
        log("Normal user login failed, falling back to admin...")
        user_token = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    if not user_token:
        log("FATAL: Cannot authenticate")
        sys.exit(1)

    # ============================================================
    # PHASE 2: Create data with normal user
    # ============================================================
    log("\n=== PHASE 2: Create data ===")

    # Read tags (public, no auth needed)
    hit("GET", "/api/v1/tags", "GET /tags (public)")

    # Create post 1 (draft)
    hit("POST", "/api/v1/posts", "POST /posts (create post 1 - draft)", token=user_token,
        body={"title": "First Post", "content": "Content of the first post.", "tags": ["java", "spring"]})

    # Create post 2 (will be published)
    hit("POST", "/api/v1/posts", "POST /posts (create post 2 - to be published)", token=user_token,
        body={"title": "Second Post", "content": "Content of the second post.", "tags": ["java"]})

    # Publish post 2
    hit("PATCH", "/api/v1/posts/{post_id}/publish", "PATCH /posts/{id}/publish", token=user_token)

    # List all posts (public, paginated)
    hit("GET", "/api/v1/posts?page=0&size=10", "GET /posts (public, paginated)")

    # Read single published post
    hit("GET", "/api/v1/posts/{post_id}", "GET /posts/{id} (public, single post)")

    # Get comments for the published post
    hit("GET", "/api/v1/posts/{post_id}/comments", "GET /posts/{id}/comments")

    # Add a comment
    hit("POST", "/api/v1/posts/{post_id}/comments", "POST /posts/{id}/comments (add comment)",
        token=user_token, body={"content": "Great post!"})

    # Like the post
    hit("POST", "/api/v1/posts/{post_id}/like", "POST /posts/{id}/like", token=user_token)

    # Get like count
    hit("GET", f"/api/v1/posts/{state.get('post_id')}/likes/count", "GET /posts/{postId}/likes/count")

    # My posts (authenticated)
    hit("GET", "/api/v1/posts/me?page=0&size=10", "GET /posts/me (authenticated)", token=user_token)

    # Posts by tag
    hit("GET", "/api/v1/tags/java/posts?page=0&size=10", "GET /tags/{tagName}/posts")

    # Update post (PUT)
    hit("PUT", "/api/v1/posts/{post_id}", "PUT /posts/{id} (update post)", token=user_token,
        body={"title": "Updated Second Post", "content": "Updated content.", "tags": ["java", "spring"]})

    # Test endpoints (authenticated as normal user)
    hit("GET", "/api/v1/test", "GET /test (basic auth check)", token=user_token)
    hit("GET", "/api/v1/test/user-dashboard", "GET /test/user-dashboard (ROLE_USER)", token=user_token)
    hit("GET", "/api/v1/test/create-post", "GET /test/create-post (POST_CREATE)", token=user_token)
    hit("GET", "/api/v1/test/delete-user", "GET /test/delete-user (should be 403)", token=user_token)

    # ============================================================
    # PHASE 3: Cleanup operations
    # ============================================================
    log("\n=== PHASE 3: Cleanup ===")

    # Delete the comment
    if state.get("comment_id") and state.get("comment_post_id"):
        hit("DELETE", f"/api/v1/posts/{state['comment_post_id']}/comments/{state['comment_id']}",
            "DELETE /posts/{postId}/comments/{commentId}", token=user_token)

    # Unpublish
    hit("PATCH", "/api/v1/posts/{post_id}/unpublish", "PATCH /posts/{id}/unpublish", token=user_token)

    # Delete the post
    hit("DELETE", "/api/v1/posts/{post_id}", "DELETE /posts/{id}", token=user_token)

    # ============================================================
    # PHASE 4: Admin operations
    # ============================================================
    log("\n=== PHASE 4: Admin operations ===")

    admin_token = login(ADMIN_EMAIL, ADMIN_PASSWORD)
    if not admin_token:
        log("Admin login failed, reusing user token")
        admin_token = user_token

    # Admin dashboard
    hit("GET", "/api/v1/admin", "GET /admin (admin only)", token=admin_token)

    # Admin test endpoint
    hit("GET", "/api/v1/test/admin-dashboard", "GET /test/admin-dashboard (ROLE_ADMIN)", token=admin_token)

    # Delete remaining post (post 1 - the draft) as admin
    status, resp = hit("GET", "/api/v1/posts?page=0&size=10",
                       "GET /posts (list before admin delete)", token=admin_token)
    if status == 200:
        ids = extract_ids_from_page(resp)
        if ids:
            admin_post_id = ids[0]
            hit("DELETE", f"/api/v1/admin/posts/{admin_post_id}",
                "DELETE /admin/posts/{id}", token=admin_token)

    log(f"\nDone! Output written to {OUTPUT}")
    print(f"\n{'='*60}\n")


if __name__ == "__main__":
    main()