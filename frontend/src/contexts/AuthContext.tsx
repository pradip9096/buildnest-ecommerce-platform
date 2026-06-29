import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { apiLogin, apiLogout, apiRegister } from '../api/auth';
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

function tokenToUser(token: string): AuthUser | null {
  const payload = decodeJwtPayload(token);
  if (!payload) return null;
  const exp = payload['exp'] as number | undefined;
  if (exp && exp * 1000 < Date.now()) return null;
  return {
    id: (payload['id'] as number) ?? (payload['userId'] as number) ?? 0,
    username: (payload['sub'] as string) ?? '',
    roles: (
      (payload['roles'] as string[] | undefined) ??
      (payload['authorities'] as string[] | undefined) ??
      []
    ).map(r => r.replace('ROLE_', '')),
  };
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
    const stored = localStorage.getItem('access_token');
    if (stored) {
      const parsed = tokenToUser(stored);
      if (parsed) {
        setUser(parsed);
        setToken(stored);
      } else {
        clearTokens();
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const tokens = await apiLogin(username, password);
    storeTokens(tokens);
    const parsed = tokenToUser(tokens.accessToken);
    setToken(tokens.accessToken);
    setUser(parsed ?? { id: tokens.userId, username: tokens.username, roles: ['USER'] });
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refresh_token');
    await apiLogout(refreshToken).catch(() => {});
    clearTokens();
    setUser(null);
    setToken(null);
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
