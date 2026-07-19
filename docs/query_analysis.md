# SQL Query Analysis Report


Analyzed at: 2026-07-17 17:27:15

Base URL: http://localhost:8080

---


### POST /auth/register

**Request:** POST /api/v1/auth/register

**Response:** HTTP 201 (0.54s)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc4NDI4OTQzNiwiZXhwIjoxNzg0Mzc1ODM2fQ.Fu_Pqzbo1ULDgWHJZYlDJltBfzoVwXxnLJDskRAYtto"
}
```

**SQL Queries (43 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        users u1_0 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        r1_0.id,
        r1_0.name 
    from
        roles r1_0 
    where
        r1_0.name=?
binding parameter (1:VARCHAR) <- [ROLE_USER]

    insert 
    into
        users
        (email, name, password, provider_id, provider_type) 
    values
        (?, ?, ?, ?, ?)
binding parameter (1:VARCHAR) <- [testuser@example.com]
binding parameter (2:VARCHAR) <- [Test User]
binding parameter (3:VARCHAR) <- [$2a$10$uurSxYlEQgMWtuqmD1.PVuYhmOHWoFCKL6BSoibyb3QKS8ME4vEfG]
binding parameter (4:VARCHAR) <- [null]
binding parameter (5:VARCHAR) <- [EMAIL]

    insert 
    into
        users_roles
        (user_id, role_id) 
    values
        (?, ?)
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [1]
```

---


### GET /tags (public)

**Request:** GET /api/v1/tags

**Response:** HTTP 200 (0.17s)

```json
{
  "tags": []
}
```

**SQL Queries (7 lines):**

```sql

    select
        t1_0.id,
        t1_0.created_at,
        t1_0.name 
    from
        tags t1_0
```

---


### POST /posts (create post 1 - draft)

**Request:** POST /api/v1/posts

**Response:** HTTP 201 (0.77s)

```json
{
  "id": 1,
  "title": "First Post",
  "content": "Content of the first post.",
  "coverImageUrl": null,
  "status": "DRAFT",
  "createdAt": "2026-07-17T17:27:18",
  "updatedAt": "2026-07-17T17:27:18",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 0,
  "likedByCurrentUser": false,
  "tags": [
    "java",
    "spring"
  ]
}
```

