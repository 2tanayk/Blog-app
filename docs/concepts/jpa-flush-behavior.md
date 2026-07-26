# JPA Flush Behavior

## What is the Persistence Context?

The persistence context (Hibernate Session) is an in-memory tracker that Spring wraps around your methods via `@Transactional`. Think of it as a **notepad**:

- When you call `save()` / `persist()`, Hibernate writes "insert this row" on the notepad — **no SQL yet**.
- When you call `findById()`, Hibernate checks the notepad first, then the database.
- When the transaction commits, Hibernate executes everything on the notepad.

This is called **transactional write-behind** — batching all SQL into one go at commit time.

## What is `flush()`?

`flush()` forces Hibernate to execute all queued SQL *right now* against the database, but **keeps the transaction open**. The rows are visible to the current transaction but not yet committed (other transactions won't see them until commit).

```java
// Without flush — no SQL sent
postRepository.save(newPost);  

// With flush — INSERT executed immediately, but not committed
postRepository.saveAndFlush(newPost);  // or: entityManager.flush()
```

## The Critical Rule: Flush-Before-Query

Hibernate has a hard rule: **before any SELECT, flush the persistence context**.

Why? Imagine this scenario:

```java
@Transactional
void updateEmail(Long userId, String newEmail) {
    User user = userRepository.findById(userId).get();
    user.setEmail(newEmail);  // ← queued: "UPDATE users SET email=... WHERE id=?"
    
    // Now Hibernate runs a SELECT (e.g. findByRole)
    // If Hibernate didn't flush first, the SELECT would see the OLD email
    // So Hibernate forces a flush before the query
}
```

Hibernate cannot let you read stale data. So **every time you trigger a query** (SELECT, JPQL, native query, proxy initialization), Hibernate flushes first.

## The Timeline from `createPost`

```java
@Transactional
public PostDto createPost(AddPostDto addPostDto, Long id) {
    User user = userRepository.getReferenceById(id);  // proxy, no SQL
    Post newPost = postMapper.toEntity(addPostDto);
    newPost.setUser(user);
    newPost.setStatus(PostStatus.DRAFT);
    newPost.setTags(resolveTags(addPostDto.tags()));   // tag INSERTs flush immediately

    Post savedPost = postRepository.save(newPost);     // persist() — queues, no SQL
    return postMapper.toDto(savedPost);                 // ← triggers user.getName()
}
```

Step-by-step SQL timeline:

```
Time  ──────────────────────────────────────────────────────────────►

resolveTags() runs:
  │  insert into tags (java)        ← saved via saveAll(), flushes immediately
  │  insert into tags (spring)      ← saved via saveAll(), flushes immediately
  │
save(newPost) called:
  │  persist(post) → queued in persistence context, NO SQL
  │
toDto() calls savedPost.getUser().getName():
  │  Hibernate: "I need a SELECT to initialize this proxy"
  │  Hibernate: "But I must FLUSH first to see all queued changes"
  │    ↓
  │  insert into posts  ← FLUSH before query
  │    ↓
  │  select from users where id=2  ← THE PROXY INITIALIZATION SELECT
  │
@Transactional commit:
  │  insert into post_tag (post_id, tag_id)  ← commit flush
  │  insert into post_tag (post_id, tag_id)  ← commit flush
```

### Why does the user SELECT come before the `post_tag` inserts?

This is the part that's confusing at first glance. The user SELECT runs **before** `post_tag` inserts, but the `post_tag` relationship is set up *before* the SELECT is triggered. So why?

The answer is **when the flush is triggered**:

1. The `post_tag` inserts are part of the **persist** queue — they're queued when `save()` is called.
2. The user SELECT is triggered when `toDto()` accesses `user.getName()` — this is the proxy initialization.
3. The `post_tag` inserts are NOT flushed at this point because no one asked for them. They're flushed at **commit time**.
4. The user SELECT forces a flush of the `posts` INSERT (because the SELECT needs to see the latest state), but the `post_tag` inserts are left in the queue.

**The flush-before-query rule only flushes what's needed for consistency.** The `post_tag` inserts don't affect the user SELECT, so they stay queued.

### Why does `resolveTags()` flush immediately but `save(post)` doesn't?

`resolveTags()` calls `tagRepository.saveAll(newTags)` — notice `saveAll()`, not `persist()`. The difference:

- `saveAll()` calls `save()` on each entity, which internally calls `persist()` and may trigger a flush depending on the `FlushMode` (default: AUTO).
- `postRepository.save(newPost)` also calls `persist()`.

Actually, both are just `persist()` under the hood. The `tag` INSERTs appear first because `resolveTags()` runs *before* `save(newPost)` in the code. Both are queued, but the tag inserts get flushed at the same flush-before-query event (or the commit). The real reason they appear earlier is simply code ordering.

## `getReferenceById` and the Proxy Trap

```java
User user = userRepository.getReferenceById(id);  // creates a PROXY
```

`getReferenceById` does **not** hit the database. It creates a **proxy object** — a placeholder that only knows the ID. If you never access any property except `getId()`, no SQL fires.

But the moment you call anything else — `getName()`, `getEmail()`, even `toString()` — Hibernate must initialize the proxy with a SELECT.

This is why `getReferenceById` is only useful when you need to set a foreign key and nothing else:

```java
// GOOD: no SELECT needed — just setting FK
newComment.setUser(userRepository.getReferenceById(userId));
// Later: commentRepository.save(newComment) — only needs user_id

// BAD: proxy initialized immediately — defeats the purpose
User user = userRepository.getReferenceById(id);
PostDto dto = postMapper.toDto(savedPost);
// toDto calls user.getName() → proxy initialized → SELECT fires
```

### When to use `getReferenceById` vs `findById`

| Method | Hits DB? | Best for |
|---|---|---|
| `getReferenceById(id)` | No (creates proxy) | Setting a foreign key when you won't read the entity's fields |
| `findById(id)` | Yes (immediate SELECT) | When you actually need the entity's data |

## Summary

| Concept | What it means |
|---|---|
| **Persistence Context** | In-memory notepad for queued DB operations |
| **Transactional write-behind** | SQL is batched and sent at commit time |
| **Flush** | Executing queued SQL immediately (transaction still open) |
| **Flush-before-query** | Hibernate's rule: flush before any SELECT to ensure consistency |
| **Commit flush** | Final flush when `@Transactional` method ends |
| **Proxy** | Placeholder from `getReferenceById` — no SQL until a field is accessed |