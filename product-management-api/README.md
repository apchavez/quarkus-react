# Product Management Platform

Aplicación fullstack para administración de productos. Backend con arquitectura hexagonal en Quarkus, frontend SPA en React, despliegue en Kubernetes.

---

## Tecnologías

### Backend
- Java 21 · Quarkus 3.32 · Gradle
- MongoDB (Panache) · Redis (caché)
- MapStruct · Lombok · Bean Validation
- SmallRye Health

### Frontend
- React 18 · TypeScript · Vite
- Material UI · React Router

### Infraestructura
- Docker · Kubernetes (nginx Ingress + cert-manager)
- GitHub Actions (CI/CD)

---

## Arquitectura del backend

```text
src/main/java/com/products/
├── adapters/
│   ├── in/rest/          ProductResource, ApiResponse
│   └── out/persistence/  MongoProductRepository
├── application/
│   ├── dto/              ProductRequest, ProductResponse, ProductsPagedResponse
│   ├── mapper/           ProductMapper (MapStruct)
│   └── usecase/          ProductUseCase
├── domain/
│   └── model/            Product, BaseEntity, PagedResponse
├── exception/            GlobalExceptionMapper, DomainExceptionMapper,
│                         ConstraintViolationExceptionMapper,
│                         JsonProcessingExceptionMapper,
│                         ProductNotFoundException, DuplicateSkuException
└── health/               LivenessCheck, ReadinessCheck
```

---

## Requisitos previos

- Java 21
- Node.js ≥ 18 y pnpm ≥ 9
- Docker Desktop (para MongoDB y Redis locales)

---

## Configuración local

Copiar y completar el archivo de variables de entorno:

```bash
cp product-management-api/.env.example product-management-api/.env
```

```env
APP_ENVIRONMENT=local
PORT=8080
MONGODB_CONNECTION_STRING=mongodb://localhost:27017
MONGODB_DATABASE=products
REDIS_URL=redis://localhost:6379
```

---

## Ejecutar en local

### 1. Dependencias (Docker)

```bash
docker run -d --name mongo -p 27017:27017 mongo:7.0
docker run -d --name redis -p 6379:6379 redis:7
```

### 2. Backend

```bash
cd product-management-api
./gradlew quarkusDev
```

Disponible en `http://localhost:8080`

### 3. Frontend

```bash
cd product-management-web
pnpm install
pnpm dev
```

Disponible en `http://localhost:5173` (proxy de `/api/v1` al backend configurado en `vite.config.ts`)

---

## Tests

```bash
cd product-management-api
./gradlew test
```

Requiere MongoDB corriendo en `localhost:27017`. Genera reporte de cobertura en `build/jacoco-report/`.

---

## Endpoints

### Productos

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/products` | Crear producto |
| `GET` | `/api/v1/products?page=0&size=10` | Listar con paginación |
| `GET` | `/api/v1/products/{id}` | Buscar por ID |
| `PUT` | `/api/v1/products/{id}` | Actualizar |
| `DELETE` | `/api/v1/products/{id}` | Eliminar |
| `GET` | `/api/v1/products/sku/{sku}` | Buscar por SKU |
| `GET` | `/api/v1/products/search?prefix=lap` | Buscar por prefijo de nombre |

### Health

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/q/health` | Estado general |
| `GET` | `/api/v1/q/health/live` | Liveness (usado por K8s) |
| `GET` | `/api/v1/q/health/ready` | Readiness (usado por K8s) |

### Formato de respuesta

```json
{
  "code": 200,
  "description": "Information obtained successfully",
  "data": { }
}
```

### Modelo de producto

```json
{
  "sku": "LAP-001",
  "name": "Laptop Pro",
  "description": "Equipo portátil de alto rendimiento",
  "category": "Tecnología",
  "price": 14999.99,
  "stock": 10,
  "active": true
}
```

---

## Colección Postman

Importar `product-management.postman_collection.json` desde la raíz del repositorio. El request de creación captura automáticamente el `id` para usarlo en las peticiones siguientes.

---

## Docker

```bash
# Backend
cd product-management-api
./gradlew build -x test
docker build -t product-api .

# Frontend
cd product-management-web
docker build -t product-web .
```

---

## CI/CD (GitHub Actions)

| Workflow | Trigger | Jobs |
|---|---|---|
| `docker-publish.yml` | Push a `main` en `product-management-api/**` | test → build → push `ghcr.io/apchavez/product-api:<sha>` |
| `docker-publish-web.yml` | Push a `main` en `product-management-web/**` | type-check + build → push `ghcr.io/apchavez/product-web:<sha>` |

---

## Kubernetes

Los manifests están en `k8s/`. Aplicar en orden:

> **Paso previo:** crear el secret con credenciales reales antes de aplicar los manifests:
> ```bash
> kubectl create secret generic mongo-secret \
>   --from-literal=MONGO_USERNAME=<user> \
>   --from-literal=MONGO_PASSWORD=<password> \
>   --from-literal=MONGODB_CONNECTION_STRING=mongodb://<user>:<password>@mongo-service:27017
> ```

```bash
kubectl apply -f k8s/issuer.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mongo.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/web-deployment.yaml
kubectl apply -f k8s/web-service.yaml
kubectl apply -f k8s/ingress.yaml
```

El Ingress expone todo bajo `product.local`:

```
product.local/          →  product-web  (React SPA)
product.local/api/v1    →  product-api  (Quarkus REST)
```
