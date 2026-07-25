import { useState, useRef, useEffect, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useCart } from '../../hooks/useCart';
import { getWishlistCount } from '../../api/wishlist';
import { NotificationBell } from './NotificationBell';

export function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const { cart } = useCart(user?.id ?? null);
  const cartCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  const [wishlistCount, setWishlistCount] = useState(0);
  useEffect(() => {
    if (!isAuthenticated) {
      setWishlistCount(0);
      return;
    }
    let cancelled = false;
    getWishlistCount()
      .then(count => {
        if (!cancelled) setWishlistCount(count);
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated]);

  const [searchInput, setSearchInput] = useState('');
  const [mobileOpen, setMobileOpen] = useState(false);
  const [accountOpen, setAccountOpen] = useState(false);
  const accountRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (accountRef.current && !accountRef.current.contains(e.target as Node)) {
        setAccountOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function handleSearch(e: FormEvent) {
    e.preventDefault();
    const keyword = searchInput.trim();
    navigate(keyword ? `/products?search=${encodeURIComponent(keyword)}` : '/products');
    setMobileOpen(false);
  }

  async function handleLogout() {
    setAccountOpen(false);
    setMobileOpen(false);
    await logout();
    navigate('/', { replace: true });
  }

  const isAdmin = user?.roles?.includes('ADMIN') ?? false;
  const isSeller = user?.roles?.includes('SELLER') ?? false;

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3 flex items-center gap-4">
        <Link to="/" className="text-xl font-bold text-gray-900 shrink-0">🏗️ BuildNest</Link>

        <form onSubmit={handleSearch} className="hidden sm:flex flex-1 gap-2 max-w-lg">
          <input
            type="search"
            value={searchInput}
            onChange={e => setSearchInput(e.target.value)}
            placeholder="Search products…"
            aria-label="Search products"
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
          <button
            type="submit"
            className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 transition-colors"
          >
            Search
          </button>
        </form>

        <div className="ml-auto hidden sm:flex items-center gap-4">
          {isAuthenticated && (
            <Link to="/account" className="relative text-gray-600 hover:text-gray-900" aria-label="Wishlist">
              <span className="text-xl">❤️</span>
              {wishlistCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-primary-600 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                  {wishlistCount > 99 ? '99+' : wishlistCount}
                </span>
              )}
            </Link>
          )}
          <Link to="/cart" className="relative text-gray-600 hover:text-gray-900" aria-label="Cart">
            <span className="text-xl">🛒</span>
            {cartCount > 0 && (
              <span className="absolute -top-2 -right-2 bg-primary-600 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                {cartCount > 99 ? '99+' : cartCount}
              </span>
            )}
          </Link>

          {isAuthenticated && <NotificationBell />}

          {isAuthenticated ? (
            <div className="relative" ref={accountRef}>
              <button
                type="button"
                onClick={() => setAccountOpen(o => !o)}
                className="flex items-center gap-2 text-sm text-gray-700 hover:text-gray-900"
              >
                <span className="w-7 h-7 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center text-xs font-bold">
                  {user?.username?.[0]?.toUpperCase() ?? '?'}
                </span>
                <span className="font-medium">{user?.username}</span>
              </button>
              {accountOpen && (
                <div className="absolute right-0 mt-2 w-44 bg-white border border-gray-200 rounded-xl shadow-lg py-1 text-sm">
                  <Link to="/account" onClick={() => setAccountOpen(false)} className="block px-4 py-2 text-gray-700 hover:bg-gray-50">
                    Account
                  </Link>
                  {isAdmin && (
                    <Link to="/admin" onClick={() => setAccountOpen(false)} className="block px-4 py-2 text-gray-700 hover:bg-gray-50">
                      Admin
                    </Link>
                  )}
                  {isSeller && (
                    <Link to="/seller" onClick={() => setAccountOpen(false)} className="block px-4 py-2 text-gray-700 hover:bg-gray-50">
                      Seller Dashboard
                    </Link>
                  )}
                  <button
                    type="button"
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 text-red-600 hover:bg-gray-50"
                  >
                    Sign out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link to="/login" className="text-sm font-medium text-primary-600 hover:text-primary-700">
              Sign in
            </Link>
          )}
        </div>

        <button
          type="button"
          onClick={() => setMobileOpen(o => !o)}
          className="ml-auto sm:hidden text-xl text-gray-600"
          aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
        >
          {mobileOpen ? '✕' : '☰'}
        </button>
      </div>

      {mobileOpen && (
        <div className="sm:hidden border-t border-gray-200 px-4 py-4 space-y-4">
          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              type="search"
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              placeholder="Search products…"
              aria-label="Search products"
              className="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <button
              type="submit"
              className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 transition-colors"
            >
              Search
            </button>
          </form>

          <Link to="/cart" onClick={() => setMobileOpen(false)} className="flex items-center gap-2 text-sm text-gray-700">
            🛒 Cart{cartCount > 0 && ` (${cartCount})`}
          </Link>

          {isAuthenticated && (
            <Link to="/account" onClick={() => setMobileOpen(false)} className="flex items-center gap-2 text-sm text-gray-700">
              ❤️ Wishlist{wishlistCount > 0 && ` (${wishlistCount})`}
            </Link>
          )}

          {isAuthenticated ? (
            <>
              <Link to="/account" onClick={() => setMobileOpen(false)} className="block text-sm text-gray-700">
                Account (@{user?.username})
              </Link>
              {isAdmin && (
                <Link to="/admin" onClick={() => setMobileOpen(false)} className="block text-sm text-gray-700">
                  Admin
                </Link>
              )}
              {isSeller && (
                <Link to="/seller" onClick={() => setMobileOpen(false)} className="block text-sm text-gray-700">
                  Seller Dashboard
                </Link>
              )}
              <button type="button" onClick={handleLogout} className="block text-sm text-red-600">
                Sign out
              </button>
            </>
          ) : (
            <Link to="/login" onClick={() => setMobileOpen(false)} className="block text-sm font-medium text-primary-600">
              Sign in
            </Link>
          )}
        </div>
      )}
    </header>
  );
}
