import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { WishlistTab } from './WishlistTab';
import { fetchWishlist, removeFromWishlist, clearWishlist } from '../../api/wishlist';
import { addToCart } from '../../api/cart';
import type { Product } from '../../types';

vi.mock('../../api/wishlist', () => ({
  fetchWishlist: vi.fn(),
  removeFromWishlist: vi.fn(),
  clearWishlist: vi.fn(),
}));

vi.mock('../../api/cart', () => ({
  addToCart: vi.fn(),
}));

const mockFetchWishlist = vi.mocked(fetchWishlist);
const mockRemoveFromWishlist = vi.mocked(removeFromWishlist);
const mockClearWishlist = vi.mocked(clearWishlist);
const mockAddToCart = vi.mocked(addToCart);

const product: Product = {
  id: 1,
  name: 'Cement Bag',
  price: 500,
  stockQuantity: 10,
  sku: 'CEM-1',
  isActive: true,
  createdAt: '2026-07-04T00:00:00Z',
};

function renderTab() {
  return render(
    <MemoryRouter>
      <WishlistTab userId={42} />
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('WishlistTab', () => {
  it('does not show Clear All when the wishlist is empty', async () => {
    mockFetchWishlist.mockResolvedValue([]);

    renderTab();

    await screen.findByText('Your wishlist is empty.');
    expect(screen.queryByRole('button', { name: 'Clear All' })).not.toBeInTheDocument();
  });

  it('shows Clear All when the wishlist has items', async () => {
    mockFetchWishlist.mockResolvedValue([product]);

    renderTab();

    expect(await screen.findByRole('button', { name: 'Clear All' })).toBeInTheDocument();
  });

  it('clears the wishlist after confirmation and empties the list', async () => {
    mockFetchWishlist.mockResolvedValue([product]);
    mockClearWishlist.mockResolvedValue(undefined);

    renderTab();

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Clear All' }));

    expect(mockClearWishlist).toHaveBeenCalled();
    await waitFor(() => expect(screen.getByText('Your wishlist is empty.')).toBeInTheDocument());
  });

  it('does not call clearWishlist when the confirmation is declined', async () => {
    vi.stubGlobal('confirm', vi.fn(() => false));
    mockFetchWishlist.mockResolvedValue([product]);

    renderTab();

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Clear All' }));

    expect(mockClearWishlist).not.toHaveBeenCalled();
  });

  it('still supports removing a single item', async () => {
    mockFetchWishlist.mockResolvedValue([product]);
    mockRemoveFromWishlist.mockResolvedValue(undefined);

    renderTab();

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Remove from wishlist' }));

    expect(mockRemoveFromWishlist).toHaveBeenCalledWith(1);
    await waitFor(() => expect(screen.getByText('Your wishlist is empty.')).toBeInTheDocument());
  });

  it('still supports moving an item to the cart', async () => {
    mockFetchWishlist.mockResolvedValue([product]);
    mockAddToCart.mockResolvedValue(undefined);
    mockRemoveFromWishlist.mockResolvedValue(undefined);

    renderTab();

    await userEvent.setup().click(await screen.findByRole('button', { name: 'Move to Cart' }));

    expect(mockAddToCart).toHaveBeenCalledWith(42, 1, 1);
  });
});
