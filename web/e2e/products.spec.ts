import { test, expect, Route } from '@playwright/test';

interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  category: string;
  price: number;
  stock: number;
  active: boolean;
}

const PRODUCT: Product = {
  id: 1,
  sku: 'PROD-001',
  name: 'Widget Alpha',
  description: 'A reliable test widget',
  category: 'Electronics',
  price: 29.99,
  stock: 50,
  active: true,
};

function pageResponse(products: Product[]) {
  return {
    data: {
      products,
      totalPages: products.length > 0 ? 1 : 0,
      totalItems: products.length,
    },
  };
}

function mockProducts(products: Product[]) {
  return async (route: Route) => {
    const method = route.request().method();
    const url = route.request().url();

    if (method === 'GET') {
      await route.fulfill({ json: pageResponse([...products]) });
    } else if (method === 'POST') {
      const body = await route.request().postDataJSON();
      const created = { ...PRODUCT, ...body };
      products.push(created);
      await route.fulfill({ status: 201, json: created });
    } else if (method === 'PUT') {
      const id = Number(url.split('/').pop());
      const body = await route.request().postDataJSON();
      const idx = products.findIndex(p => p.id === id);
      if (idx >= 0) products[idx] = { ...products[idx], ...body };
      await route.fulfill({ json: products[idx >= 0 ? idx : 0] });
    } else if (method === 'DELETE') {
      const id = Number(url.split('/').pop());
      const idx = products.findIndex(p => p.id === id);
      if (idx >= 0) products.splice(idx, 1);
      await route.fulfill({ status: 204, body: '' });
    } else {
      await route.continue();
    }
  };
}

test.describe('Products', () => {
  test('shows empty state when no products exist', async ({ page }) => {
    await page.route('**/api/v1/products**', mockProducts([]));

    await page.goto('/');

    await expect(page.getByText('No hay productos')).toBeVisible();
  });

  test('shows products in the table', async ({ page }) => {
    await page.route('**/api/v1/products**', mockProducts([PRODUCT]));

    await page.goto('/');

    await expect(page.getByText('PROD-001')).toBeVisible();
    await expect(page.getByText('Widget Alpha')).toBeVisible();
    await expect(page.getByText('Electronics')).toBeVisible();
  });

  test('creates a product and shows it in the table', async ({ page }) => {
    const products: Product[] = [];
    await page.route('**/api/v1/products**', mockProducts(products));

    await page.goto('/');
    await expect(page.getByText('No hay productos')).toBeVisible();

    await page.getByLabel('SKU').fill('PROD-001');
    await page.getByLabel('Nombre').fill('Widget Alpha');
    await page.getByLabel('Descripción').fill('A reliable test widget');
    await page.getByLabel('Categoría').fill('Electronics');
    await page.getByLabel('Precio').fill('29.99');
    await page.getByLabel('Stock').fill('50');

    await page.getByRole('button', { name: 'Crear' }).click();

    await expect(page.getByText('PROD-001')).toBeVisible();
    await expect(page.getByText('Widget Alpha')).toBeVisible();
  });

  test('edits a product and shows updated data', async ({ page }) => {
    const products: Product[] = [{ ...PRODUCT }];
    await page.route('**/api/v1/products**', mockProducts(products));

    await page.goto('/');
    await expect(page.getByText('Widget Alpha')).toBeVisible();

    await page.getByRole('button', { name: 'Editar' }).first().click();

    await page.getByLabel('Nombre').clear();
    await page.getByLabel('Nombre').fill('Widget Beta');
    await page.getByRole('button', { name: 'Actualizar' }).click();

    await expect(page.getByText('Widget Beta')).toBeVisible();
  });

  test('deletes a product and shows empty state', async ({ page }) => {
    const products: Product[] = [{ ...PRODUCT }];
    await page.route('**/api/v1/products**', mockProducts(products));

    await page.goto('/');
    await expect(page.getByText('PROD-001')).toBeVisible();

    await page.getByRole('button', { name: 'Eliminar' }).first().click();

    await expect(page.getByText('No hay productos')).toBeVisible();
  });

  test('shows validation error for invalid SKU format', async ({ page }) => {
    await page.route('**/api/v1/products**', mockProducts([]));

    await page.goto('/');

    await page.getByLabel('SKU').fill('invalid sku!');
    await page.getByLabel('SKU').blur();

    await expect(page.getByText(/SKU/i)).toBeVisible();
  });

  test('shows validation error for empty required fields', async ({ page }) => {
    await page.route('**/api/v1/products**', mockProducts([]));

    await page.goto('/');

    await page.getByLabel('Nombre').focus();
    await page.getByLabel('Nombre').blur();

    await expect(page.getByText(/requerido|required/i)).toBeVisible();
  });
});
