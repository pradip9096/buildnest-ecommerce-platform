import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { InventoryAnalyticsTab } from './InventoryAnalyticsTab';
import {
  fetchInventoryHighDemandLowStock,
  fetchInventorySeasonalPatterns,
  fetchInventoryStockTurnover,
  fetchInventoryRestockingPlan,
  type InventoryDemandProduct,
  type InventorySeasonalPattern,
  type InventoryTurnoverProduct,
  type InventoryRestockingPlan,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchInventoryHighDemandLowStock: vi.fn(),
  fetchInventorySeasonalPatterns: vi.fn(),
  fetchInventoryStockTurnover: vi.fn(),
  fetchInventoryRestockingPlan: vi.fn(),
}));

const mockFetchDemand = vi.mocked(fetchInventoryHighDemandLowStock);
const mockFetchPatterns = vi.mocked(fetchInventorySeasonalPatterns);
const mockFetchTurnover = vi.mocked(fetchInventoryStockTurnover);
const mockFetchPlan = vi.mocked(fetchInventoryRestockingPlan);

const demandProducts: InventoryDemandProduct[] = [
  {
    productId: 1,
    productName: 'Cordless Drill',
    currentStock: 3,
    minimumThreshold: 10,
    shortfall: 7,
    demandScore: 12,
    riskLevel: 'CRITICAL',
    recommendedAction: 'Restock to 44 units',
  },
];

const patterns: InventorySeasonalPattern[] = [
  {
    productId: 2,
    productName: 'Paint Roller',
    totalBreaches: 6,
    breachFrequency: '0.20 breaches per day',
    pattern: 'HIGH_DEMAND',
    currentStock: 15,
    suggestedSafetyStock: 22,
  },
];

const turnover: InventoryTurnoverProduct[] = [
  {
    productId: 3,
    productName: 'Ceramic Tile',
    currentStock: 40,
    recentTransactions: 25,
    turnoverCategory: 'VERY_HIGH_TURNOVER',
    healthStatus: 'HEALTHY',
  },
];

const plan: InventoryRestockingPlan = {
  generatedAt: '2026-07-21T00:00:00',
  analysisPeriod: '2026-06-21T00:00:00 to 2026-07-21T00:00:00',
  urgentRestocks: demandProducts,
  urgentCount: 1,
  seasonalPatterns: patterns,
  patternCount: 1,
  stockAnalysis: turnover,
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('InventoryAnalyticsTab', () => {
  it('renders high-demand products, seasonal patterns and turnover analysis', async () => {
    mockFetchDemand.mockResolvedValue(demandProducts);
    mockFetchPatterns.mockResolvedValue(patterns);
    mockFetchTurnover.mockResolvedValue(turnover);

    render(<InventoryAnalyticsTab />);

    await waitFor(() => expect(screen.getByText('Cordless Drill')).toBeInTheDocument());
    expect(screen.getByText('CRITICAL')).toBeInTheDocument();
    expect(screen.getByText('Paint Roller')).toBeInTheDocument();
    expect(screen.getByText('Ceramic Tile')).toBeInTheDocument();
    expect(screen.getByText('HEALTHY')).toBeInTheDocument();
  });

  it('shows an error message when a section fetch fails', async () => {
    mockFetchDemand.mockRejectedValue(new Error('Failed to load high-demand low-inventory products'));
    mockFetchPatterns.mockResolvedValue([]);
    mockFetchTurnover.mockResolvedValue([]);

    render(<InventoryAnalyticsTab />);

    await waitFor(() =>
      expect(screen.getByText('Failed to load high-demand low-inventory products')).toBeInTheDocument()
    );
  });

  it('generates a predictive restocking plan on demand', async () => {
    mockFetchDemand.mockResolvedValue([]);
    mockFetchPatterns.mockResolvedValue([]);
    mockFetchTurnover.mockResolvedValue([]);
    mockFetchPlan.mockResolvedValue(plan);

    render(<InventoryAnalyticsTab />);
    await waitFor(() => expect(mockFetchDemand).toHaveBeenCalled());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Generate Plan' }));

    await waitFor(() => expect(screen.getByText('Urgent Restocks')).toBeInTheDocument());
    expect(screen.getAllByText('1')).toHaveLength(2);
    expect(mockFetchPlan).toHaveBeenCalledWith(30);
  });
});
