import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CouponsTab } from './CouponsTab';
import {
  fetchAdminCoupons,
  createAdminCoupon,
  deactivateAdminCoupon,
  type AdminCoupon,
} from '../../api/admin';
import { ApiError } from '../../api/client';

vi.mock('../../api/admin', () => ({
  fetchAdminCoupons: vi.fn(),
  createAdminCoupon: vi.fn(),
  deactivateAdminCoupon: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminCoupons);
const mockCreate = vi.mocked(createAdminCoupon);
const mockDeactivate = vi.mocked(deactivateAdminCoupon);

const save10: AdminCoupon = {
  id: 1,
  code: 'SAVE10',
  discountType: 'PERCENTAGE',
  discountValue: 10,
  minOrderValue: 0,
  usageLimit: null,
  usageCount: 3,
  expiresAt: null,
  isActive: true,
};

const bigOrder: AdminCoupon = {
  id: 2,
  code: 'BIGORDER',
  discountType: 'FIXED',
  discountValue: 500,
  minOrderValue: 2000,
  usageLimit: 100,
  usageCount: 10,
  expiresAt: null,
  isActive: true,
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('CouponsTab', () => {
  it('renders the coupon list', async () => {
    mockFetch.mockResolvedValue([save10, bigOrder]);

    render(<CouponsTab />);

    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());
    expect(screen.getByText('BIGORDER')).toBeInTheDocument();
    expect(screen.getByText('10%')).toBeInTheDocument();
    expect(screen.getByText('₹500')).toBeInTheDocument();
  });

  it('filters the list via search', async () => {
    mockFetch.mockResolvedValue([save10, bigOrder]);

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    await userEvent.setup().type(screen.getByPlaceholderText('Search coupons…'), 'BIG');

    expect(screen.queryByText('SAVE10')).not.toBeInTheDocument();
    expect(screen.getByText('BIGORDER')).toBeInTheDocument();
  });

  it('shows the empty state when there are no coupons', async () => {
    mockFetch.mockResolvedValue([]);

    render(<CouponsTab />);

    await waitFor(() => expect(screen.getByText('No coupons found')).toBeInTheDocument());
  });

  it('creates a coupon via the modal and adds it to the list', async () => {
    mockFetch.mockResolvedValue([save10]);
    mockCreate.mockResolvedValue({
      id: 3,
      code: 'WELCOME20',
      discountType: 'PERCENTAGE',
      discountValue: 20,
      minOrderValue: 0,
      usageLimit: null,
      usageCount: 0,
      expiresAt: null,
      isActive: true,
    });

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Coupon' }));
    await user.type(screen.getByLabelText('Code'), 'WELCOME20');
    await user.type(screen.getByLabelText('Discount %'), '20');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(mockCreate).toHaveBeenCalled());
    await waitFor(() => expect(screen.getByText('WELCOME20')).toBeInTheDocument());
  });

  it('rejects a code shorter than 2 characters without calling the API', async () => {
    mockFetch.mockResolvedValue([save10]);

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Coupon' }));
    await user.type(screen.getByLabelText('Code'), 'A');
    await user.type(screen.getByLabelText('Discount %'), '10');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Coupon code must be at least 2 characters.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('rejects a percentage discount above 100 without calling the API', async () => {
    mockFetch.mockResolvedValue([save10]);

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Coupon' }));
    await user.type(screen.getByLabelText('Code'), 'TOOMUCH');
    await user.type(screen.getByLabelText('Discount %'), '150');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Percentage discount cannot exceed 100.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('surfaces the backend error message when create fails (e.g. duplicate code)', async () => {
    mockFetch.mockResolvedValue([save10]);
    mockCreate.mockRejectedValue(new ApiError('A coupon with code SAVE10 already exists', 400));

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Coupon' }));
    await user.type(screen.getByLabelText('Code'), 'SAVE10');
    await user.type(screen.getByLabelText('Discount %'), '10');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() =>
      expect(screen.getByText('A coupon with code SAVE10 already exists')).toBeInTheDocument()
    );
  });

  it('surfaces the backend error message when deactivate fails', async () => {
    mockFetch.mockResolvedValue([save10]);
    mockDeactivate.mockRejectedValue(new ApiError('Coupon not found', 404));

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Deactivate' }));

    await waitFor(() => expect(screen.getByText('Coupon not found')).toBeInTheDocument());
    expect(screen.getByText('SAVE10')).toBeInTheDocument();
  });

  it('deactivates a coupon successfully and updates its status', async () => {
    mockFetch.mockResolvedValue([save10]);
    mockDeactivate.mockResolvedValue({ ...save10, isActive: false });

    render(<CouponsTab />);
    await waitFor(() => expect(screen.getByText('SAVE10')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Deactivate' }));

    await waitFor(() => expect(mockDeactivate).toHaveBeenCalledWith(1));
    await waitFor(() => expect(screen.getByText('Inactive')).toBeInTheDocument());
    expect(screen.queryByRole('button', { name: 'Deactivate' })).not.toBeInTheDocument();
  });
});
