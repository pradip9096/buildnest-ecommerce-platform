import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { PrivacyTab } from './PrivacyTab';
import { useAuth } from '../../hooks/useAuth';
import { exportMyData, deleteMyAccount } from '../../api/user';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../api/user', () => ({
  exportMyData: vi.fn(),
  deleteMyAccount: vi.fn(),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockExportMyData = vi.mocked(exportMyData);
const mockDeleteMyAccount = vi.mocked(deleteMyAccount);

function renderTab() {
  return render(
    <MemoryRouter>
      <PrivacyTab />
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  mockUseAuth.mockReturnValue({
    user: { id: 1, username: 'alice', roles: [] },
    isAuthenticated: true,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
  });
});

describe('PrivacyTab (#128, COMP-01/COMP-02)', () => {
  it('exports data when the download button is clicked', async () => {
    mockExportMyData.mockResolvedValue({ profile: { username: 'alice' } });
    const user = userEvent.setup();

    renderTab();
    await user.click(screen.getByTestId('privacy-export-button'));

    expect(mockExportMyData).toHaveBeenCalledTimes(1);
  });

  it('requires a two-step confirmation before deleting the account', async () => {
    const user = userEvent.setup();
    renderTab();

    expect(screen.queryByTestId('privacy-delete-confirm')).not.toBeInTheDocument();

    await user.click(screen.getByTestId('privacy-delete-button'));
    expect(screen.getByTestId('privacy-delete-confirm')).toBeInTheDocument();
    expect(mockDeleteMyAccount).not.toHaveBeenCalled();
  });

  it('deletes the account and logs out after confirmation', async () => {
    mockDeleteMyAccount.mockResolvedValue(undefined);
    const logout = vi.fn().mockResolvedValue(undefined);
    mockUseAuth.mockReturnValue({
      user: { id: 1, username: 'alice', roles: [] },
      isAuthenticated: true,
      loading: false,
      login: vi.fn(),
      logout,
      register: vi.fn(),
    });
    const user = userEvent.setup();

    renderTab();
    await user.click(screen.getByTestId('privacy-delete-button'));
    await user.click(screen.getByTestId('privacy-delete-confirm'));

    expect(mockDeleteMyAccount).toHaveBeenCalledTimes(1);
    expect(logout).toHaveBeenCalledTimes(1);
  });

  it('shows an error message when deletion fails', async () => {
    mockDeleteMyAccount.mockRejectedValue(new Error('Failed to delete account'));
    const user = userEvent.setup();

    renderTab();
    await user.click(screen.getByTestId('privacy-delete-button'));
    await user.click(screen.getByTestId('privacy-delete-confirm'));

    expect(await screen.findByText('Failed to delete account')).toBeInTheDocument();
  });
});
