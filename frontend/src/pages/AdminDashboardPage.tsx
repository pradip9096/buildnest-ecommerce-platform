import { useState } from 'react';
import { OverviewTab } from '../components/admin/OverviewTab';
import { OrdersTab } from '../components/admin/OrdersTab';
import { InventoryTab } from '../components/admin/InventoryTab';
import { UsersTab } from '../components/admin/UsersTab';
import { AuditLogTab } from '../components/admin/AuditLogTab';
import { CategoriesTab } from '../components/admin/CategoriesTab';
import { ProductsTab } from '../components/admin/ProductsTab';
import { TagsTab } from '../components/admin/TagsTab';
import { CouponsTab } from '../components/admin/CouponsTab';
import { ShippingMethodsTab } from '../components/admin/ShippingMethodsTab';
import { WebhookSubscriptionsTab } from '../components/admin/WebhookSubscriptionsTab';
import { SalesAnalyticsTab } from '../components/admin/SalesAnalyticsTab';
import { InventoryAnalyticsTab } from '../components/admin/InventoryAnalyticsTab';
import { InventoryThresholdsTab } from '../components/admin/InventoryThresholdsTab';

type Tab = 'overview' | 'analytics' | 'orders' | 'inventory' | 'inventory-analytics' | 'inventory-thresholds' | 'products' | 'categories' | 'tags' | 'coupons' | 'shipping' | 'webhooks' | 'users' | 'audit';

const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'overview',   label: 'Overview',   icon: '📊' },
  { id: 'analytics',  label: 'Analytics',  icon: '📈' },
  { id: 'orders',     label: 'Orders',     icon: '🛒' },
  { id: 'inventory',  label: 'Inventory',  icon: '📦' },
  { id: 'inventory-analytics', label: 'Inventory Analytics', icon: '📉' },
  { id: 'inventory-thresholds', label: 'Inventory Thresholds', icon: '🚨' },
  { id: 'products',   label: 'Products',   icon: '🏷️' },
  { id: 'categories', label: 'Categories', icon: '🗂️' },
  { id: 'tags',       label: 'Tags',       icon: '🔖' },
  { id: 'coupons',    label: 'Coupons',    icon: '🎟️' },
  { id: 'shipping',   label: 'Shipping',   icon: '🚚' },
  { id: 'webhooks',   label: 'Webhooks',   icon: '🔗' },
  { id: 'users',      label: 'Users',      icon: '👥' },
  { id: 'audit',      label: 'Audit Log',  icon: '🔍' },
];

export function AdminDashboardPage() {
  const [activeTab, setActiveTab] = useState<Tab>('overview');

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="mb-6 flex items-center gap-2">
          <h1 className="text-lg font-semibold text-gray-900">Admin Dashboard</h1>
          <span className="px-2 py-0.5 bg-red-100 text-red-700 rounded-full text-xs font-bold uppercase">Admin</span>
        </div>
        <div className="flex flex-col sm:flex-row gap-6">
          <nav className="sm:w-52 flex-shrink-0">
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide px-3 mb-2">Dashboard</p>
            <ul className="space-y-1">
              {TABS.map(tab => (
                <li key={tab.id}>
                  <button
                    type="button"
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

          <div className="flex-1 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 min-h-[500px]">
            {activeTab === 'overview'  && <OverviewTab />}
            {activeTab === 'analytics' && <SalesAnalyticsTab />}
            {activeTab === 'orders'    && <OrdersTab />}
            {activeTab === 'inventory' && <InventoryTab />}
            {activeTab === 'inventory-analytics' && <InventoryAnalyticsTab />}
            {activeTab === 'inventory-thresholds' && <InventoryThresholdsTab />}
            {activeTab === 'products' && <ProductsTab />}
            {activeTab === 'categories' && <CategoriesTab />}
            {activeTab === 'tags'       && <TagsTab />}
            {activeTab === 'coupons'    && <CouponsTab />}
            {activeTab === 'shipping'   && <ShippingMethodsTab />}
            {activeTab === 'webhooks'   && <WebhookSubscriptionsTab />}
            {activeTab === 'users'     && <UsersTab />}
            {activeTab === 'audit'     && <AuditLogTab />}
          </div>
        </div>
      </main>
    </div>
  );
}
