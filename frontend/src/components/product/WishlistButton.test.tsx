import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { WishlistButton } from './WishlistButton';
import { useAuth } from '../../hooks/useAuth';
import { isInWishlist, addToWishlist, removeFromWishlist } from '../../api/wishlist';
import type { AuthUser } from '../../types';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../api/wishlist', () => ({
  isInWishlist: vi.fn(),
  addToWishlist: vi.fn(),
  removeFromWishlist: vi.fn(),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockIsInWishlist = vi.mocked(isInWishlist);
const mockAddToWishlist = vi.mocked(addToWishlist);
const mockRemoveFromWishlist = vi.mocked(removeFromWishlist);

function authState(overrides: Partial<ReturnType<typeof useAuth>>) {
  return {
    user: null,
    isAuthenticated: false,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    ...overrides,
  };
}

const user: AuthUser = { id: 42, username: 'alice', roles: ['USER'] };

beforeEach(() => {
  vi.clearAllMocks();
});

describe('WishlistButton', () => {
  it('renders nothing when unauthenticated', () => {
    mockUseAuth.mockReturnValue(authState({}));

    render(<WishlistButton productId={5} />);

    expect(screen.queryByRole('button')).not.toBeInTheDocument();
    expect(mockIsInWishlist).not.toHaveBeenCalled();
  });

  it('checks membership on mount and shows the filled state when in the wishlist', async () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockIsInWishlist.mockResolvedValue(true);

    render(<WishlistButton productId={5} />);

    expect(mockIsInWishlist).toHaveBeenCalledWith(5);
    expect(await screen.findByRole('button', { name: 'Remove from wishlist' })).toBeInTheDocument();
  });

  it('shows the empty state when not in the wishlist', async () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockIsInWishlist.mockResolvedValue(false);

    render(<WishlistButton productId={5} />);

    expect(await screen.findByRole('button', { name: 'Add to wishlist' })).toBeInTheDocument();
  });

  it('calls addToWishlist and flips to the removed-state label when toggled on', async () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockIsInWishlist.mockResolvedValue(false);
    mockAddToWishlist.mockResolvedValue(undefined);

    render(<WishlistButton productId={5} />);

    const button = await screen.findByRole('button', { name: 'Add to wishlist' });
    await userEvent.setup().click(button);

    expect(mockAddToWishlist).toHaveBeenCalledWith(5);
    expect(await screen.findByRole('button', { name: 'Remove from wishlist' })).toBeInTheDocument();
  });

  it('calls removeFromWishlist when toggled off', async () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockIsInWishlist.mockResolvedValue(true);
    mockRemoveFromWishlist.mockResolvedValue(undefined);

    render(<WishlistButton productId={5} />);

    const button = await screen.findByRole('button', { name: 'Remove from wishlist' });
    await userEvent.setup().click(button);

    expect(mockRemoveFromWishlist).toHaveBeenCalledWith(5);
    expect(await screen.findByRole('button', { name: 'Add to wishlist' })).toBeInTheDocument();
  });

  it('reverts the optimistic update when the API call fails', async () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockIsInWishlist.mockResolvedValue(false);
    mockAddToWishlist.mockRejectedValue(new Error('failed'));

    render(<WishlistButton productId={5} />);

    const button = await screen.findByRole('button', { name: 'Add to wishlist' });
    await userEvent.setup().click(button);

    expect(await screen.findByRole('button', { name: 'Add to wishlist' })).toBeInTheDocument();
  });
});
