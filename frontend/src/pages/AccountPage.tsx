import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { ProfileTab } from '../components/account/ProfileTab';
import { OrdersTab } from '../components/account/OrdersTab';
import { WishlistTab } from '../components/account/WishlistTab';
import { AddressesTab } from '../components/account/AddressesTab';
import { SecurityTab } from '../components/account/SecurityTab';

type Tab = 'profile' | 'orders' | 'addresses' | 'wishlist' | 'security';

const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'profile',   label: 'Profile',    icon: '👤' },
  { id: 'orders',    label: 'Orders',     icon: '📦' },
  { id: 'addresses', label: 'Addresses',  icon: '📍' },
  { id: 'wishlist',  label: 'Wishlist',   icon: '❤️' },
  { id: 'security',  label: 'Security',   icon: '🔒' },
];

export function AccountPage() {
  const { user, token, isAuthenticated, loading, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<Tab>('profile');
  const [loggingOut, setLoggingOut] = useState(false);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-amber-400 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🔒</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Sign in to view your account</h1>
          <Link to="/login" state={{ from: '/account' }}
            className="inline-block mt-4 bg-amber-500 hover:bg-amber-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors">
            Sign in
          </Link>
        </div>
      </div>
    );
  }

  const handleLogout = async () => {
    setLoggingOut(true);
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-200 px-4 py-4">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/" className="text-2xl font-bold text-amber-600">🏗️ BuildNest</Link>
            <span className="text-gray-300">/</span>
            <span className="text-gray-600 font-medium">Account</span>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/cart" className="text-sm text-gray-500 hover:text-gray-800">🛒 Cart</Link>
            <button onClick={handleLogout} disabled={loggingOut}
              className="text-sm text-gray-500 hover:text-red-600 disabled:opacity-60 transition-colors">
              {loggingOut ? 'Signing out…' : 'Sign out'}
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-8">
        <div className="mb-6 flex items-center gap-3">
          <div className="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center text-lg font-bold text-amber-700">
            {user?.username?.[0]?.toUpperCase() ?? '?'}
          </div>
          <div>
            <p className="font-semibold text-gray-900">@{user?.username}</p>
            <p className="text-sm text-gray-400">Member account</p>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row gap-6">
          {/* Sidebar nav */}
          <nav className="sm:w-48 flex-shrink-0">
            <ul className="space-y-1">
              {TABS.map(tab => (
                <li key={tab.id}>
                  <button
                    onClick={() => setActiveTab(tab.id)}
                    className={`w-full text-left flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                      activeTab === tab.id
                        ? 'bg-amber-500 text-white'
                        : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    <span>{tab.icon}</span>
                    {tab.label}
                  </button>
                </li>
              ))}
            </ul>
          </nav>

          {/* Tab content */}
          <div className="flex-1 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 min-h-[400px]">
            {activeTab === 'profile'   && token && <ProfileTab token={token} />}
            {activeTab === 'orders'    && token && user && <OrdersTab token={token} userId={user.id} />}
            {activeTab === 'addresses' && <AddressesTab />}
            {activeTab === 'wishlist'  && token && user && <WishlistTab token={token} userId={user.id} />}
            {activeTab === 'security'  && token && user && <SecurityTab token={token} userId={user.id} />}
          </div>
        </div>
      </main>
    </div>
  );
}
