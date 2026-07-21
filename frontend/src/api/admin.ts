import { request, requestData } from './client';
import type { ApiResponse } from '../types';

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

// ── Sales analytics ──────────────────────────────────────────────────────────

export interface SalesTopProduct {
  productId: number;
  productName: string;
  unitsSold: number;
  revenue: number;
}

export interface SalesRevenueTrendPoint {
  date: string;
  revenue: number;
  orderCount: number;
}

export interface SalesDashboard {
  dailyRevenue: number;
  weeklyRevenue: number;
  monthlyRevenue: number;
  yearlyRevenue: number;
  dailyOrders: number;
  weeklyOrders: number;
  monthlyOrders: number;
  totalOrders: number;
  averageOrderValue: number;
  totalCustomers: number;
  newCustomersThisMonth: number;
  customerRetentionRate: number;
  topSellingProducts: SalesTopProduct[];
  revenueByCategory: Record<string, number>;
  cartAbandonmentRate: number;
  conversionRate: number;
  revenueTrend: SalesRevenueTrendPoint[];
  startDate: string;
  endDate: string;
}

export async function fetchSalesDashboard(
  params: { startDate?: string; endDate?: string } = {}
): Promise<SalesDashboard> {
  const q = new URLSearchParams();
  if (params.startDate) q.set('startDate', params.startDate);
  if (params.endDate) q.set('endDate', params.endDate);
  const qs = q.toString();
  return requestData<SalesDashboard>(
    `/api/v1/admin/analytics/sales/dashboard${qs ? `?${qs}` : ''}`,
    {},
    'Failed to load sales dashboard'
  );
}

export async function fetchCustomerLifetimeValue(userId: number): Promise<number> {
  return requestData<number>(
    `/api/v1/admin/analytics/sales/customer-lifetime-value/${userId}`,
    {},
    'Failed to load customer lifetime value'
  );
}

// ── Inventory analytics ──────────────────────────────────────────────────────

export interface InventoryDemandProduct {
  productId: number;
  productName: string;
  currentStock: number;
  minimumThreshold: number;
  shortfall: number;
  demandScore: number;
  riskLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  recommendedAction: string;
}

export interface InventorySeasonalPattern {
  productId: number;
  productName: string;
  totalBreaches: number;
  breachFrequency: string;
  pattern: 'VERY_HIGH_DEMAND' | 'HIGH_DEMAND' | 'MODERATE_DEMAND' | 'LOW_DEMAND';
  currentStock: number;
  suggestedSafetyStock: number;
}

export interface InventoryTurnoverProduct {
  productId: number;
  productName: string;
  currentStock: number;
  recentTransactions: number;
  turnoverCategory: 'VERY_HIGH_TURNOVER' | 'HIGH_TURNOVER' | 'MODERATE_TURNOVER' | 'LOW_TURNOVER' | 'STAGNANT';
  healthStatus: 'HEALTHY' | 'OVERSTOCKED' | 'UNDERSTOCKED' | 'CRITICAL';
}

export interface InventoryRestockingPlan {
  generatedAt: string;
  analysisPeriod: string;
  urgentRestocks: InventoryDemandProduct[];
  urgentCount: number;
  seasonalPatterns: InventorySeasonalPattern[];
  patternCount: number;
  stockAnalysis: InventoryTurnoverProduct[];
}

function dateRangeQuery(params: { startDate?: string; endDate?: string }): string {
  const q = new URLSearchParams();
  if (params.startDate) q.set('startDate', params.startDate);
  if (params.endDate) q.set('endDate', params.endDate);
  const qs = q.toString();
  return qs ? `?${qs}` : '';
}

export async function fetchInventoryHighDemandLowStock(
  params: { startDate: string; endDate: string }
): Promise<InventoryDemandProduct[]> {
  return requestData<InventoryDemandProduct[]>(
    `/api/admin/inventory-analytics/high-demand-low-inventory${dateRangeQuery(params)}`,
    {},
    'Failed to load high-demand low-inventory products'
  );
}

export async function fetchInventorySeasonalPatterns(
  params: { startDate: string; endDate: string }
): Promise<InventorySeasonalPattern[]> {
  return requestData<InventorySeasonalPattern[]>(
    `/api/admin/inventory-analytics/seasonal-patterns${dateRangeQuery(params)}`,
    {},
    'Failed to load seasonal demand patterns'
  );
}

