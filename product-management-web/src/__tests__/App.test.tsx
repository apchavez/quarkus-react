import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import App from '../App';
import { useProducts } from '../hooks/useProducts';
import type { Product } from '../types/product';

vi.mock('../hooks/useProducts');

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

const createMock = (overrides: Partial<ReturnType<typeof useProducts>> = {}) => ({
  products: [],
  editingProduct: null,
  setEditingProduct: vi.fn(),
  page: 1,
  setPage: vi.fn(),
  totalPages: 1,
  message: '',
  setMessage: vi.fn(),
  handleSubmit: vi.fn(),
  handleDelete: vi.fn(),
  reload: vi.fn(),
  ...overrides,
});

beforeEach(() => {
  vi.mocked(useProducts).mockReturnValue(createMock());
});

describe('App', () => {
  it('renders Product Management title', () => {
    render(<App />);
    expect(screen.getByText('Product Management')).toBeTruthy();
  });

  it('renders Recargar button', () => {
    render(<App />);
    expect(screen.getByText('Recargar')).toBeTruthy();
  });

  it('calls reload when Recargar is clicked', () => {
    const mock = createMock();
    vi.mocked(useProducts).mockReturnValue(mock);

    render(<App />);
    fireEvent.click(screen.getByText('Recargar'));

    expect(mock.reload).toHaveBeenCalledTimes(1);
  });

  it('renders products in table', () => {
    vi.mocked(useProducts).mockReturnValue(createMock({ products: [mockProduct] }));

    render(<App />);

    expect(screen.getByText('Laptop Pro')).toBeTruthy();
  });

  it('renders form clear button', () => {
    render(<App />);
    expect(screen.getByText('Limpiar')).toBeTruthy();
  });

  it('calls setEditingProduct(null) when Limpiar is clicked', () => {
    const mock = createMock({ editingProduct: mockProduct });
    vi.mocked(useProducts).mockReturnValue(mock);

    render(<App />);
    fireEvent.click(screen.getByText('Limpiar'));

    expect(mock.setEditingProduct).toHaveBeenCalledWith(null);
  });

  it('renders pagination navigation', () => {
    vi.mocked(useProducts).mockReturnValue(createMock({ totalPages: 3, page: 1 }));

    render(<App />);

    expect(screen.getByRole('navigation')).toBeTruthy();
  });

  it('calls setPage when a pagination page is clicked', () => {
    const mock = createMock({ totalPages: 3, page: 1 });
    vi.mocked(useProducts).mockReturnValue(mock);

    render(<App />);

    const page2Button = screen.getByRole('button', { name: /page 2/i });
    fireEvent.click(page2Button);

    expect(mock.setPage).toHaveBeenCalledWith(2);
  });

  it('shows snackbar alert when message is set', () => {
    vi.mocked(useProducts).mockReturnValue(
      createMock({ message: 'Operación exitosa' })
    );

    render(<App />);

    expect(screen.getByText('Operación exitosa')).toBeTruthy();
  });

  it('calls setMessage when snackbar closes', () => {
    const mock = createMock({ message: 'Test message' });
    vi.mocked(useProducts).mockReturnValue(mock);

    render(<App />);

    const closeButtons = screen.getAllByRole('button', { name: /close/i });
    fireEvent.click(closeButtons[0]);

    expect(mock.setMessage).toHaveBeenCalledWith('');
  });
});
