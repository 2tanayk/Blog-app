# JPA Inverse Mapping & Entity Graph Queries — Revision Notes

---

## 1. The Foundation — What is a Relationship in JPA?

In the DB, a relationship is just a **foreign key column**. Nothing more.

```
likes table:
  id | user_id | post_id | created_at
```

JPA maps that FK column to Java objects. The "inverse mapping" question is purely about
how many directions you represent that single FK in Java.

---

## 2. Owning Side vs Inverse Side

| Side | Annotation | Who has it | DB effect |
|---|---|---|---|
| Owning | `@ManyToOne` | Entity with the FK column | Creates the FK column |
| Inverse | `@OneToMany(mappedBy = "fieldName")` | Other entity | No DB effect — Java only |

**Critical rule: Hibernate only looks at the owning side (`@ManyToOne`) to write to the DB.**
The inverse side (`mappedBy`) is purely for Java object graph navigation. It has zero effect
on INSERT or UPDATE SQL.

`mappedBy` value = the field name on the owning side that holds the relationship.

---

## 3. The Decision — When to Add the Inverse Side

Ask these two questions in order:

**Q1: Do I need cascade or orphanRemoval through this relationship?**
- YES → add `@OneToMany` inverse side (e.g. Post → Comments, deleting a post deletes its comments)
- NO → go to Q2

**Q2: Does my application code ever navigate this direction?**
- YES → add `@OneToMany` inverse side
- NO → skip it entirely, use the repository

```
// One-line rule:
// Add inverse side only for cascade/orphanRemoval OR genuine object graph navigation.
// Everything else — use the repository.
```

---

## 4. The Repository Replaces Every Navigation

Every collection navigation the inverse side enables, the repository does better:

| Inverse navigation | Repository equivalent |
|---|---|
| `post.getLikes()` | `likeRepository.findByPostId(postId)` |
| `post.getComments()` | `commentRepository.findByPostId(postId, pageable)` |
| `user.getPosts()` | `postRepository.findByAuthorId(authorId, pageable)` |

Repository wins because you get **pagination, sorting, and projection for free**.
You can never paginate `post.getComments()` cleanly.

The only thing the repository **cannot** do: cascade and orphanRemoval on parent deletion.
That behavior lives on the `@OneToMany` mapping itself — irreplaceable.

---

## 5. Applied Example — Blog App

| Relationship | Owning Side | Add Inverse on Parent? | Why |
|---|---|---|---|
| `Like → Post` | `@ManyToOne` on `Like` | ❌ No | Never navigate `post.getLikes()`, use repository |
| `Like → User` | `@ManyToOne` on `Like` | ❌ No | Never navigate `user.getLikes()`, use repository |
| `Comment → Post` | `@ManyToOne` on `Comment` | ✅ Yes | Need `CascadeType.ALL` + `orphanRemoval` |
| `Comment → User` | `@ManyToOne` on `Comment` | ❌ No | Never navigate `user.getComments()`, use repository |
| `Post ↔ Tag` | `@ManyToMany` on `Post` | ✅ Yes (optional) | Actively manage `post.getTags()` collection |

---

## 6. Entity Graph — What Join It Uses

`@EntityGraph` always uses a **LEFT OUTER JOIN** regardless of which side you're on.

Left outer join is used because the associated entity might be null — an inner join would
drop the parent entity from results if the association is null, which is wrong.

```java
@EntityGraph(attributePaths = {"author"})
List<Comment> findByPostId(Long postId, Pageable pageable);
```

Fires:
```sql
SELECT c.*, u.*
FROM comments c
LEFT OUTER JOIN users u ON c.author_id = u.id
WHERE c.post_id = ?
```

---

## 7. The Row Multiplication Problem — OneToMany Side

When you EntityGraph from the **inverse/OneToMany side** (e.g. Post → Comments):

```sql
SELECT p.*, c.*
FROM posts p
LEFT OUTER JOIN comments c ON c.post_id = p.id
WHERE p.id = ?
```

If a post has 10 comments → **10 rows returned**, all with duplicated post data:

```
post_id | title        | comment_id | content
1       | "Spring JPA" | 1          | "Great"
1       | "Spring JPA" | 2          | "Helpful"
1       | "Spring JPA" | 3          | "Thanks"
... 7 more rows with same post data
```

Hibernate collapses this into one Post with List<Comment> correctly.
Fine for `findById` (single entity). **Breaks pagination on list queries.**

### The Pagination Warning
```
HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory!
```
Hibernate applies LIMIT/OFFSET in memory, not at DB level. Page sizes become wrong.

### Fix for Paginated Lists
Use **two separate queries**:
1. Fetch paginated IDs cleanly with DB-level `LIMIT/OFFSET`
2. `WHERE id IN (...)` to batch fetch associations for those IDs

Or use `@BatchSize` on the collection to control the second query.

---

## 8. ManyToOne Side — Always Clean

When you EntityGraph from the **owning/ManyToOne side** (e.g. Comment → Post, Comment → User):

```sql
SELECT c.*, p.*
FROM comments c
LEFT OUTER JOIN posts p ON c.post_id = p.id
WHERE c.post_id = ?
```

Each comment joins to exactly **one** post → 1:1 at the row level → no multiplication.
10 comments = 10 rows. Flat, predictable, pagination works perfectly at DB level.

---

## 9. Mental Model — Join Direction vs Row Multiplication

These are two **separate concerns**:

| Concern | What it means |
|---|---|
| Left vs Inner join | Whether null associations are included (Hibernate always picks LEFT for safety) |
| Row multiplication | Whether one parent row fans out into multiple result rows |

Left outer join does NOT cause multiplication. Multiplication is caused purely by
**one-to-many cardinality** — one parent matching multiple child rows.

```
@ManyToOne (FK side):
  Join → N rows for N entities → flat, clean, pagination safe

@OneToMany (inverse side):
  Join → N×M rows → multiplication, Hibernate collapses in memory,
  pagination breaks on list queries
```

---

## 10. Key Takeaways

1. The inverse side (`@OneToMany mappedBy`) has **zero DB effect** — it's Java-only
2. Hibernate writes to DB only through the **owning side** (`@ManyToOne`)
3. Skip the inverse side unless you need **cascade/orphanRemoval or genuine navigation**
4. The repository replaces every navigation the inverse side would give you — with pagination bonus
5. `@EntityGraph` always uses LEFT OUTER JOIN regardless of direction
6. LEFT OUTER JOIN ≠ row multiplication — multiplication is a one-to-many cardinality problem
7. Always prefer querying from the **ManyToOne side** — flat result set, DB-level pagination
8. For paginated list queries needing collections — use two queries, never a join
