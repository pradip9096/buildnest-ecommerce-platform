import { request, requestData } from './client';

// ── Dashboard stats ──────────────────────────────────────────────────────────

export interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
}

export async function fetchDashboardStats(): Promise<DashboardStats> {
  return requestData<DashboardStats>('/api/admin/reports/dashboard', {}, 'Failed to load dashboard stats');
}

// ── Orders ───────────────────────────────────────────────────────────────────

export interface AdminOrder {
  id: number;
  userId: number;
  status: string;
  totalAmount: number;
  createdAt: string;
}

export async function fetchAdminOrders(
  params: { status?: string; page?: number; size?: number } = {}
): Promise<{ content: AdminOrder[]; totalElements: number; totalPages: number }> {
  const q = new URLSearchParams();
  if (params.status) q.set('status', params.status);
  q.set('page', String(params.page ?? 0));
  q.set('size', String(params.size ?? 20));
  const data = await requestData<{ content: AdminOrder[]; totalElements: number; totalPages: number }>(
    `/api/v1/admin/orders?${q}`,
    {},
    'Failed to load orders'
  );
  return data ?? { content: [], totalElements: 0, totalPages: 0 };
}

export async function updateOrderStatus(
  orderId: number,
  status: string
): Promise<void> {
  await request(
    `/api/v1/admin/orders/${orderId}/status`,
    { method: 'PATCH', body: { status } },
    'Failed to update status'
  );
}

export async function refundOrder(
  orderId: number,
  amount: number,
  reason: string
): Promise<void> {
  await request(
    `/api/v1/admin/orders/${orderId}/refund`,
    { method: 'POST', body: { amount, reason } },
    'Failed to process refund'
  );
}

// ── Inventory ────────────────────────────────────────────────────────────────

export interface InventoryItem {
  productId: number;
  productName: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  status: string;
}

export async function fetchAdminInventory(): Promise<InventoryItem[]> {
  const data = await requestData<InventoryItem[]>('/api/v1/admin/inventory', {}, 'Failed to load inventory');
  return Array.isArray(data) ? data : [];
}

export async function adjustInventory(
  productId: number,
  delta: number,
  reason: string
): Promise<void> {
  await request(
    `/api/v1/admin/inventory/${productId}`,
    { method: 'PATCH', body: { delta, reason } },
    'Failed to adjust inventory'
  );
}

export interface InventoryDetail {
  id: number;
  quantityInStock: number;
  quantityReserved: number;
  availableQuantity: number;
  status: string;
}

export async function fetchInventoryDetail(productId: number): Promise<InventoryDetail> {
  return requestData<InventoryDetail>(
    `/api/v1/admin/inventory/product/${productId}`,
    {},
    'Failed to load inventory detail'
  );
}

export async function addStock(productId: number, quantity: number): Promise<InventoryDetail> {
  return requestData<InventoryDetail>(
    `/api/v1/admin/inventory/add-stock/${productId}?quantity=${quantity}`,
    { method: 'POST' },
    'Failed to add stock'
  );
}

export async function setStock(productId: number, quantity: number): Promise<InventoryDetail> {
  return requestData<InventoryDetail>(
    `/api/v1/admin/inventory/update-stock/${productId}?quantity=${quantity}`,
    { method: 'POST' },
    'Failed to update stock'
  );
}

export async function checkStockAvailability(productId: number, quantity: number): Promise<boolean> {
  return requestData<boolean>(
    `/api/v1/admin/inventory/check-availability/${productId}?quantity=${quantity}`,
    {},
    'Failed to check availability'
  );
}

// ── Users ────────────────────────────────────────────────────────────────────

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  roles?: string[];
  enabled?: boolean;
}

export interface UpdateUserInput {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
}

export async function fetchAdminUsers(): Promise<AdminUser[]> {
  const data = await requestData<AdminUser[]>('/api/admin/users', {}, 'Failed to load users');
  return Array.isArray(data) ? data : [];
}

export async function fetchAdminUser(userId: number): Promise<AdminUser> {
  return requestData<AdminUser>(`/api/admin/users/${userId}`, {}, 'Failed to load user');
}

export async function updateAdminUser(userId: number, input: UpdateUserInput): Promise<AdminUser> {
  return requestData<AdminUser>(
    `/api/admin/users/${userId}`,
    { method: 'PUT', body: input },
    'Failed to update user'
  );
}

export async function deleteAdminUser(userId: number): Promise<void> {
  await request(`/api/admin/users/${userId}`, { method: 'DELETE' }, 'Failed to disable user');
}

// ── Categories ───────────────────────────────────────────────────────────────

export interface AdminCategory {
  id: number;
  name: string;
  description?: string;
  imageUrl?: string;
  isActive?: boolean;
  parentCategory?: { id: number } | null;
}

export interface CategoryFormInput {
  name: string;
  description?: string;
  imageUrl?: string;
  parentId?: number | null;
}

