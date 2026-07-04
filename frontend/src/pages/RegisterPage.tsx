import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface Form {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

type Field = keyof Form;

const EMPTY: Form = { firstName: '', lastName: '', username: '', email: '', password: '', confirmPassword: '' };

function validate(f: Form): Partial<Record<Field, string>> {
  const e: Partial<Record<Field, string>> = {};
  if (f.firstName.trim().length < 2) e.firstName = 'First name must be at least 2 characters';
  if (f.lastName.trim().length < 2) e.lastName = 'Last name must be at least 2 characters';
  if (f.username.trim().length < 3) e.username = 'Username must be at least 3 characters';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) e.email = 'Enter a valid email address';
  if (f.password.length < 12) e.password = 'Password must be at least 12 characters';
  if (f.confirmPassword !== f.password) e.confirmPassword = 'Passwords do not match';
  return e;
}

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState<Form>(EMPTY);
  const [touched, setTouched] = useState<Partial<Record<Field, boolean>>>({});
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const errors = validate(form);

  const set = (field: Field) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [field]: e.target.value }));
  const blur = (field: Field) => () =>
    setTouched(t => ({ ...t, [field]: true }));

  const field = (name: Field, label: string, type = 'text', placeholder = '') => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type={type}
        value={form[name]}
        onChange={set(name)}
        onBlur={blur(name)}
        placeholder={placeholder}
        autoComplete={name === 'confirmPassword' ? 'new-password' : name}
        className={`w-full border rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 ${
          touched[name] && errors[name] ? 'border-red-400' : 'border-gray-300'
        }`}
      />
      {touched[name] && errors[name] && (
        <p className="text-red-500 text-xs mt-1">{errors[name]}</p>
      )}
    </div>
  );

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setTouched(Object.fromEntries(Object.keys(EMPTY).map(k => [k, true])));
    if (Object.keys(errors).length > 0) return;
    setLoading(true);
    setError(null);
    try {
      await register({
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
      });
      navigate('/login', { state: { registered: true } });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold text-primary-600">🏗️ BuildNest</Link>
          <h1 className="mt-4 text-2xl font-bold text-gray-900">Create an account</h1>
          <p className="mt-1 text-sm text-gray-500">Start building your dream home today</p>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <form onSubmit={handleSubmit} noValidate className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              {field('firstName', 'First name', 'text', 'Aarav')}
              {field('lastName', 'Last name', 'text', 'Sharma')}
            </div>
            {field('username', 'Username', 'text', 'aarav_sharma')}
            {field('email', 'Email', 'email', 'aarav@example.com')}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                  onChange={set('password')}
                  onBlur={blur('password')}
                  placeholder="Min. 12 characters"
                  autoComplete="new-password"
                  className={`w-full border rounded-xl px-3 py-2.5 pr-10 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400 ${
                    touched.password && errors.password ? 'border-red-400' : 'border-gray-300'
                  }`}
                />
                <button type="button" onClick={() => setShowPassword(s => !s)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-xs">
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
              {touched.password && errors.password && (
                <p className="text-red-500 text-xs mt-1">{errors.password}</p>
              )}
            </div>

            {field('confirmPassword', 'Confirm password', 'password', '••••••••••••')}

            {error && (
              <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl px-3 py-2">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white font-semibold py-3 rounded-xl transition-colors mt-2"
            >
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>
        </div>

        <p className="mt-5 text-center text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
