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

- Login (`/login`) — credenciales demo: `admin`/`admin123` (ADMIN+USER), `user`/`user123` (USER)
- Rutas de producto protegidas por sesión (`RequireAuth`); sin token válido, redirige a `/login`
- Logout desde la barra superior (`AppLayout`)
- Listado paginado de productos
- Crear / Editar / Eliminar producto
- Validación de formulario en cliente
- Feedback visual de éxito y error
- UI responsive

---

## Estructura

```
src/
├── __tests__/
│   ├── App.test.tsx
│   └── routes.test.tsx
├── api/
│   ├── __tests__/
│   │   ├── productsApi.test.ts
│   │   └── httpClient.test.ts
│   ├── productsApi.ts        # Llamadas HTTP al backend
│   └── httpClient.ts         # apiFetch — agrega Authorization: Bearer, maneja 401
├── auth/
│   ├── __tests__/
│   │   ├── AuthContext.test.tsx
│   │   ├── LoginPage.test.tsx
│   │   ├── RequireAuth.test.tsx
│   │   ├── AppLayout.test.tsx
│   │   ├── authApi.test.ts
│   │   └── tokenStorage.test.ts
│   ├── AuthContext.tsx       # Estado de sesión (login/logout/isAuthenticated)
│   ├── LoginPage.tsx         # Formulario de login (ruta /login)
│   ├── RequireAuth.tsx       # Route guard — redirige a /login sin sesión
│   ├── AppLayout.tsx         # Barra superior con usuario + logout
│   ├── authApi.ts            # POST /api/v1/auth/login
│   └── tokenStorage.ts       # Persistencia del token en localStorage
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
pnpm test:coverage  # ejecuta con reporte de cobertura (verifica umbral ≥ 80%)
pnpm test:watch     # modo watch
```

86 tests en 13 suites:

- `ProductForm.test.tsx` — 17 tests: validación, envío, modo edición, revalidación en tiempo real
- `useProducts.test.ts` — 15 tests: carga inicial, create, update, delete, errores, paginación, editingProduct
- `productsApi.test.ts` — 10 tests: getProducts, createProduct, updateProduct, deleteProduct (happy path + error path)
- `App.test.tsx` — 10 tests: render, interacciones de UI, paginación, snackbar
- `ProductsTable.test.tsx` — 7 tests: renderizado, lista vacía, callbacks de editar/eliminar
- `routes.test.tsx` — 5 tests: estructura de rutas (login pública + rutas protegidas)
- `tokenStorage.test.ts` — 4 tests: set/get/clear, JSON malformado en localStorage
- `LoginPage.test.tsx` — 4 tests: render, login exitoso, credenciales inválidas, redirect si ya autenticado
- `AuthContext.test.tsx` — 4 tests: login/logout, estado inicial, error fuera de provider
- `httpClient.test.ts` — 4 tests: agrega Bearer token, limpia sesión y redirige en 401
- `RequireAuth.test.tsx` — 2 tests: pasa con sesión, redirige a /login sin sesión
- `authApi.test.ts` — 2 tests: login exitoso, credenciales inválidas
- `AppLayout.test.tsx` — 2 tests: muestra usuario, logout

Coverage (Vitest v8): ≥ 80% en statements, branches, functions y lines. Thresholds configurados en `vite.config.ts`.

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

GitHub Actions (`.github/workflows/ci.yml`, job `frontend` + `docker-web`):

1. `tsc --noEmit` — verificación de tipos
2. `pnpm test` — suite de tests
3. `pnpm test:coverage` — verificación de cobertura ≥ 80%
4. `pnpm build` — build de producción
4. Docker build + push a `ghcr.io/apchavez/product-web` (**solo en push a `main`**, no en PR)

Se dispara en push **y** pull_request hacia `main` dentro de `web/`.

---

## Integración con el backend

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login — `authApi.ts`, guarda el token vía `tokenStorage.ts` |
| `GET` | `/api/v1/products?page=0&size=10` | Listado paginado |
| `POST` | `/api/v1/products` | Crear producto |
| `PUT` | `/api/v1/products/{id}` | Actualizar producto |
| `DELETE` | `/api/v1/products/{id}` | Eliminar producto |

Todas las llamadas a `/api/v1/products/**` pasan por `httpClient.apiFetch`, que agrega el header `Authorization: Bearer <token>` automáticamente. Si el backend responde `401`, `apiFetch` limpia la sesión y redirige a `/login`.
