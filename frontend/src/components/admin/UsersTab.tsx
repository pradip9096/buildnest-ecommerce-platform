import { useState, useEffect } from 'react';
import { fetchAdminUsers, deleteAdminUser, type AdminUser } from '../../api/admin';

interface Props { token: string; }

export function UsersTab({ token }: Props) {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [disabling, setDisabling] = useState<number | null>(null);
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchAdminUsers(token)
      .then(setUsers)
      .catch(e => setError(e instanceof Error ? e.message : 'Failed to load users'))
      .finally(() => setLoading(false));
  }, [token]);

  const handleDisable = async (user: AdminUser) => {
    if (!confirm(`Disable account for @${user.username}? This action can be reversed by re-enabling the account in the database.`)) return;
    setDisabling(user.id);
    try {
      await deleteAdminUser(token, user.id);
      setUsers(prev => prev.map(u => u.id === user.id ? { ...u, enabled: false } : u));
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to disable user');
    } finally {
      setDisabling(null);
    }
  };

  const filtered = users.filter(u =>
    u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase()) ||
    `${u.firstName} ${u.lastName}`.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-lg font-semibold text-gray-900">Users</h2>
        <div className="flex items-center gap-3">
          <input
            type="search"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search users…"
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm w-48 focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <span className="text-sm text-gray-400">{users.length} total</span>
        </div>
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">User</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Roles</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(5)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">
                  {search ? 'No users match your search' : 'No users found'}
                </td>
              </tr>
            ) : filtered.map(user => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center text-sm font-bold text-primary-700 flex-shrink-0">
                      {user.firstName?.[0]?.toUpperCase() ?? user.username[0].toUpperCase()}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{user.firstName} {user.lastName}</p>
                      <p className="text-xs text-gray-400">@{user.username}</p>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3 text-gray-600">{user.email}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-1 flex-wrap">
                    {(user.roles ?? ['USER']).map(role => (
                      <span key={role}
                        className={`px-2 py-0.5 rounded-full text-xs font-semibold ${role === 'ADMIN' ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-600'}`}>
                        {role}
                      </span>
                    ))}
                  </div>
                </td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${user.enabled !== false ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {user.enabled !== false ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  {user.enabled !== false && (
                    <button
                      onClick={() => handleDisable(user)}
                      disabled={disabling === user.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 border border-red-200 hover:border-red-400 rounded-lg px-3 py-1 transition-colors disabled:opacity-50"
                    >
                      {disabling === user.id ? 'Disabling…' : 'Disable'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
