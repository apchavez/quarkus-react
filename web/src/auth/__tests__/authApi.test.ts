import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { login } from '../authApi';

const mockFetch = vi.fn();

beforeEach(() => {
  vi.stubGlobal('fetch', mockFetch);
  mockFetch.mockReset();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('login', () => {
  it('returns the login result on success', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({
        data: { token: 'jwt-token', tokenType: 'Bearer', expiresIn: 3600, username: 'admin', roles: ['ADMIN'] },
      }),
    });

    const result = await login('admin', 'admin123');

    expect(result).toEqual({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'admin',
      roles: ['ADMIN'],
    });
    expect(mockFetch).toHaveBeenCalledWith(
      '/api/v1/auth/login',
      expect.objectContaining({ method: 'POST' })
    );
  });

  it('throws on invalid credentials', async () => {
    mockFetch.mockResolvedValue({ ok: false });

    await expect(login('admin', 'wrong')).rejects.toThrow('Usuario o contraseña incorrectos');
  });
});
