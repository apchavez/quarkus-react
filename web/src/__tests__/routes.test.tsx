import { describe, it, expect, vi } from 'vitest';
import { getAppRoutes } from '../routes';

vi.mock('../App', () => ({
  default: () => null,
}));

describe('getAppRoutes', () => {
  it('returns 2 routes', () => {
    const routes = getAppRoutes();
    expect(routes).toHaveLength(2);
  });

  it('has root path at index 0', () => {
    const routes = getAppRoutes();
    expect(routes[0].path).toBe('/');
  });

  it('has wildcard path at index 1', () => {
    const routes = getAppRoutes();
    expect(routes[1].path).toBe('*');
  });

  it('each route has an element', () => {
    const routes = getAppRoutes();
    routes.forEach((route) => {
      expect(route.element).toBeDefined();
    });
  });
});
