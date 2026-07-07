import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { apiResetPassword } from '../api/auth';

interface Form {
  newPassword: string;
  confirmPassword: string;
}

type Field = keyof Form;

const EMPTY: Form = { newPassword: '', confirmPassword: '' };

function validate(f: Form): Partial<Record<Field, string>> {
  const e: Partial<Record<Field, string>> = {};
  if (f.newPassword.length < 12) e.newPassword = 'Password must be at least 12 characters';
  if (f.confirmPassword !== f.newPassword) e.confirmPassword = 'Passwords do not match';
  return e;
}

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [form, setForm] = useState<Form>(EMPTY);
  const [touched, setTouched] = useState<Partial<Record<Field, boolean>>>({});
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const errors = validate(form);

  const set = (field: Field) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [field]: e.target.value }));
  const blur = (field: Field) => () => setTouched(t => ({ ...t, [field]: true }));

  const field = (name: Field, label: string, placeholder: string) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type="password"
        value={form[name]}
        onChange={set(name)}
        onBlur={blur(name)}
        placeholder={placeholder}
        autoComplete="new-password"
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
    setTouched({ newPassword: true, confirmPassword: true });
    if (!token || Object.keys(errors).length > 0) return;
    setLoading(true);
    setError(null);
    try {
      await apiResetPassword(token, form.newPassword);
      navigate('/login', { state: { passwordReset: true } });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reset password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold text-primary-600">🏗️ BuildNest</Link>
          <h1 className="mt-4 text-2xl font-bold text-gray-900">Set a new password</h1>
          <p className="mt-1 text-sm text-gray-500">Choose a strong new password for your account</p>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          {!token ? (
            <div className="text-sm text-gray-700 text-center space-y-4">
              <p>This reset link is missing or invalid. Please request a new one.</p>
              <Link
                to="/forgot-password"
                className="inline-block text-primary-600 hover:text-primary-700 font-medium"
              >
                Request a new reset link
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
              {field('newPassword', 'New password', 'Min. 12 characters')}
              {field('confirmPassword', 'Confirm new password', '••••••••••••')}

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
                {loading ? 'Resetting…' : 'Reset password'}
              </button>
            </form>
          )}
        </div>

        <p className="mt-5 text-center text-sm text-gray-500">
          Remembered your password?{' '}
          <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
