import { useState } from 'react';
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
  const { user, token } = useAuth();
  const [activeTab, setActiveTab] = useState<Tab>('profile');

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-5xl mx-auto px-4 py-8">
        <div className="mb-6 flex items-center gap-3">
          <div className="w-12 h-12 rounded-full bg-primary-100 flex items-center justify-center text-lg font-bold text-primary-700">
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
                        ? 'bg-primary-500 text-white'
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
            {activeTab === 'addresses' && token && <AddressesTab token={token} />}
            {activeTab === 'wishlist'  && token && user && <WishlistTab token={token} userId={user.id} />}
            {activeTab === 'security'  && token && <SecurityTab token={token} />}
          </div>
        </div>
      </main>
    </div>
  );
}
