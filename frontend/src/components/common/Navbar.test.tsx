import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { Navbar } from './Navbar';
import { useAuth } from '../../hooks/useAuth';
import { fetchCart } from '../../api/cart';
import { getWishlistCount } from '../../api/wishlist';
import type { AuthUser, Cart } from '../../types';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../api/cart', () => ({
  fetchCart: vi.fn(),
  addToCart: vi.fn(),
  removeCartItem: vi.fn(),
}));

vi.mock('../../api/wishlist', () => ({
  getWishlistCount: vi.fn(),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockFetchCart = vi.mocked(fetchCart);
const mockGetWishlistCount = vi.mocked(getWishlistCount);

function authState(overrides: Partial<ReturnType<typeof useAuth>>) {
  return {
    user: null,
    isAuthenticated: false,
    loading: false,
    login: vi.fn(),
    logout: vi.fn().mockResolvedValue(undefined),
    register: vi.fn(),
    ...overrides,
  };
}

function renderNavbar(initialEntries = ['/']) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <Routes>
        <Route path="*" element={<Navbar />} />
      </Routes>
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  mockFetchCart.mockResolvedValue({ cartId: 1, userId: 1, items: [], totalAmount: 0 } as Cart);
  mockGetWishlistCount.mockResolvedValue(0);
});

describe('Navbar', () => {
  it('always renders the logo/home link', () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderNavbar();

    expect(screen.getByText('🏗️ BuildNest')).toBeInTheDocument();
  });

  it('shows a Sign in link when unauthenticated', () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderNavbar();

    expect(screen.getAllByText('Sign in').length).toBeGreaterThan(0);
    expect(screen.queryByText(/^@/)).not.toBeInTheDocument();
  });

  it('shows the username and an account dropdown when authenticated', async () => {
    const user: AuthUser = { id: 1, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));

    renderNavbar();

    expect(screen.getByText('alice')).toBeInTheDocument();
    expect(screen.queryByText('Admin', { selector: 'a' })).not.toBeInTheDocument();

    const trigger = screen.getByRole('button', { name: /alice/i });
    await userEvent.setup().click(trigger);

    expect(screen.getByRole('link', { name: 'Account' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
  });

  it('shows an Admin link in the dropdown only for users with the ADMIN role', async () => {
    const user: AuthUser = { id: 1, username: 'bob', roles: ['ADMIN'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));

    renderNavbar();

    await userEvent.setup().click(screen.getByRole('button', { name: /bob/i }));

    expect(screen.getByRole('link', { name: 'Admin' })).toBeInTheDocument();
  });

  it('calls logout and navigates home when Sign out is clicked', async () => {
    const logout = vi.fn().mockResolvedValue(undefined);
    const user: AuthUser = { id: 1, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user, logout }));

    renderNavbar();

    const userEv = userEvent.setup();
    await userEv.click(screen.getByRole('button', { name: /alice/i }));
    await userEv.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(logout).toHaveBeenCalledTimes(1);
  });

  it('shows the cart item count as a badge when the cart has items', async () => {
    const user: AuthUser = { id: 1, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockFetchCart.mockResolvedValue({
      cartId: 1,
      userId: 1,
      items: [
        { cartItemId: 1, productId: 1, productName: 'Cement', quantity: 2, price: 100, itemTotal: 200 },
        { cartItemId: 2, productId: 2, productName: 'Bricks', quantity: 3, price: 10, itemTotal: 30 },
      ],
      totalAmount: 230,
    });

    renderNavbar();

    await waitFor(() => expect(screen.getByText('5')).toBeInTheDocument());
  });

  it('shows no cart badge when the cart is empty', async () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderNavbar();

    await waitFor(() => expect(mockFetchCart).not.toHaveBeenCalled());
    expect(screen.queryByText(/^\d+$/)).not.toBeInTheDocument();
  });

  it('shows the wishlist count as a badge when authenticated with items', async () => {
    const user: AuthUser = { id: 1, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));
    mockGetWishlistCount.mockResolvedValue(4);

    renderNavbar();

    expect(mockGetWishlistCount).toHaveBeenCalled();
    await waitFor(() => expect(screen.getByText('4')).toBeInTheDocument());
  });

  it('hides the wishlist link entirely when unauthenticated', () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderNavbar();

    expect(mockGetWishlistCount).not.toHaveBeenCalled();
    expect(screen.queryByLabelText('Wishlist')).not.toBeInTheDocument();
  });

  it('navigates to a filtered product search on submit', async () => {
    mockUseAuth.mockReturnValue(authState({}));

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<Navbar />} />
          <Route path="/products" element={<div>Products Page</div>} />
        </Routes>
      </MemoryRouter>
    );

    const user = userEvent.setup();
    await user.type(screen.getByPlaceholderText('Search products…'), 'cement');
    await user.click(screen.getAllByRole('button', { name: 'Search' })[0]);

    expect(await screen.findByText('Products Page')).toBeInTheDocument();
  });

  it('toggles the mobile menu open and closed', async () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderNavbar();

    const user = userEvent.setup();
    const toggle = screen.getByRole('button', { name: 'Open menu' });
    await user.click(toggle);

    expect(screen.getByRole('button', { name: 'Close menu' })).toBeInTheDocument();
  });
});
