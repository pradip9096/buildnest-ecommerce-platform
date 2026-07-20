import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ShippingMethodsTab } from './ShippingMethodsTab';
import {
  fetchAdminShippingMethods,
  createAdminShippingMethod,
  updateAdminShippingMethod,
  deactivateAdminShippingMethod,
  type AdminShippingMethod,
} from '../../api/admin';
import { ApiError } from '../../api/client';

vi.mock('../../api/admin', () => ({
  fetchAdminShippingMethods: vi.fn(),
  createAdminShippingMethod: vi.fn(),
  updateAdminShippingMethod: vi.fn(),
  deactivateAdminShippingMethod: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminShippingMethods);
const mockCreate = vi.mocked(createAdminShippingMethod);
const mockUpdate = vi.mocked(updateAdminShippingMethod);
const mockDeactivate = vi.mocked(deactivateAdminShippingMethod);

const standard: AdminShippingMethod = {
  id: 1,
  name: 'Standard Shipping',
  description: 'Reliable, no rush',
  baseCost: 50,
  costPerKg: 5,
  estimatedDaysMin: 3,
  estimatedDaysMax: 7,
  isActive: true,
};
const express: AdminShippingMethod = {
  id: 2,
  name: 'Express Shipping',
  baseCost: 150,
  costPerKg: 10,
  estimatedDaysMin: 1,
  estimatedDaysMax: 2,
  isActive: true,
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('ShippingMethodsTab', () => {
  it('renders the shipping method list', async () => {
    mockFetch.mockResolvedValue([standard, express]);

    render(<ShippingMethodsTab />);

    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());
    expect(screen.getByText('Express Shipping')).toBeInTheDocument();
  });

  it('filters the list via search', async () => {
    mockFetch.mockResolvedValue([standard, express]);

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());

    await userEvent.setup().type(screen.getByPlaceholderText('Search shipping methods…'), 'Express');

    expect(screen.queryByText('Standard Shipping')).not.toBeInTheDocument();
    expect(screen.getByText('Express Shipping')).toBeInTheDocument();
  });

  it('shows the empty state when there are no shipping methods', async () => {
    mockFetch.mockResolvedValue([]);

    render(<ShippingMethodsTab />);

    await waitFor(() => expect(screen.getByText('No shipping methods found')).toBeInTheDocument());
  });

  it('creates a shipping method via the modal and adds it to the list', async () => {
    mockFetch.mockResolvedValue([standard]);
    mockCreate.mockResolvedValue({
      id: 3,
      name: 'Economy Shipping',
      baseCost: 20,
      costPerKg: 2,
      estimatedDaysMin: 7,
      estimatedDaysMax: 14,
      isActive: true,
    });

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Method' }));
    await user.type(screen.getByLabelText('Name'), 'Economy Shipping');
    await user.type(screen.getByLabelText('Base Cost (₹)'), '20');
    await user.type(screen.getByLabelText('Min Days'), '7');
    await user.type(screen.getByLabelText('Max Days'), '14');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(mockCreate).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'Economy Shipping', baseCost: 20, estimatedDaysMin: 7, estimatedDaysMax: 14 })
    ));
    await waitFor(() => expect(screen.getByText('Economy Shipping')).toBeInTheDocument());
  });

  it('rejects a name shorter than 2 characters without calling the API', async () => {
    mockFetch.mockResolvedValue([standard]);

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Method' }));
    await user.type(screen.getByLabelText('Name'), 'A');
    await user.type(screen.getByLabelText('Base Cost (₹)'), '10');
    await user.type(screen.getByLabelText('Min Days'), '1');
    await user.type(screen.getByLabelText('Max Days'), '2');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Name must be at least 2 characters.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('rejects max days less than min days without calling the API', async () => {
    mockFetch.mockResolvedValue([standard]);

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Method' }));
    await user.type(screen.getByLabelText('Name'), 'Broken Range');
    await user.type(screen.getByLabelText('Base Cost (₹)'), '10');
    await user.type(screen.getByLabelText('Min Days'), '5');
    await user.type(screen.getByLabelText('Max Days'), '2');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Max estimated days cannot be less than min estimated days.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('pre-fills the form on edit', async () => {
    mockFetch.mockResolvedValue([standard, express]);
    mockUpdate.mockResolvedValue(standard);

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());

    await userEvent.setup().click(screen.getAllByRole('button', { name: 'Edit' })[0]);

    expect(screen.getByLabelText('Name')).toHaveValue('Standard Shipping');
    expect(screen.getByLabelText('Base Cost (₹)')).toHaveValue(50);
  });

  it('surfaces the backend message when deactivation fails', async () => {
    mockFetch.mockResolvedValue([standard]);
    mockDeactivate.mockRejectedValue(new ApiError('Cannot deactivate the only active shipping method', 400));

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Standard Shipping')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Deactivate' }));

    await waitFor(() => expect(screen.getByText('Cannot deactivate the only active shipping method')).toBeInTheDocument());
    expect(screen.getByText('Standard Shipping')).toBeInTheDocument();
  });

  it('deactivates a shipping method successfully and marks it inactive', async () => {
    mockFetch.mockResolvedValue([standard, express]);
    mockDeactivate.mockResolvedValue(undefined);

    render(<ShippingMethodsTab />);
    await waitFor(() => expect(screen.getByText('Express Shipping')).toBeInTheDocument());

    const deactivateButtons = screen.getAllByRole('button', { name: 'Deactivate' });
    await userEvent.setup().click(deactivateButtons[1]);

    await waitFor(() => expect(mockDeactivate).toHaveBeenCalledWith(2));
    await waitFor(() => expect(screen.getAllByText('Inactive').length).toBeGreaterThan(0));
  });
});
