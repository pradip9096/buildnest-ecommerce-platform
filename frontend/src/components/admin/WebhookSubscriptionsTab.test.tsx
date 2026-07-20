import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { WebhookSubscriptionsTab } from './WebhookSubscriptionsTab';
import {
  fetchAdminWebhookSubscriptions,
  createAdminWebhookSubscription,
  deactivateAdminWebhookSubscription,
  deleteAdminWebhookSubscription,
  type AdminWebhookSubscription,
} from '../../api/admin';
import { ApiError } from '../../api/client';

vi.mock('../../api/admin', () => ({
  fetchAdminWebhookSubscriptions: vi.fn(),
  createAdminWebhookSubscription: vi.fn(),
  deactivateAdminWebhookSubscription: vi.fn(),
  deleteAdminWebhookSubscription: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminWebhookSubscriptions);
const mockCreate = vi.mocked(createAdminWebhookSubscription);
const mockDeactivate = vi.mocked(deactivateAdminWebhookSubscription);
const mockDelete = vi.mocked(deleteAdminWebhookSubscription);

const orderPlaced: AdminWebhookSubscription = {
  id: 1,
  eventType: 'order.placed',
  targetUrl: 'https://example.com/hooks/orders',
  active: true,
  failureCount: 0,
  lastDeliveryStatus: 'SUCCESS',
  createdAt: '2026-07-01T00:00:00',
};
const paymentFailed: AdminWebhookSubscription = {
  id: 2,
  eventType: 'payment.failed',
  targetUrl: 'https://example.com/hooks/payments',
  active: true,
  failureCount: 3,
  createdAt: '2026-07-02T00:00:00',
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('WebhookSubscriptionsTab', () => {
  it('renders the webhook subscription list', async () => {
    mockFetch.mockResolvedValue([orderPlaced, paymentFailed]);

    render(<WebhookSubscriptionsTab />);

    await waitFor(() => expect(screen.getByText('order.placed')).toBeInTheDocument());
    expect(screen.getByText('payment.failed')).toBeInTheDocument();
  });

  it('filters the list via search', async () => {
    mockFetch.mockResolvedValue([orderPlaced, paymentFailed]);

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('order.placed')).toBeInTheDocument());

    await userEvent.setup().type(screen.getByPlaceholderText('Search event types…'), 'payment');

    expect(screen.queryByText('order.placed')).not.toBeInTheDocument();
    expect(screen.getByText('payment.failed')).toBeInTheDocument();
  });

  it('shows the empty state when there are no webhook subscriptions', async () => {
    mockFetch.mockResolvedValue([]);

    render(<WebhookSubscriptionsTab />);

    await waitFor(() => expect(screen.getByText('No webhook subscriptions found')).toBeInTheDocument());
  });

  it('creates a webhook subscription via the modal and adds it to the list', async () => {
    mockFetch.mockResolvedValue([orderPlaced]);
    mockCreate.mockResolvedValue({
      id: 3,
      eventType: 'product.updated',
      targetUrl: 'https://example.com/hooks/products',
      active: true,
      failureCount: 0,
      createdAt: '2026-07-20T00:00:00',
    });

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('order.placed')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Subscription' }));
    await user.type(screen.getByLabelText('Event Type'), 'product.updated');
    await user.type(screen.getByLabelText('Target URL'), 'https://example.com/hooks/products');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(mockCreate).toHaveBeenCalledWith(
      expect.objectContaining({ eventType: 'product.updated', targetUrl: 'https://example.com/hooks/products' })
    ));
    await waitFor(() => expect(screen.getByText('product.updated')).toBeInTheDocument());
  });

  it('rejects a blank event type without calling the API', async () => {
    mockFetch.mockResolvedValue([orderPlaced]);

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('order.placed')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Subscription' }));
    await user.type(screen.getByLabelText('Target URL'), 'https://example.com/hooks/x');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Event type is required.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('rejects an invalid target URL without calling the API', async () => {
    mockFetch.mockResolvedValue([orderPlaced]);

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('order.placed')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Subscription' }));
    await user.type(screen.getByLabelText('Event Type'), 'order.placed');
    await user.type(screen.getByLabelText('Target URL'), 'not-a-url');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Target URL must be a valid http/https URL.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('surfaces the backend message when deactivation fails', async () => {
    mockFetch.mockResolvedValue([orderPlaced]);
    mockDeactivate.mockRejectedValue(new ApiError('Cannot deactivate this subscription', 400));

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('order.placed')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Deactivate' }));

    await waitFor(() => expect(screen.getByText('Cannot deactivate this subscription')).toBeInTheDocument());
    expect(screen.getByText('order.placed')).toBeInTheDocument();
  });

  it('deactivates a webhook subscription successfully and marks it inactive', async () => {
    mockFetch.mockResolvedValue([orderPlaced, paymentFailed]);
    mockDeactivate.mockResolvedValue({ ...paymentFailed, active: false });

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('payment.failed')).toBeInTheDocument());

    const deactivateButtons = screen.getAllByRole('button', { name: 'Deactivate' });
    await userEvent.setup().click(deactivateButtons[1]);

    await waitFor(() => expect(mockDeactivate).toHaveBeenCalledWith(2));
    await waitFor(() => expect(screen.getAllByText('Inactive').length).toBeGreaterThan(0));
  });

  it('deletes a webhook subscription and removes it from the list', async () => {
    mockFetch.mockResolvedValue([orderPlaced, paymentFailed]);
    mockDelete.mockResolvedValue(undefined);

    render(<WebhookSubscriptionsTab />);
    await waitFor(() => expect(screen.getByText('payment.failed')).toBeInTheDocument());

    const deleteButtons = screen.getAllByRole('button', { name: 'Delete' });
    await userEvent.setup().click(deleteButtons[1]);

    await waitFor(() => expect(mockDelete).toHaveBeenCalledWith(2));
    await waitFor(() => expect(screen.queryByText('payment.failed')).not.toBeInTheDocument());
  });
});
