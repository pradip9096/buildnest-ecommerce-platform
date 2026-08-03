import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { exportMyData, deleteMyAccount } from '../../api/user';

/** GDPR right-to-access/erasure controls (#128, COMP-01). */
export function PrivacyTab() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [exporting, setExporting] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [confirmingDelete, setConfirmingDelete] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleExport = async () => {
    setExporting(true);
    setError(null);
    try {
      const data = await exportMyData();
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'buildnest-my-data.json';
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to export your data');
    } finally {
      setExporting(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    setError(null);
    try {
      await deleteMyAccount();
      await logout();
      navigate('/');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to delete your account');
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-8 max-w-lg">
      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-1">Export your data</h2>
        <p className="text-sm text-gray-500 mb-3">
          Download a copy of all data associated with your account (GDPR right to access).
        </p>
        <button type="button" onClick={handleExport} disabled={exporting}
          data-testid="privacy-export-button"
          className="bg-primary-500 hover:bg-primary-600 disabled:opacity-60 text-white font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm">
          {exporting ? 'Preparing export…' : 'Download my data'}
        </button>
      </div>

      <div className="border-t border-gray-100 pt-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-1">Delete your account</h2>
        <p className="text-sm text-gray-500 mb-3">
          Your account is deactivated immediately and your personal information is
          permanently anonymised 30 days later (GDPR right to erasure). This cannot be undone.
        </p>

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl px-3 py-2 mb-3">
            {error}
          </p>
        )}

        {!confirmingDelete ? (
          <button type="button" onClick={() => setConfirmingDelete(true)}
            data-testid="privacy-delete-button"
            className="border border-red-300 text-red-600 hover:bg-red-50 font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm">
            Delete my account
          </button>
        ) : (
          <div className="space-y-3">
            <p className="text-sm font-medium text-gray-900">Are you sure? This cannot be undone.</p>
            <div className="flex gap-3">
              <button type="button" onClick={handleDelete} disabled={deleting}
                data-testid="privacy-delete-confirm"
                className="bg-red-600 hover:bg-red-700 disabled:opacity-60 text-white font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm">
                {deleting ? 'Deleting…' : 'Yes, delete my account'}
              </button>
              <button type="button" onClick={() => setConfirmingDelete(false)} disabled={deleting}
                className="text-gray-600 hover:bg-gray-100 font-semibold px-6 py-2.5 rounded-xl transition-colors text-sm">
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
