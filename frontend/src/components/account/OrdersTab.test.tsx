import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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

    render(<OrdersTab userId={42} />);

    await waitFor(() => expect(screen.getByText('Order #1')).toBeInTheDocument());
    expect(mockFetchOrders).toHaveBeenCalledWith();
  });

  it('shows the genuine empty state only when the fetch actually succeeds with zero orders', async () => {
    mockFetchOrders.mockResolvedValue([]);

    render(<OrdersTab userId={42} />);

    await waitFor(() => expect(screen.getByText(/haven.t placed any orders/i)).toBeInTheDocument());
  });

  it('surfaces an error instead of the false empty state when the fetch fails (e.g. an expired token)', async () => {
    mockFetchOrders.mockRejectedValue(new Error('Failed to fetch orders (401)'));

    render(<OrdersTab userId={42} />);

    await waitFor(() => expect(screen.getByText('Failed to fetch orders (401)')).toBeInTheDocument());
    expect(screen.queryByText(/haven.t placed any orders/i)).not.toBeInTheDocument();
  });

  it('retries the fetch and renders the list when Retry is clicked after a failure', async () => {
    const user = userEvent.setup();
    mockFetchOrders.mockRejectedValueOnce(new Error('Failed to fetch orders (500)'));
    mockFetchOrders.mockResolvedValueOnce([order]);

    render(<OrdersTab userId={42} />);

    await waitFor(() => expect(screen.getByText('Failed to fetch orders (500)')).toBeInTheDocument());

    await user.click(screen.getByRole('button', { name: 'Retry' }));

    await waitFor(() => expect(screen.getByText('Order #1')).toBeInTheDocument());
    expect(mockFetchOrders).toHaveBeenCalledTimes(2);
  });

  it('renders orders with no orderGroupId as individual rows with no group label', async () => {
    mockFetchOrders.mockResolvedValue([order]);

    render(<OrdersTab userId={42} />);

    await waitFor(() => expect(screen.getByText('Order #1')).toBeInTheDocument());
    expect(screen.queryByText(/purchase,.*shipments/)).not.toBeInTheDocument();
  });

  it('groups sibling orders sharing an orderGroupId under one "1 purchase, N shipments" label', async () => {
    const groupOrderA: Order = { id: 10, userId: 42, orderGroupId: 900, status: 'CONFIRMED', totalAmount: 200, createdAt: '2026-07-04T00:00:00Z' };
    const groupOrderB: Order = { id: 11, userId: 42, orderGroupId: 900, status: 'CONFIRMED', totalAmount: 300, createdAt: '2026-07-04T00:00:00Z' };
    mockFetchOrders.mockResolvedValue([groupOrderA, groupOrderB, order]);

    render(<OrdersTab userId={42} />);

    await waitFor(() => expect(screen.getByText('Order #10')).toBeInTheDocument());
    expect(screen.getByText('1 purchase, 2 shipments')).toBeInTheDocument();
    expect(screen.getByText('Order #11')).toBeInTheDocument();
    // the standalone (non-grouped) order still renders without the label attached to it
    expect(screen.getByText('Order #1')).toBeInTheDocument();
  });
});
