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

type FormErrors = Partial<Record<keyof Product, string>>;

const SKU_PATTERN = /^[A-Za-z0-9_-]{1,50}$/;
const TEXT_PATTERN = /^[\p{L}\p{N}\s.,:;_\-()/]{1,255}$/u;

const emptyProduct: Product = {
  sku: '',
  name: '',
  description: '',
  category: '',
  price: 0,
  stock: 0,
  active: true,
};

function validate(form: Product): FormErrors {
  const errors: FormErrors = {};

  if (!form.sku || !form.sku.trim()) {
    errors.sku = 'El SKU es requerido';
  } else if (!SKU_PATTERN.test(form.sku)) {
    errors.sku = 'Solo letras, números, guiones y guiones bajos (máx. 50)';
  }

  if (!form.name || !form.name.trim()) {
    errors.name = 'El nombre es requerido';
  } else if (!TEXT_PATTERN.test(form.name)) {
    errors.name = 'Formato de nombre inválido (máx. 255 caracteres)';
  }

  if (form.description && form.description.trim() && !TEXT_PATTERN.test(form.description)) {
    errors.description = 'Formato de descripción inválido (máx. 255 caracteres)';
  }

  if (!form.category || !form.category.trim()) {
    errors.category = 'La categoría es requerida';
  } else if (!TEXT_PATTERN.test(form.category)) {
    errors.category = 'Formato de categoría inválido (máx. 255 caracteres)';
  }

  if (form.price == null || form.price === undefined) {
    errors.price = 'El precio es requerido';
  } else if (form.price < 0) {
    errors.price = 'El precio debe ser mayor o igual a 0';
  }

  if (form.stock == null || form.stock === undefined) {
    errors.stock = 'El stock es requerido';
  } else if (!Number.isInteger(form.stock) || form.stock < 0) {
    errors.stock = 'El stock debe ser un entero mayor o igual a 0';
  }

  return errors;
}

export default function ProductForm({ initialData, onSubmit, onCancelEdit }: Props) {
  const [form, setForm] = useState<Product>(emptyProduct);
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    setForm(initialData ?? emptyProduct);
    setErrors({});
    setSubmitted(false);
  }, [initialData]);

  const handleChange = (field: keyof Product, value: string | number | boolean) => {
    const updated = { ...form, [field]: value };
    setForm(updated);
    if (submitted) {
      setErrors(validate(updated));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
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
            error={!!errors.sku}
            helperText={errors.sku}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Nombre"
            value={form.name}
            onChange={(e) => handleChange('name', e.target.value)}
            error={!!errors.name}
            helperText={errors.name}
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Descripción"
            value={form.description}
            onChange={(e) => handleChange('description', e.target.value)}
            error={!!errors.description}
            helperText={errors.description}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Categoría"
            value={form.category}
            onChange={(e) => handleChange('category', e.target.value)}
            error={!!errors.category}
            helperText={errors.category}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            fullWidth
            type="number"
            label="Precio"
            value={form.price}
            onChange={(e) => handleChange('price', Number(e.target.value))}
            error={!!errors.price}
            helperText={errors.price}
            inputProps={{ min: 0, step: 0.01 }}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            fullWidth
            type="number"
            label="Stock"
            value={form.stock}
            onChange={(e) => handleChange('stock', parseInt(e.target.value, 10) || 0)}
            error={!!errors.stock}
            helperText={errors.stock}
            inputProps={{ min: 0, step: 1 }}
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
