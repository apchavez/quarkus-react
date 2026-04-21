import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Checkbox,
  FormControlLabel,
  Grid,
  TextField,
} from '@mui/material';
import type { Product } from '../types/product';

type Props = {
  initialData?: Product | null;
  onSubmit: (product: Product) => void;
  onCancelEdit: () => void;
};

const emptyProduct: Product = {
  sku: '',
  name: '',
  description: '',
  category: '',
  price: 0,
  stock: 0,
  active: true,
};

export default function ProductForm({ initialData, onSubmit, onCancelEdit }: Props) {
  const [form, setForm] = useState<Product>(emptyProduct);

  useEffect(() => {
    setForm(initialData ?? emptyProduct);
  }, [initialData]);

  const handleChange = (field: keyof Product, value: string | number | boolean) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ mb: 4 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="SKU"
            value={form.sku}
            onChange={(e) => handleChange('sku', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Nombre"
            value={form.name}
            onChange={(e) => handleChange('name', e.target.value)}
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Descripción"
            value={form.description}
            onChange={(e) => handleChange('description', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Categoría"
            value={form.category}
            onChange={(e) => handleChange('category', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            fullWidth
            type="number"
            label="Precio"
            value={form.price}
            onChange={(e) => handleChange('price', Number(e.target.value))}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            fullWidth
            type="number"
            label="Stock"
            value={form.stock}
            onChange={(e) => handleChange('stock', Number(e.target.value))}
          />
        </Grid>
        <Grid item xs={12}>
          <FormControlLabel
            control={
              <Checkbox
                checked={form.active}
                onChange={(e) => handleChange('active', e.target.checked)}
              />
            }
            label="Activo"
          />
        </Grid>
        <Grid item xs={12}>
          <Button type="submit" variant="contained" sx={{ mr: 2 }}>
            {form.id ? 'Actualizar' : 'Crear'}
          </Button>
          <Button variant="outlined" onClick={onCancelEdit}>
            Limpiar
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
}