import { useState } from 'react';
import { submitSellerReview } from '../../api/sellerReviews';
import { ApiError } from '../../api/client';
import { StarRating } from '../product/StarRating';
import { useSellerReviews } from '../../hooks/useSellerReviews';

type Props = {
  sellerId: number;
};

function StarRatingInput({ value, onChange }: { value: number; onChange: (rating: number) => void }) {
  const [hovered, setHovered] = useState(0);
  const display = hovered || value;

  return (
    <div className="flex gap-1" role="radiogroup" aria-label="Seller rating">
      {[1, 2, 3, 4, 5].map(star => (
        <button
          key={star}
          type="button"
          role="radio"
          aria-checked={value === star}
          aria-label={`${star} star${star > 1 ? 's' : ''}`}
          onClick={() => onChange(star)}
          onMouseEnter={() => setHovered(star)}
          onMouseLeave={() => setHovered(0)}
          className="text-2xl leading-none focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-400 rounded"
        >
          <span className={star <= display ? 'text-amber-400' : 'text-gray-300'}>★</span>
        </button>
      ))}
    </div>
  );
}

/**
 * Buyer-to-seller rating panel (FR-SEL-07, #558) — shown from an order's
 * detail view once a seller is known (Order.sellerId, exposed by #558's
 * backend fix to OrderResponseDTO). Mirrors WriteReviewForm's shape,
 * scoped by sellerId instead of productId.
 */
export function SellerReviewPanel({ sellerId }: Props) {
  const { summary, refetch } = useSellerReviews(sellerId, 0, 1);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (rating === 0) {
      setError('Please select a star rating.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await submitSellerReview(sellerId, rating, comment.trim());
      setSuccess(true);
      setRating(0);
      setComment('');
      refetch();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to submit review. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mt-4 p-4 bg-gray-50 border border-gray-200 rounded-xl">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-gray-900">Rate this seller</h3>
        {summary && summary.totalReviews > 0 && (
          <div className="flex items-center gap-1.5">
            <StarRating rating={summary.averageRating} size="sm" />
            <span className="text-xs text-gray-500">
              {summary.averageRating.toFixed(1)} ({summary.totalReviews})
            </span>
          </div>
        )}
      </div>

      <form onSubmit={handleSubmit} noValidate>
        <div className="mb-3">
          <StarRatingInput value={rating} onChange={r => { setRating(r); setSuccess(false); }} />
        </div>

        <textarea
          value={comment}
          onChange={e => { setComment(e.target.value); setSuccess(false); }}
          maxLength={2000}
          rows={2}
          placeholder="Share your experience with this seller…"
          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 resize-none mb-3"
        />

        {error && (
          <p className="mb-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        {success && (
          <p className="mb-3 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
            Thanks — your seller review has been submitted.
          </p>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold px-5 py-2 rounded-xl transition-colors text-sm"
        >
          {submitting ? 'Submitting…' : 'Submit Rating'}
        </button>
      </form>
    </div>
  );
}
