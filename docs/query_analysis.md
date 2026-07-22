# SQL Query Analysis Report

Analyzed at: 2026-07-23 01:24:25

---

### register_user

**Request:** POST /api/v1/auth/register

**Response:** HTTP 201 (1.61s)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc4NDc1MDA2NywiZXhwIjoxNzg0ODM2NDY3fQ.SZMsn964z7lEgmjisuJwnokpaI7E7oEgprNUoxMZv38"
}
```

**SQL Queries:** 4

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from users u1_0 where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select r1_0.id, r1_0.name from roles r1_0 where r1_0.name=? binding parameter (1:VARCHAR) <- [ROLE_USER]
insert into users (email, name, password, provider_id, provider_type) values (?, ?, ?, ?, ?) binding parameter (1:VARCHAR) <- [testuser@example.com] binding parameter (2:VARCHAR) <- [Test User] binding parameter (3:VARCHAR) <- [$2a$10$yq50QHpCTZmDgGDflfD9fOwEUPZ7ctiidSSXsd29DOXlOETyxt7tq] binding parameter (4:VARCHAR) <- [null] binding parameter (5:VARCHAR) <- [EMAIL]
insert into users_roles (user_id, role_id) values (?, ?) binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [1]
```

---

### login_user

**Request:** POST /api/v1/auth/login

**Response:** HTTP 200 (0.88s)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTc4NDc1MDA2OCwiZXhwIjoxNzg0ODM2NDY4fQ.0HV46o0Mf0wmh4HUhRfpuklyorbzrndTNAMSxf2TJ-s"
}
```

**SQL Queries:** 2

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from users u1_0 where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
```

---

### login_admin

**Request:** POST /api/v1/auth/login

**Response:** HTTP 200 (0.34s)

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBibG9nLmNvbSIsImlhdCI6MTc4NDc1MDA2OSwiZXhwIjoxNzg0ODM2NDY5fQ.1pOuGOESKXVVcXi0W4zvWLD7aXiF_KmI3IvgFoHZ1w8"
}
```

**SQL Queries:** 2

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from users u1_0 where u1_0.email=? binding parameter (1:VARCHAR) <- [admin@blog.com]
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [admin@blog.com]
```

---

### create_post_draft

**Request:** POST /api/v1/posts

**Response:** HTTP 201 (1.07s)

```json
{
  "id": 1,
  "title": "First Post",
  "content": "Content of the first post.",
  "coverImageUrl": null,
  "status": "DRAFT",
  "createdAt": "2026-07-23T01:24:30",
  "updatedAt": "2026-07-23T01:24:30",
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

**SQL Queries:** 8

**Repeated query shapes (fact, not a verdict — worth checking against the source):**
- ran 2x: `insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (?:bigint) <- [?] binding parameter (?:bigint) <- [?]`

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select t1_0.id, t1_0.created_at, t1_0.name from tags t1_0 where t1_0.name in (?, ?) binding parameter (1:VARCHAR) <- [java] binding parameter (2:VARCHAR) <- [spring]
insert into tags (created_at, name) values (?, ?) binding parameter (1:TIMESTAMP) <- [2026-07-23T01:24:30.182486] binding parameter (2:VARCHAR) <- [java]
insert into tags (created_at, name) values (?, ?) binding parameter (1:TIMESTAMP) <- [2026-07-23T01:24:30.196976] binding parameter (2:VARCHAR) <- [spring]
insert into posts (content, cover_image_url, created_at, status, title, updated_at, user_id) values (?, ?, ?, ?, ?, ?, ?) binding parameter (1:VARCHAR) <- [Content of the first post.] binding parameter (2:VARCHAR) <- [null] binding parameter (3:TIMESTAMP) <- [2026-07-23T01:24:30.205567] binding parameter (4:VARCHAR) <- [DRAFT] binding parameter (5:VARCHAR) <- [First Post] binding parameter (6:TIMESTAMP) <- [2026-07-23T01:24:30.205567] binding parameter (7:BIGINT) <- [2]
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from users u1_0 where u1_0.id=? binding parameter (1:BIGINT) <- [2]
insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (1:BIGINT) <- [1] binding parameter (2:BIGINT) <- [1]
insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (1:BIGINT) <- [1] binding parameter (2:BIGINT) <- [2]
```

