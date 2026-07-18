import { useState } from 'react';
import { updateAdminUser, type AdminUser, type UpdateUserInput } from '../../api/admin';

interface Props {
  user: AdminUser;
  onClose: () => void;
  onSuccess: (updated: AdminUser) => void;
}

const EMAIL_PATTERN = /^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const PHONE_PATTERN = /^\+?[1-9]\d{1,14}$/;

function validate(input: UpdateUserInput): string | null {
  if (!input.firstName.trim() || input.firstName.trim().length < 2 || input.firstName.trim().length > 50) {
    return 'First name must be between 2 and 50 characters.';
  }
  if (!input.lastName.trim() || input.lastName.trim().length < 2 || input.lastName.trim().length > 50) {
    return 'Last name must be between 2 and 50 characters.';
  }
  if (!EMAIL_PATTERN.test(input.email) || input.email.length > 254) {
    return 'Enter a valid email address.';
  }
  if (input.phone && !PHONE_PATTERN.test(input.phone.replace(/[\s\-().]+/g, ''))) {
    return 'Enter a valid phone number (e.g. +14155552671).';
  }
  return null;
}

export function UserDetailModal({ user, onClose, onSuccess }: Props) {
  const [editing, setEditing] = useState(false);
  const [firstName, setFirstName] = useState(user.firstName);
  const [lastName, setLastName] = useState(user.lastName);
  const [email, setEmail] = useState(user.email);
  const [phone, setPhone] = useState(user.phoneNumber ?? '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const input: UpdateUserInput = {
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: email.trim(),
      phone: phone.trim() || undefined,
    };
    const validationError = validate(input);
    if (validationError) { setError(validationError); return; }

    setLoading(true);
    setError(null);
    try {
      const updated = await updateAdminUser(user.id, input);
      onSuccess(updated);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to update user');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">{editing ? 'Edit User' : 'User Details'}</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        {editing ? (
          <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">First name</label>
              <input
                type="text"
                value={firstName}
                onChange={e => setFirstName(e.target.value)}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Last name</label>
              <input
                type="text"
                value={lastName}
                onChange={e => setLastName(e.target.value)}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="text"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input
                type="tel"
                value={phone}
                onChange={e => setPhone(e.target.value)}
                placeholder="e.g. +14155552671"
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
              />
            </div>

            {error && <p className="text-red-600 text-sm">{error}</p>}

            <div className="flex gap-3 pt-1">
              <button
                type="button"
                onClick={() => setEditing(false)}
                className="flex-1 border border-gray-200 text-gray-700 rounded-xl py-2.5 text-sm font-medium hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="flex-1 bg-primary-500 hover:bg-primary-600 text-white rounded-xl py-2.5 text-sm font-semibold disabled:opacity-60 transition-colors"
              >
                {loading ? 'Saving…' : 'Save'}
              </button>
            </div>
          </form>
        ) : (
          <div className="px-6 py-4 space-y-3">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Username</p>
              <p className="text-sm text-gray-900">@{user.username}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Name</p>
              <p className="text-sm text-gray-900">{user.firstName} {user.lastName}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Email</p>
              <p className="text-sm text-gray-900">{user.email}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Phone</p>
              <p className="text-sm text-gray-900">{user.phoneNumber || '—'}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Roles</p>
              <div className="flex gap-1 flex-wrap mt-1">
                {(user.roles ?? ['USER']).map(role => (
                  <span key={role}
                    className={`px-2 py-0.5 rounded-full text-xs font-semibold ${role === 'ADMIN' ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-600'}`}>
                    {role}
                  </span>
                ))}
              </div>
            </div>

            <div className="flex gap-3 pt-3">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 border border-gray-200 text-gray-700 rounded-xl py-2.5 text-sm font-medium hover:bg-gray-50 transition-colors"
              >
                Close
              </button>
              <button
                type="button"
                onClick={() => setEditing(true)}
                className="flex-1 bg-primary-500 hover:bg-primary-600 text-white rounded-xl py-2.5 text-sm font-semibold transition-colors"
              >
                Edit
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
