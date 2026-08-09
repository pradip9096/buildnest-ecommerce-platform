import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { CartItemRow } from '../components/cart/CartItemRow';

export function CartPage() {
  const { user, isAuthenticated } = useAuth();
  const { cart, loading, error, addItem, removeItem, reload } = useCart(
    user?.id ?? null
  );
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [coupon, setCoupon] = useState('');
  const [couponMsg, setCouponMsg] = useState<string | null>(null);
  const navigate = useNavigate();

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🛒</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Your cart is waiting</h1>
          <p className="text-gray-600 mb-6">Sign in to view your cart and continue shopping.</p>
          <Link
            to="/login"
            className="inline-block bg-primary-500 hover:bg-primary-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors"
          >
            Sign in
          </Link>
        </div>
      </div>
    );
  }

  const handleRemove = async (cartItemId: number) => {
    setRemovingId(cartItemId);
    try {
      await removeItem(cartItemId);
    } finally {
      setRemovingId(null);
    }
  };

  const handleAdd = async (productId: number) => {
    await addItem(productId, 1);
  };

  const handleApplyCoupon = (e: React.FormEvent) => {
    e.preventDefault();
    if (!coupon.trim()) return;
    setCouponMsg('Coupon codes are not yet supported. Check back soon!');
  };

  const isEmpty = !cart || cart.items.length === 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-5xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">
          Shopping Cart
          {cart && cart.items.length > 0 && (
            <span className="ml-2 text-base font-normal text-gray-600">
              ({cart.items.length} {cart.items.length === 1 ? 'item' : 'items'})
            </span>
          )}
        </h1>

        {loading && (
          <div className="space-y-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-20 bg-white rounded-xl animate-pulse border border-gray-100" />
            ))}
          </div>
        )}

        {error && !loading && (
          <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-red-700 text-sm flex items-center gap-2">
            <span>⚠️</span>
            <span>{error}</span>
            <button type="button" onClick={reload} className="ml-auto underline text-red-600 hover:text-red-800">Retry</button>
          </div>
        )}

        {!loading && !error && isEmpty && (
          <div className="text-center py-16">
            <div className="text-6xl mb-4">🛒</div>
            <h2 className="text-xl font-semibold text-gray-700 mb-2">Your cart is empty</h2>
            <p className="text-gray-600 mb-6">Discover our range of home construction and décor products.</p>
            <Link
              to="/products"
              className="inline-block bg-primary-500 hover:bg-primary-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors"
            >
              Browse Products
            </Link>
          </div>
        )}

        {!loading && !error && !isEmpty && cart && (
          <div className="flex flex-col lg:flex-row gap-6">
            <div className="flex-1">
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm px-6">
                {cart.items.map(item => (
                  <CartItemRow
                    key={item.cartItemId}
                    item={item}
                    onRemove={handleRemove}
                    onAdd={handleAdd}
                    removing={removingId === item.cartItemId}
                  />
                ))}
              </div>

              <form onSubmit={handleApplyCoupon} className="mt-4 flex gap-2">
                <input
                  type="text"
                  value={coupon}
                  onChange={e => { setCoupon(e.target.value); setCouponMsg(null); }}
                  placeholder="Coupon code"
                  className="flex-1 border border-gray-300 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                />
                <button
                  type="submit"
                  className="bg-gray-800 hover:bg-gray-900 text-white font-medium px-4 py-2.5 rounded-xl text-sm transition-colors"
                >
                  Apply
                </button>
              </form>
              {couponMsg && (
                <p className="mt-2 text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
                  {couponMsg}
                </p>
              )}
            </div>

            <div className="lg:w-80">
              <div data-testid="order-summary" className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 sticky top-6">
                <h2 className="font-semibold text-gray-900 mb-4">Order Summary</h2>
                <div className="space-y-2 text-sm">
                  {cart.items.map(item => (
                    <div key={item.cartItemId} className="flex justify-between text-gray-600">
                      <span className="truncate mr-2">{item.productName} × {item.quantity}</span>
                      <span className="flex-shrink-0">₹{item.itemTotal.toFixed(2)}</span>
                    </div>
                  ))}
                  <div className="border-t border-gray-100 pt-2 flex justify-between font-semibold text-gray-900">
                    <span>Subtotal</span>
                    <span>₹{cart.totalAmount.toFixed(2)}</span>
                  </div>
                  <p className="text-gray-600 text-xs">Shipping calculated at checkout</p>
                </div>

                <button
                  type="button"
                  onClick={() => navigate('/checkout')}
                  data-testid="checkout-button"
                  className="mt-5 w-full bg-primary-500 hover:bg-primary-600 text-white font-semibold py-3 rounded-xl transition-colors"
                >
                  Proceed to Checkout
                </button>
                <Link
                  to="/products"
                  className="mt-3 block text-center text-sm text-gray-600 hover:text-gray-700"
                >
                  ← Continue Shopping
                </Link>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
