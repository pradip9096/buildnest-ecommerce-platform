import { useEffect, useState, useCallback } from 'react';
import { fetchSellerReviews, fetchSellerReviewSummary } from '../api/sellerReviews';
import type { PagedResponse, SellerReview, SellerReviewSummary } from '../types';

interface UseSellerReviewsResult {
  reviews: SellerReview[];
  summary: SellerReviewSummary | null;
  totalPages: number;
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

export function useSellerReviews(sellerId: number, page: number, pageSize = 5): UseSellerReviewsResult {
  const [reviews, setReviews] = useState<SellerReview[]>([]);
  const [summary, setSummary] = useState<SellerReviewSummary | null>(null);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refetchToken, setRefetchToken] = useState(0);

  const refetch = useCallback(() => setRefetchToken(t => t + 1), []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    Promise.all([
      fetchSellerReviews(sellerId, page, pageSize),
      fetchSellerReviewSummary(sellerId),
    ])
      .then(([paged, sum]: [PagedResponse<SellerReview>, SellerReviewSummary]) => {
        if (!cancelled) {
          setReviews(paged.content ?? []);
          setTotalPages(paged.totalPages ?? 0);
          setSummary(sum);
        }
      })
      .catch(err => { if (!cancelled) setError(err instanceof Error ? err.message : 'Unknown error'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [sellerId, page, pageSize, refetchToken]);

  return { reviews, summary, totalPages, loading, error, refetch };
}
