import { useState } from 'react';
import { Link } from 'react-router-dom';
import { submitReview } from '../../api/reviews';
import { ApiError } from '../../api/client';

type Props = {
  productId: number;
  isAuthenticated: boolean;
  onSubmitted: () => void;
};

function StarRatingInput({ value, onChange }: { value: number; onChange: (rating: number) => void }) {
  const [hovered, setHovered] = useState(0);
  const display = hovered || value;

  return (
    <div className="flex gap-1" role="radiogroup" aria-label="Rating">
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

export function WriteReviewForm({ productId, isAuthenticated, onSubmitted }: Props) {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  if (!isAuthenticated) {
    return (
      <div className="mt-8 p-4 bg-gray-50 border border-gray-200 rounded-xl text-sm text-gray-600">
        <Link to="/login" className="text-primary-600 font-medium hover:underline">Sign in</Link> to write a review.
      </div>
    );
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (rating === 0) {
      setError('Please select a star rating.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await submitReview(productId, rating, comment.trim());
      setSuccess(true);
      setRating(0);
      setComment('');
      onSubmitted();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to submit review. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-8 p-6 bg-gray-50 border border-gray-200 rounded-2xl" noValidate>
      <h3 className="text-base font-semibold text-gray-900 mb-4">Write a Review</h3>

      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-1">Your rating</label>
        <StarRatingInput value={rating} onChange={r => { setRating(r); setSuccess(false); }} />
      </div>

      <div className="mb-4">
        <label htmlFor="review-comment" className="block text-sm font-medium text-gray-700 mb-1">
          Your review (optional)
        </label>
        <textarea
          id="review-comment"
          value={comment}
          onChange={e => { setComment(e.target.value); setSuccess(false); }}
          maxLength={2000}
          rows={4}
          placeholder="Share your experience with this product…"
          className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 resize-none"
        />
      </div>

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      {success && (
        <p className="mb-4 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
          Thanks — your review has been submitted.
        </p>
      )}

      <button
        type="submit"
        disabled={submitting}
        className="bg-primary-600 hover:bg-primary-700 disabled:opacity-60 text-white font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm"
      >
        {submitting ? 'Submitting…' : 'Submit Review'}
      </button>
    </form>
  );
}
