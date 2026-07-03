import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../AuthContext';
import * as authApi from '../authApi';

function Consumer() {
  const { isAuthenticated, username, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="status">{isAuthenticated ? 'in' : 'out'}</span>
      <span data-testid="username">{username ?? ''}</span>
      <button onClick={() => login('admin', 'admin123')}>login</button>
      <button onClick={logout}>logout</button>
    </div>
  );
}

beforeEach(() => {
  localStorage.clear();
});

afterEach(() => {
  vi.restoreAllMocks();
});

describe('AuthProvider / useAuth', () => {
  it('starts unauthenticated when no token is stored', () => {
    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('status').textContent).toBe('out');
  });

  it('becomes authenticated after a successful login', async () => {
    vi.spyOn(authApi, 'login').mockResolvedValue({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'admin',
      roles: ['ADMIN'],
    });

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>
    );

    fireEvent.click(screen.getByText('login'));

    await waitFor(() => expect(screen.getByTestId('status').textContent).toBe('in'));
    expect(screen.getByTestId('username').textContent).toBe('admin');
  });

  it('clears state on logout', async () => {
    vi.spyOn(authApi, 'login').mockResolvedValue({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      username: 'admin',
      roles: ['ADMIN'],
    });

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>
    );

    fireEvent.click(screen.getByText('login'));
    await waitFor(() => expect(screen.getByTestId('status').textContent).toBe('in'));

    fireEvent.click(screen.getByText('logout'));
    expect(screen.getByTestId('status').textContent).toBe('out');
  });

  it('throws when useAuth is used outside AuthProvider', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => render(<Consumer />)).toThrow('useAuth must be used within an AuthProvider');
    consoleError.mockRestore();
  });
});
