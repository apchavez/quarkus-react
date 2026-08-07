import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import AppLayout from '../AppLayout';
import * as AuthContextModule from '../AuthContext';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('AppLayout', () => {
  it('renders the username and children', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      token: 'jwt-token',
      username: 'admin',
      roles: ['ADMIN'],
      isAuthenticated: true,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(
      <AppLayout>
        <div>child content</div>
      </AppLayout>
    );

    expect(screen.getByText('admin')).toBeTruthy();
    expect(screen.getByText('child content')).toBeTruthy();
  });

  it('calls logout when the button is clicked', () => {
    const logoutMock = vi.fn();
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      token: 'jwt-token',
      username: 'admin',
      roles: ['ADMIN'],
      isAuthenticated: true,
      login: vi.fn(),
      logout: logoutMock,
    });

    render(
      <AppLayout>
        <div>child content</div>
      </AppLayout>
    );

    fireEvent.click(screen.getByText('Cerrar sesión'));
    expect(logoutMock).toHaveBeenCalledTimes(1);
  });
});
