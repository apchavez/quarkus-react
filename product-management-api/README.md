# Product Management Platform

Aplicación fullstack para administración de productos, compuesta por backend API, frontend web y scripts de soporte.

Proyecto desarrollado como portafolio técnico para demostrar experiencia en desarrollo backend con Java, arquitectura moderna, APIs REST y organización profesional tipo monorepo.

---

# Vista General

```text
product-management/
├── product-management-api     -> Backend Java + Quarkus
├── product-management-web     -> Frontend Web
├── scripts                    -> Scripts de automatización / utilidades
├── postman_collection.json    -> Colección para pruebas de API
└── README.md
```

---

# Tecnologías Utilizadas

## Backend

- Java 21
- Quarkus
- Gradle
- MongoDB
- Redis
- Lombok
- MapStruct

## Frontend

- Aplicación web desacoplada del backend

## Herramientas

- Postman
- Git
- VS Code

---

# Funcionalidades Generales

✅ Gestión completa de productos  
✅ CRUD de productos  
✅ Búsqueda por SKU  
✅ Búsqueda por nombre  
✅ Paginación  
✅ Validación de datos  
✅ Manejo global de errores  
✅ Health Checks  
✅ Arquitectura por capas  
✅ Configuración por variables de entorno  
✅ Separación frontend / backend

---

# Modelo de Producto

```json
{
  "sku": "SKU-001",
  "name": "Laptop Pro",
  "description": "Equipo portátil de alto rendimiento",
  "category": "Tecnología",
  "price": 14999.99,
  "stock": 10,
  "active": true
}
```

---

# Arquitectura Backend

```text
product-management-api/src/main/java/com/products
├── adapters
│   ├── in/rest
│   └── out/persistence
├── application
│   ├── dto
│   ├── mapper
│   └── usecase
├── domain
│   └── model
├── exception
├── health
└── validation
```

---

# Configuración Local

## Backend (`product-management-api/.env`)

```env
APP_ENVIRONMENT=local
PORT=8080

MONGODB_CONNECTION_STRING=mongodb://localhost:27017
MONGODB_DATABASE=products

REDIS_URL=redis://localhost:6379
```

---

# Ejecutar Proyecto

## 1. Backend

```bash
cd product-management-api
./gradlew quarkusDev
```

Disponible en:

```text
http://localhost:8080
```

---

## 2. Frontend

```bash
cd product-management-web
npm install
npm run dev
```

---

# Compilar Backend

```bash
cd product-management-api
./gradlew build
```

---

# Endpoints Principales

| Método | Endpoint                              |
| ------ | ------------------------------------- |
| POST   | /api/v1/products                      |
| GET    | /api/v1/products                      |
| GET    | /api/v1/products/{id}                 |
| PUT    | /api/v1/products/{id}                 |
| DELETE | /api/v1/products/{id}                 |
| GET    | /api/v1/products/sku/{sku}            |
| GET    | /api/v1/products/search?prefix=laptop |

---

# Health Checks

| Método | Endpoint               |
| ------ | ---------------------- |
| GET    | /api/v1/q/health       |
| GET    | /api/v1/q/health/live  |
| GET    | /api/v1/q/health/ready |

---

# Colección Postman

Archivo incluido en la raíz del proyecto:

```text
postman_collection.json
```

Importar en Postman para probar endpoints rápidamente.

---

# Objetivo Técnico del Proyecto

Este proyecto fue construido para demostrar experiencia práctica en:

- Desarrollo backend con Java
- APIs REST profesionales
- Quarkus Framework
- Persistencia NoSQL con MongoDB
- Integración de frontend con backend
- Arquitectura limpia y mantenible
- Validaciones robustas
- Manejo profesional de errores
- Organización real de repositorio fullstack

---

# Estado del Proyecto

Proyecto funcional y en mejora continua como parte de portafolio profesional.

---

# Autor

Desarrollado como proyecto personal de crecimiento técnico y showcase profesional.

---

# Licencia

Uso educativo y demostrativo.
