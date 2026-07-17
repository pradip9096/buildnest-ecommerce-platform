import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductsTab } from './ProductsTab';
import {
  fetchAdminProducts,
  createAdminProduct,
  updateAdminProduct,
  deleteAdminProduct,
  fetchAdminCategories,
  type AdminProduct,
  type AdminCategory,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchAdminProducts: vi.fn(),
  createAdminProduct: vi.fn(),
  updateAdminProduct: vi.fn(),
  deleteAdminProduct: vi.fn(),
  fetchAdminCategories: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminProducts);
const mockCreate = vi.mocked(createAdminProduct);
const mockUpdate = vi.mocked(updateAdminProduct);
const mockDelete = vi.mocked(deleteAdminProduct);
const mockFetchCategories = vi.mocked(fetchAdminCategories);

const cement: AdminProduct = {
  id: 1,
  name: 'Premium Cement 50kg',
  description: 'High-strength cement',
  price: 499.99,
  stockQuantity: 100,
  sku: 'CEM-50KG',
  isActive: true,
  category: { id: 3, name: 'Building Materials' },
};
const paint: AdminProduct = {
  id: 2,
  name: 'Weatherproof Paint 10L',
  price: 899,
  stockQuantity: 40,
  sku: 'PNT-10L',
  isActive: true,
  category: { id: 4, name: 'Paints' },
};
const categories: AdminCategory[] = [
  { id: 3, name: 'Building Materials', isActive: true, parentCategory: null },
  { id: 4, name: 'Paints', isActive: true, parentCategory: null },
];

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
  mockFetchCategories.mockResolvedValue(categories);
});

describe('ProductsTab', () => {
  it('renders the product list', async () => {
    mockFetch.mockResolvedValue([cement, paint]);

    render(<ProductsTab />);

    await waitFor(() => expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument());
    expect(screen.getByText('Weatherproof Paint 10L')).toBeInTheDocument();
  });

  it('filters the list via search by name or SKU', async () => {
    mockFetch.mockResolvedValue([cement, paint]);

    render(<ProductsTab />);
    await waitFor(() => expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument());

    await userEvent.setup().type(screen.getByPlaceholderText('Search products…'), 'PNT-10L');

    expect(screen.queryByText('Premium Cement 50kg')).not.toBeInTheDocument();
    expect(screen.getByText('Weatherproof Paint 10L')).toBeInTheDocument();
  });

  it('shows the empty state when there are no products', async () => {
    mockFetch.mockResolvedValue([]);

    render(<ProductsTab />);

    await waitFor(() => expect(screen.getByText('No products found')).toBeInTheDocument());
  });

  it('creates a product via the modal and adds it to the list', async () => {
    mockFetch.mockResolvedValue([cement]);
    mockCreate.mockResolvedValue({ id: 5, name: 'Steel Rod 12mm', price: 120, isActive: true, category: { id: 3 } });

    render(<ProductsTab />);
    await waitFor(() => expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Product' }));
    await user.type(screen.getByLabelText('Name'), 'Steel Rod 12mm');
    await user.type(screen.getByLabelText('Description'), 'High tensile steel reinforcement rod');
    await user.type(screen.getByLabelText('Price'), '120');
    await user.selectOptions(screen.getByLabelText('Category'), '3');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(mockCreate).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'Steel Rod 12mm', price: 120, categoryId: 3 })
    ));
    await waitFor(() => expect(screen.getByText('Steel Rod 12mm')).toBeInTheDocument());
  });

  it('rejects a missing category without calling the API', async () => {
    mockFetch.mockResolvedValue([cement]);

    render(<ProductsTab />);
    await waitFor(() => expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Product' }));
    await user.type(screen.getByLabelText('Name'), 'Steel Rod 12mm');
    await user.type(screen.getByLabelText('Description'), 'High tensile steel reinforcement rod');
    await user.type(screen.getByLabelText('Price'), '120');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Category is required.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('pre-fills the form on edit', async () => {
    mockFetch.mockResolvedValue([cement]);
    mockUpdate.mockResolvedValue(cement);

    render(<ProductsTab />);
    await waitFor(() => expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Edit' }));

    expect(screen.getByLabelText('Name')).toHaveValue('Premium Cement 50kg');
    expect(screen.getByLabelText('SKU')).toHaveValue('CEM-50KG');
  });

  it('surfaces the backend error message when delete fails', async () => {
    mockFetch.mockResolvedValue([cement]);
    mockDelete.mockRejectedValue(new Error('Cannot delete product 1: referenced by open orders'));

    render(<ProductsTab />);
    await waitFor(() => expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Delete' }));

    await waitFor(() => expect(screen.getByText('Cannot delete product 1: referenced by open orders')).toBeInTheDocument());
    expect(screen.getByText('Premium Cement 50kg')).toBeInTheDocument();
  });

  it('deletes a product successfully and marks it inactive', async () => {
    mockFetch.mockResolvedValue([cement, paint]);
    mockDelete.mockResolvedValue(undefined);

    render(<ProductsTab />);
    await waitFor(() => expect(screen.getByText('Weatherproof Paint 10L')).toBeInTheDocument());

    const deleteButtons = screen.getAllByRole('button', { name: 'Delete' });
    await userEvent.setup().click(deleteButtons[1]);

    await waitFor(() => expect(mockDelete).toHaveBeenCalledWith(2));
    await waitFor(() => {
      const statusBadges = screen.getAllByText('Inactive');
      expect(statusBadges.length).toBeGreaterThan(0);
    });
  });
});
