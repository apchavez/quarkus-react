import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { apiFetch, UnauthorizedError } from '../httpClient';
import { setAuth, clearAuth, getToken } from '../../auth/tokenStorage';

const mockFetch = vi.fn();

beforeEach(() => {
  vi.stubGlobal('fetch', mockFetch);
  mockFetch.mockReset();
  localStorage.clear();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('apiFetch', () => {
  it('adds Authorization header when a token is stored', async () => {
    setAuth('jwt-token', 'admin', ['ADMIN']);
    mockFetch.mockResolvedValue({ ok: true, status: 200 });

    await apiFetch('/api/v1/products');

    const [, init] = mockFetch.mock.calls[0];
    const headers = new Headers(init.headers);
    expect(headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('omits Authorization header when no token is stored', async () => {
    mockFetch.mockResolvedValue({ ok: true, status: 200 });

    await apiFetch('/api/v1/products');

    const [, init] = mockFetch.mock.calls[0];
    const headers = new Headers(init.headers);
    expect(headers.get('Authorization')).toBeNull();
  });

  it('clears auth and throws UnauthorizedError on 401', async () => {
    setAuth('jwt-token', 'admin', ['ADMIN']);
    mockFetch.mockResolvedValue({ ok: false, status: 401 });

    await expect(apiFetch('/api/v1/products')).rejects.toBeInstanceOf(UnauthorizedError);
    expect(getToken()).toBeNull();
  });

  it('returns the response as-is on non-401 responses', async () => {
    mockFetch.mockResolvedValue({ ok: false, status: 500 });

    const response = await apiFetch('/api/v1/products');

    expect(response.status).toBe(500);
  });
});
