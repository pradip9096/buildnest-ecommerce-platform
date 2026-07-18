import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UserDetailModal } from './UserDetailModal';
import { updateAdminUser, type AdminUser } from '../../api/admin';

vi.mock('../../api/admin', () => ({
  updateAdminUser: vi.fn(),
}));

const mockUpdateAdminUser = vi.mocked(updateAdminUser);

const user: AdminUser = {
  id: 7,
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
});

describe('UserDetailModal', () => {
  it('shows user details in view mode', () => {
    render(<UserDetailModal user={user} onClose={vi.fn()} onSuccess={vi.fn()} />);

    expect(screen.getByText('@aarav')).toBeInTheDocument();
    expect(screen.getByText('Aarav Sharma')).toBeInTheDocument();
    expect(screen.getByText('aarav@example.com')).toBeInTheDocument();
    expect(screen.getByText('+14155552671')).toBeInTheDocument();
  });

  it('switches to edit mode and saves an update', async () => {
    mockUpdateAdminUser.mockResolvedValue({ ...user, firstName: 'Rohan' });
    const onSuccess = vi.fn();
    render(<UserDetailModal user={user} onClose={vi.fn()} onSuccess={onSuccess} />);

    const uiUser = userEvent.setup();
    await uiUser.click(screen.getByRole('button', { name: 'Edit' }));

    const firstNameInput = screen.getByDisplayValue('Aarav');
    await uiUser.clear(firstNameInput);
    await uiUser.type(firstNameInput, 'Rohan');
    await uiUser.click(screen.getByRole('button', { name: 'Save' }));

    expect(mockUpdateAdminUser).toHaveBeenCalledWith(7, {
      firstName: 'Rohan',
      lastName: 'Sharma',
      email: 'aarav@example.com',
      phone: '+14155552671',
    });
    expect(onSuccess).toHaveBeenCalledWith({ ...user, firstName: 'Rohan' });
  });

  it('rejects an invalid email without calling the API', async () => {
    render(<UserDetailModal user={user} onClose={vi.fn()} onSuccess={vi.fn()} />);

    const uiUser = userEvent.setup();
    await uiUser.click(screen.getByRole('button', { name: 'Edit' }));

    const emailInput = screen.getByDisplayValue('aarav@example.com');
    await uiUser.clear(emailInput);
    await uiUser.type(emailInput, 'not-an-email');
    await uiUser.click(screen.getByRole('button', { name: 'Save' }));

    expect(screen.getByText('Enter a valid email address.')).toBeInTheDocument();
    expect(mockUpdateAdminUser).not.toHaveBeenCalled();
  });

  it('rejects an invalid phone number without calling the API', async () => {
    render(<UserDetailModal user={user} onClose={vi.fn()} onSuccess={vi.fn()} />);

    const uiUser = userEvent.setup();
    await uiUser.click(screen.getByRole('button', { name: 'Edit' }));

    const phoneInput = screen.getByDisplayValue('+14155552671');
    await uiUser.clear(phoneInput);
    await uiUser.type(phoneInput, 'abc123');
    await uiUser.click(screen.getByRole('button', { name: 'Save' }));

    expect(screen.getByText('Enter a valid phone number (e.g. +14155552671).')).toBeInTheDocument();
    expect(mockUpdateAdminUser).not.toHaveBeenCalled();
  });

  it('surfaces a backend error', async () => {
    mockUpdateAdminUser.mockRejectedValue(new Error('Email already in use'));
    render(<UserDetailModal user={user} onClose={vi.fn()} onSuccess={vi.fn()} />);

    const uiUser = userEvent.setup();
    await uiUser.click(screen.getByRole('button', { name: 'Edit' }));
    await uiUser.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Email already in use')).toBeInTheDocument();
  });

  it('calls onClose when Close is clicked in view mode', async () => {
    const onClose = vi.fn();
    render(<UserDetailModal user={user} onClose={onClose} onSuccess={vi.fn()} />);

    const uiUser = userEvent.setup();
    await uiUser.click(screen.getByRole('button', { name: 'Close' }));

    expect(onClose).toHaveBeenCalled();
  });
});
