import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AddressesTab } from './AddressesTab';
import { fetchAddresses, createAddress, deleteAddress, setDefaultAddress } from '../../api/addresses';
import type { Address } from '../../types';

vi.mock('../../api/addresses', () => ({
  fetchAddresses: vi.fn(),
  createAddress: vi.fn(),
  updateAddress: vi.fn(),
  deleteAddress: vi.fn(),
  setDefaultAddress: vi.fn(),
}));

const mockFetchAddresses = vi.mocked(fetchAddresses);
const mockCreateAddress = vi.mocked(createAddress);
const mockDeleteAddress = vi.mocked(deleteAddress);
const mockSetDefaultAddress = vi.mocked(setDefaultAddress);

const address: Address = {
  id: 1,
  streetAddress: '123 Main Street',
  city: 'Mumbai',
  state: 'Maharashtra',
  postalCode: '400001',
  country: 'India',
  isDefault: true,
  addressType: 'SHIPPING',
};

const secondAddress: Address = {
  ...address,
  id: 2,
  streetAddress: '456 Second Street',
  isDefault: false,
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('AddressesTab', () => {
  it('renders the address list on success', async () => {
    mockFetchAddresses.mockResolvedValue([address]);

    render(<AddressesTab token="token-abc" />);

    await waitFor(() => expect(screen.getByText('123 Main Street')).toBeInTheDocument());
    expect(screen.getByText('Default')).toBeInTheDocument();
    expect(mockFetchAddresses).toHaveBeenCalledWith('token-abc');
  });

  it('shows the empty state when there are no saved addresses', async () => {
    mockFetchAddresses.mockResolvedValue([]);

    render(<AddressesTab token="token-abc" />);

    await waitFor(() => expect(screen.getByText('No saved addresses yet')).toBeInTheDocument());
  });

  it('shows an error with a retry option when the fetch fails', async () => {
    mockFetchAddresses.mockRejectedValueOnce(new Error('Failed to fetch addresses (500)'));
    mockFetchAddresses.mockResolvedValueOnce([address]);

    render(<AddressesTab token="token-abc" />);

    await waitFor(() => expect(screen.getByText('Failed to fetch addresses (500)')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }));

    await waitFor(() => expect(screen.getByText('123 Main Street')).toBeInTheDocument());
  });

  it('adds a new address via the form and reloads the list', async () => {
    mockFetchAddresses.mockResolvedValueOnce([]).mockResolvedValueOnce([address]);
    mockCreateAddress.mockResolvedValue(address);

    render(<AddressesTab token="token-abc" />);
    await waitFor(() => expect(screen.getByText('No saved addresses yet')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ Add address' }));
    await user.type(screen.getByLabelText('Street address'), '123 Main Street');
    await user.type(screen.getByLabelText('City'), 'Mumbai');
    await user.type(screen.getByLabelText('State'), 'Maharashtra');
    await user.type(screen.getByLabelText('Postal code'), '400001');
    await user.click(screen.getByRole('button', { name: 'Save address' }));

    await waitFor(() => expect(mockCreateAddress).toHaveBeenCalledWith(
      expect.objectContaining({ streetAddress: '123 Main Street', city: 'Mumbai' }),
      'token-abc'
    ));
    await waitFor(() => expect(screen.getByText('123 Main Street')).toBeInTheDocument());
  });

  it('deletes an address and reloads the list', async () => {
    mockFetchAddresses.mockResolvedValueOnce([address, secondAddress]).mockResolvedValueOnce([address]);
    mockDeleteAddress.mockResolvedValue(undefined);

    render(<AddressesTab token="token-abc" />);
    await waitFor(() => expect(screen.getByText('456 Second Street')).toBeInTheDocument());

    const deleteButtons = screen.getAllByRole('button', { name: 'Delete' });
    await userEvent.setup().click(deleteButtons[1]);

    await waitFor(() => expect(mockDeleteAddress).toHaveBeenCalledWith(2, 'token-abc'));
    await waitFor(() => expect(screen.queryByText('456 Second Street')).not.toBeInTheDocument());
  });

  it('sets a non-default address as default and reloads the list', async () => {
    mockFetchAddresses
      .mockResolvedValueOnce([address, secondAddress])
      .mockResolvedValueOnce([{ ...address, isDefault: false }, { ...secondAddress, isDefault: true }]);
    mockSetDefaultAddress.mockResolvedValue({ ...secondAddress, isDefault: true });

    render(<AddressesTab token="token-abc" />);
    await waitFor(() => expect(screen.getByText('456 Second Street')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Set as default' }));

    await waitFor(() => expect(mockSetDefaultAddress).toHaveBeenCalledWith(2, 'token-abc'));
  });
});
