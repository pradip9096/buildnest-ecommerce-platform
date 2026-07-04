import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { ProductCard } from './ProductCard';
import { useAuth } from '../../hooks/useAuth';
import { addToCart } from '../../api/cart';
import type { AuthUser, Product } from '../../types';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../api/cart', () => ({
  addToCart: vi.fn(),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockAddToCart = vi.mocked(addToCart);

function authState(overrides: Partial<ReturnType<typeof useAuth>>) {
  return {
    user: null,
    token: null,
    isAuthenticated: false,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    ...overrides,
  };
}

const product: Product = {
  id: 1,
  name: 'Cement Bag',
  price: 500,
  stockQuantity: 10,
  sku: 'CEM-1',
  isActive: true,
  createdAt: '2026-07-04T00:00:00Z',
};

function renderCard(p: Product = product) {
  return render(
    <MemoryRouter>
      <ProductCard product={p} />
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
});

afterEach(() => {
  vi.useRealTimers();
});

describe('ProductCard — quick add to cart', () => {
  it('shows an Add to Cart button for an in-stock product', () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderCard();

    expect(screen.getByRole('button', { name: 'Add to Cart' })).toBeInTheDocument();
  });

  it('hides the button and shows an out-of-stock label when stock is zero', () => {
    mockUseAuth.mockReturnValue(authState({}));

    renderCard({ ...product, stockQuantity: 0 });

    expect(screen.queryByRole('button', { name: 'Add to Cart' })).not.toBeInTheDocument();
    expect(screen.getByText('Out of stock')).toBeInTheDocument();
  });

  it('prompts sign-in instead of calling the API when unauthenticated', async () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: false }));

    renderCard();

    await userEvent.setup().click(screen.getByRole('button', { name: 'Add to Cart' }));

    expect(mockAddToCart).not.toHaveBeenCalled();
    expect(await screen.findByRole('button', { name: 'Sign in to add' })).toBeInTheDocument();
  });

  it('calls addToCart with quantity 1 and shows success feedback when authenticated', async () => {
    const user: AuthUser = { id: 42, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user, token: 'token-abc' }));
    mockAddToCart.mockResolvedValue(undefined);

    renderCard();

    await userEvent.setup().click(screen.getByRole('button', { name: 'Add to Cart' }));

    expect(mockAddToCart).toHaveBeenCalledWith(42, 1, 1, 'token-abc');
    expect(await screen.findByRole('button', { name: 'Added ✓' })).toBeInTheDocument();
  });

  it('shows an error state when the API call fails', async () => {
    const user: AuthUser = { id: 42, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user, token: 'token-abc' }));
    mockAddToCart.mockRejectedValue(new Error('Failed to add item (500)'));

    renderCard();

    await userEvent.setup().click(screen.getByRole('button', { name: 'Add to Cart' }));

    expect(await screen.findByRole('button', { name: 'Failed — retry' })).toBeInTheDocument();
  });

  it('reverts to the idle label after the feedback window elapses', async () => {
    const user: AuthUser = { id: 42, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user, token: 'token-abc' }));
    mockAddToCart.mockResolvedValue(undefined);

    renderCard();

    await userEvent.setup().click(screen.getByRole('button', { name: 'Add to Cart' }));

    await screen.findByRole('button', { name: 'Added ✓' });

    await waitFor(
      () => expect(screen.getByRole('button', { name: 'Add to Cart' })).toBeInTheDocument(),
      { timeout: 3000 }
    );
  });

  it('does not navigate away from the listing page when the button is clicked', async () => {
    const user: AuthUser = { id: 42, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user, token: 'token-abc' }));
    mockAddToCart.mockResolvedValue(undefined);

    render(
      <MemoryRouter initialEntries={['/products']}>
        <ProductCard product={product} />
      </MemoryRouter>
    );

    await userEvent.setup().click(screen.getByRole('button', { name: 'Add to Cart' }));

    expect(window.location.pathname).not.toBe(`/products/${product.id}`);
  });
});
