# equals() and hashCode() in JPA Entities

## The Core Problem

When Hibernate loads an entity from the database, it doesn't always return the same Java object. It can create **multiple Java objects** that represent the **same database row** — especially with:

- **Lazy loading** — Hibernate creates a proxy object first, then loads the real entity later
- **Multiple queries** — the same row fetched in different queries returns different object instances
- **Session boundaries** — entities detached and re-attached across transactions

A `Set` in Java uses `equals()` and `hashCode()` to decide if two objects are the same. If you have two different Java objects representing the same database row, the Set needs to know they're the same thing — otherwise you get **duplicates** (or, worse, subtle corruption).

### Visual Example

Without proper `equals`/`hashCode`, a `HashSet` sees two different Java objects as distinct entries, even though they represent the same database row:

```
bucket[2] → [Tag("java")@123, Tag("java")@456]  ← duplicates!
```

With proper `equals`/`hashCode`, the Set deduplicates correctly:

```
bucket[2] → [Tag("java")@123]  ← second instance matches the first
```

---

## Which Entities Need It

| Entity | Stored in a `Set`? | Via |
|--------|-------------------|-----|
| **Role** | ✅ Yes | `User.roles` — `Set<Role>` |
| **Privilege** | ✅ Yes | `Role.privileges` — `Set<Privilege>` |
| **Tag** | ✅ Yes | `Post.tags` — `Set<Tag>` |
| **Post** | ✅ Yes | `Tag.posts` — `Set<Post>` (inverse of `@ManyToMany`) |
| **User** | ❌ No | Only `@ManyToOne` refs, never in a Set |
| **Comment** | ❌ No | `Post.comments` is a `List<Comment>` (Lists use index-based identity, not equals/hashCode) |

---

## Approach 1: Business Key — Role, Privilege, Tag

These entities have a `name` column with `unique = true, nullable = false` — a **natural business key** that is stable and unique. Use it directly.

### Implementation

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;                    // (1)
    if (!(o instanceof Role role)) return false;   // (2)
    return Objects.equals(name, role.name);        // (3)
}

@Override
public int hashCode() {
    return Objects.hash(name);                     // (4)
}
```

### Line-by-line explanation

**`(1)` — Reference identity check**
```java
if (this == o) return true;
```
`this == o` is Java's **reference equality** — it checks if both variables point to the exact same object in memory. If they're the same object, they're definitely equal. This is a fast-path optimization.

**`(2)` — Type check**
```java
if (!(o instanceof Role role)) return false;
```
If the other object isn't even a `Role` at all, they can't be equal. The pattern `o instanceof Role role` (Java 17+) checks the type AND declares a variable `role` cast to that type in one step — no separate cast line needed.

**`(3)` — Field equality**
```java
return Objects.equals(name, role.name);
```
`Objects.equals(a, b)` is a null-safe version of `a.equals(b)`. It handles the case where `name` might be null (returns `true` if both are null, `false` if one is null). Two `Role` objects are equal if they have the same `name` string.

**`(4)` — Hash computation**
```java
return Objects.hash(name);
```
`hashCode()` is used by `HashSet` and `HashMap` to decide **which bucket** to put an object in. The **contract** is: if two objects are `equals()`, they MUST have the same `hashCode()`. Since equality is based on `name`, the hash must also be based on `name`.

`Objects.hash(name)` computes a hash from the `name` field. Two Roles with the same name will get the same hash, so they end up in the same bucket, where the `HashSet` then calls `equals()` to confirm they're identical.

### Hash Set behavior visualized

```java
Set<Tag> tags = new HashSet<>();
tags.add(new Tag("java"));  // hash = hash("java") → bucket[2]
tags.add(new Tag("java"));  // hash = hash("java") → bucket[2]
                             // equals("java", "java") → true → no duplicate
```

---

## Approach 2: ID-based — Post

Post has no unique business key. `title` isn't unique — two posts could share a title. The only unique property is the auto-generated database `id`.

But using `id` directly is tricky because of the **transient entity problem**:

```java
Post post = new Post();  // id = null (not persisted yet)
Set<Post> set = new HashSet<>();
set.add(post);           // post has hashCode = ???

// ... save to database ...
// Hibernate sets post.id = 42

set.contains(post);      // hashCode changed! Now it looks in a different bucket
                         // Can't find it — the object is "lost" from the Set!
```

### The fix

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Post post)) return false;
    return id != null && Objects.equals(id, post.id);   // ← id != null guard
}

@Override
public int hashCode() {
    return getClass().hashCode();   // ← fixed value, never changes
}
```

### How it works

**Before persisting** (id = null):
- `hashCode()` → `Post.class.hashCode()` — say, `12345`
- Two different new Post objects both get hash `12345` → **same bucket**
- `equals()`: `id != null` is **false**, so the method **returns false**
- They're treated as different objects — correct! Two unsaved drafts are distinct.

**After persisting** (id = 42):
- `hashCode()` → still `12345` (never changes) → **same bucket as before**
- `equals()`: `id != null` is **true**, so it compares `Objects.equals(42, post.id)`
- If the other Post has the same ID, they're equal → no duplicates in the Set

### The key insight

**`hashCode()` never changes**, so the object never "jumps buckets" after `persist()`. The `id != null` guard in `equals()` means:

| State | `hashCode()` | `equals()` behavior |
|-------|-------------|---------------------|
| Transient (id = null) | Fixed | Always returns false for other objects (distinct) |
| Persisted (id = 42) | Fixed (same) | Compares by database ID |

This avoids the worst of both worlds: transient objects don't accidentally collide, and persisted objects deduplicate correctly.

---

## What NOT to do

### ❌ Using Lombok's `@EqualsAndHashCode` on all fields

```java
@Data  // includes @EqualsAndHashCode on ALL fields
@Entity
public class Post {
    @Id private Long id;
    private String title;
    private String content;
    // ...
}
```

Problems:
- `title` and `content` are mutable — if you change them while the entity is in a Set, the hash changes and the object is lost
- Lazy-loaded associations (`User user`) throw `LazyInitializationException` if accessed outside a session
- Two different posts with the same title and content would be incorrectly treated as equal

### ❌ Using only the `@Id` without the null guard

```java
@Override
public int hashCode() {
    return Objects.hash(id);  // id is null → hash = 0
}
```

After `persist()`, `id` changes from `null` to `42`. The hash changes from `0` to `hash(42)`. The object moves to a different bucket — lost from the Set.

---

## Best Practices Summary

| Scenario | Strategy |
|----------|----------|
| Has a unique, immutable business key (e.g., `name`, `email`, `slug`) | Use the business key |
| No business key (auto-generated ID only) | Use ID-based with `id != null` guard and fixed `hashCode()` |
| Always | Include `import java.util.Objects;` |
| Use `instanceof` (not `getClass()`) | Preserves equality across Hibernate proxy subclasses |
| Keep `hashCode()` stable | Never use mutable fields |

---

## References

- [Vlad Mihalcea — The best way to implement equals and hashCode in JPA](https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/)
- [Hibernate User Guide — Equals and HashCode](https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#mapping-model-pojo-equals)