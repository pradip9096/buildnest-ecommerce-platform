import { useCallback, useEffect, useState, type DependencyList, type Dispatch, type SetStateAction } from 'react';

export interface UseAsyncResult<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  /** Re-runs the fetcher without waiting for a dependency to change. */
  reload: () => void;
  /** Escape hatch for optimistic local updates (e.g. removing an item after a mutation) without a full re-fetch. */
  setData: Dispatch<SetStateAction<T | null>>;
}

/**
 * Generalizes the fetch-in-useEffect + data/loading/error + cancellation-flag
 * pattern that was hand-written across ~15 hooks and components. `deps`
 * behaves exactly like a useEffect dependency array (re-runs `fetcher` when
 * any entry changes); `reload()` re-runs it on demand (e.g. a Retry button).
 */
export function useAsync<T>(fetcher: () => Promise<T>, deps: DependencyList): UseAsyncResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadIndex, setReloadIndex] = useState(0);

  const reload = useCallback(() => setReloadIndex(i => i + 1), []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    fetcher()
      .then(result => {
        if (!cancelled) setData(result);
      })
      .catch(err => {
        if (!cancelled) setError(err instanceof Error ? err.message : 'Unknown error');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, reloadIndex]);

  return { data, loading, error, reload, setData };
}
