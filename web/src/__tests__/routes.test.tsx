import { describe, it, expect, vi } from 'vitest';
import { getAppRoutes } from '../routes';

vi.mock('../App', () => ({
  default: () => null,
}));
vi.mock('../auth/LoginPage', () => ({
  default: () => null,
}));
vi.mock('../auth/RequireAuth', () => ({
  default: ({ children }: { children: React.ReactNode }) => children,
}));
vi.mock('../auth/AppLayout', () => ({
  default: ({ children }: { children: React.ReactNode }) => children,
}));

describe('getAppRoutes', () => {
  it('returns 3 routes', () => {
    const routes = getAppRoutes();
    expect(routes).toHaveLength(3);
  });

  it('has login path at index 0', () => {
    const routes = getAppRoutes();
    expect(routes[0].path).toBe('/login');
  });

  it('has root path at index 1', () => {
    const routes = getAppRoutes();
    expect(routes[1].path).toBe('/');
  });

  it('has wildcard path at index 2', () => {
    const routes = getAppRoutes();
    expect(routes[2].path).toBe('*');
  });

  it('each route has an element', () => {
    const routes = getAppRoutes();
    routes.forEach((route) => {
      expect(route.element).toBeDefined();
    });
  });
});
