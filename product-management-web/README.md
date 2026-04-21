# Product Management Web

Frontend web para administración de productos.

Aplicación desarrollada con React + TypeScript + Vite que consume la API REST del módulo backend.

---

## Stack Tecnológico

- React
- TypeScript
- Vite
- Material UI
- React Router DOM
- pnpm

---

## Funcionalidades

- Listado de productos
- Crear producto
- Editar producto
- Eliminar producto
- Paginación
- Integración con backend REST
- UI responsive
- Mensajes de éxito/error

---

## Estructura del Proyecto

src/
api/productsApi.ts
components/ProductForm.tsx
components/ProductsTable.tsx
types/product.ts
App.tsx
main.tsx
routes.tsx

---

## Variables de Entorno

Crear archivo `.env`

VITE_API_URL=http://localhost:8080

---

## Ejecutar Proyecto

pnpm install
pnpm dev

Aplicación disponible en:

http://localhost:5173

---

## Build Producción

pnpm build

---

## Integración Backend

Este frontend consume los siguientes endpoints:

- GET /api/v1/products?page=0&size=10
- POST /api/v1/products
- PUT /api/v1/products/{id}
- DELETE /api/v1/products/{id}

---

## Objetivo Técnico

Demostrar experiencia en:

- Consumo de APIs REST
- React moderno con TypeScript
- Manejo de estado y formularios
- UI profesional con Material UI
- Integración real frontend/backend