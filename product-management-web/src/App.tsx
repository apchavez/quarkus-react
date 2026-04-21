import { useEffect, useState } from 'react';
import { Alert, Box, Button, Container, Pagination, Snackbar, Typography } from '@mui/material';
import ProductForm from './components/ProductForm';
import ProductsTable from './components/ProductsTable';
import { createProduct, deleteProduct, getProducts, updateProduct } from './api/productsApi';
import type { Product } from './types/product';

export default function App() {
  const [products, setProducts] = useState<Product[]>([]);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [message, setMessage] = useState('');

  const loadProducts = async (pageNumber = 1) => {
    try {
      const data = await getProducts(pageNumber - 1, 10);
      setProducts(data.content ?? []);
      setTotalPages(data.totalPages ?? 1);
    } catch {
      setMessage('Error cargando productos');
    }
  };

  useEffect(() => {
    loadProducts(page);
  }, [page]);

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
      loadProducts(page);
    } catch {
      setMessage('Error guardando producto');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteProduct(id);
      setMessage('Producto eliminado');
      loadProducts(page);
    } catch {
      setMessage('Error eliminando producto');
    }
  };

  return (
    <Container maxWidth="lg">
      <Box py={4}>
        <Typography variant="h4" gutterBottom>
          Product Management
        </Typography>

        <ProductForm
          initialData={editingProduct}
          onSubmit={handleSubmit}
          onCancelEdit={() => setEditingProduct(null)}
        />

        <ProductsTable products={products} onEdit={setEditingProduct} onDelete={handleDelete} />

        <Box mt={3} display="flex" justifyContent="center">
          <Pagination count={totalPages} page={page} onChange={(_, value) => setPage(value)} color="primary" />
        </Box>

        <Box mt={2}>
          <Button variant="outlined" onClick={() => loadProducts(page)}>
            Recargar
          </Button>
        </Box>
      </Box>

      <Snackbar open={!!message} autoHideDuration={3000} onClose={() => setMessage('')}>
        <Alert severity="info" onClose={() => setMessage('')}>
          {message}
        </Alert>
      </Snackbar>
    </Container>
  );
}
