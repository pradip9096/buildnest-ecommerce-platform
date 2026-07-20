import { useEffect, useRef, useState, useCallback } from 'react';

export interface OrderStatusNotification {
  id: string;
  orderId: number;
  previousStatus: string;
  newStatus: string;
  receivedAt: number;
}

interface UseNotificationsResult {
  notifications: OrderStatusNotification[];
  unreadCount: number;
  markAllRead: () => void;
}

interface OrderStatusEventPayload {
  orderId: number;
  previousStatus: string;
  newStatus: string;
}

/**
 * Consumes the backend's in-app notification SSE stream
 * (GET /api/user/notifications/stream, NOTIF-02, #63) while the user is
 * authenticated. Auth travels via the httpOnly JWT cookie — EventSource's
 * withCredentials mirrors this app's `fetch(..., { credentials: 'include' })`
 * convention (see api/client.ts), no Authorization header is needed or
 * possible on EventSource.
 */
export function useNotifications(isAuthenticated: boolean): UseNotificationsResult {
  const [notifications, setNotifications] = useState<OrderStatusNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      eventSourceRef.current?.close();
      eventSourceRef.current = null;
      setNotifications([]);
      setUnreadCount(0);
      return;
    }

    const eventSource = new EventSource('/api/user/notifications/stream', { withCredentials: true });
    eventSourceRef.current = eventSource;

    eventSource.addEventListener('order-status', (event: MessageEvent<string>) => {
      const payload = JSON.parse(event.data) as OrderStatusEventPayload;
      const notification: OrderStatusNotification = {
        id: `${payload.orderId}-${Date.now()}`,
        orderId: payload.orderId,
        previousStatus: payload.previousStatus,
        newStatus: payload.newStatus,
        receivedAt: Date.now(),
      };
      setNotifications(prev => [notification, ...prev].slice(0, 20));
      setUnreadCount(count => count + 1);
    });

    // EventSource auto-reconnects on transient errors by default; nothing to
    // do here beyond letting the browser's built-in retry take over.
    eventSource.onerror = () => {};

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, [isAuthenticated]);

  const markAllRead = useCallback(() => setUnreadCount(0), []);

  return { notifications, unreadCount, markAllRead };
}
