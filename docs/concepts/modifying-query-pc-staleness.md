# `@Modifying` Native Queries and Persistence Context Staleness

## The problem

A `@Modifying` native query (e.g. `DELETE FROM join_table WHERE ...`) runs directly against the database. Hibernate's first-level cache (persistence context) is **not notified** of the change. Any managed entities already in the PC still reflect the old state.

## Concrete example

```java
// TagRepository.java
@Modifying(flushAutomatically = true)
@Query(nativeQuery = true, value = "DELETE FROM post_tag WHERE tag_id = :tagId")
void deletePostTagAssociations(@Param("tagId") Long tagId);
```

Sequence of events in a `@Transactional` service method:

1. `tagRepository.existsById(id)` → `Tag` entity loaded into PC
2. Native query runs → `post_tag` rows deleted in DB, but PC unchanged
3. `tagRepository.deleteById(id)` → `em.remove(tag)` called
4. Transaction commits → Hibernate flushes

**The danger**: If any `Post` entities are already in the PC (loaded by a prior operation in the same transaction), their `Set<Tag> tags` still contain the deleted tag. When Hibernate dirty-checks at flush time, it sees the `post_tag` join table has no row for this association and **re-inserts it** — which would fail with a FK constraint violation since the tag row is also being deleted.

## The fix: `clearAutomatically = true`

```java
@Modifying(flushAutomatically = true, clearAutomatically = true)
@Query(nativeQuery = true, value = "DELETE FROM post_tag WHERE tag_id = :tagId")
void deletePostTagAssociations(@Param("tagId") Long tagId);
```

`clearAutomatically = true` calls `EntityManager.clear()` after the bulk DML executes, evicting **all** entities from the persistence context. Subsequent operations (like `deleteById`) re-load fresh state from the database.

## When the risk is real vs. theoretical

| Scenario | Risk |
|---|---|
| Native query inside a fresh `@Transactional` method, no other entities loaded | Low — no stale entities to cause problems |
| Native query called within a larger transaction that already loaded related entities | **High** — dirty checking will re-insert deleted rows |
| Method is refactored later to do more work before/after the query | **Latent** — works today, breaks tomorrow |

## Trade-off vs. the idiomatic JPA approach

The idiomatic alternative is to work with managed entities:

```java
Tag tag = tagRepository.findById(id).orElseThrow(...);
for (Post post : tag.getPosts()) {
    post.getTags().remove(tag);  // Hibernate manages the join table
}
tagRepository.delete(tag);
```

| Approach | Pros | Cons |
|---|---|---|
| Native query + `clearAutomatically` | Single SQL, no entity loading, safe | Evicts entire PC (minor perf hit if many entities were loaded) |
| Idiomatic JPA | PC stays consistent, no native SQL | Loads all associated entities into memory; slower for large collections |

## Rule of thumb

**Always** add `clearAutomatically = true` to any `@Modifying` native or JPQL bulk query. The cost is negligible and it prevents a class of bugs that are hard to reproduce and harder to debug. `flushAutomatically = true` should also be added to flush pending changes before the bulk DML runs.

**Why:** The alternative — "I know no entities are loaded in this transaction" — is a fragile assumption that breaks silently when the code is refactored.