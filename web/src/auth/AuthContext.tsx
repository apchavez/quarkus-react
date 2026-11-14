import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { login as loginRequest } from './authApi';
import { getToken, getUsername, getRoles, setAuth, clearAuth } from './tokenStorage';

interface AuthState {
  token: string | null;
  username: string | null;
  roles: string[];
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getToken());
  const [username, setUsername] = useState<string | null>(() => getUsername());
  const [roles, setRoles] = useState<string[]>(() => getRoles());

  const login = useCallback(async (user: string, password: string) => {
    const result = await loginRequest(user, password);
    setAuth(result.token, result.username, result.roles);
    setToken(result.token);
    setUsername(result.username);
    setRoles(result.roles);
  }, []);

  const logout = useCallback(() => {
    clearAuth();
    setToken(null);
    setUsername(null);
    setRoles([]);
  }, []);

  return (
    <AuthContext.Provider
      value={{ token, username, roles, isAuthenticated: !!token, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
