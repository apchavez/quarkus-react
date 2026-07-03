import { renderHook, act, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useProducts } from '../useProducts';
import * as api from '../../api/productsApi';
import type { Product } from '../../types/product';

vi.mock('../../api/productsApi');

const mockProduct: Product = {
  id: 'aabbccddeeff001122334455',
  sku: 'SKU-001',
  name: 'Laptop Pro',
  description: 'Una laptop de alto rendimiento',
  category: 'Technology',
  price: 999.99,
  stock: 10,
  active: true,
};

const mockPage = {
  content: [mockProduct],
  totalPages: 3,
  totalElements: 1,
  number: 0,
  size: 10,
};

beforeEach(() => {
  vi.mocked(api.getProducts).mockResolvedValue(mockPage);
});

afterEach(() => {
  vi.clearAllMocks();
});

describe('useProducts — carga inicial', () => {
  it('carga los productos al montar y actualiza totalPages', async () => {
    const { result } = renderHook(() => useProducts());

    await waitFor(() => expect(result.current.products).toHaveLength(1));

    expect(api.getProducts).toHaveBeenCalledWith(0, 10);
    expect(result.current.products[0]).toEqual(mockProduct);
    expect(result.current.totalPages).toBe(3);
  });

  it('muestra mensaje de error cuando falla la carga', async () => {
    vi.mocked(api.getProducts).mockRejectedValueOnce(new Error('Network error'));

    const { result } = renderHook(() => useProducts());

    await waitFor(() =>
      expect(result.current.message).toBe('Error cargando productos')
    );
    expect(result.current.products).toHaveLength(0);
  });
});

describe('useProducts — handleSubmit (crear)', () => {
  it('llama createProduct cuando el producto no tiene id', async () => {
    vi.mocked(api.createProduct).mockResolvedValue(mockProduct);
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    const newProduct: Product = { ...mockProduct, id: undefined };
    await act(async () => {
      await result.current.handleSubmit(newProduct);
    });

    expect(api.createProduct).toHaveBeenCalledWith(newProduct);
    expect(api.updateProduct).not.toHaveBeenCalled();
    expect(result.current.message).toBe('Producto creado');
    expect(result.current.editingProduct).toBeNull();
    expect(api.getProducts).toHaveBeenCalledTimes(2);
  });

  it('muestra el mensaje de error cuando falla la creación', async () => {
    vi.mocked(api.createProduct).mockRejectedValueOnce(
      new Error('Error al crear producto')
    );
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    await act(async () => {
      await result.current.handleSubmit({ ...mockProduct, id: undefined });
    });

    expect(result.current.message).toBe('Error al crear producto');
  });
});

describe('useProducts — handleSubmit (actualizar)', () => {
  it('llama updateProduct cuando el producto tiene id', async () => {
    vi.mocked(api.updateProduct).mockResolvedValue(mockProduct);
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    await act(async () => {
      await result.current.handleSubmit(mockProduct);
    });

    expect(api.updateProduct).toHaveBeenCalledWith(mockProduct.id, mockProduct);
    expect(api.createProduct).not.toHaveBeenCalled();
    expect(result.current.message).toBe('Producto actualizado');
    expect(result.current.editingProduct).toBeNull();
  });

  it('muestra el mensaje de error cuando falla la actualización', async () => {
    vi.mocked(api.updateProduct).mockRejectedValueOnce(
      new Error('Error al actualizar producto')
    );
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    await act(async () => {
      await result.current.handleSubmit(mockProduct);
    });

    expect(result.current.message).toBe('Error al actualizar producto');
  });
});

describe('useProducts — handleDelete', () => {
  it('llama deleteProduct con el id correcto y recarga la lista', async () => {
    vi.mocked(api.deleteProduct).mockResolvedValue(undefined);
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    await act(async () => {
      await result.current.handleDelete('aabbccddeeff001122334455');
    });

    expect(api.deleteProduct).toHaveBeenCalledWith('aabbccddeeff001122334455');
    expect(result.current.message).toBe('Producto eliminado');
    expect(api.getProducts).toHaveBeenCalledTimes(2);
  });

  it('muestra el mensaje de error cuando falla la eliminación', async () => {
    vi.mocked(api.deleteProduct).mockRejectedValueOnce(
      new Error('Error al eliminar producto')
    );
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    await act(async () => {
      await result.current.handleDelete('aabbccddeeff001122334455');
    });

    expect(result.current.message).toBe('Error al eliminar producto');
  });
});

describe('useProducts — paginación y estado de edición', () => {
  it('cambia de página y llama getProducts con el offset correcto', async () => {
    const { result } = renderHook(() => useProducts());
    await waitFor(() => expect(result.current.products).toHaveLength(1));

    act(() => {
      result.current.setPage(2);
    });

    await waitFor(() =>
      expect(api.getProducts).toHaveBeenCalledWith(1, 10)
    );
  });

  it('setEditingProduct actualiza el producto en edición', () => {
    const { result } = renderHook(() => useProducts());

    act(() => {
      result.current.setEditingProduct(mockProduct);
    });

    expect(result.current.editingProduct).toEqual(mockProduct);
  });

  it('setEditingProduct con null limpia el producto en edición', () => {
    const { result } = renderHook(() => useProducts());

    act(() => {
      result.current.setEditingProduct(mockProduct);
    });
    act(() => {
      result.current.setEditingProduct(null);
    });

    expect(result.current.editingProduct).toBeNull();
  });
});
