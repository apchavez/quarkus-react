import { useCallback, useEffect, useState } from 'react';
import { createProduct, deleteProduct, getProducts, updateProduct } from '../api/productsApi';
import type { Product } from '../types/product';

export function useProducts() {
  const [products, setProducts] = useState<Product[]>([]);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [message, setMessage] = useState('');

  const loadProducts = useCallback(async (pageNumber: number) => {
    try {
      const data = await getProducts(pageNumber - 1, 10);
      setProducts(data.content ?? []);
      setTotalPages(data.totalPages ?? 1);
    } catch {
      setMessage('Error cargando productos');
    }
  }, []);

  useEffect(() => {
    loadProducts(page);
  }, [page, loadProducts]);

  const handleSubmit = async (product: Product) => {
    try {
      if (product.id) {
        await updateProduct(product.id, product);
        setMessage('Producto actualizado');
      } else {
        await createProduct(product);
        setMessage('Producto creado');
      }
      setEditingProduct(null);
      await loadProducts(page);
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Error guardando producto');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteProduct(id);
      setMessage('Producto eliminado');
      await loadProducts(page);
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Error eliminando producto');
    }
  };

  return {
    products,
    editingProduct,
    setEditingProduct,
    page,
    setPage,
    totalPages,
    message,
    setMessage,
    handleSubmit,
    handleDelete,
    reload: () => loadProducts(page),
  };
}
