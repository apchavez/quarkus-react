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

Arquitectura hexagonal (ports & adapters). El núcleo de la aplicación no depende de ningún framework de infraestructura: los adaptadores implementan los puertos definidos en la capa de aplicación.

```text
src/main/java/com/products/
├── adapters/                         ← Capa de infraestructura
│   ├── in/rest/                        Adaptador de entrada
│   │   ├── ProductResource               JAX-RS — inyecta ProductServicePort
│   │   └── ApiResponse                   Envoltorio estándar de respuestas
│   └── out/persistence/                Adaptador de salida
│       └── MongoProductRepository        implements ProductRepositoryPort
│                                         (Panache + caché Caffeine/Redis)
│
├── application/                      ← Núcleo de la aplicación
│   ├── port/
│   │   ├── in/
│   │   │   └── ProductServicePort        Puerto de entrada (contrato del use case)
│   │   └── out/
│   │       └── ProductRepositoryPort     Puerto de salida (contrato de persistencia)
│   ├── dto/                            ProductRequest · ProductResponse · ProductsPagedResponse
│   ├── mapper/                         ProductMapper (MapStruct)
│   └── usecase/
│       └── ProductUseCase              implements ProductServicePort
│                                       inyecta ProductRepositoryPort
│
├── domain/                           ← Modelo de dominio puro
│   └── model/                          Product · BaseEntity · PagedResponse
│
├── exception/                        GlobalExceptionMapper · DomainExceptionMapper
│                                     ConstraintViolationExceptionMapper
│                                     JsonProcessingExceptionMapper
│                                     ProductNotFoundException · DuplicateSkuException
└── health/                           LivenessCheck · ReadinessCheck (MongoDB + Redis)
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
cp api/.env.example api/.env
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

### Opción A — Docker Compose (stack completo)

```bash
docker compose up --build
```

Levanta mongo, redis, backend y frontend. Disponible en `http://localhost`.

> El gateway nginx enruta `/api/v1` → backend y `/` → frontend, igual que el Ingress de K8s.

### Opción B — Desarrollo con hot-reload

```bash
# 1. Infraestructura
docker run -d --name mongo -p 27017:27017 mongo:7.0
docker run -d --name redis -p 6379:6379 redis:7

# 2. Backend (hot-reload)
cd api
./gradlew quarkusDev

# 3. Frontend (hot-reload, en otra terminal)
cd web
pnpm install
pnpm dev
```

Backend en `http://localhost:8080` · Frontend en `http://localhost:5173`

---

## Tests y coverage

```bash
cd api
./gradlew test
./gradlew jacocoTestCoverageVerification
```

Requiere MongoDB corriendo en `localhost:27017`. Genera reporte de cobertura en `build/jacoco-report/`.

Coverage verificado con JaCoCo: mínimo ≥ 80% de líneas (LINE). Cobertura actual ≈ 87%.

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

## OpenAPI / Swagger UI

Con el proyecto corriendo, disponible en:

| Recurso | URL |
|---|---|
| Especificación OpenAPI (JSON) | `http://localhost:8080/api/v1/q/openapi` |
| Swagger UI | `http://localhost:8080/api/v1/q/swagger-ui` |

---

## Postman

Los archivos están en `postman/` en la raíz del repositorio.

| Archivo | Descripción |
|---|---|
| `product-management.postman_collection.json` | Colección principal (10 requests) |
| `product-management.local.postman_environment.json` | Environment local — `http://localhost` (gateway Docker Compose) |
| `product-management.k8s.postman_environment.json` | Environment K8s — `http://product.local` (Ingress nginx) |

**Pasos de importación:**

1. En Postman, ir a **Import** y seleccionar los tres archivos de `postman/`.
2. Seleccionar el environment en la esquina superior derecha:
   - **product-management — local** → apunta al gateway Docker Compose en `http://localhost`.
   - **product-management — k8s** → apunta al Ingress en `http://product.local`.
3. Ejecutar los requests en orden: `01 - Create Product` captura `productId` automáticamente para los requests `03`, `06` y `07`.

> Para K8s, añadir `product.local` en `/etc/hosts` apuntando a la IP del Ingress controller.

---

## Docker

```bash
# Backend
cd api
./gradlew build -x test
docker build -t product-api .

# Frontend
cd web
docker build -t product-web .
```

---

## CI/CD (GitHub Actions)

| Workflow | Trigger | Jobs |
|---|---|---|
| `docker-publish.yml` | Push y PR a `main` en `api/**` | test + coverage → (solo push) build → push `ghcr.io/apchavez/product-api:<sha>` |
| `docker-publish-web.yml` | Push y PR a `main` en `web/**` | type-check → test → build → (solo push) push `ghcr.io/apchavez/product-web:<sha>` |

---

## Kubernetes

Los manifests están en `k8s/` en la raíz del repositorio. Aplicar en orden desde la raíz:

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
kubectl apply -f k8s/redis.yaml
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
