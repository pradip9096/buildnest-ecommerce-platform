import type { ApiResponse } from '../types';

function authHeaders(token: string): HeadersInit {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

// ── Dashboard stats ──────────────────────────────────────────────────────────

export interface DashboardStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
}

export async function fetchDashboardStats(token: string): Promise<DashboardStats> {
  const res = await fetch('/api/admin/reports/dashboard', { headers: authHeaders(token) });
  const body: ApiResponse<DashboardStats> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to load dashboard stats');
  return body.data;
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
  token: string,
  params: { status?: string; page?: number; size?: number } = {}
): Promise<{ content: AdminOrder[]; totalElements: number; totalPages: number }> {
  const q = new URLSearchParams();
  if (params.status) q.set('status', params.status);
  q.set('page', String(params.page ?? 0));
  q.set('size', String(params.size ?? 20));
  const res = await fetch(`/api/v1/admin/orders?${q}`, { headers: authHeaders(token) });
  const body: ApiResponse<{ content: AdminOrder[]; totalElements: number; totalPages: number }> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to load orders');
  return body.data ?? { content: [], totalElements: 0, totalPages: 0 };
}

export async function updateOrderStatus(
  token: string,
  orderId: number,
  status: string
): Promise<void> {
  const res = await fetch(`/api/v1/admin/orders/${orderId}/status`, {
    method: 'PATCH',
    headers: authHeaders(token),
    body: JSON.stringify({ status }),
  });
  if (!res.ok) {
    const body: ApiResponse<null> = await res.json().catch(() => ({ success: false, message: 'Failed', data: null }));
    throw new Error(body.message ?? 'Failed to update status');
  }
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

export async function fetchAdminInventory(token: string): Promise<InventoryItem[]> {
  const res = await fetch('/api/v1/admin/inventory', { headers: authHeaders(token) });
  const body: ApiResponse<InventoryItem[]> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to load inventory');
  return Array.isArray(body.data) ? body.data : [];
}

export async function adjustInventory(
  token: string,
  productId: number,
  delta: number,
  reason: string
): Promise<void> {
  const res = await fetch(`/api/v1/admin/inventory/${productId}`, {
    method: 'PATCH',
    headers: authHeaders(token),
    body: JSON.stringify({ delta, reason }),
  });
  if (!res.ok) {
    const body: ApiResponse<null> = await res.json().catch(() => ({ success: false, message: 'Failed', data: null }));
    throw new Error(body.message ?? 'Failed to adjust inventory');
  }
}

// ── Users ────────────────────────────────────────────────────────────────────

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles?: string[];
  enabled?: boolean;
}

export async function fetchAdminUsers(token: string): Promise<AdminUser[]> {
  const res = await fetch('/api/admin/users', { headers: authHeaders(token) });
  const body: ApiResponse<AdminUser[]> = await res.json();
  if (!res.ok) throw new Error(body.message ?? 'Failed to load users');
  return Array.isArray(body.data) ? body.data : [];
}

export async function deleteAdminUser(token: string, userId: number): Promise<void> {
  const res = await fetch(`/api/admin/users/${userId}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error('Failed to disable user');
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

export async function fetchAuditLogs(
  token: string,
  page = 0,
  size = 20
): Promise<AuditLogPage> {
  const res = await fetch(`/api/admin/audit?page=${page}&size=${size}`, {
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error('Failed to load audit logs');
  // AuditLogController returns Page<AuditLog> directly, not wrapped in ApiResponse
  const body = await res.json();
  return {
    content: body.content ?? [],
    totalElements: body.totalElements ?? 0,
    totalPages: body.totalPages ?? 0,
    number: body.number ?? 0,
  };
}
