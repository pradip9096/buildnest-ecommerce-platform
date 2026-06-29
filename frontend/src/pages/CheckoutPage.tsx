import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { CheckoutStepper } from '../components/checkout/CheckoutStepper';
import { AddressStep } from '../components/checkout/AddressStep';
import { ShippingStep } from '../components/checkout/ShippingStep';
import { PaymentStep } from '../components/checkout/PaymentStep';
import {
  fetchShippingOptions,
  setCheckoutAddress,
  selectCheckoutShipping,
  initiateCheckoutPayment,
  confirmCheckout,
} from '../api/checkout';
import type { CheckoutSession, ShippingOption } from '../types';

const PLACEHOLDER_ADDRESS_ID = 1;

export function CheckoutPage() {
  const { user, token, isAuthenticated } = useAuth();
  const { cart } = useCart(user?.id ?? null, token);
  const navigate = useNavigate();

  const [step, setStep] = useState(0);
  const [session, setSession] = useState<CheckoutSession | null>(null);
  const [shippingOptions, setShippingOptions] = useState<ShippingOption[]>([]);
  const [shippingLoading, setShippingLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token || step !== 1) return;
    setShippingLoading(true);
    fetchShippingOptions(token)
      .then(setShippingOptions)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load shipping options'))
      .finally(() => setShippingLoading(false));
  }, [token, step]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🔒</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Sign in to checkout</h1>
          <p className="text-gray-500 mb-6">You need to be signed in to complete your purchase.</p>
          <Link
            to="/login"
            className="inline-block bg-amber-500 hover:bg-amber-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors"
          >
            Sign in
          </Link>
        </div>
      </div>
    );
  }

  const handleAddressNext = async (postalCode: string) => {
    if (!token) return;
    setActionLoading(true);
    setError(null);
    try {
      const sess = await setCheckoutAddress(PLACEHOLDER_ADDRESS_ID, token);
      setSession(sess);
      setStep(1);
      await fetchShippingOptions(token, postalCode).then(setShippingOptions).catch(() => {});
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save address');
    } finally {
      setActionLoading(false);
    }
  };

  const handleShippingNext = async (shippingMethodId: number) => {
    if (!token) return;
    setActionLoading(true);
    setError(null);
    try {
      const sess = await selectCheckoutShipping(shippingMethodId, token);
      setSession(sess);
      const paymentSess = await initiateCheckoutPayment(token);
      setSession(paymentSess);
      setStep(2);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to select shipping');
    } finally {
      setActionLoading(false);
    }
  };

  const handlePay = async () => {
    if (!token) return;
    setActionLoading(true);
    setError(null);
    try {
      const order = await confirmCheckout(token);
      navigate(`/orders/${order.id}`, { state: { order } });
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to confirm order');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-200 px-4 py-4">
        <div className="max-w-3xl mx-auto flex items-center gap-3">
          <Link to="/" className="text-2xl font-bold text-amber-600">🏗️ BuildNest</Link>
          <span className="text-gray-300">/</span>
          <Link to="/cart" className="text-gray-500 hover:text-gray-700">Cart</Link>
          <span className="text-gray-300">/</span>
          <span className="text-gray-600 font-medium">Checkout</span>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-4 py-8">
        <CheckoutStepper currentStep={step} />

        <div className="flex flex-col lg:flex-row gap-6">
          <div className="flex-1 bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            {step === 0 && (
              <AddressStep
                onNext={handleAddressNext}
                loading={actionLoading}
                error={error}
              />
            )}
            {step === 1 && (
              <ShippingStep
                options={shippingOptions}
                loading={shippingLoading}
                error={error}
                onNext={handleShippingNext}
                onBack={() => { setStep(0); setError(null); }}
              />
            )}
            {step === 2 && (
              <PaymentStep
                session={session}
                totalAmount={cart?.totalAmount ?? 0}
                loading={actionLoading}
                error={error}
                onPay={handlePay}
                onBack={() => { setStep(1); setError(null); }}
              />
            )}
          </div>

          {cart && (
            <div className="lg:w-72">
              <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 sticky top-6">
                <h2 className="font-semibold text-gray-900 mb-3 text-sm">Order Summary</h2>
                <div className="space-y-1.5 text-sm">
                  {cart.items.map(item => (
                    <div key={item.cartItemId} className="flex justify-between text-gray-600">
                      <span className="truncate mr-2">{item.productName} × {item.quantity}</span>
                      <span className="flex-shrink-0">₹{item.itemTotal.toFixed(2)}</span>
                    </div>
                  ))}
                  {session?.shippingCost != null && (
                    <div className="flex justify-between text-gray-600">
                      <span>Shipping</span>
                      <span>₹{Number(session.shippingCost).toFixed(2)}</span>
                    </div>
                  )}
                  <div className="border-t border-gray-100 pt-2 flex justify-between font-semibold text-gray-900">
                    <span>Total</span>
                    <span>
                      ₹{(cart.totalAmount + Number(session?.shippingCost ?? 0)).toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
