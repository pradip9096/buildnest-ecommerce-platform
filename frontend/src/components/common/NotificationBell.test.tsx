import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { NotificationBell } from './NotificationBell';
import { useNotifications } from '../../hooks/useNotifications';
import type { OrderStatusNotification } from '../../hooks/useNotifications';

vi.mock('../../hooks/useNotifications', () => ({
  useNotifications: vi.fn(),
}));

const mockUseNotifications = vi.mocked(useNotifications);

function renderBell() {
  return render(
    <MemoryRouter>
      <NotificationBell />
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('NotificationBell', () => {
  it('shows no unread badge when there are no notifications', () => {
    mockUseNotifications.mockReturnValue({ notifications: [], unreadCount: 0, markAllRead: vi.fn() });

    renderBell();

    expect(screen.queryByText(/^\d+$/)).not.toBeInTheDocument();
  });

  it('shows the unread count badge', () => {
    mockUseNotifications.mockReturnValue({ notifications: [], unreadCount: 3, markAllRead: vi.fn() });

    renderBell();

    expect(screen.getByText('3')).toBeInTheDocument();
  });

  it('opens the dropdown, lists notifications, and marks them read on click', async () => {
    const markAllRead = vi.fn();
    const notifications: OrderStatusNotification[] = [
      { id: '1', orderId: 42, previousStatus: 'PENDING', newStatus: 'SHIPPED', receivedAt: Date.now() },
    ];
    mockUseNotifications.mockReturnValue({ notifications, unreadCount: 1, markAllRead });

    renderBell();
    await userEvent.setup().click(screen.getByRole('button', { name: 'Notifications' }));

    expect(screen.getByText('Order #42')).toBeInTheDocument();
    expect(screen.getByText('Pending → Shipped')).toBeInTheDocument();
    expect(markAllRead).toHaveBeenCalledTimes(1);
  });

  it('shows an empty state when the dropdown is open with no notifications', async () => {
    mockUseNotifications.mockReturnValue({ notifications: [], unreadCount: 0, markAllRead: vi.fn() });

    renderBell();
    await userEvent.setup().click(screen.getByRole('button', { name: 'Notifications' }));

    expect(screen.getByText('No notifications yet')).toBeInTheDocument();
  });
});
