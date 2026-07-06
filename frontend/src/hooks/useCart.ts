import { useState, useEffect, useCallback } from 'react';
import { fetchCart, addToCart, removeCartItem } from '../api/cart';
import type { Cart } from '../types';

export function useCart(userId: number | null) {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!userId) return;
    setLoading(true);
    setError(null);
    try {
      setCart(await fetchCart(userId));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load cart');
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => { load(); }, [load]);

  const addItem = useCallback(async (productId: number, quantity = 1) => {
    if (!userId) return;
    await addToCart(userId, productId, quantity);
    await load();
  }, [userId, load]);

  const removeItem = useCallback(async (cartItemId: number) => {
    await removeCartItem(cartItemId);
    await load();
  }, [load]);

  return { cart, loading, error, addItem, removeItem, reload: load };
}
