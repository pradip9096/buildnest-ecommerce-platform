import type { CheckoutSession } from '../../types';

interface Props {
  session: CheckoutSession | null;
  totalAmount: number;
  loading: boolean;
  error: string | null;
  onPay: () => void;
  onBack: () => void;
}

export function PaymentStep({ session, totalAmount, loading, error, onPay, onBack }: Props) {
  const shippingCost = Number(session?.shippingCost ?? 0);
  const discountAmount = Number(session?.discountAmount ?? 0);
  const grandTotal = totalAmount + shippingCost - discountAmount;

  return (
    <div>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Payment</h2>

      <div className="border border-gray-200 rounded-xl p-4 mb-5 space-y-2 text-sm">
        <div className="flex justify-between text-gray-600">
          <span>Subtotal</span>
          <span>₹{totalAmount.toFixed(2)}</span>
        </div>
        <div className="flex justify-between text-gray-600">
          <span>Shipping</span>
          <span>₹{shippingCost.toFixed(2)}</span>
        </div>
        {discountAmount > 0 && (
          <div className="flex justify-between text-green-700">
            <span>Discount {session?.couponCode ? `(${session.couponCode})` : ''}</span>
            <span>-₹{discountAmount.toFixed(2)}</span>
          </div>
        )}
        <div className="flex justify-between font-semibold text-gray-900 pt-2 border-t border-gray-100">
          <span>Total</span>
          <span>₹{grandTotal.toFixed(2)}</span>
        </div>
      </div>

      {session?.razorpayOrderId && (
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-4 mb-5 text-sm">
          <p className="text-gray-500 mb-1">Razorpay Order ID</p>
          <p className="font-mono text-gray-800 break-all">{session.razorpayOrderId}</p>
        </div>
      )}

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-5 text-sm text-blue-800">
        Secure payment powered by Razorpay. You will be redirected to complete the payment.
      </div>

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      <div className="flex gap-3">
        <button
          type="button"
          onClick={onBack}
          disabled={loading}
          className="flex-1 border border-gray-300 text-gray-700 font-semibold py-3 rounded-xl hover:bg-gray-50 transition-colors disabled:opacity-60"
        >
          Back
        </button>
        <button
          type="button"
          onClick={onPay}
          disabled={loading}
          className="flex-1 bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors"
        >
          {loading ? 'Processing…' : `Pay ₹${grandTotal.toFixed(2)}`}
        </button>
      </div>
    </div>
  );
}
