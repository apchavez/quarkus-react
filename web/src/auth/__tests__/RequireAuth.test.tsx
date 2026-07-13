import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import RequireAuth from '../RequireAuth';
import * as AuthContextModule from '../AuthContext';

function renderWithAuth(isAuthenticated: boolean) {
  vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
    token: isAuthenticated ? 'jwt-token' : null,
    username: isAuthenticated ? 'admin' : null,
    roles: [],
    isAuthenticated,
    login: vi.fn(),
    logout: vi.fn(),
  });

  return render(
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route path="/login" element={<div>login page</div>} />
        <Route
          path="/"
          element={
            <RequireAuth>
              <div>protected content</div>
            </RequireAuth>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

describe('RequireAuth', () => {
  it('renders children when authenticated', () => {
    renderWithAuth(true);
    expect(screen.getByText('protected content')).toBeTruthy();
  });

  it('redirects to /login when not authenticated', () => {
    renderWithAuth(false);
    expect(screen.getByText('login page')).toBeTruthy();
    expect(screen.queryByText('protected content')).toBeNull();
  });
});
