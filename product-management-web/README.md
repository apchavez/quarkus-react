# Product Management Web

Frontend para administración de productos, construido con React + TypeScript + Vite.

---

## Stack

| Capa | Tecnología |
|---|---|
| UI | React 18 + TypeScript |
| Componentes | Material UI 5 |
| Routing | React Router DOM 6 |
| Build | Vite 6 |
| Gestor de paquetes | pnpm |
| Tests | Vitest 2 + React Testing Library |
| Servidor prod | nginx (Docker) |

---

## Funcionalidades

- Listado paginado de productos
- Crear / Editar / Eliminar producto
- Validación de formulario en cliente
- Feedback visual de éxito y error
- UI responsive

---

## Estructura

```
src/
├── api/
│   └── productsApi.ts        # Llamadas HTTP al backend
├── components/
│   ├── __tests__/
│   │   ├── ProductForm.test.tsx
│   │   └── ProductsTable.test.tsx
│   ├── ProductForm.tsx
│   └── ProductsTable.tsx
├── hooks/
│   ├── __tests__/
│   │   └── useProducts.test.ts
│   └── useProducts.ts        # Estado y lógica de productos
├── test/
│   └── setup.ts              # Configuración global de tests
├── types/
│   └── product.ts
├── App.tsx
├── main.tsx
└── routes.tsx
```

---

## Variables de entorno

```bash
cp .env.example .env
```

| Variable | Descripción | Default |
|---|---|---|
| `VITE_API_URL` | URL base del backend | `http://localhost:8080` |

En producción nginx enruta `/api/v1` al backend directamente; esta variable solo afecta al servidor de desarrollo.

---

## Desarrollo local

```bash
pnpm install
pnpm dev          # http://localhost:5173
```

El dev server redirige `/api/v1/*` al backend mediante el proxy de Vite.

---

## Tests y coverage

```bash
pnpm test           # ejecuta una vez (CI mode)
pnpm test:coverage  # ejecuta con reporte de cobertura
pnpm test:watch     # modo watch
```

27 tests en 3 suites:

- `useProducts.test.ts` — 11 tests: carga inicial, create, update, delete, errores, paginación, editingProduct
- `ProductForm.test.tsx` — 10 tests: validación, envío, modo edición, revalidación en tiempo real
- `ProductsTable.test.tsx` — 6 tests: renderizado, lista vacía, callbacks de editar/eliminar

Coverage actual (Vitest v8): ~66% statements, ~75% branches, ~54% functions. Thresholds configurados en `vite.config.ts` (statements 60%, branches 70%, functions 50%). Los archivos `App.tsx`, `main.tsx`, `routes.tsx` y `productsApi.ts` no tienen tests unitarios — pendiente para escalar a ≥ 80%.

---

## Build y Docker

```bash
# Build de producción
pnpm build

# Imagen Docker (nginx + SPA)
docker build -t product-web .
docker run -p 80:80 product-web
```

---

## CI/CD

GitHub Actions (`.github/workflows/docker-publish-web.yml`):

1. `tsc --noEmit` — verificación de tipos
2. `pnpm test` — suite de tests
3. `pnpm build` — build de producción
4. Docker build + push a `ghcr.io/apchavez/product-web` (**solo en push a `main`**, no en PR)

Se dispara en push **y** pull_request hacia `main` dentro de `product-management-web/`.

---

## Integración con el backend

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/products?page=0&size=10` | Listado paginado |
| `POST` | `/api/v1/products` | Crear producto |
| `PUT` | `/api/v1/products/{id}` | Actualizar producto |
| `DELETE` | `/api/v1/products/{id}` | Eliminar producto |
