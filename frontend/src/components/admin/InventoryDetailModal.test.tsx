import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { InventoryDetailModal } from './InventoryDetailModal';
import {
  fetchInventoryDetail,
  addStock,
  setStock,
  checkStockAvailability,
  type InventoryItem,
  type InventoryDetail,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchInventoryDetail: vi.fn(),
  addStock: vi.fn(),
  setStock: vi.fn(),
  checkStockAvailability: vi.fn(),
}));

const mockFetchDetail = vi.mocked(fetchInventoryDetail);
const mockAddStock = vi.mocked(addStock);
const mockSetStock = vi.mocked(setStock);
const mockCheckAvailability = vi.mocked(checkStockAvailability);

const item: InventoryItem = {
  productId: 7,
  productName: 'Premium Cement 50kg',
  quantity: 100,
  reservedQuantity: 10,
  availableQuantity: 90,
  status: 'IN_STOCK',
};

const detail: InventoryDetail = {
  id: 1,
  quantityInStock: 100,
  quantityReserved: 10,
  availableQuantity: 90,
  status: 'IN_STOCK',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('InventoryDetailModal', () => {
  it('loads and displays inventory detail', async () => {
    mockFetchDetail.mockResolvedValue(detail);

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={vi.fn()} />);

    await waitFor(() => expect(mockFetchDetail).toHaveBeenCalledWith(7));
    expect(await screen.findByText('100')).toBeInTheDocument();
    expect(screen.getByText('90')).toBeInTheDocument();
  });

  it('adds stock and calls onChanged', async () => {
    mockFetchDetail.mockResolvedValue(detail);
    mockAddStock.mockResolvedValue({ ...detail, quantityInStock: 150, availableQuantity: 140 });
    const onChanged = vi.fn();

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={onChanged} />);
    await waitFor(() => expect(mockFetchDetail).toHaveBeenCalled());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Quantity to add'), '50');
    await user.click(screen.getByRole('button', { name: 'Add Stock' }));

    await waitFor(() => expect(mockAddStock).toHaveBeenCalledWith(7, 50));
    await waitFor(() => expect(onChanged).toHaveBeenCalled());
    expect(await screen.findByText('150')).toBeInTheDocument();
  });

  it('sets stock to an exact quantity', async () => {
    mockFetchDetail.mockResolvedValue(detail);
    mockSetStock.mockResolvedValue({ ...detail, quantityInStock: 25, availableQuantity: 15 });
    const onChanged = vi.fn();

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={onChanged} />);
    await waitFor(() => expect(mockFetchDetail).toHaveBeenCalled());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Set exact quantity'), '25');
    await user.click(screen.getByRole('button', { name: 'Set Stock' }));

    await waitFor(() => expect(mockSetStock).toHaveBeenCalledWith(7, 25));
    await waitFor(() => expect(onChanged).toHaveBeenCalled());
  });

  it('checks availability and shows the result', async () => {
    mockFetchDetail.mockResolvedValue(detail);
    mockCheckAvailability.mockResolvedValue(true);

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={vi.fn()} />);
    await waitFor(() => expect(mockFetchDetail).toHaveBeenCalled());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Quantity to check'), '5');
    await user.click(screen.getByRole('button', { name: 'Check' }));

    await waitFor(() => expect(mockCheckAvailability).toHaveBeenCalledWith(7, 5));
    const result = await screen.findByText('Available', { selector: '.text-green-700' });
    expect(result).toBeInTheDocument();
  });

  it('shows an unavailable result distinctly', async () => {
    mockFetchDetail.mockResolvedValue(detail);
    mockCheckAvailability.mockResolvedValue(false);

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={vi.fn()} />);
    await waitFor(() => expect(mockFetchDetail).toHaveBeenCalled());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Quantity to check'), '500');
    await user.click(screen.getByRole('button', { name: 'Check' }));

    expect(await screen.findByText('Not available')).toBeInTheDocument();
  });

  it('rejects a negative quantity without calling the API', async () => {
    mockFetchDetail.mockResolvedValue(detail);

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={vi.fn()} />);
    await waitFor(() => expect(mockFetchDetail).toHaveBeenCalled());

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Quantity to add'), '-5');
    await user.click(screen.getByRole('button', { name: 'Add Stock' }));

    expect(screen.getByText('Enter a non-negative integer quantity.')).toBeInTheDocument();
    expect(mockAddStock).not.toHaveBeenCalled();
  });

  it('shows a fetch error', async () => {
    mockFetchDetail.mockRejectedValue(new Error('Failed to load inventory detail'));

    render(<InventoryDetailModal item={item} onClose={vi.fn()} onChanged={vi.fn()} />);

    expect(await screen.findByText('Failed to load inventory detail')).toBeInTheDocument();
  });
});
