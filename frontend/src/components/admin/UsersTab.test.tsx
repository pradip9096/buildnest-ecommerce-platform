import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UsersTab } from './UsersTab';
import { fetchAdminUsers, updateAdminUser, type AdminUser } from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchAdminUsers: vi.fn(),
  deleteAdminUser: vi.fn(),
  updateAdminUser: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminUsers);
const mockUpdate = vi.mocked(updateAdminUser);

const aarav: AdminUser = {
  id: 1,
  username: 'aarav',
  email: 'aarav@example.com',
  firstName: 'Aarav',
  lastName: 'Sharma',
  phoneNumber: '+14155552671',
  roles: ['USER'],
  enabled: true,
};

beforeEach(() => {
  vi.clearAllMocks();
  mockFetch.mockResolvedValue([aarav]);
});

describe('UsersTab', () => {
  it('opens the detail modal on View and reflects a saved edit in the list', async () => {
    mockUpdate.mockResolvedValue({ ...aarav, firstName: 'Rohan' });
    render(<UsersTab />);

    await waitFor(() => expect(screen.getByText('@aarav')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'View' }));
    expect(screen.getByText('User Details')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Edit' }));
    await user.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(mockUpdate).toHaveBeenCalledWith(1, {
      firstName: 'Aarav',
      lastName: 'Sharma',
      email: 'aarav@example.com',
      phone: '+14155552671',
    }));
    await waitFor(() => expect(screen.queryByText('User Details')).not.toBeInTheDocument());
    expect(screen.getByText('Rohan Sharma')).toBeInTheDocument();
  });
});
