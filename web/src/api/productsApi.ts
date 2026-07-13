import type { Product } from '../types/product';
import { apiFetch } from './httpClient';

const BASE_URL = '/api/v1/products';

export async function getProducts(page = 0, size = 10) {
  const response = await apiFetch(`${BASE_URL}?page=${page}&size=${size}`);

  if (!response.ok) {
    throw new Error('Error al obtener productos');
  }

  const json = await response.json();

  const products = json?.data?.products ?? [];
  const totalPages = json?.data?.totalPages ?? 1;
  const totalElements = json?.data?.totalItems ?? products.length;

  return {
    content: products,
    totalPages,
    totalElements,
    number: page,
    size
  };
}

export async function createProduct(product: Product): Promise<Product> {
  const response = await apiFetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(product)
  });
  if (!response.ok) throw new Error('Error al crear producto');
  return response.json();
}

export async function updateProduct(id: string, product: Product): Promise<Product> {
  const response = await apiFetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(product)
  });
  if (!response.ok) throw new Error('Error al actualizar producto');
  return response.json();
}

export async function deleteProduct(id: string): Promise<void> {
  const response = await apiFetch(`${BASE_URL}/${id}`, {
    method: 'DELETE'
  });
  if (!response.ok) throw new Error('Error al eliminar producto');
}
