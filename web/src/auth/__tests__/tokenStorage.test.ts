import { describe, it, expect, beforeEach } from 'vitest';
import { getToken, getUsername, getRoles, setAuth, clearAuth } from '../tokenStorage';

beforeEach(() => {
  localStorage.clear();
});

describe('tokenStorage', () => {
  it('returns null/empty when nothing stored', () => {
    expect(getToken()).toBeNull();
    expect(getUsername()).toBeNull();
    expect(getRoles()).toEqual([]);
  });

  it('persists and reads back auth state', () => {
    setAuth('jwt-token', 'admin', ['ADMIN', 'USER']);

    expect(getToken()).toBe('jwt-token');
    expect(getUsername()).toBe('admin');
    expect(getRoles()).toEqual(['ADMIN', 'USER']);
  });

  it('clears all auth state', () => {
    setAuth('jwt-token', 'admin', ['ADMIN']);
    clearAuth();

    expect(getToken()).toBeNull();
    expect(getUsername()).toBeNull();
    expect(getRoles()).toEqual([]);
  });

  it('returns empty roles array when stored value is malformed JSON', () => {
    localStorage.setItem('auth_roles', 'not-json');
    expect(getRoles()).toEqual([]);
  });
});
