import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProductForm from '../ProductForm';
import type { Product } from '../../types/product';

const onSubmit = vi.fn();
const onCancelEdit = vi.fn();

const validProduct: Product = {
  id: 'aabbccddeeff001122334455',
  sku: 'SKU-001',
  name: 'Laptop Pro',
  description: 'A great laptop',
  category: 'Technology',
  price: 999.99,
  stock: 10,
  active: true,
};

function renderForm(initialData?: Product | null) {
  return render(
    <ProductForm
      initialData={initialData}
      onSubmit={onSubmit}
      onCancelEdit={onCancelEdit}
    />
  );
}

beforeEach(() => {
  onSubmit.mockClear();
  onCancelEdit.mockClear();
});

describe('ProductForm — modo creación', () => {
  it('muestra botón Crear cuando no hay producto inicial', () => {
    renderForm();
    expect(screen.getByRole('button', { name: 'Crear' })).toBeInTheDocument();
  });

  it('muestra botón Actualizar cuando se edita un producto existente', () => {
    renderForm(validProduct);
    expect(screen.getByRole('button', { name: 'Actualizar' })).toBeInTheDocument();
  });

  it('llama onCancelEdit al hacer clic en Limpiar', async () => {
    renderForm();
    await userEvent.click(screen.getByRole('button', { name: 'Limpiar' }));
    expect(onCancelEdit).toHaveBeenCalledOnce();
  });
});

describe('ProductForm — validación al enviar vacío', () => {
  it('muestra errores de campos requeridos y no llama onSubmit', async () => {
    renderForm();
    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(screen.getByText('El SKU es requerido')).toBeInTheDocument();
    expect(screen.getByText('El nombre es requerido')).toBeInTheDocument();
    expect(screen.getByText('La categoría es requerida')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });
});

describe('ProductForm — validación de formato', () => {
  it('muestra error de formato para SKU inválido', async () => {
    renderForm();
    fireEvent.change(screen.getByLabelText('SKU'), {
      target: { value: 'INVALID SKU!' },
    });
    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(
      screen.getByText(/Solo letras, números, guiones/i)
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('muestra error de formato para nombre con caracteres inválidos', async () => {
    renderForm();
    fireEvent.change(screen.getByLabelText('Nombre'), {
      target: { value: 'Laptop@Pro#2024' },
    });
    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(
      screen.getByText(/Formato de nombre inválido/i)
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('no muestra error de descripción cuando está vacía (campo opcional)', async () => {
    renderForm();
    fireEvent.change(screen.getByLabelText('SKU'), { target: { value: 'SKU-001' } });
    fireEvent.change(screen.getByLabelText('Nombre'), { target: { value: 'Laptop Pro' } });
    fireEvent.change(screen.getByLabelText('Categoría'), { target: { value: 'Technology' } });

    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(screen.queryByText(/Formato de descripción/i)).not.toBeInTheDocument();
    expect(onSubmit).toHaveBeenCalledOnce();
  });
});

describe('ProductForm — envío exitoso', () => {
  it('llama onSubmit cuando el formulario es válido', async () => {
    renderForm();

    fireEvent.change(screen.getByLabelText('SKU'), {
      target: { value: 'SKU-001' },
    });
    fireEvent.change(screen.getByLabelText('Nombre'), {
      target: { value: 'Laptop Pro' },
    });
    fireEvent.change(screen.getByLabelText('Categoría'), {
      target: { value: 'Technology' },
    });

    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(onSubmit).toHaveBeenCalledOnce();
  });

  it('precarga los valores del producto al editar', () => {
    renderForm(validProduct);

    expect(screen.getByLabelText('SKU')).toHaveValue('SKU-001');
    expect(screen.getByLabelText('Nombre')).toHaveValue('Laptop Pro');
    expect(screen.getByLabelText('Categoría')).toHaveValue('Technology');
  });
});

describe('ProductForm — revalidación en tiempo real', () => {
  it('elimina el error de SKU cuando se corrige después del primer envío', async () => {
    renderForm();

    // primer envío — fuerza errores
    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));
    expect(screen.getByText('El SKU es requerido')).toBeInTheDocument();

    // corrige el campo
    fireEvent.change(screen.getByLabelText('SKU'), {
      target: { value: 'SKU-001' },
    });
    expect(screen.queryByText('El SKU es requerido')).not.toBeInTheDocument();
  });
});
