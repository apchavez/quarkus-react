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

function fillValidBase() {
  fireEvent.change(screen.getByLabelText('SKU'), { target: { value: 'SKU-001' } });
  fireEvent.change(screen.getByLabelText('Nombre'), { target: { value: 'Laptop Pro' } });
  fireEvent.change(screen.getByLabelText('Categoría'), { target: { value: 'Technology' } });
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

  it('muestra error de formato para descripción con caracteres inválidos', async () => {
    renderForm();
    fillValidBase();
    fireEvent.change(screen.getByLabelText('Descripción'), {
      target: { value: 'Invalid@Description#2024' },
    });

    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(
      screen.getByText(/Formato de descripción inválido/i)
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('muestra error de formato para categoría con caracteres inválidos', async () => {
    renderForm();
    fillValidBase();
    fireEvent.change(screen.getByLabelText('Categoría'), {
      target: { value: 'Invalid@Category#2024' },
    });

    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(
      screen.getByText(/Formato de categoría inválido/i)
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('no muestra error de descripción cuando está vacía (campo opcional)', async () => {
    renderForm();
    fillValidBase();

    await userEvent.click(screen.getByRole('button', { name: 'Crear' }));

    expect(screen.queryByText(/Formato de descripción/i)).not.toBeInTheDocument();
    expect(onSubmit).toHaveBeenCalledOnce();
  });
});

describe('ProductForm — envío exitoso', () => {
  it('llama onSubmit cuando el formulario es válido', async () => {
    renderForm();
    fillValidBase();

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

describe('ProductForm — validación de precio y stock', () => {
  it('muestra error cuando el precio es negativo', () => {
    const { container } = renderForm();
    fillValidBase();
    fireEvent.change(screen.getByLabelText('Precio'), { target: { value: '-10' } });

    // Dispatched directly on the form: a real click on the submit button would be
    // silently swallowed by the browser's native HTML5 constraint validation, since the
    // price input carries min=0 - it would never even reach React's onSubmit handler.
    fireEvent.submit(container.querySelector('form')!);

    expect(
      screen.getByText('El precio debe ser mayor o igual a 0')
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('muestra error cuando el precio es requerido (null/undefined)', async () => {
    renderForm({ ...validProduct, price: undefined } as unknown as Product);

    await userEvent.click(screen.getByRole('button', { name: 'Actualizar' }));

    expect(screen.getByText('El precio es requerido')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('muestra error cuando el stock es negativo', () => {
    const { container } = renderForm();
    fillValidBase();
    fireEvent.change(screen.getByLabelText('Stock'), { target: { value: '-3' } });

    // Same reasoning as the negative-price case: the Stock input carries min=0, so a
    // real click would never reach React's onSubmit.
    fireEvent.submit(container.querySelector('form')!);

    expect(
      screen.getByText('El stock debe ser un entero mayor o igual a 0')
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('muestra error cuando el stock no es un entero', () => {
    const { container } = renderForm({ ...validProduct, stock: 3.5 });

    // The Stock input also carries step=1, so 3.5 is a native step-mismatch - a real
    // click would be swallowed the same way as the min-constraint cases above.
    fireEvent.submit(container.querySelector('form')!);

    expect(
      screen.getByText('El stock debe ser un entero mayor o igual a 0')
    ).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('muestra error cuando el stock es requerido (null/undefined)', async () => {
    renderForm({ ...validProduct, stock: undefined } as unknown as Product);

    await userEvent.click(screen.getByRole('button', { name: 'Actualizar' }));

    expect(screen.getByText('El stock es requerido')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
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
