import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';

afterEach(() => {
  cleanup();
});

// jsdom has no EventSource implementation. A minimal stub lets any component
// that mounts one (e.g. NotificationBell via useNotifications) render without
// crashing in tests that aren't specifically exercising SSE behavior — those
// tests mock EventSource themselves with finer control.
class EventSourceStub {
  static readonly CONNECTING = 0;
  static readonly OPEN = 1;
  static readonly CLOSED = 2;
  onerror: ((this: EventSource, ev: Event) => void) | null = null;
  onmessage: ((this: EventSource, ev: MessageEvent) => void) | null = null;
  onopen: ((this: EventSource, ev: Event) => void) | null = null;
  readyState = EventSourceStub.CONNECTING;
  addEventListener(): void {}
  removeEventListener(): void {}
  close(): void {}
  dispatchEvent(): boolean {
    return true;
  }
}

if (typeof globalThis.EventSource === 'undefined') {
  // @ts-expect-error -- test-only stub, not a spec-complete EventSource
  globalThis.EventSource = EventSourceStub;
}
