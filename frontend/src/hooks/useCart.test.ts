import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useCart } from './useCart';
import { fetchCart, addToCart, removeCartItem } from '../api/cart';
import type { Cart } from '../types';

vi.mock('../api/cart', () => ({
  fetchCart: vi.fn(),
  addToCart: vi.fn(),
  removeCartItem: vi.fn(),
}));

const mockFetchCart = vi.mocked(fetchCart);
const mockAddToCart = vi.mocked(addToCart);
const mockRemoveCartItem = vi.mocked(removeCartItem);

const emptyCart: Cart = { cartId: 1, userId: 42, items: [], totalAmount: 0 };
const cartWithItem: Cart = {
  cartId: 1,
  userId: 42,
  items: [{ cartItemId: 9, productId: 5, productName: 'Cement', quantity: 1, price: 100, itemTotal: 100 }],
  totalAmount: 100,
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('useCart', () => {
  it('does not fetch when userId is missing', () => {
    renderHook(() => useCart(null));
    expect(mockFetchCart).not.toHaveBeenCalled();
  });

  it('loads the cart on mount when userId is present', async () => {
    mockFetchCart.mockResolvedValue(emptyCart);

    const { result } = renderHook(() => useCart(42));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(mockFetchCart).toHaveBeenCalledWith(42);
    expect(result.current.cart).toEqual(emptyCart);
    expect(result.current.error).toBeNull();
  });

  it('surfaces an error message when the cart fails to load', async () => {
    mockFetchCart.mockRejectedValue(new Error('Failed to fetch cart (500)'));

    const { result } = renderHook(() => useCart(42));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch cart (500)');
    expect(result.current.cart).toBeNull();
  });

  it('adds an item and reloads the cart', async () => {
    mockFetchCart.mockResolvedValueOnce(emptyCart).mockResolvedValueOnce(cartWithItem);
    mockAddToCart.mockResolvedValue(undefined);

    const { result } = renderHook(() => useCart(42));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await result.current.addItem(5, 1);

    expect(mockAddToCart).toHaveBeenCalledWith(42, 5, 1);
    expect(mockFetchCart).toHaveBeenCalledTimes(2);
    await waitFor(() => expect(result.current.cart).toEqual(cartWithItem));
  });

  it('removes an item and reloads the cart', async () => {
    mockFetchCart.mockResolvedValueOnce(cartWithItem).mockResolvedValueOnce(emptyCart);
    mockRemoveCartItem.mockResolvedValue(undefined);

    const { result } = renderHook(() => useCart(42));
    await waitFor(() => expect(result.current.loading).toBe(false));

    await result.current.removeItem(9);

    expect(mockRemoveCartItem).toHaveBeenCalledWith(9);
    expect(mockFetchCart).toHaveBeenCalledTimes(2);
    await waitFor(() => expect(result.current.cart).toEqual(emptyCart));
  });
});
