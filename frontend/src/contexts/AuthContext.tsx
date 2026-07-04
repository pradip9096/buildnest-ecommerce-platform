import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { apiLogin, apiLogout, apiRefresh, apiRegister } from '../api/auth';
import { fetchProfile } from '../api/user';
import { setUnauthorizedHandler } from '../api/client';
import type { AuthUser, AuthTokens } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  register: (payload: {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

// The JWT deliberately carries only subject/issuedAt/expiration (see spring-security.md) —
// it never contains the user's id or roles. Those must come from the login response or
// GET /api/user/profile, never decoded from the token.
function isTokenExpired(token: string): boolean {
  const payload = decodeJwtPayload(token);
  if (!payload) return true;
  const exp = payload['exp'] as number | undefined;
  return exp !== undefined && exp * 1000 < Date.now();
}

function normalizeRoles(roles: string[] | undefined): string[] {
  return (roles ?? []).map(r => r.replace('ROLE_', ''));
}

function storeTokens(tokens: AuthTokens) {
  localStorage.setItem('access_token', tokens.accessToken);
  if (tokens.refreshToken) localStorage.setItem('refresh_token', tokens.refreshToken);
}

function clearTokens() {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function restoreSession() {
      const stored = localStorage.getItem('access_token');
      if (!stored || isTokenExpired(stored)) {
        if (stored) clearTokens();
        setLoading(false);
        return;
      }
      try {
        const profile = await fetchProfile(stored);
        if (cancelled) return;
        setUser({ id: profile.id, username: profile.username, roles: normalizeRoles(profile.roles) });
        setToken(stored);
      } catch {
        if (!cancelled) clearTokens();
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    restoreSession();
    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const tokens = await apiLogin(username, password);
    storeTokens(tokens);
    setToken(tokens.accessToken);
    // Roles are never in the JWT or the login response (see spring-security.md) —
    // fetch the profile to get the authoritative role list.
    const profile = await fetchProfile(tokens.accessToken).catch(() => null);
    setUser({
      id: tokens.userId,
      username: tokens.username,
      roles: normalizeRoles(profile?.roles),
    });
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refresh_token');
    await apiLogout(refreshToken).catch(() => {});
    clearTokens();
    setUser(null);
    setToken(null);
  }, []);

  // Registered with the shared HTTP client so a 401 on any authenticated
  // request triggers one silent refresh-and-retry instead of forcing the
  // user to reload the page (or losing in-progress work) the moment the
  // short-lived access token expires mid-session.
  useEffect(() => {
    const handleUnauthorized = async (): Promise<string | null> => {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        clearTokens();
        setUser(null);
        setToken(null);
        return null;
      }
      try {
        const tokens = await apiRefresh(refreshToken);
        storeTokens(tokens);
        setToken(tokens.accessToken);
        return tokens.accessToken;
      } catch {
        clearTokens();
        setUser(null);
        setToken(null);
        return null;
      }
    };

    setUnauthorizedHandler(handleUnauthorized);
    return () => setUnauthorizedHandler(null);
  }, []);

  const register = useCallback(async (payload: {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => {
    await apiRegister(payload);
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!user, loading, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
