-- 1. Insert Blog Permissions
INSERT INTO privileges (name) VALUES ('POST_READ'), ('POST_CREATE'), ('POST_EDIT'), ('POST_DELETE'), ('USER_MANAGE');

-- 2. Insert High-Level System Roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- 3. Map Permissions to ROLE_USER (Can read and write/edit their own posts)
-- Assuming ROLE_USER id=1, POST_READ id=1, POST_CREATE id=2, POST_EDIT id=3
INSERT INTO roles_privileges (role_id, privilege_id) VALUES (1, 1), (1, 2), (1, 3);

-- 4. Map Permissions to ROLE_ADMIN (Can manage users and hard delete any post)
-- Assuming ROLE_ADMIN id=2, POST_DELETE id=4, USER_MANAGE id=5
INSERT INTO roles_privileges (role_id, privilege_id) VALUES (2, 1), (2, 2), (2, 3), (2, 4), (2, 5);

-- 5. Seed an admin user (password: admin123)
INSERT INTO users (name, email, password, provider_type)
VALUES ('Admin', 'admin@blog.com', '$2a$10$o0MXO3KdDMTgMwAf818Fe.OuB7/8iknaEQZi.0ZC8u1.Yc7ie3hkq', 'EMAIL');

-- 6. Assign ROLE_ADMIN to the admin user
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@blog.com' AND r.name = 'ROLE_ADMIN';

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