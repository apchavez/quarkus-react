import { Alert, Box, Button, Container, Pagination, Snackbar, Typography } from '@mui/material';
import ProductForm from './components/ProductForm';
import ProductsTable from './components/ProductsTable';
import { useProducts } from './hooks/useProducts';

export default function App() {
  const {
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
    reload,
  } = useProducts();

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
          <Button variant="outlined" onClick={reload}>
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
