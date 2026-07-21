import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SalesAnalyticsTab } from './SalesAnalyticsTab';
import {
  fetchSalesDashboard,
  fetchCustomerLifetimeValue,
  type SalesDashboard,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchSalesDashboard: vi.fn(),
  fetchCustomerLifetimeValue: vi.fn(),
}));

const mockFetchDashboard = vi.mocked(fetchSalesDashboard);
const mockFetchClv = vi.mocked(fetchCustomerLifetimeValue);

const dashboard: SalesDashboard = {
  dailyRevenue: 1200,
  weeklyRevenue: 8400,
  monthlyRevenue: 36000,
  yearlyRevenue: 400000,
  dailyOrders: 4,
  weeklyOrders: 28,
  monthlyOrders: 120,
  totalOrders: 900,
  averageOrderValue: 300,
  totalCustomers: 500,
  newCustomersThisMonth: 40,
  customerRetentionRate: 0.62,
  topSellingProducts: [
    { productId: 1, productName: 'Cordless Drill', unitsSold: 25, revenue: 12500 },
  ],
  revenueByCategory: { Tools: 15000, Lumber: 9000 },
  cartAbandonmentRate: 0.35,
  conversionRate: 0.045,
  revenueTrend: [
    { date: '2026-07-01', revenue: 1000, orderCount: 3 },
    { date: '2026-07-02', revenue: 1500, orderCount: 5 },
  ],
  startDate: '2026-07-01',
  endDate: '2026-07-20',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('SalesAnalyticsTab', () => {
  it('renders dashboard stats, trend, top products and category revenue', async () => {
    mockFetchDashboard.mockResolvedValue(dashboard);

    render(<SalesAnalyticsTab />);

    await waitFor(() => expect(screen.getByText('₹36,000')).toBeInTheDocument());
    expect(screen.getByText('4.5%')).toBeInTheDocument();
    expect(screen.getByText('35.0%')).toBeInTheDocument();
    expect(screen.getByText('Cordless Drill')).toBeInTheDocument();
    expect(screen.getByText('Tools')).toBeInTheDocument();
  });

  it('shows an error message when the dashboard fetch fails', async () => {
    mockFetchDashboard.mockRejectedValue(new Error('Failed to load sales dashboard'));

    render(<SalesAnalyticsTab />);

    await waitFor(() =>
      expect(screen.getByText('Failed to load sales dashboard')).toBeInTheDocument()
    );
  });

  it('looks up customer lifetime value by user ID', async () => {
    mockFetchDashboard.mockResolvedValue(dashboard);
    mockFetchClv.mockResolvedValue(45000);

    render(<SalesAnalyticsTab />);
    await waitFor(() => expect(screen.getByText('Cordless Drill')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('User ID'), '7');
    await user.click(screen.getByRole('button', { name: 'Lookup' }));

    await waitFor(() => expect(screen.getByText('₹45,000')).toBeInTheDocument());
    expect(mockFetchClv).toHaveBeenCalledWith(7);
  });

  it('shows a validation error for an invalid user ID', async () => {
    mockFetchDashboard.mockResolvedValue(dashboard);

    render(<SalesAnalyticsTab />);
    await waitFor(() => expect(screen.getByText('Cordless Drill')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Lookup' }));

    expect(screen.getByText('Enter a valid user ID')).toBeInTheDocument();
    expect(mockFetchClv).not.toHaveBeenCalled();
  });
});
