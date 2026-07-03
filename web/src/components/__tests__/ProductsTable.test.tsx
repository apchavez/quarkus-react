import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProductsTable from '../ProductsTable';
import type { Product } from '../../types/product';

const onEdit = vi.fn();
const onDelete = vi.fn();

const products: Product[] = [
  {
    id: 'aabbccddeeff001122334455',
    sku: 'SKU-001',
    name: 'Laptop Pro',
    description: 'Great laptop',
    category: 'Technology',
    price: 999.99,
    stock: 10,
    active: true,
  },
  {
    id: 'aabbccddeeff001122334456',
    sku: 'SKU-002',
    name: 'Mouse Inalámbrico',
    description: '',
    category: 'Periféricos',
    price: 29.99,
    stock: 50,
    active: false,
  },
];

function renderTable(items: Product[] = products) {
  return render(
    <ProductsTable products={items} onEdit={onEdit} onDelete={onDelete} />
  );
}

describe('ProductsTable — lista vacía', () => {
  it('muestra mensaje cuando no hay productos', () => {
    renderTable([]);
    expect(screen.getByText('No hay productos')).toBeInTheDocument();
  });
});

describe('ProductsTable — renderizado de productos', () => {
  it('muestra una fila por cada producto', () => {
    renderTable();
    expect(screen.getByText('SKU-001')).toBeInTheDocument();
    expect(screen.getByText('SKU-002')).toBeInTheDocument();
    expect(screen.getByText('Laptop Pro')).toBeInTheDocument();
    expect(screen.getByText('Mouse Inalámbrico')).toBeInTheDocument();
  });

  it('muestra Sí / No para el campo activo', () => {
    renderTable();
    expect(screen.getByText('Sí')).toBeInTheDocument();
    expect(screen.getByText('No')).toBeInTheDocument();
  });

  it('muestra los encabezados de la tabla', () => {
    renderTable();
    expect(screen.getByText('SKU')).toBeInTheDocument();
    expect(screen.getByText('Nombre')).toBeInTheDocument();
    expect(screen.getByText('Precio')).toBeInTheDocument();
    expect(screen.getByText('Stock')).toBeInTheDocument();
    expect(screen.getByText('Acciones')).toBeInTheDocument();
  });
});

describe('ProductsTable — acciones', () => {
  it('llama onEdit con el producto al hacer clic en Editar', async () => {
    renderTable();
    const editButtons = screen.getAllByRole('button', { name: 'Editar' });
    await userEvent.click(editButtons[0]);
    expect(onEdit).toHaveBeenCalledWith(products[0]);
  });

  it('llama onDelete con el id al hacer clic en Eliminar', async () => {
    renderTable();
    const deleteButtons = screen.getAllByRole('button', { name: 'Eliminar' });
    await userEvent.click(deleteButtons[0]);
    expect(onDelete).toHaveBeenCalledWith('aabbccddeeff001122334455');
  });
});
