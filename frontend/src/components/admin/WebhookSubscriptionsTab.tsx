import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchAdminWebhookSubscriptions,
  deactivateAdminWebhookSubscription,
  deleteAdminWebhookSubscription,
  type AdminWebhookSubscription,
} from '../../api/admin';
import { WebhookSubscriptionFormModal } from './WebhookSubscriptionFormModal';

export function WebhookSubscriptionsTab() {
  const { data, loading, error, setData } = useAsync<AdminWebhookSubscription[]>(
    () => fetchAdminWebhookSubscriptions(),
    []
  );
  const subscriptions = data ?? [];
  const [search, setSearch] = useState('');
  const [pendingId, setPendingId] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);

  const filtered = search
    ? subscriptions.filter(s => s.eventType.toLowerCase().includes(search.toLowerCase()))
    : subscriptions;

  const handleDeactivate = async (subscription: AdminWebhookSubscription) => {
    if (!confirm(`Deactivate webhook subscription for "${subscription.eventType}"? Deliveries to it will stop.`)) return;
    setPendingId(subscription.id);
    setActionError(null);
    try {
      const updated = await deactivateAdminWebhookSubscription(subscription.id);
      setData(prev => (prev ?? []).map(s => (s.id === subscription.id ? updated : s)));
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to deactivate webhook subscription');
    } finally {
      setPendingId(null);
    }
  };

  const handleDelete = async (subscription: AdminWebhookSubscription) => {
    if (!confirm(`Delete webhook subscription for "${subscription.eventType}"? This cannot be undone.`)) return;
    setPendingId(subscription.id);
    setActionError(null);
    try {
      await deleteAdminWebhookSubscription(subscription.id);
      setData(prev => (prev ?? []).filter(s => s.id !== subscription.id));
    } catch (e) {
      setActionError(e instanceof Error ? e.message : 'Failed to delete webhook subscription');
    } finally {
      setPendingId(null);
    }
  };

  const handleSaved = (saved: AdminWebhookSubscription) => {
    setData(prev => [...(prev ?? []), saved]);
    setShowModal(false);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Webhook Subscriptions</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search event types…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{subscriptions.length} total</span>
          <button
            type="button"
            onClick={() => setShowModal(true)}
            className="bg-primary-500 hover:bg-primary-600 text-white rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors"
          >
            + New Subscription
          </button>
        </div>
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}
      {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Event Type</th>
              <th className="px-4 py-3">Target URL</th>
              <th className="px-4 py-3">Failures</th>
              <th className="px-4 py-3">Last Delivery</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(6)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No webhook subscriptions match your search' : 'No webhook subscriptions found'}
                </td>
              </tr>
            ) : filtered.map(subscription => (
              <tr key={subscription.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <div className="font-medium text-gray-900">{subscription.eventType}</div>
                </td>
                <td className="px-4 py-3 text-gray-500 max-w-xs truncate">{subscription.targetUrl}</td>
                <td className="px-4 py-3 text-gray-500">{subscription.failureCount}</td>
                <td className="px-4 py-3 text-gray-500">{subscription.lastDeliveryStatus ?? '—'}</td>
                <td className="px-4 py-3">
                  <span
                    className={`text-xs font-medium px-2 py-1 rounded-full ${
                      subscription.active
                        ? 'bg-green-100 text-green-700'
                        : 'bg-gray-100 text-gray-500'
                    }`}
                  >
                    {subscription.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    {subscription.active && (
                      <button
                        type="button"
                        onClick={() => handleDeactivate(subscription)}
                        disabled={pendingId === subscription.id}
                        className="text-xs font-medium text-primary-600 hover:text-primary-800 border border-primary-200 hover:border-primary-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                      >
                        {pendingId === subscription.id ? 'Deactivating…' : 'Deactivate'}
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={() => handleDelete(subscription)}
                      disabled={pendingId === subscription.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                    >
                      {pendingId === subscription.id ? 'Deleting…' : 'Delete'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <WebhookSubscriptionFormModal
          onClose={() => setShowModal(false)}
          onSaved={handleSaved}
        />
      )}
    </div>
  );
}
