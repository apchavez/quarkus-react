import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { getProducts, createProduct, updateProduct, deleteProduct } from '../productsApi';
import type { Product } from '../../types/product';

const mockProduct: Product = {
  id: 'aabbccddeeff001122334455',
  sku: 'SKU-001',
  name: 'Laptop Pro',
  description: 'Test description',
  category: 'Technology',
  price: 999.99,
  stock: 10,
  active: true,
};

const mockFetch = vi.fn();

beforeEach(() => {
  vi.stubGlobal('fetch', mockFetch);
  mockFetch.mockReset();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('getProducts', () => {
  it('returns mapped products on success', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({
        data: { products: [mockProduct], totalPages: 3, totalItems: 25 },
      }),
    });

    const result = await getProducts(0, 10);

    expect(result.content).toEqual([mockProduct]);
    expect(result.totalPages).toBe(3);
    expect(result.totalElements).toBe(25);
    expect(result.number).toBe(0);
    expect(result.size).toBe(10);
  });

  it('uses default page and size', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => ({ data: { products: [], totalPages: 1, totalItems: 0 } }),
    });

    await getProducts();

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('page=0'),
      expect.anything()
    );
  });

  it('throws on error response', async () => {
    mockFetch.mockResolvedValue({ ok: false });
    await expect(getProducts()).rejects.toThrow('Error al obtener productos');
  });
});

describe('createProduct', () => {
  it('returns created product on success', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => mockProduct,
    });

    const result = await createProduct(mockProduct);

    expect(result).toEqual(mockProduct);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({ method: 'POST' })
    );
  });

  it('throws on error response', async () => {
    mockFetch.mockResolvedValue({ ok: false });
    await expect(createProduct(mockProduct)).rejects.toThrow('Error al crear producto');
  });
});

describe('updateProduct', () => {
  it('returns updated product on success', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => mockProduct,
    });

    const result = await updateProduct('aabbccddeeff001122334455', mockProduct);

    expect(result).toEqual(mockProduct);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('aabbccddeeff001122334455'),
      expect.objectContaining({ method: 'PUT' })
    );
  });

  it('throws on error response', async () => {
    mockFetch.mockResolvedValue({ ok: false });
    await expect(updateProduct('id', mockProduct)).rejects.toThrow('Error al actualizar producto');
  });
});

describe('deleteProduct', () => {
  it('resolves without value on success', async () => {
    mockFetch.mockResolvedValue({ ok: true });

    await expect(deleteProduct('aabbccddeeff001122334455')).resolves.toBeUndefined();
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('aabbccddeeff001122334455'),
      expect.objectContaining({ method: 'DELETE' })
    );
  });

  it('throws on error response', async () => {
    mockFetch.mockResolvedValue({ ok: false });
    await expect(deleteProduct('id')).rejects.toThrow('Error al eliminar producto');
  });
});
