-- SmartShelfX Database Diagnostic Script
-- Run this to check all potential issues with user roles and authentication

USE smartshelfx;

-- ====================================
-- 1. CHECK ALL USERS AND THEIR ROLES
-- ====================================
SELECT '=== ALL USERS ===' as Info;
SELECT 
    id,
    email,
    role,
    CONCAT('[', role, ']') as role_with_brackets,  -- Shows hidden spaces
    LENGTH(role) as role_length,  -- Should be 5 for Admin, 7 for Manager, 6 for Vendor
    full_name,
    enabled,
    created_at
FROM users
ORDER BY role, email;

-- ====================================
-- 2. FIND ADMIN USER SPECIFICALLY
-- ====================================
SELECT '=== ADMIN USER CHECK ===' as Info;
SELECT 
    id,
    email,
    role,
    CASE 
        WHEN role = 'Admin' THEN '✅ Correct'
        WHEN role = 'admin' THEN '⚠️ Lowercase - needs fix'
        WHEN role LIKE 'Admin%' OR role LIKE '%Admin' THEN '⚠️ Has spaces - needs trim'
        ELSE '❌ Wrong value'
    END as role_status,
    LENGTH(role) as role_length,
    enabled,
    full_name
FROM users
WHERE email = 'admin@smartshelfx.local';

-- ====================================
-- 3. FIND ALL PROBLEMATIC ROLES
-- ====================================
SELECT '=== PROBLEMATIC ROLES ===' as Info;
SELECT 
    id,
    email,
    role,
    CASE 
        WHEN role NOT IN ('Admin', 'Manager', 'Vendor') THEN 'Invalid Role'
        WHEN role LIKE ' %' OR role LIKE '% ' THEN 'Has Spaces'
        ELSE 'OK'
    END as issue
FROM users
WHERE role NOT IN ('Admin', 'Manager', 'Vendor')
   OR role LIKE ' %' 
   OR role LIKE '% ';

-- ====================================
-- 4. FIX ALL ROLE ISSUES
-- ====================================
SELECT '=== APPLYING FIXES ===' as Info;

-- Normalize common variations
UPDATE users SET role = 'Admin' WHERE LOWER(TRIM(role)) = 'admin';
UPDATE users SET role = 'Manager' WHERE LOWER(TRIM(role)) IN ('manager', 'store manager', 'warehouse manager');
UPDATE users SET role = 'Vendor' WHERE LOWER(TRIM(role)) = 'vendor';

-- Set default for any remaining invalid roles
UPDATE users SET role = 'Manager' 
WHERE role NOT IN ('Admin', 'Manager', 'Vendor') 
  AND role IS NOT NULL 
  AND TRIM(role) != '';

-- ====================================
-- 5. VERIFY FIXES
-- ====================================
SELECT '=== VERIFICATION AFTER FIXES ===' as Info;
SELECT 
    id,
    email,
    role,
    LENGTH(role) as role_length,
    enabled,
    full_name
FROM users
ORDER BY role, email;

-- ====================================
-- 6. ADMIN USER FINAL CHECK
-- ====================================
SELECT '=== ADMIN USER FINAL STATUS ===' as Info;
SELECT 
    email,
    role,
    CASE 
        WHEN role = 'Admin' THEN '✅ READY TO USE'
        ELSE '❌ STILL HAS ISSUES'
    END as status,
    enabled
FROM users
WHERE email = 'admin@smartshelfx.local';