**SQL Queries (105 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        t1_0.id,
        t1_0.created_at,
        t1_0.name 
    from
        tags t1_0 
    where
        t1_0.name in (?, ?)
binding parameter (1:VARCHAR) <- [java]
binding parameter (2:VARCHAR) <- [spring]

    insert 
    into
        tags
        (created_at, name) 
    values
        (?, ?)
binding parameter (1:TIMESTAMP) <- [2026-07-17T17:27:18.459326]
binding parameter (2:VARCHAR) <- [java]

    insert 
    into
        tags
        (created_at, name) 
    values
        (?, ?)
binding parameter (1:TIMESTAMP) <- [2026-07-17T17:27:18.469589]
binding parameter (2:VARCHAR) <- [spring]

    insert 
    into
        posts
        (content, cover_image_url, created_at, status, title, updated_at, user_id) 
    values
        (?, ?, ?, ?, ?, ?, ?)
binding parameter (1:VARCHAR) <- [Content of the first post.]
binding parameter (2:VARCHAR) <- [null]
binding parameter (3:TIMESTAMP) <- [2026-07-17T17:27:18.474630]
binding parameter (4:VARCHAR) <- [DRAFT]
binding parameter (5:VARCHAR) <- [First Post]
binding parameter (6:TIMESTAMP) <- [2026-07-17T17:27:18.474630]
binding parameter (7:BIGINT) <- [2]

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        users u1_0 
    where
        u1_0.id=?
binding parameter (1:BIGINT) <- [2]

    insert 
    into
        post_tag
        (post_id, tag_id) 
    values
        (?, ?)
binding parameter (1:BIGINT) <- [1]
binding parameter (2:BIGINT) <- [1]

    insert 
    into
        post_tag
        (post_id, tag_id) 
    values
        (?, ?)
binding parameter (1:BIGINT) <- [1]
binding parameter (2:BIGINT) <- [2]
```

---


### POST /posts (create post 2 - to be published)

**Request:** POST /api/v1/posts

**Response:** HTTP 201 (0.20s)

```json
{
  "id": 2,
  "title": "Second Post",
  "content": "Content of the second post.",
  "coverImageUrl": null,
  "status": "DRAFT",
  "createdAt": "2026-07-17T17:27:19",
  "updatedAt": "2026-07-17T17:27:19",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 0,
  "likedByCurrentUser": false,
  "tags": [
    "java"
  ]
}
```

**SQL Queries (77 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        t1_0.id,
        t1_0.created_at,
        t1_0.name 
    from
        tags t1_0 
    where
        t1_0.name in (?)
binding parameter (1:VARCHAR) <- [java]

    insert 
    into
        posts
        (content, cover_image_url, created_at, status, title, updated_at, user_id) 
    values
        (?, ?, ?, ?, ?, ?, ?)
binding parameter (1:VARCHAR) <- [Content of the second post.]
binding parameter (2:VARCHAR) <- [null]
binding parameter (3:TIMESTAMP) <- [2026-07-17T17:27:19.051052]
binding parameter (4:VARCHAR) <- [DRAFT]
binding parameter (5:VARCHAR) <- [Second Post]
binding parameter (6:TIMESTAMP) <- [2026-07-17T17:27:19.051052]
binding parameter (7:BIGINT) <- [2]

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        users u1_0 
    where
        u1_0.id=?
binding parameter (1:BIGINT) <- [2]

    insert 
    into
        post_tag
        (post_id, tag_id) 
    values
        (?, ?)
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [1]
```

---


### PATCH /posts/{id}/publish

**Request:** PATCH /api/v1/posts/2/publish

**Response:** HTTP 200 (0.31s)

```json
{
  "id": 2,
  "title": "Second Post",
  "content": "Content of the second post.",
  "coverImageUrl": null,
  "status": "PUBLISHED",
  "createdAt": "2026-07-17T17:27:19",
  "updatedAt": "2026-07-17T17:27:19",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 0,
  "likedByCurrentUser": false,
  "tags": [
    "java"
  ]
}
```

**SQL Queries (101 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id 
    from
        posts p1_0 
    where
        p1_0.id=? 
        and p1_0.user_id=? 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [2]
binding parameter (3:INTEGER) <- [1]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        t1_0.post_id,
        t1_1.id,
        t1_1.created_at,
        t1_1.name 
    from
        post_tag t1_0 
    join
        tags t1_1 
            on t1_1.id=t1_0.tag_id 
    where
        t1_0.post_id=?
binding parameter (1:BIGINT) <- [2]

    update
        posts 
    set
        content=?,
        cover_image_url=?,
        status=?,
        title=?,
        updated_at=?,
        user_id=? 
    where
        id=?
binding parameter (1:VARCHAR) <- [Content of the second post.]
binding parameter (2:VARCHAR) <- [null]
binding parameter (3:VARCHAR) <- [PUBLISHED]
binding parameter (4:VARCHAR) <- [Second Post]
binding parameter (5:TIMESTAMP) <- [2026-07-17T17:27:19.632853]
binding parameter (6:BIGINT) <- [2]
binding parameter (7:BIGINT) <- [2]
```

---


### GET /posts (public, paginated)

**Request:** GET /api/v1/posts?page=0&size=10

**Response:** HTTP 200 (0.44s)

```json
{
  "content": [
    {
      "id": 2,
      "title": "Second Post",
      "excerpt": "Content of the second post.",
      "coverImageUrl": null,
      "status": "PUBLISHED",
      "createdAt": "2026-07-17T17:27:19",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 0
    },
    {
      "id": 1,
      "title": "First Post",
      "excerpt": "Content of the first post.",
      "coverImageUrl": null,
      "status": "DRAFT",
      "createdAt": "2026-07-17T17:27:18",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 0
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 2,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 2,
  "totalPages": 1
}
```

**SQL Queries (41 lines):**

```sql

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    order by
        p1_0.created_at desc 
    offset
        ? rows 
    fetch
        first ? rows only
binding parameter (1:INTEGER) <- [0]
binding parameter (2:INTEGER) <- [10]

    select
        pl1_0.post_id,
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id in (?, ?) 
    group by
        pl1_0.post_id
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [1]
```

---


### GET /posts/{id} (public, single post)

**Request:** GET /api/v1/posts/2

**Response:** HTTP 200 (0.24s)

```json
{
  "id": 2,
  "title": "Second Post",
  "content": "Content of the second post.",
  "coverImageUrl": null,
  "status": "PUBLISHED",
  "createdAt": "2026-07-17T17:27:19",
  "updatedAt": "2026-07-17T17:27:19",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 0,
  "likedByCurrentUser": false,
  "tags": [
    "java"
  ]
}
```

**SQL Queries (46 lines):**

```sql

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id=?
binding parameter (1:BIGINT) <- [2]

    select
        t1_0.post_id,
        t1_1.id,
        t1_1.created_at,
        t1_1.name 
    from
        post_tag t1_0 
    join
        tags t1_1 
            on t1_1.id=t1_0.tag_id 
    where
        t1_0.post_id=?
binding parameter (1:BIGINT) <- [2]
```

---


### GET /posts/{id}/comments

**Request:** GET /api/v1/posts/2/comments

**Response:** HTTP 200 (0.17s)

```json
{
  "content": [],
  "empty": true,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 0,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 0,
  "totalPages": 0
}
```

**SQL Queries (25 lines):**

```sql

    select
        c1_0.id,
        c1_0.content,
        c1_0.created_at,
        c1_0.post_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        comments c1_0 
    left join
        users u1_0 
            on u1_0.id=c1_0.user_id 
    where
        c1_0.post_id=? 
    order by
        c1_0.created_at desc 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [2]
binding parameter (2:INTEGER) <- [10]
```

---


### POST /posts/{id}/comments (add comment)

**Request:** POST /api/v1/posts/2/comments

**Response:** HTTP 201 (0.21s)

```json
{
  "id": 1,
  "content": "Great post!",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "createdAt": "2026-07-17T17:27:21"
}
```

**SQL Queries (70 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id 
    from
        posts p1_0 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        users u1_0 
    where
        u1_0.id=?
binding parameter (1:BIGINT) <- [2]

    insert 
    into
        comments
        (content, created_at, post_id, user_id) 
    values
        (?, ?, ?, ?)
binding parameter (1:VARCHAR) <- [Great post!]
binding parameter (2:TIMESTAMP) <- [2026-07-17T17:27:21.973493]
binding parameter (3:BIGINT) <- [2]
binding parameter (4:BIGINT) <- [2]
```

---


### POST /posts/{id}/like

**Request:** POST /api/v1/posts/2/like

**Response:** HTTP 201 (0.19s)

```json
{
  "liked": true,
  "likeCount": 1
}
```

**SQL Queries (77 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id 
    from
        posts p1_0 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        pl1_0.id,
        pl1_0.created_at,
        pl1_0.post_id,
        pl1_0.user_id 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id=? 
        and pl1_0.user_id=?
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [2]

    insert 
    into
        post_likes
        (created_at, post_id, user_id) 
    values
        (?, ?, ?)
binding parameter (1:TIMESTAMP) <- [2026-07-17T17:27:22.468574]
binding parameter (2:BIGINT) <- [2]
binding parameter (3:BIGINT) <- [2]

    select
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id=?
binding parameter (1:BIGINT) <- [2]
```

---


### GET /posts/{postId}/likes/count

**Request:** GET /api/v1/posts/2/likes/count

**Response:** HTTP 200 (0.24s)

```json
1
```

**SQL Queries (16 lines):**

```sql

    select
        count(*) 
    from
        posts p1_0 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id=?
binding parameter (1:BIGINT) <- [2]
```

---


### GET /posts/me (authenticated)

**Request:** GET /api/v1/posts/me?page=0&size=10

**Response:** HTTP 200 (0.34s)

```json
{
  "content": [
    {
      "id": 2,
      "title": "Second Post",
      "excerpt": "Content of the second post.",
      "coverImageUrl": null,
      "status": "PUBLISHED",
      "createdAt": "2026-07-17T17:27:19",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 1
    },
    {
      "id": 1,
      "title": "First Post",
      "excerpt": "Content of the first post.",
      "coverImageUrl": null,
      "status": "DRAFT",
      "createdAt": "2026-07-17T17:27:18",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 0
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 2,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 2,
  "totalPages": 1
}
```

**SQL Queries (72 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    where
        p1_0.user_id=? 
    order by
        p1_0.created_at desc 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [2]
binding parameter (2:INTEGER) <- [10]

    select
        pl1_0.post_id,
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id in (?, ?) 
    group by
        pl1_0.post_id
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [1]
```

---


### GET /tags/{tagName}/posts

**Request:** GET /api/v1/tags/java/posts?page=0&size=10

**Response:** HTTP 200 (0.24s)

```json
{
  "content": [
    {
      "id": 2,
      "title": "Second Post",
      "excerpt": "Content of the second post.",
      "coverImageUrl": null,
      "status": "PUBLISHED",
      "createdAt": "2026-07-17T17:27:19",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 1
    },
    {
      "id": 1,
      "title": "First Post",
      "excerpt": "Content of the first post.",
      "coverImageUrl": null,
      "status": "DRAFT",
      "createdAt": "2026-07-17T17:27:18",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 0
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 2,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 2,
  "totalPages": 1
}
```

**SQL Queries (47 lines):**

```sql

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    left join
        post_tag t1_0 
            on p1_0.id=t1_0.post_id 
    left join
        tags t1_1 
            on t1_1.id=t1_0.tag_id 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    where
        t1_1.name=? 
    order by
        p1_0.created_at desc 
    fetch
        first ? rows only
binding parameter (1:VARCHAR) <- [java]
binding parameter (2:INTEGER) <- [10]

    select
        pl1_0.post_id,
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id in (?, ?) 
    group by
        pl1_0.post_id
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [1]
```

---


### PUT /posts/{id} (update post)

**Request:** PUT /api/v1/posts/2

**Response:** HTTP 200 (0.26s)

```json
{
  "id": 2,
  "title": "Updated Second Post",
  "content": "Updated content.",
  "coverImageUrl": null,
  "status": "PUBLISHED",
  "createdAt": "2026-07-17T17:27:19",
  "updatedAt": "2026-07-17T17:27:19",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 0,
  "likedByCurrentUser": false,
  "tags": [
    "java",
    "spring"
  ]
}
```

**SQL Queries (123 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id 
    from
        posts p1_0 
    where
        p1_0.id=? 
        and p1_0.user_id=? 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [2]
binding parameter (3:INTEGER) <- [1]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        t1_0.id,
        t1_0.created_at,
        t1_0.name 
    from
        tags t1_0 
    where
        t1_0.name in (?, ?)
binding parameter (1:VARCHAR) <- [java]
binding parameter (2:VARCHAR) <- [spring]

    update
        posts 
    set
        content=?,
        cover_image_url=?,
        status=?,
        title=?,
        updated_at=?,
        user_id=? 
    where
        id=?
binding parameter (1:VARCHAR) <- [Updated content.]
binding parameter (2:VARCHAR) <- [null]
binding parameter (3:VARCHAR) <- [PUBLISHED]
binding parameter (4:VARCHAR) <- [Updated Second Post]
binding parameter (5:TIMESTAMP) <- [2026-07-17T17:27:24.795721]
binding parameter (6:BIGINT) <- [2]
binding parameter (7:BIGINT) <- [2]

    delete 
    from
        post_tag 
    where
        post_id=?
binding parameter (1:BIGINT) <- [2]

    insert 
    into
        post_tag
        (post_id, tag_id) 
    values
        (?, ?)
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [1]

    insert 
    into
        post_tag
        (post_id, tag_id) 
    values
        (?, ?)
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [2]
```

---


### GET /test (basic auth check)

**Request:** GET /api/v1/test

**Response:** HTTP 200 (0.28s)

```
JWT is valid for testuser@example.com
```

**SQL Queries (31 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]
```

---


### GET /test/user-dashboard (ROLE_USER)

**Request:** GET /api/v1/test/user-dashboard

**Response:** HTTP 200 (0.19s)

```
SUCCESS: You reached the User Dashboard! Anyone with ROLE_USER can see this.
```

**SQL Queries (31 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]
```

---


### GET /test/create-post (POST_CREATE)

**Request:** GET /api/v1/test/create-post

**Response:** HTTP 200 (0.22s)

```
SUCCESS: Action Allowed! You possess the fine-grained 'POST_CREATE' privilege.
```

**SQL Queries (31 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]
```

---


### GET /test/delete-user (should be 403)

**Request:** GET /api/v1/test/delete-user

**Response:** HTTP 403 (0.37s)

```json
{
  "status": 403,
  "message": "Access Denied",
  "timestamp": "2026-07-17T17:27:27.1113626",
  "path": "/test/delete-user"
}
```

**SQL Queries (31 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]
```

---


### DELETE /posts/{postId}/comments/{commentId}

**Request:** DELETE /api/v1/posts/2/comments/1

**Response:** HTTP 0 (0.32s)

**SQL Queries (55 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        c1_0.id 
    from
        comments c1_0 
    where
        c1_0.id=? 
        and c1_0.user_id=? 
        and c1_0.post_id=? 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [1]
binding parameter (2:BIGINT) <- [2]
binding parameter (3:BIGINT) <- [2]
binding parameter (4:INTEGER) <- [1]

    delete 
    from
        comments c1_0 
    where
        c1_0.id=? 
        and c1_0.post_id=?
binding parameter (1:BIGINT) <- [1]
binding parameter (2:BIGINT) <- [2]
```

---


### PATCH /posts/{id}/unpublish

**Request:** PATCH /api/v1/posts/2/unpublish

**Response:** HTTP 200 (0.30s)

```json
{
  "id": 2,
  "title": "Updated Second Post",
  "content": "Updated content.",
  "coverImageUrl": null,
  "status": "DRAFT",
  "createdAt": "2026-07-17T17:27:19",
  "updatedAt": "2026-07-17T17:27:24",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 0,
  "likedByCurrentUser": false,
  "tags": [
    "java",
    "spring"
  ]
}
```

**SQL Queries (101 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id 
    from
        posts p1_0 
    where
        p1_0.id=? 
        and p1_0.user_id=? 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [2]
binding parameter (3:INTEGER) <- [1]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        t1_0.post_id,
        t1_1.id,
        t1_1.created_at,
        t1_1.name 
    from
        post_tag t1_0 
    join
        tags t1_1 
            on t1_1.id=t1_0.tag_id 
    where
        t1_0.post_id=?
binding parameter (1:BIGINT) <- [2]

    update
        posts 
    set
        content=?,
        cover_image_url=?,
        status=?,
        title=?,
        updated_at=?,
        user_id=? 
    where
        id=?
binding parameter (1:VARCHAR) <- [Updated content.]
binding parameter (2:VARCHAR) <- [null]
binding parameter (3:VARCHAR) <- [DRAFT]
binding parameter (4:VARCHAR) <- [Updated Second Post]
binding parameter (5:TIMESTAMP) <- [2026-07-17T17:27:28.397146]
binding parameter (6:BIGINT) <- [2]
binding parameter (7:BIGINT) <- [2]
```

---


### DELETE /posts/{id}

**Request:** DELETE /api/v1/posts/2

**Response:** HTTP 0 (0.38s)

**SQL Queries (103 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [testuser@example.com]

    select
        p1_0.id 
    from
        posts p1_0 
    where
        p1_0.id=? 
        and p1_0.user_id=? 
    fetch
        first ? rows only
binding parameter (1:BIGINT) <- [2]
binding parameter (2:BIGINT) <- [2]
binding parameter (3:INTEGER) <- [1]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id 
    from
        posts p1_0 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [2]

    select
        c1_0.post_id,
        c1_0.id,
        c1_0.content,
        c1_0.created_at,
        c1_0.user_id 
    from
        comments c1_0 
    where
        c1_0.post_id=?
binding parameter (1:BIGINT) <- [2]

    select
        l1_0.post_id,
        l1_0.id,
        l1_0.created_at,
        l1_0.user_id 
    from
        post_likes l1_0 
    where
        l1_0.post_id=?
binding parameter (1:BIGINT) <- [2]

    delete 
    from
        post_tag 
    where
        post_id=?
binding parameter (1:BIGINT) <- [2]

    delete 
    from
        post_likes 
    where
        id=?
binding parameter (1:BIGINT) <- [1]

    delete 
    from
        posts 
    where
        id=?
binding parameter (1:BIGINT) <- [2]
```

---


### GET /admin (admin only)

**Request:** GET /api/v1/admin

**Response:** HTTP 200 (0.19s)

```
Admin access granted for admin@blog.com
```

**SQL Queries (31 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [admin@blog.com]
```

---


### GET /test/admin-dashboard (ROLE_ADMIN)

**Request:** GET /api/v1/test/admin-dashboard

**Response:** HTTP 200 (0.21s)

```
SUCCESS: Welcome Supreme Commander! Only accounts with ROLE_ADMIN can see this.
```

**SQL Queries (31 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [admin@blog.com]
```

---


### GET /posts (list before admin delete)

**Request:** GET /api/v1/posts?page=0&size=10

**Response:** HTTP 200 (0.19s)

```json
{
  "content": [
    {
      "id": 1,
      "title": "First Post",
      "excerpt": "Content of the first post.",
      "coverImageUrl": null,
      "status": "DRAFT",
      "createdAt": "2026-07-17T17:27:18",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "likeCount": 0
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 1,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "unpaged": false
  },
  "size": 10,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**SQL Queries (71 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [admin@blog.com]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id,
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type 
    from
        posts p1_0 
    join
        users u1_0 
            on u1_0.id=p1_0.user_id 
    order by
        p1_0.created_at desc 
    offset
        ? rows 
    fetch
        first ? rows only
binding parameter (1:INTEGER) <- [0]
binding parameter (2:INTEGER) <- [10]

    select
        pl1_0.post_id,
        count(pl1_0.id) 
    from
        post_likes pl1_0 
    where
        pl1_0.post_id in (?) 
    group by
        pl1_0.post_id
binding parameter (1:BIGINT) <- [1]
```

---


### DELETE /admin/posts/{id}

**Request:** DELETE /api/v1/admin/posts/1

**Response:** HTTP 0 (0.32s)

**SQL Queries (83 lines):**

```sql

    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.provider_id,
        u1_0.provider_type,
        r1_0.user_id,
        r1_1.id,
        r1_1.name,
        p1_0.role_id,
        p1_1.id,
        p1_1.name 
    from
        users u1_0 
    left join
        users_roles r1_0 
            on u1_0.id=r1_0.user_id 
    left join
        roles r1_1 
            on r1_1.id=r1_0.role_id 
    left join
        roles_privileges p1_0 
            on r1_1.id=p1_0.role_id 
    left join
        privileges p1_1 
            on p1_1.id=p1_0.privilege_id 
    where
        u1_0.email=?
binding parameter (1:VARCHAR) <- [admin@blog.com]

    select
        p1_0.id,
        p1_0.content,
        p1_0.cover_image_url,
        p1_0.created_at,
        p1_0.status,
        p1_0.title,
        p1_0.updated_at,
        p1_0.user_id 
    from
        posts p1_0 
    where
        p1_0.id=?
binding parameter (1:BIGINT) <- [1]

    select
        c1_0.post_id,
        c1_0.id,
        c1_0.content,
        c1_0.created_at,
        c1_0.user_id 
    from
        comments c1_0 
    where
        c1_0.post_id=?
binding parameter (1:BIGINT) <- [1]

    select
        l1_0.post_id,
        l1_0.id,
        l1_0.created_at,
        l1_0.user_id 
    from
        post_likes l1_0 
    where
        l1_0.post_id=?
binding parameter (1:BIGINT) <- [1]

    delete 
    from
        post_tag 
    where
        post_id=?
binding parameter (1:BIGINT) <- [1]

    delete 
    from
        posts 
    where
        id=?
binding parameter (1:BIGINT) <- [1]
```

<!-- ANALYSIS:
  Queries: 4 (2 SELECT, 2 INSERT)
  Verdict: Clean
  Issues: None. Standard registration flow: checks for existing user, assigns ROLE_USER, inserts user + role mapping.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None. Simple single-table scan.
-->

<!-- ANALYSIS:
  Queries: 8 (3 SELECT, 5 INSERT)
  Verdict: Clean
  Issues: Tags are inserted individually rather than batch-inserted, but this is JPA persist() behavior and acceptable.
-->

<!-- ANALYSIS:
  Queries: 5 (3 SELECT, 2 INSERT)
  Verdict: Clean
  Issues: Same pattern as post 1. No N+1.
-->

<!-- ANALYSIS:
  Queries: 5 (4 SELECT, 1 UPDATE)
  Verdict: Clean
  Issues: Ownership check uses fetch-first (efficient). Full entity load before update is standard JPA.
-->

<!-- ANALYSIS:
  Queries: 2 SELECT
  Verdict: Clean
  Issues: None. Pagination at DB level (OFFSET/FETCH). Like counts batched with GROUP BY on post_id IN clause -- no N+1.
-->

<!-- ANALYSIS:
  Queries: 3 SELECT
  Verdict: Clean
  Issues: None. Post + author joined in one query, separate like count and tag queries. No N+1.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None. Single query with LEFT JOIN on users, paginated at DB level.
-->

<!-- ANALYSIS:
  Queries: 4 (3 SELECT, 1 INSERT)
  Verdict: Clean
  Issues: None. Standard authenticated insert flow.
-->

<!-- ANALYSIS:
  Queries: 5 (4 SELECT, 1 INSERT)
  Verdict: Clean
  Issues: 4 SELECTs is a bit chatty (user lookup, post lookup, existing like check, post re-fetch) but no N+1 pattern.
-->

<!-- ANALYSIS:
  Queries: 2 SELECT
  Verdict: Clean
  Issues: None. One SELECT for count, one for post existence.
-->

<!-- ANALYSIS:
  Queries: 3 SELECT
  Verdict: Clean
  Issues: None. Pagination at DB level. Like counts batched with GROUP BY.
-->

<!-- ANALYSIS:
  Queries: 2 SELECT
  Verdict: Clean
  Issues: None. Pagination at DB level.
-->

<!-- ANALYSIS:
  Queries: 8 (4 SELECT, 2 INSERT, 1 UPDATE, 1 DELETE)
  Verdict: Clean
  Issues: Tag reassignment causes DELETE of old post_tag rows + INSERT of new ones. Expected JPA orphan removal behavior.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean (expected 403)
  Issues: None. Correctly blocked by security.
-->

<!-- ANALYSIS:
  Queries: 3 (2 SELECT, 1 DELETE)
  Verdict: Clean
  Issues: Status 0 is a script artifact (204 No Content body parsing). Endpoint was hit successfully.
-->

<!-- ANALYSIS:
  Queries: 5 (4 SELECT, 1 UPDATE)
  Verdict: Clean
  Issues: Same pattern as publish.
-->

<!-- ANALYSIS:
  Queries: 8 (5 SELECT, 3 DELETE)
  Verdict: Clean
  Issues: Status 0 is a script artifact (204 No Content). Cascade deletes post_tag, comments, then post.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None.
-->

<!-- ANALYSIS:
  Queries: 1 SELECT
  Verdict: Clean
  Issues: None.
-->

<!-- ANALYSIS:
  Queries: 3 SELECT
  Verdict: Clean
  Issues: None. Pagination at DB level, batched like counts.
-->

<!-- ANALYSIS:
  Queries: 6 (4 SELECT, 2 DELETE)
  Verdict: Clean
  Issues: Status 0 is a script artifact (204 No Content). Admin delete follows same cascade.
-->


---

