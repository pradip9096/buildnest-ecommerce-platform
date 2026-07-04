import { useState, useEffect, useCallback } from 'react';
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

interface Props { token: string; userId: number; }

export function OrdersTab({ token, userId }: Props) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Order | null>(null);

  const loadOrders = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchOrders(token)
      .then(setOrders)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load orders'))
      .finally(() => setLoading(false));
  }, [token]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders, userId]);

  const openDetail = async (order: Order) => {
    if (order.orderItems) { setSelected(order); return; }
    try {
      const detail = await fetchOrderById(order.id, token);
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
        onClick={loadOrders}
        className="bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-colors"
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
        <div className="space-y-2">
          {orders.map(order => (
            <button
              key={order.id}
              onClick={() => openDetail(order)}
              className="w-full text-left border border-gray-100 rounded-xl px-4 py-3 hover:border-amber-300 hover:bg-amber-50/50 transition-colors"
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
      )}

      {selected && <OrderDetailModal order={selected} onClose={() => setSelected(null)} />}
    </>
  );
}
