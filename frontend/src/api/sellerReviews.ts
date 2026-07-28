import { requestData } from './client';
import type { PagedResponse, SellerReview, SellerReviewSummary } from '../types';

export async function fetchSellerReviews(
  sellerId: number,
  page = 0,
  size = 5,
): Promise<PagedResponse<SellerReview>> {
  const params = new URLSearchParams({ page: String(page), size: String(size), sortBy: 'createdAt', direction: 'DESC' });
  return requestData<PagedResponse<SellerReview>>(
    `/api/sellers/${sellerId}/reviews?${params}`,
    {},
    s => `Failed to fetch seller reviews: ${s}`
  );
}

export async function fetchSellerReviewSummary(sellerId: number): Promise<SellerReviewSummary> {
  return requestData<SellerReviewSummary>(
    `/api/sellers/${sellerId}/reviews/summary`,
    {},
    s => `Failed to fetch seller rating summary: ${s}`
  );
}

export async function submitSellerReview(
  sellerId: number,
  rating: number,
  comment: string,
): Promise<SellerReview> {
  return requestData<SellerReview>(
    `/api/sellers/${sellerId}/reviews`,
    { method: 'POST', body: { rating, comment } },
    s => `Failed to submit seller review (${s})`
  );
}
