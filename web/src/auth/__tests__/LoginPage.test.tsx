import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import LoginPage from '../LoginPage';
import * as AuthContextModule from '../AuthContext';

function renderLoginPage(overrides: Partial<ReturnType<typeof AuthContextModule.useAuth>> = {}) {
  vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
    token: null,
    username: null,
    roles: [],
    isAuthenticated: false,
    login: vi.fn(),
    logout: vi.fn(),
    ...overrides,
  });

  return render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<div>home page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

afterEach(() => {
  vi.restoreAllMocks();
});

describe('LoginPage', () => {
  it('renders the login form', () => {
    renderLoginPage();
    expect(screen.getByText('Iniciar sesión')).toBeTruthy();
    expect(screen.getByLabelText('Usuario')).toBeTruthy();
    expect(screen.getByLabelText('Contraseña')).toBeTruthy();
  });

  it('calls login with the entered credentials and navigates home on success', async () => {
    const loginMock = vi.fn().mockResolvedValue(undefined);
    renderLoginPage({ login: loginMock });

    fireEvent.change(screen.getByLabelText('Usuario'), { target: { value: 'admin' } });
    fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'admin123' } });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));

    await waitFor(() => expect(loginMock).toHaveBeenCalledWith('admin', 'admin123'));
    await waitFor(() => expect(screen.getByText('home page')).toBeTruthy());
  });

  it('shows an error message when login fails', async () => {
    const loginMock = vi.fn().mockRejectedValue(new Error('Invalid'));
    renderLoginPage({ login: loginMock });

    fireEvent.change(screen.getByLabelText('Usuario'), { target: { value: 'admin' } });
    fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'wrong' } });
    fireEvent.click(screen.getByRole('button', { name: /ingresar/i }));

    await waitFor(() =>
      expect(screen.getByText('Usuario o contraseña incorrectos')).toBeTruthy()
    );
  });

  it('redirects immediately when already authenticated', () => {
    renderLoginPage({ isAuthenticated: true });
    expect(screen.getByText('home page')).toBeTruthy();
  });
});
