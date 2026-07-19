import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PaymentStep } from './PaymentStep';
import type { CheckoutSession } from '../../types';

const baseSession: CheckoutSession = {
  userId: 1,
  cartId: 1,
  step: 'PENDING_PAYMENT',
  shippingCost: 50,
};

describe('PaymentStep discount display', () => {
  it('shows no discount line and the plain grand total when no coupon was applied', () => {
    render(
      <PaymentStep
        session={baseSession}
        totalAmount={200}
        loading={false}
        error={null}
        onPay={vi.fn()}
        onBack={vi.fn()}
      />
    );

    expect(screen.queryByText(/Discount/)).not.toBeInTheDocument();
    expect(screen.getByText('Pay ₹250.00')).toBeInTheDocument();
  });

  it('subtracts the applied discount from the grand total and labels it with the coupon code', () => {
    render(
      <PaymentStep
        session={{ ...baseSession, couponCode: 'SAVE10', discountAmount: 20 }}
        totalAmount={200}
        loading={false}
        error={null}
        onPay={vi.fn()}
        onBack={vi.fn()}
      />
    );

    expect(screen.getByText('Discount (SAVE10)')).toBeInTheDocument();
    expect(screen.getByText('-₹20.00')).toBeInTheDocument();
    expect(screen.getByText('Pay ₹230.00')).toBeInTheDocument();
  });
});
