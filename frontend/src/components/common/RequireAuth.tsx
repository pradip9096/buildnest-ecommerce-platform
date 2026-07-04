import type { ReactNode } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

type Props = {
  children: ReactNode;
  role?: string;
};

export function RequireAuth({ children, role }: Props) {
  const { isAuthenticated, user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-primary-400 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  const hasRequiredRole = !role || (user?.roles?.includes(role) ?? false);

  if (!hasRequiredRole) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-sm">
          <div className="text-5xl mb-4">🚫</div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Access denied</h1>
          <p className="text-gray-500 text-sm mb-4">{role} role required.</p>
          <Link to="/" className="text-primary-600 hover:text-primary-800 text-sm font-medium">← Back to store</Link>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