---

### create_post_to_publish

**Request:** POST /api/v1/posts

**Response:** HTTP 201 (0.33s)

```json
{
  "id": 2,
  "title": "Second Post",
  "content": "Content of the second post.",
  "coverImageUrl": null,
  "status": "DRAFT",
  "createdAt": "2026-07-23T01:24:30",
  "updatedAt": "2026-07-23T01:24:30",
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

**SQL Queries:** 5

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select t1_0.id, t1_0.created_at, t1_0.name from tags t1_0 where t1_0.name in (?) binding parameter (1:VARCHAR) <- [java]
insert into posts (content, cover_image_url, created_at, status, title, updated_at, user_id) values (?, ?, ?, ?, ?, ?, ?) binding parameter (1:VARCHAR) <- [Content of the second post.] binding parameter (2:VARCHAR) <- [null] binding parameter (3:TIMESTAMP) <- [2026-07-23T01:24:30.844816] binding parameter (4:VARCHAR) <- [DRAFT] binding parameter (5:VARCHAR) <- [Second Post] binding parameter (6:TIMESTAMP) <- [2026-07-23T01:24:30.844816] binding parameter (7:BIGINT) <- [2]
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from users u1_0 where u1_0.id=? binding parameter (1:BIGINT) <- [2]
insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [1]
```

---

### publish_post

**Request:** PATCH /api/v1/posts/2/publish

**Response:** HTTP 200 (0.29s)

```json
{
  "id": 2,
  "title": "Second Post",
  "content": "Content of the second post.",
  "coverImageUrl": null,
  "status": "PUBLISHED",
  "createdAt": "2026-07-23T01:24:30",
  "updatedAt": "2026-07-23T01:24:30",
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

**SQL Queries:** 5

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id from posts p1_0 where p1_0.id=? and p1_0.user_id=? fetch first ? rows only binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [2] binding parameter (3:INTEGER) <- [1]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 join users u1_0 on u1_0.id=p1_0.user_id where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select t1_0.post_id, t1_1.id, t1_1.created_at, t1_1.name from post_tag t1_0 join tags t1_1 on t1_1.id=t1_0.tag_id where t1_0.post_id=? binding parameter (1:BIGINT) <- [2]
update posts set content=?, cover_image_url=?, status=?, title=?, updated_at=?, user_id=? where id=? binding parameter (1:VARCHAR) <- [Content of the second post.] binding parameter (2:VARCHAR) <- [null] binding parameter (3:VARCHAR) <- [PUBLISHED] binding parameter (4:VARCHAR) <- [Second Post] binding parameter (5:TIMESTAMP) <- [2026-07-23T01:24:31.321953] binding parameter (6:BIGINT) <- [2] binding parameter (7:BIGINT) <- [2]
```

---

### add_comment

**Request:** POST /api/v1/posts/2/comments

**Response:** HTTP 201 (0.23s)

```json
{
  "id": 1,
  "content": "Great post!",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "createdAt": "2026-07-23T01:24:31"
}
```

**SQL Queries:** 4

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id from posts p1_0 where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from users u1_0 where u1_0.id=? binding parameter (1:BIGINT) <- [2]
insert into comments (content, created_at, post_id, user_id) values (?, ?, ?, ?) binding parameter (1:VARCHAR) <- [Great post!] binding parameter (2:TIMESTAMP) <- [2026-07-23T01:24:31.777474] binding parameter (3:BIGINT) <- [2] binding parameter (4:BIGINT) <- [2]
```

---

### like_post

**Request:** POST /api/v1/posts/2/like

**Response:** HTTP 201 (0.49s)

```json
{
  "liked": true,
  "likeCount": 1
}
```

**SQL Queries:** 5

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id from posts p1_0 where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select pl1_0.id, pl1_0.created_at, pl1_0.post_id, pl1_0.user_id from post_likes pl1_0 where pl1_0.post_id=? and pl1_0.user_id=? binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [2]
insert into post_likes (created_at, post_id, user_id) values (?, ?, ?) binding parameter (1:TIMESTAMP) <- [2026-07-23T01:24:32.395164] binding parameter (2:BIGINT) <- [2] binding parameter (3:BIGINT) <- [2]
select count(pl1_0.id) from post_likes pl1_0 where pl1_0.post_id=? binding parameter (1:BIGINT) <- [2]
```

