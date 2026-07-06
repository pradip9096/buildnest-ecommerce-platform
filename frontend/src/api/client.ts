import type { ApiResponse } from '../types';

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
}

type FallbackMessage = string | ((status: number) => string);

function resolveFallback(fallback: FallbackMessage | undefined, status: number): string {
  if (typeof fallback === 'function') return fallback(status);
  return fallback ?? `Request failed (${status})`;
}

/** Reads the non-httpOnly XSRF-TOKEN cookie set by the backend's double-submit CSRF repository. */
function getCsrfToken(): string | undefined {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : undefined;
}

function buildHeaders(method: string | undefined, hasBody: boolean, extra?: HeadersInit): HeadersInit {
  const headers: Record<string, string> = {};
  if (hasBody) headers['Content-Type'] = 'application/json';
  const isSafeMethod = !method || method === 'GET' || method === 'HEAD';
  if (!isSafeMethod) {
    const csrfToken = getCsrfToken();
    if (csrfToken) headers['X-XSRF-TOKEN'] = csrfToken;
  }
  return { ...headers, ...extra };
}

/**
 * Called on a 401 response. Should attempt a silent cookie-based token refresh
 * and return whether it succeeded (the caller is responsible for any resulting
 * logout on failure). Registered by `AuthContext` so this module never depends
 * on it directly.
 */
type UnauthorizedHandler = () => Promise<boolean>;

let unauthorizedHandler: UnauthorizedHandler | null = null;

export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler;
}

/**
 * Sends a request and returns the raw parsed JSON body.
 * Throws `ApiError` on a non-2xx response, preferring the backend's own
 * `ApiResponse.message` when the error body includes one.
 *
 * A 401 response triggers one silent refresh attempt (via the registered
 * unauthorized handler) and retry before the error is thrown.
 */
export async function request<T>(
  path: string,
  options: RequestOptions = {},
  fallbackMessage?: FallbackMessage,
  isRetry = false
): Promise<T> {
  const { body, headers, method, ...rest } = options;
  const hasBody = body !== undefined;

  const res = await fetch(path, {
    ...rest,
    method,
    credentials: 'include',
    headers: buildHeaders(method, hasBody, headers),
    body: hasBody ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && !isRetry && unauthorizedHandler) {
    const refreshed = await unauthorizedHandler();
    if (refreshed) {
      return request<T>(path, options, fallbackMessage, true);
    }
  }

  if (!res.ok) {
    const errorBody = (await res.json().catch(() => null)) as Partial<ApiResponse<unknown>> | null;
    throw new ApiError(errorBody?.message ?? resolveFallback(fallbackMessage, res.status), res.status);
  }

  if (res.status === 204) return undefined as T;

  return (await res.json().catch(() => undefined)) as T;
}

/**
 * Sends a request expecting the backend's standard `ApiResponse<T>` envelope
 * and returns the unwrapped `data`. Throws `ApiError` when the response is a
 * non-2xx status, or when the envelope's `success` flag is `false`.
 */
export async function requestData<T>(
  path: string,
  options: RequestOptions = {},
  fallbackMessage?: FallbackMessage
): Promise<T> {
  const body = await request<ApiResponse<T>>(path, options, fallbackMessage);
  if (!body.success) {
    throw new ApiError(body.message ?? resolveFallback(fallbackMessage, 0), 0);
  }
  return body.data;
}
