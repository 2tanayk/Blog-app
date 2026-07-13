-- 1. Insert Blog Permissions
INSERT INTO privileges (name)
VALUES ('POST_READ'),
       ('POST_CREATE'),
       ('POST_EDIT_OWN'),
       ('POST_EDIT_ANY'),
       ('POST_DELETE_OWN'),
       ('POST_DELETE_ANY'),
       ('COMMENT_CREATE'),
       ('COMMENT_EDIT_OWN'),
       ('COMMENT_DELETE_OWN'),
       ('COMMENT_DELETE_ANY'),
       ('TAG_DELETE');


-- 2. Insert High-Level System Roles
INSERT INTO roles (name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN');

-- 3. ROLE_USER -> privileges
--    Full control over their own content only.
INSERT INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r,privileges p
WHERE r.name = 'ROLE_USER'
  AND p.name IN ('POST_READ', 'POST_CREATE', 'POST_EDIT_OWN', 'POST_DELETE_OWN',
                 'COMMENT_CREATE', 'COMMENT_EDIT_OWN', 'COMMENT_DELETE_OWN'
    );

-- 4. Map Permissions to ROLE_ADMIN (Can manage users and hard delete any post)
-- Assuming ROLE_ADMIN id=2, POST_DELETE id=4, USER_MANAGE id=5
INSERT INTO roles_privileges (role_id, privilege_id)
SELECT r.id, p.id
FROM roles r,privileges p
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN (
                 'POST_READ', 'POST_CREATE', 'POST_EDIT_ANY', 'POST_DELETE_ANY',
                 'COMMENT_CREATE', 'COMMENT_EDIT_OWN', 'COMMENT_DELETE_ANY',
                 'TAG_DELETE'
    );

-- 5. Seed an admin user (password: admin123)
INSERT INTO users (name, email, password, provider_type)
VALUES ('Admin', 'admin@blog.com', '$2a$10$o0MXO3KdDMTgMwAf818Fe.OuB7/8iknaEQZi.0ZC8u1.Yc7ie3hkq', 'EMAIL');

-- 6. Assign ROLE_ADMIN to the admin user
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.email = 'admin@blog.com'
  AND r.name = 'ROLE_ADMIN';

--
--   │  Detail  │               Value                │
--   ├──────────┼────────────────────────────────────┤
--   │ Email    │ admin@blog.com                     │
--   ├──────────┼────────────────────────────────────┤
--   │ Password │ admin123                           │
--   ├──────────┼────────────────────────────────────┤
--   │ Role     │ ROLE_ADMIN (with all 5 privileges) │
--   ├──────────┼────────────────────────────────────┤
--   │ Provider │ EMAIL (password-based login)       │
--   └──────────┴────────────────────────────────────┘