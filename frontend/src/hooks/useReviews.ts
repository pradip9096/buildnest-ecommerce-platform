import { useEffect, useState } from 'react';
import { fetchReviews, fetchReviewSummary } from '../api/reviews';
import type { PagedResponse, Review, ReviewSummary } from '../types';

interface UseReviewsResult {
  reviews: Review[];
  summary: ReviewSummary | null;
  totalPages: number;
  loading: boolean;
  error: string | null;
}

export function useReviews(productId: number, page: number, pageSize = 5): UseReviewsResult {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [summary, setSummary] = useState<ReviewSummary | null>(null);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    Promise.all([
      fetchReviews(productId, page, pageSize),
      fetchReviewSummary(productId),
    ])
      .then(([paged, sum]: [PagedResponse<Review>, ReviewSummary]) => {
        if (!cancelled) {
          setReviews(paged.content ?? []);
          setTotalPages(paged.totalPages ?? 0);
          setSummary(sum);
        }
      })
      .catch(err => { if (!cancelled) setError(err instanceof Error ? err.message : 'Unknown error'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [productId, page, pageSize]);

  return { reviews, summary, totalPages, loading, error };
}
