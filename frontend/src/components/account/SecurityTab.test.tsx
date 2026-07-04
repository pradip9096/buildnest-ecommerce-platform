import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SecurityTab } from './SecurityTab';
import { changePassword } from '../../api/user';

vi.mock('../../api/user', () => ({
  changePassword: vi.fn(),
}));

const mockChangePassword = vi.mocked(changePassword);

beforeEach(() => {
  vi.clearAllMocks();
});

async function fillAndSubmit(oldPassword: string, newPassword: string, confirm = newPassword) {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText('Current password'), oldPassword);
  await user.type(screen.getByLabelText('New password'), newPassword);
  await user.type(screen.getByLabelText('Confirm new password'), confirm);
  await user.click(screen.getByRole('button', { name: 'Change Password' }));
}

describe('SecurityTab', () => {
  it('calls changePassword with only the token and passwords — no userId', async () => {
    mockChangePassword.mockResolvedValue(undefined);

    render(<SecurityTab token="token-abc" />);
    await fillAndSubmit('oldPass123456', 'newPass123456');

    expect(mockChangePassword).toHaveBeenCalledWith('token-abc', 'oldPass123456', 'newPass123456');
  });

  it('shows a confirmation message on success', async () => {
    mockChangePassword.mockResolvedValue(undefined);

    render(<SecurityTab token="token-abc" />);
    await fillAndSubmit('oldPass123456', 'newPass123456');

    expect(await screen.findByText('Password changed successfully.')).toBeInTheDocument();
  });

  it('shows a clear error message when the current password is wrong', async () => {
    mockChangePassword.mockRejectedValue(new Error('Old password is incorrect'));

    render(<SecurityTab token="token-abc" />);
    await fillAndSubmit('wrongPassword', 'newPass123456');

    expect(await screen.findByText('Old password is incorrect')).toBeInTheDocument();
  });

  it('validates new password length before calling the API', async () => {
    render(<SecurityTab token="token-abc" />);
    await fillAndSubmit('oldPass123456', 'short', 'short');

    expect(screen.getByText('New password must be at least 12 characters')).toBeInTheDocument();
    expect(mockChangePassword).not.toHaveBeenCalled();
  });

  it('validates that new password and confirmation match before calling the API', async () => {
    render(<SecurityTab token="token-abc" />);
    await fillAndSubmit('oldPass123456', 'newPass123456', 'differentPass1234');

    expect(screen.getByText('New passwords do not match')).toBeInTheDocument();
    expect(mockChangePassword).not.toHaveBeenCalled();
  });

  it('clears the password fields after a successful change', async () => {
    mockChangePassword.mockResolvedValue(undefined);

    render(<SecurityTab token="token-abc" />);
    await fillAndSubmit('oldPass123456', 'newPass123456');

    await waitFor(() => {
      expect(screen.getByLabelText('Current password')).toHaveValue('');
      expect(screen.getByLabelText('New password')).toHaveValue('');
      expect(screen.getByLabelText('Confirm new password')).toHaveValue('');
    });
  });
});
