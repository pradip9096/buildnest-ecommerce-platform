import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { InventoryThresholdsTab } from './InventoryThresholdsTab';
import {
  fetchInventoryBelowThreshold,
  fetchInventoryThresholdBreaches,
  fetchInventoryFrequentProblems,
  fetchInventoryReportSummary,
  setProductThreshold,
  type InventoryBelowThreshold,
  type InventoryThresholdBreach,
  type InventoryFrequentProblem,
  type InventorySummary,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchInventoryBelowThreshold: vi.fn(),
  fetchInventoryThresholdBreaches: vi.fn(),
  fetchInventoryFrequentProblems: vi.fn(),
  fetchInventoryReportSummary: vi.fn(),
  setProductThreshold: vi.fn(),
}));

const mockFetchBelowThreshold = vi.mocked(fetchInventoryBelowThreshold);
const mockFetchBreaches = vi.mocked(fetchInventoryThresholdBreaches);
const mockFetchFrequentProblems = vi.mocked(fetchInventoryFrequentProblems);
const mockFetchSummary = vi.mocked(fetchInventoryReportSummary);
const mockSetProductThreshold = vi.mocked(setProductThreshold);

const belowThreshold: InventoryBelowThreshold[] = [
  {
    productId: 1,
    productName: 'Cordless Drill',
    currentQuantity: 3,
    minimumThreshold: 10,
    shortfall: 7,
    status: 'Low Stock',
    lastBreach: '2026-07-20T00:00:00',
  },
];

const breaches: InventoryThresholdBreach[] = [
  {
    id: 1,
    productId: 1,
    productName: 'Cordless Drill',
    breachType: 'LOW_STOCK',
    currentQuantity: 3,
    thresholdLevel: 10,
    timestamp: '2026-07-20T00:00:00',
    details: 'Fell below minimum',
  },
];

const frequentProblems: InventoryFrequentProblem[] = [
  {
    productId: 2,
    productName: 'Paint Roller',
    breachCount: 5,
    latestBreach: '2026-07-19T00:00:00',
    currentStock: 12,
  },
];

const summary: InventorySummary = {
  totalProducts: 100,
  inStock: 80,
  lowStock: 15,
  outOfStock: 5,
  totalQuantityInStock: 5000,
  totalQuantityReserved: 200,
  totalAvailable: 4800,
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('InventoryThresholdsTab', () => {
  it('renders summary stats, below-threshold products, breaches and frequent problems', async () => {
    mockFetchSummary.mockResolvedValue(summary);
    mockFetchBelowThreshold.mockResolvedValue(belowThreshold);
    mockFetchBreaches.mockResolvedValue(breaches);
    mockFetchFrequentProblems.mockResolvedValue(frequentProblems);

    render(<InventoryThresholdsTab />);

    await waitFor(() => expect(screen.getAllByText('Cordless Drill').length).toBeGreaterThan(0));
    expect(screen.getByText('100')).toBeInTheDocument();
    expect(screen.getByText('Paint Roller')).toBeInTheDocument();
    expect(screen.getByText('5 breaches')).toBeInTheDocument();
  });

  it('shows an empty state when no products are below threshold', async () => {
    mockFetchSummary.mockResolvedValue(summary);
    mockFetchBelowThreshold.mockResolvedValue([]);
    mockFetchBreaches.mockResolvedValue([]);
    mockFetchFrequentProblems.mockResolvedValue([]);

    render(<InventoryThresholdsTab />);

    await waitFor(() =>
      expect(screen.getByText('No products currently below their threshold.')).toBeInTheDocument()
    );
  });

  it('shows an error message when a section fetch fails', async () => {
    mockFetchSummary.mockResolvedValue(summary);
    mockFetchBelowThreshold.mockRejectedValue(new Error('Failed to load below-threshold products'));
    mockFetchBreaches.mockResolvedValue([]);
    mockFetchFrequentProblems.mockResolvedValue([]);

    render(<InventoryThresholdsTab />);

    await waitFor(() =>
      expect(screen.getByText('Failed to load below-threshold products')).toBeInTheDocument()
    );
  });

  it('sets a product threshold and reloads the below-threshold list', async () => {
    mockFetchSummary.mockResolvedValue(summary);
    mockFetchBelowThreshold.mockResolvedValue(belowThreshold);
    mockFetchBreaches.mockResolvedValue([]);
    mockFetchFrequentProblems.mockResolvedValue([]);
    mockSetProductThreshold.mockResolvedValue(undefined);

    render(<InventoryThresholdsTab />);
    await waitFor(() => expect(screen.getAllByText('Cordless Drill').length).toBeGreaterThan(0));

    const user = userEvent.setup();
    const input = screen.getByPlaceholderText('10');
    await user.type(input, '15');
    await user.click(screen.getByRole('button', { name: 'Set' }));

    await waitFor(() => expect(mockSetProductThreshold).toHaveBeenCalledWith(1, 15));
    expect(mockFetchBelowThreshold).toHaveBeenCalledTimes(2);
  });
});
