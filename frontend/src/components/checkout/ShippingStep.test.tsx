import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ShippingStep } from './ShippingStep';
import type { CheckoutSession, ShippingOption } from '../../types';

const options: ShippingOption[] = [
  {
    id: 1,
    name: 'Standard',
    baseCost: 50,
    calculatedCost: 50,
    estimatedDaysMin: 3,
    estimatedDaysMax: 5,
  },
];

const baseSession: CheckoutSession = {
  userId: 1,
  cartId: 1,
  step: 'PENDING_SHIPPING',
};

function renderStep(overrides: Partial<React.ComponentProps<typeof ShippingStep>> = {}) {
  const onApplyCoupon = vi.fn().mockResolvedValue(undefined);
  const onNext = vi.fn();
  const onBack = vi.fn();
  render(
    <ShippingStep
      options={options}
      loading={false}
      error={null}
      session={baseSession}
      couponLoading={false}
      onApplyCoupon={onApplyCoupon}
      onNext={onNext}
      onBack={onBack}
      {...overrides}
    />
  );
  return { onApplyCoupon, onNext, onBack };
}

describe('ShippingStep coupon application', () => {
  it('submits the entered coupon code to onApplyCoupon', async () => {
    const user = userEvent.setup();
    const { onApplyCoupon } = renderStep();

    await user.type(screen.getByPlaceholderText('Enter coupon code'), 'SAVE10');
    await user.click(screen.getByRole('button', { name: 'Apply' }));

    await waitFor(() => expect(onApplyCoupon).toHaveBeenCalledWith('SAVE10'));
  });

  it('does not call onApplyCoupon for a blank code', async () => {
    const user = userEvent.setup();
    const { onApplyCoupon } = renderStep();

    await user.click(screen.getByRole('button', { name: 'Apply' }));

    expect(onApplyCoupon).not.toHaveBeenCalled();
  });

  it('shows the backend error message when applying a coupon fails', async () => {
    const user = userEvent.setup();
    const onApplyCoupon = vi.fn().mockRejectedValue(new Error('Coupon expired'));
    renderStep({ onApplyCoupon });

    await user.type(screen.getByPlaceholderText('Enter coupon code'), 'EXPIRED');
    await user.click(screen.getByRole('button', { name: 'Apply' }));

    expect(await screen.findByText('Coupon expired')).toBeInTheDocument();
  });

  it('shows the applied coupon and discount instead of the input once session has one', () => {
    renderStep({
      session: { ...baseSession, couponCode: 'SAVE10', discountAmount: 25 },
    });

    expect(screen.getByText('SAVE10')).toBeInTheDocument();
    expect(screen.getByText(/discount ₹25\.00/)).toBeInTheDocument();
    expect(screen.queryByPlaceholderText('Enter coupon code')).not.toBeInTheDocument();
  });
});

describe('ShippingStep default selection (#652)', () => {
  it('auto-selects the first option once it arrives asynchronously after mount', async () => {
    const user = userEvent.setup();
    const onApplyCoupon = vi.fn().mockResolvedValue(undefined);
    const onNext = vi.fn();
    const onBack = vi.fn();

    // Mirrors CheckoutPage's real usage: ShippingStep first mounts with an
    // empty options array (fetch not yet resolved), then the parent
    // re-renders with the fetched options once the request completes.
    const { rerender } = render(
      <ShippingStep
        options={[]}
        loading={false}
        error={null}
        session={baseSession}
        couponLoading={false}
        onApplyCoupon={onApplyCoupon}
        onNext={onNext}
        onBack={onBack}
      />
    );

    rerender(
      <ShippingStep
        options={options}
        loading={false}
        error={null}
        session={baseSession}
        couponLoading={false}
        onApplyCoupon={onApplyCoupon}
        onNext={onNext}
        onBack={onBack}
      />
    );

    await waitFor(() =>
      expect(screen.getByRole('radio')).toBeChecked()
    );

    await user.click(screen.getByRole('button', { name: /continue to payment/i }));

    expect(onNext).toHaveBeenCalledWith(options[0].id);
    expect(screen.queryByText('Please select a shipping method')).not.toBeInTheDocument();
  });
});
