import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { WriteReviewForm } from './WriteReviewForm';
import { submitReview } from '../../api/reviews';
import { ApiError } from '../../api/client';

vi.mock('../../api/reviews', () => ({
  submitReview: vi.fn(),
}));

const mockSubmit = vi.mocked(submitReview);

function renderForm(isAuthenticated = true, onSubmitted = vi.fn()) {
  return render(
    <MemoryRouter>
      <WriteReviewForm productId={1} isAuthenticated={isAuthenticated} onSubmitted={onSubmitted} />
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('WriteReviewForm', () => {
  it('prompts sign-in when not authenticated, and does not render a form', () => {
    renderForm(false);

    expect(screen.getByText('Sign in')).toBeInTheDocument();
    expect(screen.queryByRole('radiogroup')).not.toBeInTheDocument();
    expect(screen.queryByText('Submit Review')).not.toBeInTheDocument();
  });

  it('requires a star rating before submitting', async () => {
    renderForm();
    const user = userEvent.setup();

    await user.click(screen.getByText('Submit Review'));

    expect(screen.getByText('Please select a star rating.')).toBeInTheDocument();
    expect(mockSubmit).not.toHaveBeenCalled();
  });

  it('submits rating and comment, shows success, and refetches the list', async () => {
    mockSubmit.mockResolvedValue({ id: 99, rating: 5, comment: 'Great!', helpfulCount: 0, createdAt: '2026-07-19T00:00:00' });
    const onSubmitted = vi.fn();
    renderForm(true, onSubmitted);
    const user = userEvent.setup();

    await user.click(screen.getByLabelText('5 stars'));
    await user.type(screen.getByLabelText('Your review (optional)'), 'Great!');
    await user.click(screen.getByText('Submit Review'));

    await waitFor(() => expect(mockSubmit).toHaveBeenCalledWith(1, 5, 'Great!'));
    expect(onSubmitted).toHaveBeenCalledTimes(1);
    expect(screen.getByText('Thanks — your review has been submitted.')).toBeInTheDocument();
  });

  it('surfaces the backend\'s duplicate-review error message verbatim', async () => {
    mockSubmit.mockRejectedValue(new ApiError('You have already reviewed this product', 400));
    renderForm();
    const user = userEvent.setup();

    await user.click(screen.getByLabelText('4 stars'));
    await user.click(screen.getByText('Submit Review'));

    await waitFor(() =>
      expect(screen.getByText('You have already reviewed this product')).toBeInTheDocument()
    );
  });
});
