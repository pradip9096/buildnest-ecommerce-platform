import type { ApiResponse, PagedResponse, Review, ReviewSummary } from '../types';

export async function fetchReviews(
  productId: number,
  page = 0,
  size = 5,
): Promise<PagedResponse<Review>> {
  const params = new URLSearchParams({ page: String(page), size: String(size), sortBy: 'createdAt', direction: 'DESC' });
  const res = await fetch(`/api/products/${productId}/reviews?${params}`);
  if (!res.ok) throw new Error(`Failed to fetch reviews: ${res.status}`);
  const json: ApiResponse<PagedResponse<Review>> = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data;
}

export async function fetchReviewSummary(productId: number): Promise<ReviewSummary> {
  const res = await fetch(`/api/products/${productId}/reviews/summary`);
  if (!res.ok) throw new Error(`Failed to fetch review summary: ${res.status}`);
  const json: ApiResponse<ReviewSummary> = await res.json();
  if (!json.success) throw new Error(json.message);
  return json.data;
}
