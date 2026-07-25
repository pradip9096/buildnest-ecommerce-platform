import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import { fetchOrders, fetchOrderById } from '../../api/orders';
import { OrderDetailModal } from './OrderDetailModal';
import type { Order } from '../../types';

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PAID: 'bg-blue-100 text-blue-700',
  SHIPPED: 'bg-indigo-100 text-indigo-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
  PAYMENT_FAILED: 'bg-red-100 text-red-700',
};

interface Props { userId: number; }

/** A group of one or more orders. A single-order group is just one order
 * with no OrderGroup (the common case); a group with 2+ orders is a
 * multi-seller checkout split into N per-seller shipments (FR-SEL-06). */
type OrderRow = { orderGroupId: number; orders: Order[] } | { orderGroupId: null; orders: [Order] };

function groupOrders(orders: Order[]): OrderRow[] {
  const rows: OrderRow[] = [];
  const groupIndex = new Map<number, number>();
  for (const order of orders) {
    if (order.orderGroupId == null) {
      rows.push({ orderGroupId: null, orders: [order] });
      continue;
    }
    const existingIndex = groupIndex.get(order.orderGroupId);
    if (existingIndex === undefined) {
      groupIndex.set(order.orderGroupId, rows.length);
      rows.push({ orderGroupId: order.orderGroupId, orders: [order] });
    } else {
      (rows[existingIndex].orders as Order[]).push(order);
    }
  }
  return rows;
}

export function OrdersTab({ userId }: Props) {
  const { data, loading, error, reload } = useAsync<Order[]>(
    () => fetchOrders(),
    [userId]
  );
  const orders = data ?? [];
  const rows = groupOrders(orders);
  const [selected, setSelected] = useState<Order | null>(null);

  const openDetail = async (order: Order) => {
    if (order.orderItems) { setSelected(order); return; }
    try {
      const detail = await fetchOrderById(order.id);
      setSelected(detail);
    } catch {
      setSelected(order);
    }
  };

  if (loading) return (
    <div className="space-y-3 animate-pulse">
      {[1,2,3].map(i => <div key={i} className="h-16 bg-gray-100 rounded-xl" />)}
    </div>
  );

  if (error) return (
    <div className="text-center py-12">
      <p className="text-red-600 text-sm mb-3">{error}</p>
      <button
        type="button"
        onClick={reload}
        className="bg-primary-500 hover:bg-primary-600 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-colors"
      >
        Retry
      </button>
    </div>
  );

  return (
    <>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Order History</h2>

      {orders.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-4xl mb-3">📦</div>
          <p className="text-gray-500">You haven&apos;t placed any orders yet.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {rows.map(row => (
            <div key={row.orderGroupId ?? `single-${row.orders[0].id}`}>
              {row.orderGroupId != null && (
                <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1.5 px-1">
                  1 purchase, {row.orders.length} shipments
                </p>
              )}
              <div className="space-y-2">
                {row.orders.map(order => (
                  <button
                    type="button"
                    key={order.id}
                    onClick={() => openDetail(order)}
                    className="w-full text-left border border-gray-100 rounded-xl px-4 py-3 hover:border-primary-300 hover:bg-primary-50/50 transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium text-gray-900 text-sm">Order #{order.id}</p>
                        <p className="text-xs text-gray-400 mt-0.5">
                          {new Date(order.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
                        </p>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${STATUS_COLOR[order.status] ?? 'bg-gray-100 text-gray-600'}`}>
                          {order.status.replace(/_/g, ' ')}
                        </span>
                        <span className="font-semibold text-gray-900 text-sm">₹{Number(order.totalAmount).toFixed(2)}</span>
                        <span className="text-gray-300">›</span>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {selected && <OrderDetailModal order={selected} onClose={() => setSelected(null)} />}
    </>
  );
}
