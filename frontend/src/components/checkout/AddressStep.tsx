import { useState } from 'react';

interface AddressForm {
  fullName: string;
  line1: string;
  line2: string;
  city: string;
  state: string;
  postalCode: string;
  phone: string;
}

interface Props {
  onNext: (postalCode: string) => void;
  loading: boolean;
  error: string | null;
}

const EMPTY: AddressForm = {
  fullName: '', line1: '', line2: '', city: '', state: '', postalCode: '', phone: '',
};

type Field = keyof AddressForm;

function validate(form: AddressForm): Partial<Record<Field, string>> {
  const e: Partial<Record<Field, string>> = {};
  if (!form.fullName.trim()) e.fullName = 'Name is required';
  if (!form.line1.trim()) e.line1 = 'Address line 1 is required';
  if (!form.city.trim()) e.city = 'City is required';
  if (!form.state.trim()) e.state = 'State is required';
  if (!/^\d{6}$/.test(form.postalCode)) e.postalCode = 'Enter a valid 6-digit postal code';
  if (!/^\d{10}$/.test(form.phone)) e.phone = 'Enter a valid 10-digit phone number';
  return e;
}

export function AddressStep({ onNext, loading, error }: Props) {
  const [form, setForm] = useState<AddressForm>(EMPTY);
  const [touched, setTouched] = useState<Partial<Record<Field, boolean>>>({});
  const errors = validate(form);

  const field = (name: Field, label: string, placeholder = '', type = 'text') => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type={type}
        value={form[name]}
        onChange={e => setForm(f => ({ ...f, [name]: e.target.value }))}
        onBlur={() => setTouched(t => ({ ...t, [name]: true }))}
        placeholder={placeholder}
        className={`w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400 ${
          touched[name] && errors[name] ? 'border-red-400' : 'border-gray-300'
        }`}
      />
      {touched[name] && errors[name] && (
        <p className="text-red-500 text-xs mt-1">{errors[name]}</p>
      )}
    </div>
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setTouched(Object.fromEntries(Object.keys(EMPTY).map(k => [k, true])));
    if (Object.keys(errors).length > 0) return;
    onNext(form.postalCode);
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Delivery Address</h2>

      <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 mb-5 text-sm text-amber-800">
        Address management is coming in a future update. Enter your delivery details below — they will be used to calculate shipping costs.
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="sm:col-span-2">{field('fullName', 'Full name', 'Jane Doe')}</div>
        <div className="sm:col-span-2">{field('line1', 'Address line 1', '123 Main Street')}</div>
        <div className="sm:col-span-2">{field('line2', 'Address line 2 (optional)', 'Apt 4B')}</div>
        {field('city', 'City', 'Mumbai')}
        {field('state', 'State', 'Maharashtra')}
        {field('postalCode', 'Postal code', '400001')}
        {field('phone', 'Phone number', '9876543210', 'tel')}
      </div>

      {error && (
        <p className="mt-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={loading}
        className="mt-6 w-full bg-amber-500 hover:bg-amber-600 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors"
      >
        {loading ? 'Saving…' : 'Continue to Shipping'}
      </button>
    </form>
  );
}
