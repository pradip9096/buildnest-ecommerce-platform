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
  token?: string;
  body?: unknown;
}

type FallbackMessage = string | ((status: number) => string);

function resolveFallback(fallback: FallbackMessage | undefined, status: number): string {
  if (typeof fallback === 'function') return fallback(status);
  return fallback ?? `Request failed (${status})`;
}

function buildHeaders(token: string | undefined, hasBody: boolean, extra?: HeadersInit): HeadersInit {
  const headers: Record<string, string> = {};
  if (hasBody) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  return { ...headers, ...extra };
}

/**
 * Sends a request and returns the raw parsed JSON body.
 * Throws `ApiError` on a non-2xx response, preferring the backend's own
 * `ApiResponse.message` when the error body includes one.
 */
export async function request<T>(
  path: string,
  options: RequestOptions = {},
  fallbackMessage?: FallbackMessage
): Promise<T> {
  const { token, body, headers, ...rest } = options;
  const hasBody = body !== undefined;

  const res = await fetch(path, {
    ...rest,
    headers: buildHeaders(token, hasBody, headers),
    body: hasBody ? JSON.stringify(body) : undefined,
  });

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
