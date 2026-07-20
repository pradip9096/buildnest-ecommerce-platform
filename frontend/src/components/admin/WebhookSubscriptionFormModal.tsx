import { useState } from 'react';
import {
  createAdminWebhookSubscription,
  type AdminWebhookSubscription,
  type WebhookSubscriptionFormInput,
} from '../../api/admin';

interface Props {
  onClose: () => void;
  onSaved: (subscription: AdminWebhookSubscription) => void;
}

export function WebhookSubscriptionFormModal({ onClose, onSaved }: Props) {
  const [eventType, setEventType] = useState('');
  const [targetUrl, setTargetUrl] = useState('');
  const [secret, setSecret] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedEventType = eventType.trim();
    const trimmedTargetUrl = targetUrl.trim();

    if (trimmedEventType.length === 0) {
      setError('Event type is required.');
      return;
    }
    if (!/^https?:\/\/.+/.test(trimmedTargetUrl)) {
      setError('Target URL must be a valid http/https URL.');
      return;
    }

    setLoading(true);
    setError(null);

    const input: WebhookSubscriptionFormInput = {
      eventType: trimmedEventType,
      targetUrl: trimmedTargetUrl,
      secret: secret.trim() || undefined,
    };

    try {
      const saved = await createAdminWebhookSubscription(input);
      onSaved(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save webhook subscription');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">New Webhook Subscription</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
          <div>
            <label htmlFor="webhook-event-type" className="block text-sm font-medium text-gray-700 mb-1">Event Type</label>
            <input
              id="webhook-event-type"
              type="text"
              value={eventType}
              onChange={e => setEventType(e.target.value)}
              placeholder="e.g. order.placed"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              maxLength={100}
            />
          </div>

          <div>
            <label htmlFor="webhook-target-url" className="block text-sm font-medium text-gray-700 mb-1">Target URL</label>
            <input
              id="webhook-target-url"
              type="text"
              value={targetUrl}
              onChange={e => setTargetUrl(e.target.value)}
              placeholder="https://example.com/webhooks/buildnest"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              maxLength={500}
            />
          </div>

          <div>
            <label htmlFor="webhook-secret" className="block text-sm font-medium text-gray-700 mb-1">Secret (optional)</label>
            <input
              id="webhook-secret"
              type="text"
              value={secret}
              onChange={e => setSecret(e.target.value)}
              placeholder="Shared secret for signature verification"
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              maxLength={255}
            />
          </div>

          {error && <p className="text-red-600 text-sm">{error}</p>}

          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 border border-gray-200 text-gray-700 rounded-xl py-2.5 text-sm font-medium hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-primary-500 hover:bg-primary-600 text-white rounded-xl py-2.5 text-sm font-semibold disabled:opacity-60 transition-colors"
            >
              {loading ? 'Saving…' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
