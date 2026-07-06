import { useState, useEffect, useCallback, type FormEvent } from 'react';
import {
  fetchAddresses,
  createAddress,
  deleteAddress,
  setDefaultAddress,
  type CreateAddressInput,
} from '../../api/addresses';
import type { Address } from '../../types';

const EMPTY_FORM: CreateAddressInput = {
  streetAddress: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'India',
  addressType: 'SHIPPING',
};

export function AddressesTab() {
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<CreateAddressInput>(EMPTY_FORM);
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [actioningId, setActioningId] = useState<number | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchAddresses()
      .then(setAddresses)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load addresses'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleAdd = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      await createAddress(form);
      setForm(EMPTY_FORM);
      setShowForm(false);
      load();
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to save address');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    setActioningId(id);
    try {
      await deleteAddress(id);
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete address');
    } finally {
      setActioningId(null);
    }
  };

  const handleSetDefault = async (id: number) => {
    setActioningId(id);
    try {
      await setDefaultAddress(id);
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set default address');
    } finally {
      setActioningId(null);
    }
  };

  const field = (name: keyof CreateAddressInput, label: string, placeholder = '') => (
    <div>
      <label htmlFor={`address-${name}`} className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        id={`address-${name}`}
        name={name}
        type="text"
        value={form[name] ?? ''}
        onChange={e => setForm(f => ({ ...f, [name]: e.target.value }))}
        placeholder={placeholder}
        required={name !== 'addressType'}
        className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
      />
    </div>
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-900">Address Book</h2>
        {!loading && !error && (
          <button
            type="button"
            onClick={() => setShowForm(s => !s)}
            className="text-sm font-medium text-primary-600 hover:text-primary-700"
          >
            {showForm ? 'Cancel' : '+ Add address'}
          </button>
        )}
      </div>

      {showForm && (
        <form onSubmit={handleAdd} noValidate className="border border-gray-100 rounded-2xl p-4 mb-5 space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="sm:col-span-2">{field('streetAddress', 'Street address', '123 Main Street')}</div>
            {field('city', 'City', 'Mumbai')}
            {field('state', 'State', 'Maharashtra')}
            {field('postalCode', 'Postal code', '400001')}
            {field('country', 'Country', 'India')}
          </div>
          {formError && (
            <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">{formError}</p>
          )}
          <button
            type="submit"
            disabled={saving}
            className="bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white font-semibold px-5 py-2 rounded-xl text-sm transition-colors"
          >
            {saving ? 'Saving…' : 'Save address'}
          </button>
        </form>
      )}

      {loading && (
        <div className="space-y-3 animate-pulse">
          {[1, 2].map(i => <div key={i} className="h-24 bg-gray-100 rounded-xl" />)}
        </div>
      )}

      {error && !loading && (
        <div className="text-center py-8">
          <p className="text-red-600 text-sm mb-3">{error}</p>
          <button
            type="button"
            onClick={load}
            className="bg-primary-500 hover:bg-primary-600 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-colors"
          >
            Retry
          </button>
        </div>
      )}

      {!loading && !error && addresses.length === 0 && (
        <div className="border-2 border-dashed border-gray-200 rounded-2xl p-10 text-center">
          <div className="text-4xl mb-3">📍</div>
          <p className="font-medium text-gray-700 mb-1">No saved addresses yet</p>
          <p className="text-sm text-gray-400 max-w-xs mx-auto">
            Save a delivery address to speed up checkout next time.
          </p>
        </div>
      )}

      {!loading && !error && addresses.length > 0 && (
        <div className="space-y-3">
          {addresses.map(address => (
            <div key={address.id} className="border border-gray-100 rounded-xl px-4 py-3">
              <div className="flex items-start justify-between gap-3">
                <div className="text-sm">
                  <p className="font-medium text-gray-900">
                    {address.streetAddress}
                    {address.isDefault && (
                      <span className="ml-2 px-2 py-0.5 bg-primary-100 text-primary-700 rounded-full text-xs font-semibold align-middle">
                        Default
                      </span>
                    )}
                  </p>
                  <p className="text-gray-500 mt-0.5">
                    {address.city}, {address.state} {address.postalCode}, {address.country}
                  </p>
                  {address.addressType && (
                    <p className="text-gray-400 text-xs mt-0.5">{address.addressType}</p>
                  )}
                </div>
                <div className="flex flex-col items-end gap-2 shrink-0">
                  {!address.isDefault && (
                    <button
                      type="button"
                      onClick={() => handleSetDefault(address.id)}
                      disabled={actioningId === address.id}
                      className="text-xs font-medium text-primary-600 hover:text-primary-800 disabled:opacity-50"
                    >
                      Set as default
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={() => handleDelete(address.id)}
                    disabled={actioningId === address.id}
                    className="text-xs font-medium text-gray-400 hover:text-red-600 disabled:opacity-50"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
