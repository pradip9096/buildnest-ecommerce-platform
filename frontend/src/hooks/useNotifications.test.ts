import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useNotifications } from './useNotifications';

type Listener = (event: MessageEvent<string>) => void;

class MockEventSource {
  static instances: MockEventSource[] = [];
  url: string;
  withCredentials: boolean;
  closed = false;
  listeners = new Map<string, Listener[]>();
  onerror: (() => void) | null = null;

  constructor(url: string, options?: { withCredentials?: boolean }) {
    this.url = url;
    this.withCredentials = options?.withCredentials ?? false;
    MockEventSource.instances.push(this);
  }

  addEventListener(type: string, listener: Listener): void {
    const existing = this.listeners.get(type) ?? [];
    existing.push(listener);
    this.listeners.set(type, existing);
  }

  close(): void {
    this.closed = true;
  }

  emit(type: string, data: unknown): void {
    for (const listener of this.listeners.get(type) ?? []) {
      listener({ data: JSON.stringify(data) } as MessageEvent<string>);
    }
  }
}

const originalEventSource = globalThis.EventSource;

beforeEach(() => {
  MockEventSource.instances = [];
  // @ts-expect-error -- test double, not spec-complete
  globalThis.EventSource = MockEventSource;
});

afterEach(() => {
  globalThis.EventSource = originalEventSource;
});

describe('useNotifications', () => {
  it('does not open a connection when unauthenticated', () => {
    renderHook(() => useNotifications(false));
    expect(MockEventSource.instances).toHaveLength(0);
  });

  it('opens a withCredentials connection to the stream endpoint when authenticated', () => {
    renderHook(() => useNotifications(true));

    expect(MockEventSource.instances).toHaveLength(1);
    expect(MockEventSource.instances[0].url).toBe('/api/user/notifications/stream');
    expect(MockEventSource.instances[0].withCredentials).toBe(true);
  });

  it('appends an order-status event to notifications and increments unread count', () => {
    const { result } = renderHook(() => useNotifications(true));
    const source = MockEventSource.instances[0];

    act(() => {
      source.emit('order-status', { orderId: 7, previousStatus: 'PENDING', newStatus: 'SHIPPED' });
    });

    expect(result.current.notifications).toHaveLength(1);
    expect(result.current.notifications[0]).toMatchObject({ orderId: 7, previousStatus: 'PENDING', newStatus: 'SHIPPED' });
    expect(result.current.unreadCount).toBe(1);
  });

  it('markAllRead resets unread count without clearing notifications', () => {
    const { result } = renderHook(() => useNotifications(true));
    const source = MockEventSource.instances[0];

    act(() => {
      source.emit('order-status', { orderId: 1, previousStatus: 'PENDING', newStatus: 'CONFIRMED' });
    });
    expect(result.current.unreadCount).toBe(1);

    act(() => {
      result.current.markAllRead();
    });

    expect(result.current.unreadCount).toBe(0);
    expect(result.current.notifications).toHaveLength(1);
  });

  it('closes the connection and clears state on logout', () => {
    const { result, rerender } = renderHook(({ authed }) => useNotifications(authed), {
      initialProps: { authed: true },
    });
    const source = MockEventSource.instances[0];

    act(() => {
      source.emit('order-status', { orderId: 1, previousStatus: 'PENDING', newStatus: 'CONFIRMED' });
    });
    expect(result.current.notifications).toHaveLength(1);

    rerender({ authed: false });

    expect(source.closed).toBe(true);
    expect(result.current.notifications).toHaveLength(0);
    expect(result.current.unreadCount).toBe(0);
  });

  it('closes the connection on unmount', () => {
    const { unmount } = renderHook(() => useNotifications(true));
    const source = MockEventSource.instances[0];

    unmount();

    expect(source.closed).toBe(true);
  });
});
