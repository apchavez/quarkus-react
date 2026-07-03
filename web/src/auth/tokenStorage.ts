const TOKEN_KEY = 'auth_token';
const USERNAME_KEY = 'auth_username';
const ROLES_KEY = 'auth_roles';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUsername(): string | null {
  return localStorage.getItem(USERNAME_KEY);
}

export function getRoles(): string[] {
  const raw = localStorage.getItem(ROLES_KEY);
  if (!raw) return [];
  try {
    return JSON.parse(raw) as string[];
  } catch {
    return [];
  }
}

export function setAuth(token: string, username: string, roles: string[]): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USERNAME_KEY, username);
  localStorage.setItem(ROLES_KEY, JSON.stringify(roles));
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USERNAME_KEY);
  localStorage.removeItem(ROLES_KEY);
}