export async function fetchInventoryStockTurnover(
  params: { startDate: string; endDate: string }
): Promise<InventoryTurnoverProduct[]> {
  return requestData<InventoryTurnoverProduct[]>(
    `/api/admin/inventory-analytics/stock-turnover${dateRangeQuery(params)}`,
    {},
    'Failed to load stock turnover analysis'
  );
}

export async function fetchInventoryRestockingPlan(
  daysPeriod: number
): Promise<InventoryRestockingPlan> {
  return requestData<InventoryRestockingPlan>(
    `/api/admin/inventory-analytics/restocking-plan?daysPeriod=${daysPeriod}`,
    {},
    'Failed to load predictive restocking plan'
  );
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

// ── Shipping Methods ────────────────────────────────────────────────────────

export interface AdminShippingMethod {
  id: number;
  name: string;
  description?: string;
  baseCost: number;
  costPerKg: number;
  estimatedDaysMin: number;
  estimatedDaysMax: number;
  isActive: boolean;
}

export interface ShippingMethodFormInput {
  name: string;
  description?: string;
  baseCost: number;
  costPerKg: number;
  estimatedDaysMin: number;
  estimatedDaysMax: number;
}

export async function fetchAdminShippingMethods(): Promise<AdminShippingMethod[]> {
  const data = await requestData<AdminShippingMethod[]>(
    '/api/v1/admin/shipping-methods',
    {},
    'Failed to load shipping methods'
  );
  return Array.isArray(data) ? data : [];
}

export async function createAdminShippingMethod(
  input: ShippingMethodFormInput
): Promise<AdminShippingMethod> {
  return requestData<AdminShippingMethod>(
    '/api/v1/admin/shipping-methods',
    { method: 'POST', body: input },
    'Failed to create shipping method'
  );
}

export async function updateAdminShippingMethod(
  id: number,
  input: ShippingMethodFormInput
): Promise<AdminShippingMethod> {
  return requestData<AdminShippingMethod>(
    `/api/v1/admin/shipping-methods/${id}`,
    { method: 'PUT', body: input },
    'Failed to update shipping method'
  );
}

export async function deactivateAdminShippingMethod(id: number): Promise<void> {
  await request(
    `/api/v1/admin/shipping-methods/${id}`,
    { method: 'DELETE' },
    'Failed to deactivate shipping method'
  );
}

// ── Webhook Subscriptions ───────────────────────────────────────────────────

export interface AdminWebhookSubscription {
  id: number;
  eventType: string;
  targetUrl: string;
  active: boolean;
  failureCount: number;
  lastDeliveryStatus?: string;
  createdAt: string;
}

export interface WebhookSubscriptionFormInput {
  eventType: string;
  targetUrl: string;
  secret?: string;
}

export async function fetchAdminWebhookSubscriptions(): Promise<AdminWebhookSubscription[]> {
  const data = await requestData<AdminWebhookSubscription[]>(
    '/api/admin/webhooks',
    {},
    'Failed to load webhook subscriptions'
  );
  return Array.isArray(data) ? data : [];
}

export async function createAdminWebhookSubscription(
  input: WebhookSubscriptionFormInput
): Promise<AdminWebhookSubscription> {
  return requestData<AdminWebhookSubscription>(
    '/api/admin/webhooks',
    { method: 'POST', body: input },
    'Failed to create webhook subscription'
  );
}

export async function deactivateAdminWebhookSubscription(id: number): Promise<AdminWebhookSubscription> {
  return requestData<AdminWebhookSubscription>(
    `/api/admin/webhooks/${id}/deactivate`,
    { method: 'PUT' },
    'Failed to deactivate webhook subscription'
  );
}

export async function deleteAdminWebhookSubscription(id: number): Promise<void> {
  await request(
    `/api/admin/webhooks/${id}`,
    { method: 'DELETE' },
    'Failed to delete webhook subscription'
  );
}

// ── Tags ─────────────────────────────────────────────────────────────────────

export interface AdminTag {
  id: number;
  name: string;
  slug: string;
}

export interface TagFormInput {
  name: string;
}

export async function fetchAdminTags(): Promise<AdminTag[]> {
  const data = await requestData<AdminTag[]>('/api/v1/admin/tags', {}, 'Failed to load tags');
  return Array.isArray(data) ? data : [];
}

export async function createAdminTag(input: TagFormInput): Promise<AdminTag> {
  return requestData<AdminTag>(
    '/api/v1/admin/tags',
    { method: 'POST', body: input },
    'Failed to create tag'
  );
}

export async function updateAdminTag(id: number, input: TagFormInput): Promise<AdminTag> {
  return requestData<AdminTag>(
    `/api/v1/admin/tags/${id}`,
    { method: 'PUT', body: input },
    'Failed to update tag'
  );
}

export async function deleteAdminTag(id: number): Promise<void> {
  await request(`/api/v1/admin/tags/${id}`, { method: 'DELETE' }, 'Failed to delete tag');
}

// ── Coupons ──────────────────────────────────────────────────────────────────

export type CouponDiscountType = 'PERCENTAGE' | 'FIXED';

export interface AdminCoupon {
  id: number;
  code: string;
  discountType: CouponDiscountType;
  discountValue: number;
  minOrderValue: number;
  usageLimit: number | null;
  usageCount: number;
  expiresAt: string | null;
  isActive: boolean;
}

export interface CouponFormInput {
  code: string;
  discountType: CouponDiscountType;
  discountValue: number;
  minOrderValue?: number;
  usageLimit?: number | null;
  expiresAt?: string | null;
}

export async function fetchAdminCoupons(): Promise<AdminCoupon[]> {
  const data = await requestData<AdminCoupon[]>(
    '/api/v1/admin/coupons',
    {},
    'Failed to load coupons'
  );
  return Array.isArray(data) ? data : [];
}

export async function createAdminCoupon(input: CouponFormInput): Promise<AdminCoupon> {
  return requestData<AdminCoupon>(
    '/api/v1/admin/coupons',
    { method: 'POST', body: input },
    'Failed to create coupon'
  );
}

export async function deactivateAdminCoupon(id: number): Promise<AdminCoupon> {
  return requestData<AdminCoupon>(
    `/api/v1/admin/coupons/${id}`,
    { method: 'DELETE' },
    'Failed to deactivate coupon'
  );
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

export async function triggerSearchReindex(): Promise<string> {
  const body = await request<ApiResponse<null>>(
    '/api/v1/admin/search/reindex',
    { method: 'POST' },
    'Failed to trigger search re-index'
  );
  return body.message;
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

// ── Product Variants ────────────────────────────────────────────────────────

export interface AdminProductVariant {
  id: number;
  sku: string;
  size?: string;
  colour?: string;
  priceAdjustment: number;
  isActive: boolean;
  effectivePrice?: number;
  inventory?: { quantityInStock: number; minimumStockLevel: number } | null;
}

export interface VariantFormInput {
  sku: string;
  size?: string;
  colour?: string;
  priceAdjustment: number;
  isActive: boolean;
  initialStockQuantity?: number;
  minimumStockLevel?: number;
}

export async function fetchProductVariants(productId: number): Promise<AdminProductVariant[]> {
  const data = await requestData<AdminProductVariant[]>(
    `/api/v1/admin/products/${productId}/variants`,
    {},
    'Failed to load variants'
  );
  return Array.isArray(data) ? data : [];
}

export async function createProductVariant(
  productId: number,
  input: VariantFormInput
): Promise<AdminProductVariant> {
  return requestData<AdminProductVariant>(
    `/api/v1/admin/products/${productId}/variants`,
    { method: 'POST', body: input },
    status => (status === 409 ? 'Variant SKU already in use' : 'Failed to create variant')
  );
}

export async function updateProductVariant(
  productId: number,
  variantId: number,
  input: VariantFormInput
): Promise<AdminProductVariant> {
  return requestData<AdminProductVariant>(
    `/api/v1/admin/products/${productId}/variants/${variantId}`,
    { method: 'PUT', body: input },
    status => (status === 409 ? 'Variant SKU already in use' : 'Failed to update variant')
  );
}

export async function deleteProductVariant(productId: number, variantId: number): Promise<void> {
  await request(
    `/api/v1/admin/products/${productId}/variants/${variantId}`,
    { method: 'DELETE' },
    'Failed to delete variant'
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
