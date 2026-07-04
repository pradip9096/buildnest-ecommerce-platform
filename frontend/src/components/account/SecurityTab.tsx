import { useState, type FormEvent } from 'react';
import { changePassword } from '../../api/user';

interface Props { token: string; }

export function SecurityTab({ token }: Props) {
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNew, setConfirmNew] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 12) { setError('New password must be at least 12 characters'); return; }
    if (newPassword !== confirmNew) { setError('New passwords do not match'); return; }
    setSaving(true); setError(null);
    try {
      await changePassword(token, oldPassword, newPassword);
      setSuccess(true);
      setOldPassword(''); setNewPassword(''); setConfirmNew('');
      setTimeout(() => setSuccess(false), 4000);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to change password');
    } finally {
      setSaving(false);
    }
  };

  const pwdField = (id: string, label: string, value: string, onChange: (v: string) => void, autoComplete: string) => (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input id={id} name={id} type="password" value={value} onChange={e => onChange(e.target.value)} autoComplete={autoComplete}
        className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400" />
    </div>
  );

  return (
    <div className="space-y-8 max-w-lg">
      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Change Password</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {pwdField('old-password', 'Current password', oldPassword, setOldPassword, 'current-password')}
          {pwdField('new-password', 'New password', newPassword, setNewPassword, 'new-password')}
          {pwdField('confirm-new-password', 'Confirm new password', confirmNew, setConfirmNew, 'new-password')}
          <p className="text-xs text-gray-400">Password must be at least 12 characters.</p>

          {error && <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl px-3 py-2">{error}</p>}
          {success && <p className="text-sm text-green-700 bg-green-50 border border-green-200 rounded-xl px-3 py-2">Password changed successfully.</p>}

          <button type="submit" disabled={saving || !oldPassword || !newPassword || !confirmNew}
            className="bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm">
            {saving ? 'Changing…' : 'Change Password'}
          </button>
        </form>
      </div>

      <div className="border-t border-gray-100 pt-6">
        <h3 className="font-semibold text-gray-900 mb-1">Two-Factor Authentication</h3>
        <p className="text-sm text-gray-500 mb-3">Add an extra layer of security to your account.</p>
        <div className="border-2 border-dashed border-gray-200 rounded-xl p-6 text-center">
          <p className="text-sm text-gray-400">2FA setup coming in a future update.</p>
        </div>
      </div>
    </div>
  );
}
