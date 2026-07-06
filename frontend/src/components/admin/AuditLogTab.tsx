import { useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import { fetchAuditLogs, type AuditLogEntry, type AuditLogPage } from '../../api/admin';

export function AuditLogTab() {
  const [page, setPage] = useState(0);
  const { data, loading, error } = useAsync<AuditLogPage>(
    () => fetchAuditLogs(page, 20),
    [page]
  );
  const entries: AuditLogEntry[] = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-gray-900">Audit Log</h2>
        <span className="text-sm text-gray-400">{totalElements.toLocaleString()} entries</span>
      </div>

      {error && <p className="text-red-600 text-sm">{error}</p>}

      <div className="overflow-x-auto rounded-xl border border-gray-100">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 text-left text-gray-500 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Timestamp</th>
              <th className="px-4 py-3">User</th>
              <th className="px-4 py-3">Action</th>
              <th className="px-4 py-3">Entity</th>
              <th className="px-4 py-3">IP</th>
              <th className="px-4 py-3">Details</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {loading ? (
              [...Array(8)].map((_, i) => (
                <tr key={i} className="animate-pulse">
                  {[...Array(6)].map((__, j) => (
                    <td key={j} className="px-4 py-3"><div className="h-4 bg-gray-100 rounded" /></td>
                  ))}
                </tr>
              ))
            ) : entries.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">No audit entries found</td>
              </tr>
            ) : entries.map(entry => (
              <tr key={entry.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3 text-gray-500 whitespace-nowrap font-mono text-xs">
                  {new Date(entry.timestamp).toLocaleString('en-IN', {
                    day: '2-digit', month: 'short', year: 'numeric',
                    hour: '2-digit', minute: '2-digit', second: '2-digit',
                  })}
                </td>
                <td className="px-4 py-3">
                  {entry.username ? (
                    <span className="font-medium text-gray-900">@{entry.username}</span>
                  ) : entry.userId ? (
                    <span className="text-gray-500">User #{entry.userId}</span>
                  ) : (
                    <span className="text-gray-300">—</span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-primary-50 text-primary-700 font-mono">
                    {entry.action}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-600">
                  {entry.entityType}
                  {entry.entityId && <span className="text-gray-400"> #{entry.entityId}</span>}
                </td>
                <td className="px-4 py-3 text-gray-400 font-mono text-xs">{entry.ipAddress ?? '—'}</td>
                <td className="px-4 py-3 text-gray-500 max-w-xs truncate" title={entry.details ?? undefined}>
                  {entry.details ?? <span className="text-gray-300">—</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            type="button"
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >
            ← Prev
          </button>
          <span className="text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
          <button
            type="button"
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="px-3 py-1.5 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >
            Next →
          </button>
        </div>
      )}
    </div>
  );
}
