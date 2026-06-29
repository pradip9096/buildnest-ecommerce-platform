import { useState, useEffect, useCallback } from 'react';
import { fetchCart, addToCart, removeCartItem } from '../api/cart';
import type { Cart } from '../types';

export function useCart(userId: number | null, token: string | null) {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!userId || !token) return;
    setLoading(true);
    setError(null);
    try {
      setCart(await fetchCart(userId, token));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load cart');
    } finally {
      setLoading(false);
    }
  }, [userId, token]);

  useEffect(() => { load(); }, [load]);

  const addItem = useCallback(async (productId: number, quantity = 1) => {
    if (!userId || !token) return;
    await addToCart(userId, productId, quantity, token);
    await load();
  }, [userId, token, load]);

  const removeItem = useCallback(async (cartItemId: number) => {
    if (!token) return;
    await removeCartItem(cartItemId, token);
    await load();
  }, [token, load]);

  return { cart, loading, error, addItem, removeItem, reload: load };
}
