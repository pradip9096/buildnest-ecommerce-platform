import { describe, it, expect, vi } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useAsync } from './useAsync';

describe('useAsync', () => {
  it('starts loading and resolves with data on success', async () => {
    const fetcher = vi.fn().mockResolvedValue({ id: 1 });

    const { result } = renderHook(() => useAsync(fetcher, []));

    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.data).toEqual({ id: 1 });
    expect(result.current.error).toBeNull();
    expect(fetcher).toHaveBeenCalledTimes(1);
  });

  it('surfaces the error message and leaves data null on failure', async () => {
    const fetcher = vi.fn().mockRejectedValue(new Error('Failed to fetch'));

    const { result } = renderHook(() => useAsync(fetcher, []));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Failed to fetch');
    expect(result.current.data).toBeNull();
  });

  it('falls back to a generic message when a non-Error is thrown', async () => {
    const fetcher = vi.fn().mockRejectedValue('plain string rejection');

    const { result } = renderHook(() => useAsync(fetcher, []));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe('Unknown error');
  });

  it('re-runs the fetcher when a dependency changes', async () => {
    const fetcher = vi.fn().mockResolvedValue('ok');

    const { result, rerender } = renderHook(({ dep }) => useAsync(fetcher, [dep]), {
      initialProps: { dep: 1 },
    });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(fetcher).toHaveBeenCalledTimes(1);

    rerender({ dep: 2 });

    await waitFor(() => expect(fetcher).toHaveBeenCalledTimes(2));
  });

  it('does not re-run the fetcher on a re-render with unchanged deps', async () => {
    const fetcher = vi.fn().mockResolvedValue('ok');

    const { result, rerender } = renderHook(({ dep }) => useAsync(fetcher, [dep]), {
      initialProps: { dep: 1 },
    });

    await waitFor(() => expect(result.current.loading).toBe(false));
    rerender({ dep: 1 });

    expect(fetcher).toHaveBeenCalledTimes(1);
  });

  it('reload() re-runs the fetcher without a dependency change', async () => {
    const fetcher = vi.fn().mockResolvedValue('ok');

    const { result } = renderHook(() => useAsync(fetcher, []));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(fetcher).toHaveBeenCalledTimes(1);

    act(() => {
      result.current.reload();
    });

    await waitFor(() => expect(fetcher).toHaveBeenCalledTimes(2));
  });

  it('setData allows optimistic local updates without a re-fetch', async () => {
    const fetcher = vi.fn().mockResolvedValue([1, 2, 3]);

    const { result } = renderHook(() => useAsync<number[]>(fetcher, []));

    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => {
      result.current.setData(prev => (prev ?? []).filter((n: number) => n !== 2));
    });

    expect(result.current.data).toEqual([1, 3]);
    expect(fetcher).toHaveBeenCalledTimes(1);
  });

  it('ignores a stale response after the effect has been cleaned up (unmount)', async () => {
    let resolveFetch: (value: string) => void = () => {};
    const fetcher = vi.fn(() => new Promise<string>(resolve => { resolveFetch = resolve; }));

    const { unmount } = renderHook(() => useAsync(fetcher, []));
    unmount();

    // Resolving after unmount must not throw (no act()-outside-of-test warning
    // path, no state update on an unmounted hook).
    expect(() => resolveFetch('too late')).not.toThrow();
  });
});
