import type { Order } from '../../types';

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PAID: 'bg-blue-100 text-blue-800',
  SHIPPED: 'bg-indigo-100 text-indigo-800',
  DELIVERED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
  PAYMENT_FAILED: 'bg-red-100 text-red-800',
};

interface Props {
  order: Order;
  onClose: () => void;
}

export function OrderDetailModal({ order, onClose }: Props) {
  const statusColor = STATUS_COLOR[order.status] ?? 'bg-gray-100 text-gray-700';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm" onClick={onClose}>
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-md max-h-[85vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-6 pt-5 pb-3 border-b border-gray-100">
          <div>
            <p className="text-xs text-gray-400 uppercase tracking-wide">Order</p>
            <p className="text-xl font-bold text-gray-900">#{order.id}</p>
          </div>
          <div className="flex items-center gap-3">
            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${statusColor}`}>
              {order.status.replace(/_/g, ' ')}
            </span>
            <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-lg leading-none">✕</button>
          </div>
        </div>

        <div className="px-6 py-4 text-sm text-gray-500 border-b border-gray-100">
          Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}
        </div>

        {order.orderItems && order.orderItems.length > 0 ? (
          <div className="px-6 py-4 space-y-3">
            {order.orderItems.map((item, idx) => (
              <div key={idx} className="flex justify-between text-sm">
                <span className="text-gray-700">{item.productName} <span className="text-gray-400">× {item.quantity}</span></span>
                <span className="font-medium text-gray-900">₹{Number(item.itemTotal).toFixed(2)}</span>
              </div>
            ))}
            <div className="border-t border-gray-100 pt-3 flex justify-between font-semibold text-gray-900">
              <span>Total</span>
              <span>₹{Number(order.totalAmount).toFixed(2)}</span>
            </div>
          </div>
        ) : (
          <div className="px-6 py-4 flex justify-between font-semibold text-gray-900 text-sm">
            <span>Total</span>
            <span>₹{Number(order.totalAmount).toFixed(2)}</span>
          </div>
        )}

        <div className="px-6 pb-5">
          <button type="button" onClick={onClose}
            className="w-full border border-gray-200 text-gray-600 font-medium py-2.5 rounded-xl hover:bg-gray-50 transition-colors text-sm">
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
