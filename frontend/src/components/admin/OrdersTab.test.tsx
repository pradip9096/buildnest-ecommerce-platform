import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { OrdersTab } from './OrdersTab';
import { fetchAdminOrders, updateOrderStatus, refundOrder, type AdminOrder } from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchAdminOrders: vi.fn(),
  updateOrderStatus: vi.fn(),
  refundOrder: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminOrders);
const mockUpdateStatus = vi.mocked(updateOrderStatus);
const mockRefund = vi.mocked(refundOrder);

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

describe('OrdersTab', () => {
  it('opens the refund modal for a row and submits a refund', async () => {
    mockFetch.mockResolvedValue({ content: [order], totalElements: 1, totalPages: 1 });
    mockRefund.mockResolvedValue(undefined);

    render(<OrdersTab />);
    await waitFor(() => expect(screen.getByText('#42')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Refund' }));

    expect(screen.getByText('Refund Order')).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText('e.g. 500.00'), '500');
    await user.type(screen.getByPlaceholderText('e.g. Customer requested cancellation'), 'Damaged item');
    const submitButton = screen.getAllByRole('button', { name: 'Refund' })
      .find(btn => btn.getAttribute('type') === 'submit')!;
    await user.click(submitButton);

    expect(mockRefund).toHaveBeenCalledWith(42, 500, 'Damaged item');
    // reload re-fetches the list on success
    await waitFor(() => expect(mockFetch).toHaveBeenCalledTimes(2));
  });

  it('closes the refund modal on cancel without calling the API', async () => {
    mockFetch.mockResolvedValue({ content: [order], totalElements: 1, totalPages: 1 });

    render(<OrdersTab />);
    await waitFor(() => expect(screen.getByText('#42')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Refund' }));
    expect(screen.getByText('Refund Order')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.queryByText('Refund Order')).not.toBeInTheDocument();
    expect(mockRefund).not.toHaveBeenCalled();
  });

  it('still allows a status update independently of refund', async () => {
    mockFetch.mockResolvedValue({ content: [order], totalElements: 1, totalPages: 1 });
    mockUpdateStatus.mockResolvedValue(undefined);

    render(<OrdersTab />);
    await waitFor(() => expect(screen.getByText('#42')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.selectOptions(screen.getByDisplayValue('DELIVERED'), 'CANCELLED');

    await waitFor(() => expect(mockUpdateStatus).toHaveBeenCalledWith(42, 'CANCELLED'));
  });
});