---

### list_tags

**Request:** GET /api/v1/tags

**Response:** HTTP 200 (0.16s)

```json
{
  "tags": [
    "java",
    "spring"
  ]
}
```

**SQL Queries:** 1

```sql
select t1_0.id, t1_0.created_at, t1_0.name from tags t1_0
```

---

### list_posts_paginated

**Request:** GET /api/v1/posts?page=0&size=10

**Response:** HTTP 200 (0.31s)

```json
{
  "content": [
    {
      "id": 2,
      "title": "Second Post",
      "excerpt": "Content of the second post.",
      "coverImageUrl": null,
      "status": "PUBLISHED",
      "createdAt": "2026-07-23T01:24:30",
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
      "createdAt": "2026-07-23T01:24:30",
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

**SQL Queries:** 2

**Pagination params present in request:** yes — LIMIT/TOP/FETCH FIRST found in SQL: yes

```sql
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 join users u1_0 on u1_0.id=p1_0.user_id order by p1_0.created_at desc offset ? rows fetch first ? rows only binding parameter (1:INTEGER) <- [0] binding parameter (2:INTEGER) <- [10]
select pl1_0.post_id, count(pl1_0.id) from post_likes pl1_0 where pl1_0.post_id in (?, ?) group by pl1_0.post_id binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [1]
```

---

### get_post_detail

**Request:** GET /api/v1/posts/2

**Response:** HTTP 200 (0.16s)

```json
{
  "id": 2,
  "title": "Second Post",
  "content": "Content of the second post.",
  "coverImageUrl": null,
  "status": "PUBLISHED",
  "createdAt": "2026-07-23T01:24:30",
  "updatedAt": "2026-07-23T01:24:31",
  "author": {
    "id": 2,
    "name": "Test User"
  },
  "likeCount": 1,
  "likedByCurrentUser": false,
  "tags": [
    "java"
  ]
}
```

**SQL Queries:** 3

```sql
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 join users u1_0 on u1_0.id=p1_0.user_id where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select count(pl1_0.id) from post_likes pl1_0 where pl1_0.post_id=? binding parameter (1:BIGINT) <- [2]
select t1_0.post_id, t1_1.id, t1_1.created_at, t1_1.name from post_tag t1_0 join tags t1_1 on t1_1.id=t1_0.tag_id where t1_0.post_id=? binding parameter (1:BIGINT) <- [2]
```

---

### get_post_comments

**Request:** GET /api/v1/posts/2/comments

**Response:** HTTP 200 (0.24s)

```json
{
  "content": [
    {
      "id": 1,
      "content": "Great post!",
      "author": {
        "id": 2,
        "name": "Test User"
      },
      "createdAt": "2026-07-23T01:24:31"
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

**SQL Queries:** 1

```sql
select c1_0.id, c1_0.content, c1_0.created_at, c1_0.post_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from comments c1_0 left join users u1_0 on u1_0.id=c1_0.user_id where c1_0.post_id=? order by c1_0.created_at desc fetch first ? rows only binding parameter (1:BIGINT) <- [2] binding parameter (2:INTEGER) <- [10]
```

---

### get_like_count

**Request:** GET /api/v1/posts/2/likes/count

**Response:** HTTP 200 (0.22s)

```json
1
```

**SQL Queries:** 2

```sql
select count(*) from posts p1_0 where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select count(pl1_0.id) from post_likes pl1_0 where pl1_0.post_id=? binding parameter (1:BIGINT) <- [2]
```

---

### my_posts

**Request:** GET /api/v1/posts/me?page=0&size=10

**Response:** HTTP 200 (0.22s)

```json
{
  "content": [
    {
      "id": 2,
      "title": "Second Post",
      "excerpt": "Content of the second post.",
      "coverImageUrl": null,
      "status": "PUBLISHED",
      "createdAt": "2026-07-23T01:24:30",
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
      "createdAt": "2026-07-23T01:24:30",
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

**SQL Queries:** 3

**Pagination params present in request:** yes — LIMIT/TOP/FETCH FIRST found in SQL: yes

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 join users u1_0 on u1_0.id=p1_0.user_id where p1_0.user_id=? order by p1_0.created_at desc fetch first ? rows only binding parameter (1:BIGINT) <- [2] binding parameter (2:INTEGER) <- [10]
select pl1_0.post_id, count(pl1_0.id) from post_likes pl1_0 where pl1_0.post_id in (?, ?) group by pl1_0.post_id binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [1]
```

---

### posts_by_tag

**Request:** GET /api/v1/tags/java/posts?page=0&size=10

**Response:** HTTP 200 (0.18s)

```json
{
  "content": [
    {
      "id": 2,
      "title": "Second Post",
      "excerpt": "Content of the second post.",
      "coverImageUrl": null,
      "status": "PUBLISHED",
      "createdAt": "2026-07-23T01:24:30",
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
      "createdAt": "2026-07-23T01:24:30",
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

**SQL Queries:** 2

**Pagination params present in request:** yes — LIMIT/TOP/FETCH FIRST found in SQL: yes

```sql
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 left join post_tag t1_0 on p1_0.id=t1_0.post_id left join tags t1_1 on t1_1.id=t1_0.tag_id join users u1_0 on u1_0.id=p1_0.user_id where t1_1.name=? order by p1_0.created_at desc fetch first ? rows only binding parameter (1:VARCHAR) <- [java] binding parameter (2:INTEGER) <- [10]
select pl1_0.post_id, count(pl1_0.id) from post_likes pl1_0 where pl1_0.post_id in (?, ?) group by pl1_0.post_id binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [1]
```

---

### update_post

**Request:** PUT /api/v1/posts/2

**Response:** HTTP 200 (0.24s)

```json
{
  "id": 2,
  "title": "Updated Second Post",
  "content": "Updated content.",
  "coverImageUrl": null,
  "status": "PUBLISHED",
  "createdAt": "2026-07-23T01:24:30",
  "updatedAt": "2026-07-23T01:24:31",
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

**SQL Queries:** 8

**Repeated query shapes (fact, not a verdict — worth checking against the source):**
- ran 2x: `insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (?:bigint) <- [?] binding parameter (?:bigint) <- [?]`

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id from posts p1_0 where p1_0.id=? and p1_0.user_id=? fetch first ? rows only binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [2] binding parameter (3:INTEGER) <- [1]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 join users u1_0 on u1_0.id=p1_0.user_id where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select t1_0.id, t1_0.created_at, t1_0.name from tags t1_0 where t1_0.name in (?, ?) binding parameter (1:VARCHAR) <- [java] binding parameter (2:VARCHAR) <- [spring]
update posts set content=?, cover_image_url=?, status=?, title=?, updated_at=?, user_id=? where id=? binding parameter (1:VARCHAR) <- [Updated content.] binding parameter (2:VARCHAR) <- [null] binding parameter (3:VARCHAR) <- [PUBLISHED] binding parameter (4:VARCHAR) <- [Updated Second Post] binding parameter (5:TIMESTAMP) <- [2026-07-23T01:24:35.837159] binding parameter (6:BIGINT) <- [2] binding parameter (7:BIGINT) <- [2]
delete from post_tag where post_id=? binding parameter (1:BIGINT) <- [2]
insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [1]
insert into post_tag (post_id, tag_id) values (?, ?) binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [2]
```

---

### delete_comment

**Request:** DELETE /api/v1/posts/2/comments/1

**Response:** HTTP 0 (0.21s)

```json
204
```

**SQL Queries:** 3

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select c1_0.id from comments c1_0 where c1_0.id=? and c1_0.user_id=? and c1_0.post_id=? fetch first ? rows only binding parameter (1:BIGINT) <- [1] binding parameter (2:BIGINT) <- [2] binding parameter (3:BIGINT) <- [2] binding parameter (4:INTEGER) <- [1]
delete from comments c1_0 where c1_0.id=? and c1_0.post_id=? binding parameter (1:BIGINT) <- [1] binding parameter (2:BIGINT) <- [2]
```

---

### unpublish_post

**Request:** PATCH /api/v1/posts/2/unpublish

**Response:** HTTP 200 (0.17s)

```json
{
  "id": 2,
  "title": "Updated Second Post",
  "content": "Updated content.",
  "coverImageUrl": null,
  "status": "DRAFT",
  "createdAt": "2026-07-23T01:24:30",
  "updatedAt": "2026-07-23T01:24:35",
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

**SQL Queries:** 5

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id from posts p1_0 where p1_0.id=? and p1_0.user_id=? fetch first ? rows only binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [2] binding parameter (3:INTEGER) <- [1]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id, u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type from posts p1_0 join users u1_0 on u1_0.id=p1_0.user_id where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select t1_0.post_id, t1_1.id, t1_1.created_at, t1_1.name from post_tag t1_0 join tags t1_1 on t1_1.id=t1_0.tag_id where t1_0.post_id=? binding parameter (1:BIGINT) <- [2]
update posts set content=?, cover_image_url=?, status=?, title=?, updated_at=?, user_id=? where id=? binding parameter (1:VARCHAR) <- [Updated content.] binding parameter (2:VARCHAR) <- [null] binding parameter (3:VARCHAR) <- [DRAFT] binding parameter (4:VARCHAR) <- [Updated Second Post] binding parameter (5:TIMESTAMP) <- [2026-07-23T01:24:36.662010] binding parameter (6:BIGINT) <- [2] binding parameter (7:BIGINT) <- [2]
```

---

### delete_post

**Request:** DELETE /api/v1/posts/2

**Response:** HTTP 0 (0.16s)

```json
204
```

**SQL Queries:** 8

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [testuser@example.com]
select p1_0.id from posts p1_0 where p1_0.id=? and p1_0.user_id=? fetch first ? rows only binding parameter (1:BIGINT) <- [2] binding parameter (2:BIGINT) <- [2] binding parameter (3:INTEGER) <- [1]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id from posts p1_0 where p1_0.id=? binding parameter (1:BIGINT) <- [2]
select c1_0.post_id, c1_0.id, c1_0.content, c1_0.created_at, c1_0.user_id from comments c1_0 where c1_0.post_id=? binding parameter (1:BIGINT) <- [2]
select l1_0.post_id, l1_0.id, l1_0.created_at, l1_0.user_id from post_likes l1_0 where l1_0.post_id=? binding parameter (1:BIGINT) <- [2]
delete from post_tag where post_id=? binding parameter (1:BIGINT) <- [2]
delete from post_likes where id=? binding parameter (1:BIGINT) <- [1]
delete from posts where id=? binding parameter (1:BIGINT) <- [2]
```

---

### admin_dashboard

**Request:** GET /api/v1/admin

**Response:** HTTP 200 (0.10s)

```
Admin access granted for admin@blog.com
```

**SQL Queries:** 1

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [admin@blog.com]
```

---

### admin_delete_draft

**Request:** DELETE /api/v1/admin/posts/1

**Response:** HTTP 0 (0.12s)

```json
204
```

**SQL Queries:** 6

```sql
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.provider_id, u1_0.provider_type, r1_0.user_id, r1_1.id, r1_1.name, p1_0.role_id, p1_1.id, p1_1.name from users u1_0 left join users_roles r1_0 on u1_0.id=r1_0.user_id left join roles r1_1 on r1_1.id=r1_0.role_id left join roles_privileges p1_0 on r1_1.id=p1_0.role_id left join privileges p1_1 on p1_1.id=p1_0.privilege_id where u1_0.email=? binding parameter (1:VARCHAR) <- [admin@blog.com]
select p1_0.id, p1_0.content, p1_0.cover_image_url, p1_0.created_at, p1_0.status, p1_0.title, p1_0.updated_at, p1_0.user_id from posts p1_0 where p1_0.id=? binding parameter (1:BIGINT) <- [1]
select c1_0.post_id, c1_0.id, c1_0.content, c1_0.created_at, c1_0.user_id from comments c1_0 where c1_0.post_id=? binding parameter (1:BIGINT) <- [1]
select l1_0.post_id, l1_0.id, l1_0.created_at, l1_0.user_id from post_likes l1_0 where l1_0.post_id=? binding parameter (1:BIGINT) <- [1]
delete from post_tag where post_id=? binding parameter (1:BIGINT) <- [1]
delete from posts where id=? binding parameter (1:BIGINT) <- [1]
```

---

