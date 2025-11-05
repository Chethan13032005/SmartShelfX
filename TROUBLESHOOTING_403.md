# 403 Error Troubleshooting Guide for Admin User

## Problem
You're logged in as `admin@smartshelfx.local` with role showing as "Admin" but still getting 403 (Access Denied) when adding products.

## Root Causes & Solutions

### 1. Backend Not Running ⚠️
**Symptom:** Debug panel shows "Backend not running (ERR_NETWORK)"

**Solution:** Start the backend
```powershell
cd "D:\Internship\Infosys_SmartShelfX AI Based Inventory Forecast and Auto Restock\backend\smartshelfx-backend"
.\mvnw.cmd spring-boot:run
```

If mvnw.cmd fails, use Maven directly:
```powershell
mvn clean spring-boot:run
```

Wait until you see:
```
Started SmartshelfxBackendApplication in X.XXX seconds
```

---

### 2. Database Role Mismatch 🔧
**Symptom:** Backend running, but debug panel shows "Backend 403"

**Most Common Cause:** The role in the database is stored as lowercase "admin" or something else, but backend expects "Admin" with capital A.

**Solution A - SQL Fix (Fastest):**
1. Open MySQL Workbench or command line
2. Run the SQL script `fix_admin_role.sql`:

```sql
USE smartshelfx;

-- Check current role
SELECT email, role FROM users WHERE email = 'admin@smartshelfx.local';

-- Fix if needed
UPDATE users SET role = 'Admin' WHERE email = 'admin@smartshelfx.local';

-- Verify
SELECT email, role FROM users WHERE email = 'admin@smartshelfx.local';
```

3. **Restart the backend** after running SQL
4. Logout and login again in the frontend

**Solution B - Backend Auto-Fix:**
The backend has a `@PostConstruct` method that normalizes roles on startup, but it only runs once when the app starts. So:
1. Restart the backend
2. Check the console logs for "normalized" mentions
3. Logout and login again

---

### 3. JWT Token Issues 🔑
**Symptom:** Token shows in debug panel but backend still denies access

**Solution:**
1. **Logout completely**
2. **Clear browser localStorage:**
   - Press F12 (open DevTools)
   - Go to Application tab
   - Click Local Storage → http://localhost:3000
   - Delete `token` and `user` keys
3. **Close and reopen browser**
4. **Login again**

---

### 4. Case Sensitivity in Role Check 📝
**Issue:** Backend checks role with `equalsIgnoreCase()` but there might be extra spaces or special characters

**Solution:** Check the exact role value in the debug panel. It should be exactly:
- `Admin` (capital A, rest lowercase, no spaces)
- Not: `admin`, `ADMIN`, ` Admin `, `Admin ` (trailing space)

If you see anything else, run the SQL fix above.

---

## Step-by-Step Debugging Process

### Step 1: Check Debug Panel
Look at the debug panel in bottom-right corner of Add Inventory page:

```
🔍 Auth Debug Info
Authenticated: ✅ Yes
Role: Admin                    ← Should be exactly "Admin"
Email: admin@smartshelfx.local
Backend: ✅ Backend OK (X products)  ← Should show OK or show the error
```

### Step 2: If Backend shows error
- **"Backend not running"** → Start backend (see Solution 1)
- **"Backend 403"** → Database role mismatch (see Solution 2)
- **"Backend 401"** → Token invalid (see Solution 3)

### Step 3: Check Backend Logs
When you try to add a product, check the backend terminal for logs like:

```java
// Good - should see:
Authentication successful for: admin@smartshelfx.local
User role: Admin

// Bad - might see:
User role: admin    ← lowercase, needs SQL fix
Access denied for user: admin@smartshelfx.local
```

### Step 4: Network Tab Check
1. Press F12 → Network tab
2. Try adding a product
3. Look for the `POST /api/products/add` request
4. Check the **Request Headers**:
   - Should have: `Authorization: Bearer eyJhbGc...`
5. Check the **Response**:
   - Status 403 with message showing why

---

## Quick Fix Checklist

- [ ] Backend is running (check terminal)
- [ ] MySQL is running (check services)
- [ ] Database role is exactly "Admin" (run SQL script)
- [ ] Logged out and logged in again
- [ ] Debug panel shows "Role: Admin"
- [ ] Debug panel shows "Backend: ✅ Backend OK"
- [ ] Browser localStorage cleared if needed

---

## Most Likely Solution

**90% of the time, it's:**
1. Database has role as lowercase "admin" instead of "Admin"
2. Backend started before the role was normalized

**Quick Fix:**
```sql
UPDATE users SET role = 'Admin' WHERE email = 'admin@smartshelfx.local';
```
Then **restart backend** and **re-login**.

---

## If Still Failing

1. **Check backend console output** when you click "Add Product"
2. **Copy the exact error message** from:
   - Debug panel
   - Browser console (F12)
   - Backend terminal
3. **Share the exact role string** shown in debug panel (copy-paste it)

The debug panel will now show exactly what the backend is returning!
