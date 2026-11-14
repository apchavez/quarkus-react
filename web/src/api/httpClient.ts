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

  let response: Response;
  try {
    response = await fetch(input, { ...init, headers });
  } catch (error) {
    console.error(JSON.stringify({ event: 'api_request_failed', url: input, method: init.method ?? 'GET', error: String(error) }));
    throw error;
  }

  if (response.status === 401) {
    console.warn(JSON.stringify({ event: 'api_unauthorized', url: input, method: init.method ?? 'GET' }));
    clearAuth();
    if (typeof window !== 'undefined') {
      window.location.assign('/login');
    }
    throw new UnauthorizedError();
  }

  if (!response.ok) {
    console.error(JSON.stringify({ event: 'api_response_error', url: input, method: init.method ?? 'GET', status: response.status }));
  }

  return response;
}
