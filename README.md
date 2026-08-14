# Blog Management System

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791)
![Docker](https://img.shields.io/badge/Docker-compose-2496ED)
![Railway](https://img.shields.io/badge/Railway-deploy-0B0D0E)
<!-- ![License](https://img.shields.io/badge/License-MIT-yellow) -->

A Medium-inspired REST API for publishing, managing, and browsing blog posts. Users authenticate via email/password **or** Google OAuth2, author posts that live through a draft → published lifecycle, and interact through comments, likes, and followable public profiles. Includes a full admin surface for content moderation and platform analytics.

- **Swagger UI:** [link](https://blog-app-production-86e7.up.railway.app/api/v1/swagger-ui/index.html)

---

## Tech Stack

| Category | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 4.1, Spring MVC |
| **Persistence** | Spring Data JPA, Hibernate 6, PostgreSQL 15 |
| **Auth / Security** | Spring Security, JJWT 0.13.0, Spring OAuth2 Client (Google/other OIDC provider) |
| **Mapping** | MapStruct, Lombok |
| **API Docs** | springdoc-openapi (Swagger UI) |
| **DevOps** | Docker / Compose, Railway (via Dockerfile) |

---

## Features

**Auth**
- Dual login paths — manual JWT (email/password) and OAuth2 — converging on a single JWT issuance, so all downstream security is auth-method agnostic
- `jti`-based blacklist invalidates tokens on logout, password change, and account deletion
- HttpOnly-cookie transport for OAuth logins, `Authorization: Bearer` for API clients

**Posts**
- Draft → published lifecycle with publish/unpublish endpoints
- Drafts visible only to the author and admins; all public reads return published content only
- Ownership-based access control for edit, publish, and delete

**Tags**
- Lowercased, trimmed normalization at the service layer plus a DB CHECK constraint
- Find-or-create resolution with deduplicated batch inserts

**Comments**
- Allowed on published posts only (drafts reject comments)
- Paginated listing per post; delete by author or admin

**Likes**
- Single toggle create/delete; drafts reject likes
- Batched like counts fetched in one query per page of posts

**Users**
- Account deletion reassigns all content (posts, comments, likes) to a seeded ghost user — preserving FK integrity and live content
- Profile (name/bio) and password updates; password change revokes the active JWT

**Admin**
- Promote users to `ROLE_ADMIN`; delete users, posts, tags, or comments
- Per-user post listing across all statuses; platform statistics (users / posts / comments / tags)

---

## Architecture & Design Decisions

### Single issuance point for JWT across manual and OAuth2 login

Both login flows — email/password and Google OAuth2 — end at the same JWT generator. Downstream security (filter chain, `@PreAuthorize`, HTTP-only cookie handling) never distinguishes how a user authenticated. This keeps one code path to audit instead of two parallel auth systems.

### `jti`-based revocation list — because JWTs are stateless

A JWT survives user deletion or password change by design; there's nothing to kill server-side. This project blacklists the token's unique `jti` in PostgreSQL with its expiry, checked in the auth filter after signature validation, with an `@Scheduled` purge (nightly) removing past-expiry rows.

### Three-tier RBAC with `_OWN`/`_ANY` privilege splits
Authorization is built as User → Role → Privilege, using `POST_EDIT_OWN` vs `POST_EDIT_ANY`-style privilege pairs. `@PreAuthorize` expressions combine ownership checks (via a small SpEL security bean) with privilege checks — so "edit your own post" and "edit any post" resolve privilege + ownership, no role hierarchy and no `hasRole()` string-matching.

### `@EntityGraph` as explicit read-path eagerism
All associations are `FetchType.LAZY`; individual read paths opt into eager fetching via named `@EntityGraph`/`@NamedAttributeNode` methods on the repo. Paginated post listings fetch author eagerly while like counts are batch-queried (`WHERE id IN (...)`) — preventing N+1 without loading the full object graph everywhere.

### Validation at publish time
Content constraints (e.g. non-blank title) are enforced on the draft → published transition rather than at save time. This keeps the "drafts may be incomplete" rule explicit in the service layer instead of hidden in the DB schema.

### Lowercase-normalized tags, enforced end to end

Tags are trimmed + lowercased + deduped at the service layer and a `CHECK (name = lower(name))` constraint backs it at the DB — duplicate tags are impossible even through a hand-written SQL path. Find-or-create keeps re-running the seed sequence idempotent.

---

## Running Locally

Prerequisites: Java 21, Docker.

```bash
git clone git@github.com:YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO
cp .env.example .env      # fill in real values
docker-compose up --build
```

- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

---

## Environment Variables

<details>
<summary>Variables</summary>

| Variable | Description | Required |
|---|---|---|
| `POSTGRES_DB` | Database name (also used to form the JDBC URL) | ✅ |
| `POSTGRES_USER` | PostgreSQL user created by the container | ✅ |
| `POSTGRES_PASSWORD` | Password for the PostgreSQL user | ✅ |
| `DB_PASSWORD` | Same as above; consumed by Spring's datasource config | ✅ |
| `JWT_SECRET` | Base64-encoded HMAC-SHA key for JWT signing | ✅ |
| `GOOGLE_CLIENT_ID` | OAuth 2.0 client ID from Google Cloud Console | ⬜ |
| `GOOGLE_CLIENT_SECRET` | OAuth 2.0 client secret for the ID | ⬜ |
| `APP_FRONTEND_URL` | Frontend origin for OAuth redirects and CORS | ✅ |

</details>

---

## Testing

- Spring Boot starter test dependencies are configured (JPA / Security / MVC / Validation)
- `./mvnw test` runs the suite (currently: application context-load test)

---

## CI/CD
- CI pipeline (GitHub Actions) is not yet wired up — ready to be added under `.github/workflows/`
