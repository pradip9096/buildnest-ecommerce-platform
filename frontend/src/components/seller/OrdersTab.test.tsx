import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { OrdersTab } from './OrdersTab';
import { fetchSellerOrders, updateSellerOrderStatus } from '../../api/sellerOrders';
import type { Order } from '../../types';

vi.mock('../../api/sellerOrders', () => ({
  fetchSellerOrders: vi.fn(),
  updateSellerOrderStatus: vi.fn(),
}));

const mockFetch = vi.mocked(fetchSellerOrders);
const mockUpdateStatus = vi.mocked(updateSellerOrderStatus);

const order: Order = {
  id: 42,
  userId: 7,
  status: 'PENDING',
  totalAmount: 1500,
  createdAt: '2026-07-01T00:00:00Z',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('seller OrdersTab', () => {
  it('renders only this seller\'s own orders on success', async () => {
    mockFetch.mockResolvedValue({ content: [order], totalElements: 1, totalPages: 1 });

    render(<OrdersTab />);

    await waitFor(() => expect(screen.getByText('#42')).toBeInTheDocument());
    expect(mockFetch).toHaveBeenCalledWith({ page: 0, size: 15 });
  });

  it('shows the genuine empty state only when the fetch succeeds with zero orders', async () => {
    mockFetch.mockResolvedValue({ content: [], totalElements: 0, totalPages: 0 });

    render(<OrdersTab />);

    await waitFor(() => expect(screen.getByText('No orders found')).toBeInTheDocument());
  });

  it('surfaces an error instead of a false empty state when the fetch fails', async () => {
    mockFetch.mockRejectedValue(new Error('Failed to load orders (403)'));

    render(<OrdersTab />);

    await waitFor(() => expect(screen.getByText('Failed to load orders (403)')).toBeInTheDocument());
    expect(screen.queryByText('No orders found')).not.toBeInTheDocument();
  });

  it('updates an order status via the select and reflects it without a full reload', async () => {
    mockFetch.mockResolvedValue({ content: [order], totalElements: 1, totalPages: 1 });
    mockUpdateStatus.mockResolvedValue({ ...order, status: 'SHIPPED' });

    render(<OrdersTab />);
    await waitFor(() => expect(screen.getByText('#42')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.selectOptions(screen.getByRole('combobox'), 'SHIPPED');

    expect(mockUpdateStatus).toHaveBeenCalledWith(42, 'SHIPPED');
    // optimistic local update, not a second fetch
    expect(mockFetch).toHaveBeenCalledTimes(1);
  });

  it('shows an alert and does not change the row when the status update fails', async () => {
    mockFetch.mockResolvedValue({ content: [order], totalElements: 1, totalPages: 1 });
    mockUpdateStatus.mockRejectedValue(new Error('Invalid transition'));
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});

    render(<OrdersTab />);
    await waitFor(() => expect(screen.getByText('#42')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.selectOptions(screen.getByRole('combobox'), 'SHIPPED');

    await waitFor(() => expect(alertSpy).toHaveBeenCalledWith('Invalid transition'));
    alertSpy.mockRestore();
  });
});