export async function fetchAdminCategories(): Promise<AdminCategory[]> {
  const data = await requestData<AdminCategory[]>('/api/v1/admin/categories', {}, 'Failed to load categories');
  return Array.isArray(data) ? data : [];
}

export async function createAdminCategory(input: CategoryFormInput): Promise<AdminCategory> {
  return requestData<AdminCategory>(
    '/api/v1/admin/categories',
    { method: 'POST', body: input },
    'Failed to create category'
  );
}

export async function updateAdminCategory(id: number, input: CategoryFormInput): Promise<AdminCategory> {
  return requestData<AdminCategory>(
    `/api/v1/admin/categories/${id}`,
    { method: 'PUT', body: input },
    'Failed to update category'
  );
}

export async function deleteAdminCategory(id: number): Promise<void> {
  await request(`/api/v1/admin/categories/${id}`, { method: 'DELETE' }, 'Failed to delete category');
}

// ── Products ─────────────────────────────────────────────────────────────────

export interface AdminProduct {
  id: number;
  name: string;
  description?: string;
  price: number;
  discountPrice?: number;
  stockQuantity?: number;
  sku?: string;
  imageUrl?: string;
  isActive?: boolean;
  isFeatured?: boolean;
  category?: { id: number; name?: string } | null;
}

export interface ProductFormInput {
  name: string;
  description: string;
  price: number;
  discountPrice?: number;
  stockQuantity?: number;
  sku?: string;
  categoryId: number;
  imageUrl?: string;
  isFeatured?: boolean;
}

export async function fetchAdminProducts(): Promise<AdminProduct[]> {
  const data = await requestData<AdminProduct[]>('/api/v1/admin/products', {}, 'Failed to load products');
  return Array.isArray(data) ? data : [];
}

export async function createAdminProduct(input: ProductFormInput): Promise<AdminProduct> {
  return requestData<AdminProduct>(
    '/api/v1/admin/products',
    { method: 'POST', body: input },
    'Failed to create product'
  );
}

export async function updateAdminProduct(id: number, input: ProductFormInput): Promise<AdminProduct> {
  return requestData<AdminProduct>(
    `/api/v1/admin/products/${id}`,
    { method: 'PUT', body: input },
    'Failed to update product'
  );
}

export async function deleteAdminProduct(id: number): Promise<void> {
  await request(`/api/v1/admin/products/${id}`, { method: 'DELETE' }, 'Failed to delete product');
}

// ── Product Images ──────────────────────────────────────────────────────────

export interface AdminProductImage {
  id: number;
  imageUrl: string;
  altText?: string;
  displayOrder: number;
  isPrimary: boolean;
}

export async function fetchProductImages(productId: number): Promise<AdminProductImage[]> {
  const data = await requestData<AdminProductImage[]>(
    `/api/v1/admin/products/${productId}/images`,
    {},
    'Failed to load images'
  );
  return Array.isArray(data) ? data : [];
}

export async function uploadProductImage(productId: number, file: File): Promise<AdminProductImage> {
  const form = new FormData();
  form.append('file', file);
  return requestData<AdminProductImage>(
    `/api/v1/admin/products/${productId}/images`,
    { method: 'POST', body: form },
    status => (status === 400 ? 'Unsupported file type or file too large' : 'Failed to upload image')
  );
}

export async function reorderProductImages(
  productId: number,
  imageIds: number[]
): Promise<AdminProductImage[]> {
  return requestData<AdminProductImage[]>(
    `/api/v1/admin/products/${productId}/images/reorder`,
    { method: 'PATCH', body: { imageIds } },
    'Failed to reorder images'
  );
}

export async function deleteProductImage(productId: number, imageId: number): Promise<void> {
  await request(
    `/api/v1/admin/products/${productId}/images/${imageId}`,
    { method: 'DELETE' },
    'Failed to delete image'
  );
}

// ── Audit Log ────────────────────────────────────────────────────────────────

export interface AuditLogEntry {
  id: number;
  action: string;
  entityType: string;
  entityId?: string;
  userId?: number;
  username?: string;
  ipAddress?: string;
  timestamp: string;
  details?: string;
}

export interface AuditLogPage {
  content: AuditLogEntry[];
  totalElements: number;
  totalPages: number;
  number: number;
}

interface RawAuditLogPage {
  content?: AuditLogEntry[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
}

export async function fetchAuditLogs(
  page = 0,
  size = 20
): Promise<AuditLogPage> {
  // AuditLogController returns Page<AuditLog> directly, not wrapped in ApiResponse
  const body = await request<RawAuditLogPage>(
    `/api/admin/audit?page=${page}&size=${size}`,
    {},
    'Failed to load audit logs'
  );
  return {
    content: body.content ?? [],
    totalElements: body.totalElements ?? 0,
    totalPages: body.totalPages ?? 0,
    number: body.number ?? 0,
  };
}
