import { useState, useEffect } from 'react';
import { fetchProfile, updateProfile } from '../../api/user';
import type { UserProfile } from '../../types';

interface Props { token: string; }

export function ProfileTab({ token }: Props) {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', address: '' });

  useEffect(() => {
    fetchProfile(token)
      .then(p => { setProfile(p); setForm({ firstName: p.firstName, lastName: p.lastName, email: p.email, phone: p.phone ?? '', address: p.address ?? '' }); })
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load profile'))
      .finally(() => setLoading(false));
  }, [token]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true); setError(null); setSuccess(false);
    try {
      const updated = await updateProfile(token, form);
      setProfile(updated);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return (
    <div className="space-y-4 animate-pulse">
      {[1,2,3].map(i => <div key={i} className="h-10 bg-gray-100 rounded-xl" />)}
    </div>
  );

  const inp = (name: keyof typeof form, label: string, type = 'text') => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type={type}
        value={form[name]}
        onChange={e => setForm(f => ({ ...f, [name]: e.target.value }))}
        className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400"
      />
    </div>
  );

  return (
    <form onSubmit={handleSave} className="space-y-4 max-w-lg">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Profile Information</h2>

      {profile && (
        <div className="flex items-center gap-3 mb-6">
          <div className="w-14 h-14 rounded-full bg-amber-100 flex items-center justify-center text-xl font-bold text-amber-700">
            {profile.firstName[0]}{profile.lastName[0]}
          </div>
          <div>
            <p className="font-semibold text-gray-900">{profile.firstName} {profile.lastName}</p>
            <p className="text-sm text-gray-500">@{profile.username}</p>
          </div>
        </div>
      )}

      <div className="grid grid-cols-2 gap-4">
        {inp('firstName', 'First name')}
        {inp('lastName', 'Last name')}
      </div>
      {inp('email', 'Email', 'email')}
      {inp('phone', 'Phone number', 'tel')}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
        <textarea
          value={form.address}
          onChange={e => setForm(f => ({ ...f, address: e.target.value }))}
          rows={2}
          className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400 resize-none"
        />
      </div>

      {error && <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl px-3 py-2">{error}</p>}
      {success && <p className="text-sm text-green-700 bg-green-50 border border-green-200 rounded-xl px-3 py-2">Profile saved successfully.</p>}

      <button type="submit" disabled={saving}
        className="bg-amber-500 hover:bg-amber-600 disabled:opacity-60 text-white font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm">
        {saving ? 'Saving…' : 'Save Changes'}
      </button>
    </form>
  );
}
