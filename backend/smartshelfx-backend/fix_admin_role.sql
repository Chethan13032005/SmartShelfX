-- Quick SQL script to check and fix admin user role
-- Run this in your MySQL client (e.g., MySQL Workbench or command line)

USE smartshelfx;

-- 1. Check the current role for admin@smartshelfx.local
SELECT id, email, role, full_name, enabled 
FROM users 
WHERE email = 'admin@smartshelfx.local';

-- 2. If the role is not 'Admin', update it:
UPDATE users 
SET role = 'Admin' 
WHERE email = 'admin@smartshelfx.local';

-- 3. Verify the change:
SELECT id, email, role, full_name, enabled 
FROM users 
WHERE email = 'admin@smartshelfx.local';

-- 4. Check all users and their roles (to see if there are other issues):
SELECT id, email, role, full_name, enabled 
FROM users 
ORDER BY role, email;
