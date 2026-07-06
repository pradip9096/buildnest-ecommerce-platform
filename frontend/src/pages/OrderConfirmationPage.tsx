import { useEffect, useState } from 'react';
import { Link, useParams, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { fetchOrderById } from '../api/orders';
import type { Order } from '../types';

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  PAID: 'Paid',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
  PAYMENT_FAILED: 'Payment Failed',
};

export function OrderConfirmationPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const { isAuthenticated } = useAuth();

  const [order, setOrder] = useState<Order | null>(
    (location.state as { order?: Order } | null)?.order ?? null
  );
  const [loading, setLoading] = useState(!order);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (order || !id) return;
    setLoading(true);
    fetchOrderById(Number(id))
      .then(setOrder)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load order'))
      .finally(() => setLoading(false));
  }, [id, order]);

  useEffect(() => {
    document.title = order ? `Order #${order.id} — BuildNest` : 'Order Confirmation — BuildNest';
  }, [order]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🔒</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Sign in to view your order</h1>
          <Link to="/login" className="inline-block mt-4 bg-primary-500 hover:bg-primary-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors">
            Sign in
          </Link>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="space-y-4 w-full max-w-md px-4">
          <div className="h-8 bg-gray-200 rounded animate-pulse w-2/3 mx-auto" />
          <div className="h-40 bg-white rounded-2xl animate-pulse border border-gray-100" />
        </div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">⚠️</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Order not found</h1>
          <p className="text-gray-500 mb-6">{error ?? 'We could not find this order.'}</p>
          <Link to="/" className="inline-block bg-primary-500 hover:bg-primary-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors">
            Back to Home
          </Link>
        </div>
      </div>
    );
  }

  const statusLabel = STATUS_LABEL[order.status] ?? order.status;

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-2xl mx-auto px-4 py-10">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center text-3xl mx-auto mb-4">
            ✅
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Order Placed Successfully!</h1>
          <p className="text-gray-500 mt-1">Thank you for shopping with BuildNest.</p>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-4">
          <div className="flex items-center justify-between mb-4">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Order Number</p>
              <p className="text-xl font-bold text-gray-900">#{order.id}</p>
            </div>
            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
              order.status === 'CANCELLED' || order.status === 'PAYMENT_FAILED'
                ? 'bg-red-100 text-red-700'
                : order.status === 'DELIVERED'
                ? 'bg-green-100 text-green-700'
                : 'bg-amber-100 text-amber-700'
            }`}>
              {statusLabel}
            </span>
          </div>

          <div className="border-t border-gray-100 pt-4 text-sm text-gray-500">
            <p>Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}</p>
          </div>
        </div>

        {order.orderItems && order.orderItems.length > 0 && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-4">
            <h2 className="font-semibold text-gray-900 mb-3">Items Ordered</h2>
            <div className="space-y-3">
              {order.orderItems.map((item, idx) => (
                <div key={idx} className="flex justify-between text-sm">
                  <span className="text-gray-700">{item.productName} × {item.quantity}</span>
                  <span className="font-medium text-gray-900">₹{Number(item.itemTotal).toFixed(2)}</span>
                </div>
              ))}
              <div className="border-t border-gray-100 pt-3 flex justify-between font-semibold text-gray-900">
                <span>Total</span>
                <span>₹{Number(order.totalAmount).toFixed(2)}</span>
              </div>
            </div>
          </div>
        )}

        <div className="flex flex-col sm:flex-row gap-3 mt-6">
          <Link
            to="/products"
            className="flex-1 text-center bg-primary-500 hover:bg-primary-600 text-white font-semibold py-3 rounded-xl transition-colors"
          >
            Continue Shopping
          </Link>
          <Link
            to="/"
            className="flex-1 text-center border border-gray-300 text-gray-700 font-semibold py-3 rounded-xl hover:bg-gray-50 transition-colors"
          >
            View All Orders
          </Link>
        </div>
      </main>
    </div>
  );
}
