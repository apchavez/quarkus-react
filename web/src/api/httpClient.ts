import { getToken, clearAuth } from '../auth/tokenStorage';

export class UnauthorizedError extends Error {
  constructor() {
    super('Unauthorized');
    this.name = 'UnauthorizedError';
  }
}

export async function apiFetch(input: string, init: RequestInit = {}): Promise<Response> {
  const token = getToken();
  const headers = new Headers(init.headers);
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(input, { ...init, headers });

  if (response.status === 401) {
    clearAuth();
    if (typeof window !== 'undefined') {
      window.location.assign('/login');
    }
    throw new UnauthorizedError();
  }

  return response;
}
