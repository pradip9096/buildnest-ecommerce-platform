import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { apiFetchCsrf, apiLogin, apiLogout, apiRefresh, apiRegister } from '../api/auth';
import { fetchProfile } from '../api/user';
import { setUnauthorizedHandler } from '../api/client';
import type { AuthUser } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
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
    consentGiven: boolean;
  }) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function normalizeRoles(roles: string[] | undefined): string[] {
  return (roles ?? []).map(r => r.replace('ROLE_', ''));
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function restoreSession() {
      // Tokens are httpOnly cookies (SEC-15) — unreadable from JS, so rehydration
      // relies entirely on whether the browser still holds a valid session cookie.
      await apiFetchCsrf().catch(() => {});
      try {
        const profile = await fetchProfile();
        if (cancelled) return;
        setUser({ id: profile.id, username: profile.username, roles: normalizeRoles(profile.roles) });
      } catch {
        // no valid session — stay logged out
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
    const authUser = await apiLogin(username, password);
    // Roles are never in the JWT or the login response (see spring-security.md) —
    // fetch the profile to get the authoritative role list. If that fetch fails
    // right after a successful login (e.g. a network blip), fall back to the
    // login response's identity fields with no roles rather than failing the
    // whole login — auth already succeeded at this point.
    try {
      const profile = await fetchProfile();
      setUser({ id: profile.id, username: profile.username, roles: normalizeRoles(profile.roles) });
    } catch {
      setUser({ id: authUser.userId, username: authUser.username, roles: [] });
    }
  }, []);

  const logout = useCallback(async () => {
    await apiLogout().catch(() => {});
    setUser(null);
  }, []);

  // Registered with the shared HTTP client so a 401 on any authenticated
  // request triggers one silent refresh-and-retry instead of forcing the
  // user to reload the page (or losing in-progress work) the moment the
  // short-lived access token expires mid-session.
  useEffect(() => {
    const handleUnauthorized = async (): Promise<boolean> => {
      try {
        await apiRefresh();
        return true;
      } catch {
        setUser(null);
        return false;
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
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, loading, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

// Context files conventionally export both the provider and its consumer hook together.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
