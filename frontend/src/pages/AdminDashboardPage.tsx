import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { OverviewTab } from '../components/admin/OverviewTab';
import { OrdersTab } from '../components/admin/OrdersTab';
import { InventoryTab } from '../components/admin/InventoryTab';
import { UsersTab } from '../components/admin/UsersTab';
import { AuditLogTab } from '../components/admin/AuditLogTab';

type Tab = 'overview' | 'orders' | 'inventory' | 'users' | 'audit';

const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'overview',   label: 'Overview',   icon: '📊' },
  { id: 'orders',     label: 'Orders',     icon: '🛒' },
  { id: 'inventory',  label: 'Inventory',  icon: '📦' },
  { id: 'users',      label: 'Users',      icon: '👥' },
  { id: 'audit',      label: 'Audit Log',  icon: '🔍' },
];

export function AdminDashboardPage() {
  const { user, token, isAuthenticated, loading, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<Tab>('overview');
  const [loggingOut, setLoggingOut] = useState(false);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-amber-400 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const isAdmin = isAuthenticated && (user?.roles?.includes('ADMIN') ?? false);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🔒</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Sign in required</h1>
          <Link to="/login" state={{ from: '/admin' }}
            className="inline-block mt-4 bg-amber-500 hover:bg-amber-600 text-white font-semibold px-6 py-3 rounded-xl transition-colors">
            Sign in
          </Link>
        </div>
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🚫</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Access denied</h1>
          <p className="text-gray-500 text-sm mb-4">Admin role required.</p>
          <Link to="/" className="text-amber-600 hover:text-amber-800 text-sm font-medium">← Back to store</Link>
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
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/" className="text-2xl font-bold text-amber-600">🏗️ BuildNest</Link>
            <span className="text-gray-300">/</span>
            <span className="text-gray-700 font-semibold">Admin</span>
            <span className="ml-1 px-2 py-0.5 bg-red-100 text-red-700 rounded-full text-xs font-bold uppercase">Admin</span>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/" className="text-sm text-gray-500 hover:text-gray-800">View store</Link>
            <span className="text-sm text-gray-400">@{user?.username}</span>
            <button onClick={handleLogout} disabled={loggingOut}
              className="text-sm text-gray-500 hover:text-red-600 disabled:opacity-60 transition-colors">
              {loggingOut ? 'Signing out…' : 'Sign out'}
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex flex-col sm:flex-row gap-6">
          <nav className="sm:w-52 flex-shrink-0">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide px-3 mb-2">Dashboard</p>
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

          <div className="flex-1 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 min-h-[500px]">
            {token && (
              <>
                {activeTab === 'overview'  && <OverviewTab  token={token} />}
                {activeTab === 'orders'    && <OrdersTab    token={token} />}
                {activeTab === 'inventory' && <InventoryTab token={token} />}
                {activeTab === 'users'     && <UsersTab     token={token} />}
                {activeTab === 'audit'     && <AuditLogTab  token={token} />}
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
