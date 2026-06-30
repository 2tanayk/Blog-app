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