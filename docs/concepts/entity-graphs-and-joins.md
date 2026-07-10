# Entity Graphs and Join-Fetching in JPA/Hibernate

## What does `@EntityGraph` do at the SQL level?

An entity graph is a hint to Hibernate: "I know I'm going to need these associations, so load them in the same query."

### Without entity graph

```java
Optional<Post> findById(1L);
```

Hibernate generates:

```sql
SELECT p.* FROM posts p WHERE p.id = 1;
```

Then when the mapper calls `post.getUser()`:

```sql
SELECT u.* FROM users u WHERE u.id = ?;
-- ? = p.user_id from the first result
```

Two separate queries.

### With `Post.withUser` (to-one association)

```java
@EntityGraph("Post.withUser")
Optional<Post> findById(1L);
```

Hibernate generates:

```sql
SELECT p.*, u.*
FROM posts p
LEFT JOIN users u ON p.user_id = u.id
WHERE p.id = 1;
```

One query. The user data arrives in the same result row because it's a `LEFT JOIN` on a **to-one** association. Each post row joins to exactly one user row, so the result set has one row per post.

| p.id | p.title | p.content | u.id | u.name |
|------|---------|-----------|------|--------|
| 1    | Hello   | ...       | 5    | Tanay  |

---

## Why adding a to-many collection (tags) is problematic

### The SQL that would be generated

```java
@NamedEntityGraph(
    name = "Post.withUserAndTags",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("tags")   // Set<Tag>, a to-many
    }
)
```

```sql
SELECT p.*, u.*, t.*
FROM posts p
LEFT JOIN users u ON p.user_id = u.id
LEFT JOIN post_tag pt ON pt.post_id = p.id
LEFT JOIN tags t ON pt.tag_id = t.id
WHERE p.id = 1;
```

For a post with 3 tags, the result set looks like:

| p.id | p.title | u.id | u.name | t.id | t.name  |
|------|---------|------|--------|------|---------|
| 1    | Hello   | 5    | Tanay  | 10   | java    |
| 1    | Hello   | 5    | Tanay  | 11   | spring  |
| 1    | Hello   | 5    | Tanay  | 12   | hibernate|

**3 rows for 1 post.** The post data and user data repeat on every row. Hibernate's `Set<Post>` deduplication handles this correctly: it constructs one `Post` object and populates its `tags` set with all three `Tag` objects.

For `findById(1L)`, this is **fine** — one query instead of two, no correctness issue.

### The problem: pagination

```java
@EntityGraph("Post.withUserAndTags")
Page<Post> findAll(Pageable pageable);  // page size = 10
```

Hibernate generates the same join:

```sql
SELECT p.*, u.*, t.*
FROM posts p
LEFT JOIN users u ON p.user_id = u.id
LEFT JOIN post_tag pt ON pt.post_id = p.id
LEFT JOIN tags t ON pt.tag_id = t.id
```

The result set has **10 × (avg tags per post)** rows, not 10 rows. If each post has ~3 tags, that's ~30 rows for 10 posts.

Hibernate **cannot apply `LIMIT 10`** to this query — that would return 10 rows, which is not 10 posts. Its fallback is:

1. Fetch **all** matching rows into memory.
2. Deduplicate by post ID.
3. Apply pagination in application memory.

This means:
- `findAll` loads every post from the database — full table scan with joins.
- The `COUNT` query for `Page` metadata also becomes inefficient.
- The `Page` content may be wrong: if the first 10 rows only yield 4 distinct post IDs, you get a page of 4 instead of 10.

---

## Comparison

| Configuration | `findById(1)` | `findAll(pageable)` |
|---|---|---|
| No entity graph | 2 queries (post, then user) | N+1 on user per page |
| `Post.withUser` (current) | 1 query, clean | 1 query, clean pagination |
| `Post.withUserAndTags` | 1 query, row duplication OK | **Broken pagination (in-memory)** |

### The fix

The same `@EntityGraph` annotation is shared across all repository methods. Once you add tags to it, you've applied a to-many join-fetch to paginated queries that can't handle it.

The correct approach is a **separate entity graph** for `findById` alone:

```java
@NamedEntityGraph(
    name = "Post.withUserAndTags",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("tags")
    }
)
```

Then in the repository, only `findById` uses it:

```java
@Override
@EntityGraph("Post.withUserAndTags")
Optional<Post> findById(Long id);
```

The paginated methods keep the original `Post.withUser` graph.

### When is it worth it?

The detail path with just `Post.withUser` is already a fixed `1 + 1`:
- 1 query for post + user (via entity graph)
- 1 query for tags (lazy load)

This does not grow with data — it's not N+1. Adding a second entity graph is a valid optimization, but it's a "when it hurts" decision, not something to do preemptively.