import { requestData } from './client';
import type { PagedResponse, Review, ReviewSummary } from '../types';

export async function fetchReviews(
  productId: number,
  page = 0,
  size = 5,
): Promise<PagedResponse<Review>> {
  const params = new URLSearchParams({ page: String(page), size: String(size), sortBy: 'createdAt', direction: 'DESC' });
  return requestData<PagedResponse<Review>>(
    `/api/products/${productId}/reviews?${params}`,
    {},
    s => `Failed to fetch reviews: ${s}`
  );
}

export async function fetchReviewSummary(productId: number): Promise<ReviewSummary> {
  return requestData<ReviewSummary>(
    `/api/products/${productId}/reviews/summary`,
    {},
    s => `Failed to fetch review summary: ${s}`
  );
}

export async function submitReview(
  productId: number,
  rating: number,
  comment: string,
): Promise<Review> {
  return requestData<Review>(
    `/api/products/${productId}/reviews`,
    { method: 'POST', body: { rating, comment } },
    s => `Failed to submit review (${s})`
  );
}
