import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductFormModal } from './ProductFormModal';
import {
  createAdminProduct,
  updateAdminProduct,
  type AdminProduct,
  type AdminCategory,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  createAdminProduct: vi.fn(),
  updateAdminProduct: vi.fn(),
}));

const mockCreate = vi.mocked(createAdminProduct);
const mockUpdate = vi.mocked(updateAdminProduct);

const categories: AdminCategory[] = [{ id: 3, name: 'Building Materials' }];

const cement: AdminProduct = {
  id: 1,
  name: 'Premium Cement 50kg',
  description: 'High-strength cement suitable for construction.',
  price: 499.99,
  stockQuantity: 100,
  sku: 'CEM-50KG',
  isActive: true,
  category: { id: 3, name: 'Building Materials' },
};

function fillRequiredFields() {
  return {
    name: screen.getByLabelText('Name'),
    description: screen.getByLabelText('Description'),
    price: screen.getByLabelText('Price'),
    category: screen.getByLabelText('Category'),
  };
}

describe('ProductFormModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('create mode: stock quantity field is enabled and sent on submit', async () => {
    const user = userEvent.setup();
    mockCreate.mockResolvedValue({ ...cement, id: 2 });
    render(
      <ProductFormModal product={null} categories={categories} onClose={vi.fn()} onSaved={vi.fn()} />
    );

    const stockInput = screen.getByLabelText('Initial Stock Quantity') as HTMLInputElement;
    expect(stockInput).not.toBeDisabled();

    const { name, description, price, category } = fillRequiredFields();
    await user.type(name, 'New Cement Bag');
    await user.type(description, 'A brand new bag of cement for testing.');
    await user.type(price, '199.99');
    await user.selectOptions(category, '3');
    await user.type(stockInput, '50');

    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(mockCreate).toHaveBeenCalledWith(
      expect.objectContaining({ stockQuantity: 50 })
    );
  });

  it('edit mode: stock quantity field is disabled and never sent on submit', async () => {
    const user = userEvent.setup();
    mockUpdate.mockResolvedValue(cement);
    render(
      <ProductFormModal product={cement} categories={categories} onClose={vi.fn()} onSaved={vi.fn()} />
    );

    const stockInput = screen.getByLabelText('Initial Stock Quantity') as HTMLInputElement;
    expect(stockInput).toBeDisabled();
    expect(stockInput.value).toBe('100');

    await user.click(screen.getByRole('button', { name: 'Save Changes' }));

    expect(mockUpdate).toHaveBeenCalledWith(
      cement.id,
      expect.objectContaining({ stockQuantity: undefined })
    );
  });
});
