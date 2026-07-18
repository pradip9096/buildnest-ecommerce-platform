import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RefundModal } from './RefundModal';
import { refundOrder, type AdminOrder } from '../../api/admin';

vi.mock('../../api/admin', () => ({
  refundOrder: vi.fn(),
}));

const mockRefundOrder = vi.mocked(refundOrder);

const order: AdminOrder = {
  id: 42,
  userId: 7,
  status: 'DELIVERED',
  totalAmount: 1500,
  createdAt: '2026-07-01T00:00:00Z',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('RefundModal', () => {
  it('processes a refund and calls onSuccess', async () => {
    mockRefundOrder.mockResolvedValue(undefined);
    const onSuccess = vi.fn();

    render(<RefundModal order={order} onClose={vi.fn()} onSuccess={onSuccess} />);

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText('e.g. 500.00'), '500');
    await user.type(screen.getByPlaceholderText('e.g. Customer requested cancellation'), 'Damaged item');
    await user.click(screen.getByRole('button', { name: 'Refund' }));

    expect(mockRefundOrder).toHaveBeenCalledWith(42, 500, 'Damaged item');
    expect(onSuccess).toHaveBeenCalled();
  });

  it('rejects a zero amount without calling the API', async () => {
    render(<RefundModal order={order} onClose={vi.fn()} onSuccess={vi.fn()} />);

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText('e.g. 500.00'), '0');
    await user.type(screen.getByPlaceholderText('e.g. Customer requested cancellation'), 'Reason');
    await user.click(screen.getByRole('button', { name: 'Refund' }));

    expect(screen.getByText('Enter a refund amount greater than zero.')).toBeInTheDocument();
    expect(mockRefundOrder).not.toHaveBeenCalled();
  });

  it('surfaces a backend error (e.g. payment not in SUCCESS status)', async () => {
    mockRefundOrder.mockRejectedValue(
      new Error('Refund is only allowed for payments in SUCCESS status; current status: PENDING')
    );

    render(<RefundModal order={order} onClose={vi.fn()} onSuccess={vi.fn()} />);

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText('e.g. 500.00'), '500');
    await user.type(screen.getByPlaceholderText('e.g. Customer requested cancellation'), 'Reason');
    await user.click(screen.getByRole('button', { name: 'Refund' }));

    expect(await screen.findByText(/only allowed for payments in SUCCESS status/)).toBeInTheDocument();
  });

  it('calls onClose when Cancel is clicked', async () => {
    const onClose = vi.fn();
    render(<RefundModal order={order} onClose={onClose} onSuccess={vi.fn()} />);

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(onClose).toHaveBeenCalled();
  });
});
