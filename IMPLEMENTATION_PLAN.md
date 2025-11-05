# SmartShelfX - Complete Role-Based Implementation Plan

## Current Status ✅
- ✅ User authentication (login/register/forgot-password)
- ✅ Role-based routing (Admin/Manager/Vendor dashboards)
- ✅ Product (Inventory) entity exists
- ✅ Basic inventory CRUD endpoints

## Missing Components 🔧

### Backend Entities & Endpoints Needed

#### 1. StockTransaction Entity
```java
- id, productId, type (IN/OUT), quantity, notes, timestamp, performedBy
- Endpoints:
  POST /api/stock/in (Manager only)
  POST /api/stock/out (Manager only)
  GET /api/stock/transactions (Manager/Admin)
  GET /api/stock/recent (Dashboard widget)
```

#### 2. PurchaseOrder Entity
```java
- id, vendorId, productId, quantity, status (PENDING/APPROVED/ACCEPTED/DISPATCHED/COMPLETED/REJECTED)
- createdBy (Manager), approvedBy (Admin), deliveryDate, notes
- Endpoints:
  POST /api/purchase-orders (Manager creates)
  GET /api/purchase-orders (role-filtered)
  PUT /api/purchase-orders/{id}/approve (Admin only)
  PUT /api/purchase-orders/{id}/reject (Admin only)
  PUT /api/purchase-orders/{id}/accept (Vendor only - for their POs)
  PUT /api/purchase-orders/{id}/dispatch (Vendor only)
  PUT /api/purchase-orders/{id}/complete (Manager/Admin)
```

#### 3. Dashboard Stats Endpoints
```java
GET /api/stats/admin (total products/vendors/managers/active POs)
GET /api/stats/manager (total SKUs/low stock/pending POs/stock value)
GET /api/stats/vendor (pending/dispatched/completed/ontime %)
```

#### 4. User Management Endpoints (Admin only)
```java
GET /api/users (list all with role filter)
POST /api/users/create (Admin creates Manager/Vendor)
PUT /api/users/{id}/role (Admin changes role)
DELETE /api/users/{id} (Admin soft-delete/deactivate)
```

### Frontend Components Needed

#### Admin Dashboard Enhancements
- Wire KPI cards to `/api/stats/admin`
- Fetch and display latest activities from audit logs
- Fetch low stock alerts from `/api/products?lowStock=true`
- "Manage Users" → navigate to `/users` (already done ✅)
- "Export Reports" → download CSV/PDF

#### Manager Dashboard Enhancements
- Wire KPI cards to `/api/stats/manager`
- "Add Product" → modal/page with form → POST `/api/products`
- "Create Purchase Order" → form with vendor/product/qty → POST `/api/purchase-orders`
- Stock-IN/OUT buttons → modals → POST `/api/stock/in` or `/api/stock/out`
- Inventory List table → fetch `/api/products` with stock levels
- Recent Stock-IN/OUT → fetch `/api/stock/recent`
- AI Forecast → placeholder chart (can integrate ML later)

#### Vendor Dashboard Enhancements
- Wire KPI cards to `/api/stats/vendor`
- Purchase Orders table → fetch `/api/purchase-orders?vendorId={current}`
- Accept button → PUT `/api/purchase-orders/{id}/accept`
- Reject button → PUT `/api/purchase-orders/{id}/reject`
- Update Dispatch → modal → PUT `/api/purchase-orders/{id}/dispatch` with tracking info
- Delivery Performance chart → calculate from PO history

### User Management Page (`/users`)
- Table: List all users (email, role, status, created date)
- "Add User" button → form → POST `/api/users/create`
- Edit role dropdown → PUT `/api/users/{id}/role`
- Deactivate button → DELETE `/api/users/{id}`

## Implementation Order 📋

### Phase 1: Backend Foundation (Priority)
1. Create `StockTransaction` model + repository + service
2. Create `PurchaseOrder` model + repository + service
3. Add `StockController` with IN/OUT endpoints (Manager auth)
4. Add `PurchaseOrderController` with full lifecycle endpoints
5. Add `StatsController` methods for all 3 roles
6. Enhance `UserController` with Admin-only CRUD

### Phase 2: Frontend Wiring
1. Create reusable modal components for forms
2. Wire Admin dashboard stats and tables
3. Wire Manager dashboard with Add Product/Stock/PO forms
4. Wire Vendor dashboard with PO actions
5. Create User Management page with full CRUD

### Phase 3: Testing & Polish
1. Test end-to-end flow:
   - Manager adds product
   - Manager creates PO
   - Admin approves PO
   - Vendor accepts PO
   - Vendor dispatches
   - Manager confirms receipt
2. Add loading states and error handling
3. Add toast notifications for actions
4. Populate charts with real data

## Security Checks ✅
- All `/api/stock/*` → Manager or Admin
- All `/api/purchase-orders` POST → Manager
- All `/api/purchase-orders/*/approve|reject` → Admin
- All `/api/purchase-orders/*/accept|dispatch` → Vendor (own POs only)
- All `/api/users/*` (except login/register) → Admin

## Database Tables (from existing SQL)
✅ users
✅ products
🔧 stock_transactions (needs migration)
🔧 purchase_orders (needs migration)
✅ notifications (exists)
✅ audit_logs (exists)

---

**Next Steps:**
1. Confirm this plan aligns with your requirements
2. I'll create all backend models, services, controllers
3. I'll wire all frontend dashboards with real forms and data
4. We'll test the complete flow end-to-end

Estimated Files to Create/Modify:
- Backend: ~8 new files (models, repos, services, controllers)
- Frontend: ~10 files (modal forms, API service hooks, dashboard updates)

Ready to proceed?
