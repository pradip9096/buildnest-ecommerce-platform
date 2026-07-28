import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SellerReviewPanel } from './SellerReviewPanel';
import { submitSellerReview, fetchSellerReviews, fetchSellerReviewSummary } from '../../api/sellerReviews';
import { ApiError } from '../../api/client';

vi.mock('../../api/sellerReviews', () => ({
  submitSellerReview: vi.fn(),
  fetchSellerReviews: vi.fn(),
  fetchSellerReviewSummary: vi.fn(),
}));

const mockSubmit = vi.mocked(submitSellerReview);
const mockFetchReviews = vi.mocked(fetchSellerReviews);
const mockFetchSummary = vi.mocked(fetchSellerReviewSummary);

beforeEach(() => {
  vi.clearAllMocks();
  mockFetchReviews.mockResolvedValue({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 1 });
  mockFetchSummary.mockResolvedValue({ averageRating: 0, totalReviews: 0, ratingDistribution: {} });
});

describe('SellerReviewPanel', () => {
  it('requires a star rating before submitting', async () => {
    render(<SellerReviewPanel sellerId={7} />);
    const user = userEvent.setup();

    await user.click(screen.getByText('Submit Rating'));

    expect(screen.getByText('Please select a star rating.')).toBeInTheDocument();
    expect(mockSubmit).not.toHaveBeenCalled();
  });

  it('submits rating and comment, shows success', async () => {
    mockSubmit.mockResolvedValue({ id: 1, rating: 5, comment: 'Fast shipping!', helpfulCount: 0, createdAt: '2026-07-28T00:00:00' });
    render(<SellerReviewPanel sellerId={7} />);
    const user = userEvent.setup();

    await user.click(screen.getByLabelText('5 stars'));
    await user.type(screen.getByPlaceholderText('Share your experience with this seller…'), 'Fast shipping!');
    await user.click(screen.getByText('Submit Rating'));

    await waitFor(() => expect(mockSubmit).toHaveBeenCalledWith(7, 5, 'Fast shipping!'));
    expect(screen.getByText('Thanks — your seller review has been submitted.')).toBeInTheDocument();
  });

  it('surfaces the backend\'s duplicate-review error message verbatim', async () => {
    mockSubmit.mockRejectedValue(new ApiError('User has already reviewed this seller', 400));
    render(<SellerReviewPanel sellerId={7} />);
    const user = userEvent.setup();

    await user.click(screen.getByLabelText('4 stars'));
    await user.click(screen.getByText('Submit Rating'));

    await waitFor(() =>
      expect(screen.getByText('User has already reviewed this seller')).toBeInTheDocument()
    );
  });

  it('shows the average rating summary when reviews already exist', async () => {
    mockFetchSummary.mockResolvedValue({ averageRating: 4.5, totalReviews: 12, ratingDistribution: { '5': 8, '4': 4 } });
    render(<SellerReviewPanel sellerId={7} />);

    await waitFor(() => expect(screen.getByText('4.5 (12)')).toBeInTheDocument());
  });
});
