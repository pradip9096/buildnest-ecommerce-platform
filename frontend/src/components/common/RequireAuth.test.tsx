import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { RequireAuth } from './RequireAuth';
import { useAuth } from '../../hooks/useAuth';
import type { AuthUser } from '../../types';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

const mockUseAuth = vi.mocked(useAuth);

function renderAt(path: string, role?: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/protected"
          element={
            <RequireAuth role={role}>
              <div>Protected content</div>
            </RequireAuth>
          }
        />
        <Route path="/login" element={<div>Login page</div>} />
        <Route path="/" element={<div>Home page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

function authState(overrides: Partial<ReturnType<typeof useAuth>>) {
  return {
    user: null,
    token: null,
    isAuthenticated: false,
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    ...overrides,
  };
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('RequireAuth', () => {
  it('shows a loading spinner while auth state is resolving', () => {
    mockUseAuth.mockReturnValue(authState({ loading: true }));

    const { container } = renderAt('/protected');

    expect(container.querySelector('.animate-spin')).toBeTruthy();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('redirects to /login when unauthenticated', () => {
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: false }));

    renderAt('/protected');

    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('renders the protected content when authenticated with no role required', () => {
    const user: AuthUser = { id: 1, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));

    renderAt('/protected');

    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  it('renders an access-denied message when the required role is missing', () => {
    const user: AuthUser = { id: 1, username: 'alice', roles: ['USER'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));

    renderAt('/protected', 'ADMIN');

    expect(screen.getByText('Access denied')).toBeInTheDocument();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('renders the protected content when the user has the required role', () => {
    const user: AuthUser = { id: 1, username: 'bob', roles: ['ADMIN'] };
    mockUseAuth.mockReturnValue(authState({ isAuthenticated: true, user }));

    renderAt('/protected', 'ADMIN');

    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });
});
