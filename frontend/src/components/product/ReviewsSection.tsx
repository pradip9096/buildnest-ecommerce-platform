import { StarRating } from './StarRating';
import type { Review, ReviewSummary } from '../../types';

interface Props {
  reviews: Review[];
  summary: ReviewSummary | null;
  totalPages: number;
  page: number;
  loading: boolean;
  onPageChange: (page: number) => void;
}

function ReviewerName(user?: Review['user']): string {
  if (!user) return 'Anonymous';
  if (user.firstName || user.lastName) return `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim();
  return user.username ?? 'Anonymous';
}

function RatingBar({ label, count, total }: { label: string; count: number; total: number }) {
  const pct = total > 0 ? (count / total) * 100 : 0;
  return (
    <div className="flex items-center gap-2 text-sm">
      <span className="w-6 text-right text-gray-600">{label}</span>
      <span className="text-amber-400 text-xs">★</span>
      <div className="flex-1 bg-gray-200 rounded-full h-2">
        <div className="bg-amber-400 h-2 rounded-full" style={{ width: `${pct}%` }} />
      </div>
      <span className="w-6 text-gray-500 text-xs">{count}</span>
    </div>
  );
}

export function ReviewsSection({ reviews, summary, totalPages, page, loading, onPageChange }: Props) {
  return (
    <section className="mt-12">
      <h2 className="text-xl font-bold text-gray-900 mb-6">Customer Reviews</h2>

      {summary && summary.totalReviews > 0 && (
        <div className="flex flex-col sm:flex-row gap-8 mb-8 p-6 bg-gray-50 rounded-2xl border border-gray-200">
          <div className="flex flex-col items-center justify-center gap-1 min-w-[120px]">
            <span className="text-5xl font-bold text-gray-900">{summary.averageRating.toFixed(1)}</span>
            <StarRating rating={summary.averageRating} size="lg" />
            <span className="text-sm text-gray-500">{summary.totalReviews} review{summary.totalReviews !== 1 ? 's' : ''}</span>
          </div>
          <div className="flex-1 flex flex-col justify-center gap-2">
            {[5, 4, 3, 2, 1].map(star => (
              <RatingBar
                key={star}
                label={String(star)}
                count={Number(summary.ratingDistribution?.[star] ?? 0)}
                total={summary.totalReviews}
              />
            ))}
          </div>
        </div>
      )}

      {loading && (
        <div className="space-y-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="animate-pulse p-4 border border-gray-100 rounded-xl">
              <div className="h-3 bg-gray-200 rounded w-24 mb-2" />
              <div className="h-3 bg-gray-200 rounded w-full mb-1" />
              <div className="h-3 bg-gray-200 rounded w-3/4" />
            </div>
          ))}
        </div>
      )}

      {!loading && reviews.length === 0 && (
        <p className="text-gray-500 text-sm">No reviews yet. Be the first to review this product.</p>
      )}

      {!loading && reviews.length > 0 && (
        <div className="space-y-6">
          {reviews.map(review => (
            <div key={review.id} className="border-b border-gray-100 pb-6 last:border-0">
              <div className="flex items-center gap-3 mb-2">
                <StarRating rating={review.rating} size="sm" />
                <span className="text-sm font-medium text-gray-900">{ReviewerName(review.user)}</span>
                <span className="text-xs text-gray-600">
                  {new Date(review.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
                </span>
              </div>
              <p className="text-sm text-gray-700 leading-relaxed">{review.comment}</p>
              {review.helpfulCount > 0 && (
                <p className="text-xs text-gray-600 mt-2">{review.helpfulCount} person{review.helpfulCount !== 1 ? 's' : ''} found this helpful</p>
              )}
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 mt-6">
          <button
            type="button"
            onClick={() => onPageChange(page - 1)}
            disabled={page === 0}
            className="px-4 py-2 text-sm rounded-lg border border-gray-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            Previous
          </button>
          <span className="text-sm text-gray-600">Page {page + 1} of {totalPages}</span>
          <button
            type="button"
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 text-sm rounded-lg border border-gray-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            Next
          </button>
        </div>
      )}
    </section>
  );
}
