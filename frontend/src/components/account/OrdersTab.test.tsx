import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { OrdersTab } from './OrdersTab';
import { fetchOrders } from '../../api/orders';
import type { Order } from '../../types';

vi.mock('../../api/orders', () => ({
  fetchOrders: vi.fn(),
  fetchOrderById: vi.fn(),
}));

const mockFetchOrders = vi.mocked(fetchOrders);

const order: Order = { id: 1, userId: 42, status: 'PENDING', totalAmount: 100, createdAt: '2026-07-04T00:00:00Z' };

beforeEach(() => {
  vi.clearAllMocks();
});

describe('OrdersTab', () => {
  it('renders the order list on success', async () => {
    mockFetchOrders.mockResolvedValue([order]);

    render(<OrdersTab token="token-abc" userId={42} />);

    await waitFor(() => expect(screen.getByText('Order #1')).toBeInTheDocument());
    expect(mockFetchOrders).toHaveBeenCalledWith('token-abc');
  });

  it('shows the genuine empty state only when the fetch actually succeeds with zero orders', async () => {
    mockFetchOrders.mockResolvedValue([]);

    render(<OrdersTab token="token-abc" userId={42} />);

    await waitFor(() => expect(screen.getByText(/haven.t placed any orders/i)).toBeInTheDocument());
  });

  it('surfaces an error instead of the false empty state when the fetch fails (e.g. an expired token)', async () => {
    mockFetchOrders.mockRejectedValue(new Error('Failed to fetch orders (401)'));

    render(<OrdersTab token="expired-token" userId={42} />);

    await waitFor(() => expect(screen.getByText('Failed to fetch orders (401)')).toBeInTheDocument());
    expect(screen.queryByText(/haven.t placed any orders/i)).not.toBeInTheDocument();
  });
});
