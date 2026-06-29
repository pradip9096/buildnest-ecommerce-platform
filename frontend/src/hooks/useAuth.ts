import { useMemo } from 'react';

interface AuthUser {
  id: number;
  username: string;
  roles: string[];
}

interface AuthState {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
}

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

export function useAuth(): AuthState {
  return useMemo(() => {
    const token = localStorage.getItem('access_token');
    if (!token) return { user: null, token: null, isAuthenticated: false };

    const payload = decodeJwtPayload(token);
    if (!payload) return { user: null, token: null, isAuthenticated: false };

    const exp = payload['exp'] as number | undefined;
    if (exp && exp * 1000 < Date.now()) {
      return { user: null, token: null, isAuthenticated: false };
    }

    const roles = (
      (payload['roles'] as string[] | undefined) ??
      (payload['authorities'] as string[] | undefined) ??
      []
    ).map(r => r.replace('ROLE_', ''));

    const user: AuthUser = {
      id: payload['id'] as number ?? payload['userId'] as number ?? 0,
      username: (payload['sub'] as string) ?? '',
      roles,
    };

    return { user, token, isAuthenticated: true };
  }, []);
}
