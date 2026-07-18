import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductVariantsModal } from './ProductVariantsModal';
import {
  fetchProductVariants,
  createProductVariant,
  updateProductVariant,
  deleteProductVariant,
  type AdminProduct,
  type AdminProductVariant,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchProductVariants: vi.fn(),
  createProductVariant: vi.fn(),
  updateProductVariant: vi.fn(),
  deleteProductVariant: vi.fn(),
}));

const mockFetch = vi.mocked(fetchProductVariants);
const mockCreate = vi.mocked(createProductVariant);
const mockUpdate = vi.mocked(updateProductVariant);
const mockDelete = vi.mocked(deleteProductVariant);

const product: AdminProduct = {
  id: 1,
  name: 'Premium Cement 50kg',
  price: 499.99,
  isActive: true,
};

const variantA: AdminProductVariant = {
  id: 10,
  sku: 'CEM-50KG-RED',
  size: '50kg',
  colour: 'Red',
  priceAdjustment: 0,
  isActive: true,
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('ProductVariantsModal', () => {
  it('lists variants with SKU and status', async () => {
    mockFetch.mockResolvedValue([variantA]);

    render(<ProductVariantsModal product={product} onClose={vi.fn()} />);

    await waitFor(() => expect(screen.getByText('CEM-50KG-RED')).toBeInTheDocument());
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('shows the empty state when there are no variants', async () => {
    mockFetch.mockResolvedValue([]);

    render(<ProductVariantsModal product={product} onClose={vi.fn()} />);

    await waitFor(() => expect(screen.getByText('No variants yet.')).toBeInTheDocument());
  });

  it('creates a variant via the add form', async () => {
    mockFetch.mockResolvedValue([]);
    mockCreate.mockResolvedValue({ ...variantA, id: 11, sku: 'CEM-50KG-BLU', colour: 'Blue' });

    render(<ProductVariantsModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getByText('No variants yet.')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByText('+ Add variant'));
    await user.type(screen.getByLabelText('SKU'), 'CEM-50KG-BLU');
    await user.clear(screen.getByLabelText('Price adjustment'));
    await user.type(screen.getByLabelText('Price adjustment'), '0');
    await user.clear(screen.getByLabelText('Initial stock quantity'));
    await user.type(screen.getByLabelText('Initial stock quantity'), '10');
    await user.clear(screen.getByLabelText('Minimum stock level'));
    await user.type(screen.getByLabelText('Minimum stock level'), '2');
    await user.click(screen.getByText('Save'));

    await waitFor(() => expect(mockCreate).toHaveBeenCalledWith(1, expect.objectContaining({ sku: 'CEM-50KG-BLU' })));
    await waitFor(() => expect(screen.getByText('CEM-50KG-BLU')).toBeInTheDocument());
  });

  it('edits an existing variant', async () => {
    mockFetch.mockResolvedValue([variantA]);
    mockUpdate.mockResolvedValue({ ...variantA, priceAdjustment: 25 });

    render(<ProductVariantsModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getByText('CEM-50KG-RED')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByText('Edit'));
    const priceInput = screen.getByLabelText('Price adjustment');
    await user.clear(priceInput);
    await user.type(priceInput, '25');
    await user.click(screen.getByText('Save'));

    await waitFor(() => expect(mockUpdate).toHaveBeenCalledWith(1, 10, expect.objectContaining({ priceAdjustment: 25 })));
  });

  it('deactivates a variant after confirmation', async () => {
    mockFetch.mockResolvedValue([variantA]);
    mockDelete.mockResolvedValue(undefined);

    render(<ProductVariantsModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getByText('CEM-50KG-RED')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByText('Deactivate'));

    await waitFor(() => expect(mockDelete).toHaveBeenCalledWith(1, 10));
    await waitFor(() => expect(screen.getByText('Inactive')).toBeInTheDocument());
  });

  it('surfaces an API error from create without crashing', async () => {
    mockFetch.mockResolvedValue([]);
    mockCreate.mockRejectedValue(new Error('Variant SKU already in use'));

    render(<ProductVariantsModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getByText('No variants yet.')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByText('+ Add variant'));
    await user.type(screen.getByLabelText('SKU'), 'CEM-50KG-RED');
    await user.clear(screen.getByLabelText('Price adjustment'));
    await user.type(screen.getByLabelText('Price adjustment'), '0');
    await user.clear(screen.getByLabelText('Initial stock quantity'));
    await user.type(screen.getByLabelText('Initial stock quantity'), '10');
    await user.clear(screen.getByLabelText('Minimum stock level'));
    await user.type(screen.getByLabelText('Minimum stock level'), '2');
    await user.click(screen.getByText('Save'));

    await waitFor(() => expect(screen.getByText('Variant SKU already in use')).toBeInTheDocument());
  });
});
