# Product Management Platform

Aplicación fullstack para administración de productos. Backend con arquitectura hexagonal en Quarkus, frontend SPA en React, despliegue en Kubernetes.

---

## Tecnologías

### Backend
- Java 21 · Quarkus 3.32 · Gradle
- MongoDB (Panache) · Redis (caché + rate limiting)
- Apache Kafka (SmallRye Reactive Messaging) — eventos de dominio
- MapStruct · Lombok · Bean Validation
- SmallRye Health
- Apache Commons CSV (bulk import) · Apache PDFBox (reporte PDF) · Apache POI (reporte Excel, streaming)

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
│   │   ├── ProductResource               JAX-RS — inyecta ProductServicePort (requiere JWT)
│   │   ├── AuthResource                  JAX-RS — POST /api/v1/auth/login (público)
│   │   ├── RateLimitingFilter            @Provider — ventana fija 100 req/60s en POST/PUT/DELETE
│   │   │                                 de /api/v1/products (Redis, fail-open, 429+Retry-After)
│   │   └── ApiResponse                   Envoltorio estándar de respuestas
│   └── out/                            Adaptadores de salida
│       ├── persistence/
│       │   └── MongoProductRepository    implements ProductRepositoryPort
│       │                                 (Panache + caché Redis, cache-aside, TTL 5min, fail-open)
│       └── messaging/
│           └── KafkaProductEventPublisher  implements ProductEventPublisherPort
│                                            (SmallRye Reactive Messaging, fire-and-forget)
│
├── application/                      ← Núcleo de la aplicación
│   ├── port/
│   │   ├── in/
│   │   │   ├── ProductServicePort        Puerto de entrada (contrato del use case)
│   │   │   └── AuthServicePort           Puerto de entrada — login
│   │   └── out/
│   │       ├── ProductRepositoryPort     Puerto de salida (contrato de persistencia)
│   │       └── ProductEventPublisherPort Puerto de salida (contrato de eventos de dominio)
│   ├── dto/                            ProductRequest · ProductResponse · ProductsPagedResponse
│   │                                   LoginRequest · LoginResponse
│   ├── mapper/                         ProductMapper (MapStruct)
│   ├── report/                         PdfReportGenerator (PDFBox) · ExcelReportGenerator (POI)
│   └── usecase/
│       ├── ProductUseCase              implements ProductServicePort
│       │                               inyecta ProductRepositoryPort + ProductEventPublisherPort
│       ├── AuthUseCase                 implements AuthServicePort — firma el JWT (HS256)
│       └── DemoUserStore               usuarios demo hardcoded (bcrypt) — no es un store real
│
├── domain/                           ← Modelo de dominio puro
│   ├── model/                          Product · BaseEntity · PagedResponse
│   └── event/                          ProductEvent · ProductEventType
│
├── exception/                        GlobalExceptionMapper · DomainExceptionMapper
│                                     ConstraintViolationExceptionMapper
│                                     JsonProcessingExceptionMapper
│                                     ProductNotFoundException · DuplicateSkuException
│                                     InvalidCredentialsException
└── health/                           LivenessCheck · ReadinessCheck (MongoDB)
```

> El caché de Redis es opcional en runtime: si Redis no está disponible, `MongoProductRepository`
> registra un warning y sirve directo desde MongoDB (fail-open) — nunca rompe una petición.

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
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## Ejecutar en local

### Opción A — Docker Compose (stack completo)

```bash
docker compose up --build
```

Levanta mongo, redis, kafka, backend y frontend. Disponible en `http://localhost`.

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

### Auth

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login — devuelve un JWT firmado RS256 con una key generada en memoria por `EphemeralJwtKeyConfigSource` (`smallrye-jwt-build`), no un archivo commiteado — ver README raíz |

Body: `{"username": "...", "password": "..."}`. Usuarios demo (`DemoUserStore.java`, hardcoded con bcrypt):

| Usuario | Password | Roles |
|---|---|---|
| `admin` | `admin123` | `ADMIN`, `USER` |
| `user` | `user123` | `USER` |

### Productos — requieren JWT

Todos los endpoints de `/api/v1/products/**` exigen header `Authorization: Bearer <token>` (obtenido de `/api/v1/auth/login`). Lectura (`GET`) acepta `ADMIN` o `USER`; escritura (`POST`/`PUT`/`DELETE`) exige `ADMIN`. Sin token válido, responden `401`.

| Método | Endpoint | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/products` | `ADMIN` | Crear producto |
| `GET` | `/api/v1/products?page=0&size=10` | `ADMIN`, `USER` | Listar con paginación |
| `GET` | `/api/v1/products/{id}` | `ADMIN`, `USER` | Buscar por ID |
| `PUT` | `/api/v1/products/{id}` | `ADMIN` | Actualizar |
| `DELETE` | `/api/v1/products/{id}` | `ADMIN` | Eliminar |
| `GET` | `/api/v1/products/sku/{sku}` | `ADMIN`, `USER` | Buscar por SKU |
| `GET` | `/api/v1/products/search?prefix=lap` | `ADMIN`, `USER` | Buscar por prefijo de nombre |
| `POST` | `/api/v1/products/import` | `ADMIN` | Bulk import vía CSV (`multipart/form-data`, campo `file`). Filas inválidas se omiten y se reportan, sin abortar el lote |
| `GET` | `/api/v1/products/report/pdf` | `ADMIN`, `USER` | Descarga un PDF del catálogo completo con resumen de valor de inventario |
| `GET` | `/api/v1/products/report/excel` | `ADMIN`, `USER` | Descarga un Excel (XLSX) del catálogo completo con resumen de valor de inventario |

CSV de import — encabezado obligatorio: `sku,name,description,category,price,stock,active`. Cada fila pasa por la misma validación que `POST /api/v1/products`; respuesta:

```json
{
  "code": 200,
  "description": "Import completed",
  "data": {
    "imported": 2,
    "failed": 1,
    "errors": [
      { "row": 4, "message": "sku is required" }
    ]
  }
}
```

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

## Eventos de dominio (Kafka)

`ProductUseCase` publica un evento en el tópico `product-events` tras cada `insert`/`update`/`delete` exitoso, vía `ProductEventPublisherPort` → `KafkaProductEventPublisher` (SmallRye Reactive Messaging, `Emitter<String>`).

| Campo | Descripción |
|---|---|
| `eventId` | UUID único del evento |
| `eventType` | `PRODUCT_CREATED` \| `PRODUCT_UPDATED` \| `PRODUCT_DELETED` |
| `occurredAt` | Timestamp ISO-8601 |
| `product` | Snapshot completo del producto en ese momento |

La publicación es **fire-and-forget**: un fallo de Kafka (broker caído, timeout) se registra como warning pero nunca hace fallar la operación de negocio que lo originó — el mismo principio fail-open que ya aplica al caché de Redis. No hay ningún consumer en este repo; es solo el productor del evento (igual que en `spring-webflux-angular`, que publica al mismo tópico `product-events` de la misma forma: nadie lo consume ahí tampoco, es una demostración del patrón productor).

En tests (`%test` profile) y en CI, el canal usa el conector `smallrye-in-memory` en vez de un broker Kafka real — no hay contenedor de Kafka en `ci.yml`.

## Rate limiting

`RateLimitingFilter` (`@Provider`, `ContainerRequestFilter`) limita `POST`/`PUT`/`DELETE` bajo `/api/v1/products` a **100 peticiones por 60 segundos por IP**, con ventana fija implementada en Redis vía un script Lua atómico (`INCR` + `EXPIRE` condicional). `GET` y cualquier otra ruta no pasan por Redis en absoluto.

- **IP del cliente:** el segmento más a la derecha de `X-Forwarded-For` (el añadido por infraestructura de confianza), o la dirección remota de la conexión si no hay header, o `"unknown"` si no hay ninguno de los dos.
- **Al exceder el límite:** `429 Too Many Requests` con header `Retry-After: 60`.
- **Fail-open:** si Redis no está disponible, la petición pasa sin ser limitada (se registra un warning) — el mismo principio que el caché de lectura.

---

## Observabilidad

### Métricas

Extension `quarkus-micrometer-registry-prometheus`. Endpoint en formato Prometheus:

```
GET /api/v1/q/metrics
```

Las métricas HTTP (`http_server_requests_seconds_*`) incluyen labels `method`, `outcome`, `status` y `uri`. El Deployment del chart de Helm (`chart/templates/deployment-api.yaml`) ya tiene las annotations `prometheus.io/scrape`, `prometheus.io/path` y `prometheus.io/port` para autodescubrimiento.

### Trazas distribuidas

Extension `quarkus-opentelemetry`. Las trazas se exportan via OTLP gRPC al colector configurado en:

```env
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317   # valor en chart/templates/configmap.yaml
```

En local el default es `http://localhost:4317`. Quarkus auto-instrumenta todas las llamadas REST, MongoDB y Redis.

### Health checks

SmallRye Health (`quarkus-smallrye-health`):

| Endpoint | Tipo | Descripción |
|---|---|---|
| `/api/v1/q/health` | Combined | Estado general |
| `/api/v1/q/health/live` | `@Liveness` | App activa (K8s livenessProbe) |
| `/api/v1/q/health/ready` | `@Readiness` | Ping real a MongoDB con timeout 2s (K8s readinessProbe) |

> Redis **no** forma parte del readiness check: es un caché auxiliar (fail-open), no una
> dependencia dura. Si cae, el pod sigue `Ready` y sirve desde MongoDB.

### Logging

JBoss LogManager con configuración en `application.properties`:

```properties
quarkus.log.level=INFO
quarkus.log.category."com.products".level=DEBUG
```

Los logs van a stdout y son capturados por el runtime de K8s / Docker.

### Alertas K8s

`chart/templates/prometheus-rule.yaml` define un `PrometheusRule` (requiere [Prometheus Operator](https://prometheus-operator.dev)) con tres alertas: `HighErrorRate` (crítico, >5% 5xx), `HighP99Latency` (warning, P99 >1s) y `PodNotReady` (crítico).

### Grafana

`chart/templates/grafana.yaml` despliega Grafana 11.1 con datasource Prometheus y dashboard pre-provisionado (request rate, error rate, P50/P99 latency, JVM memory).

```bash
kubectl port-forward svc/grafana 3000:3000
# Abrir http://localhost:3000 (acceso anónimo en modo Viewer)
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
| `quarkus-react.postman_collection.json` | Colección principal |
| `quarkus-react.local.postman_environment.json` | Environment local — `http://localhost` (gateway Docker Compose) |
| `quarkus-react.k8s.postman_environment.json` | Environment K8s — `http://product.local` (Ingress nginx) |

**Pasos de importación:**

1. En Postman, ir a **Import** y seleccionar los tres archivos de `postman/`.
2. Seleccionar el environment en la esquina superior derecha:
   - **product-management — local** → apunta al gateway Docker Compose en `http://localhost`.
   - **product-management — k8s** → apunta al Ingress en `http://product.local`.
3. Ejecutar los requests en orden: `00 - Login` captura el JWT automáticamente (usado como Bearer auth a nivel de colección para los requests `01`-`07`); `01 - Create Product` captura `productId` para los requests `03`, `06` y `07`.

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
| `ci.yml` (`docker-api`) | Push a `main` | test + coverage (job `backend`) → build → push `ghcr.io/apchavez/product-api:<sha>` |
| `ci.yml` (`docker-web`) | Push a `main` | type-check → test → coverage (job `frontend`) → build → push `ghcr.io/apchavez/product-web:<sha>` |

---

## Kubernetes

Los manifests reales están en `chart/` (Helm) en la raíz del repositorio — es lo mismo que aplica `deploy.yml` en CI, que despliega siempre el tag `:latest` publicado por `ci.yml` (jobs `docker-api`/`docker-web`, sin archivos ancla ni commits automáticos de CI).

> **Paso previo:** las credenciales de Mongo se pasan como `--set` al instalar el chart, no se crean por separado:
> ```bash
> helm upgrade --install product-management ./chart \
>   --namespace product-management --create-namespace \
>   --set secrets.mongoUsername=<user> \
>   --set secrets.mongoPassword=<password> \
>   --set "secrets.mongoConnectionString=mongodb://<user>:<password>@mongo-service:27017"
> ```

```bash
helm upgrade --install product-management ./chart --namespace product-management --create-namespace
```

El Ingress expone todo bajo `product.local`:

```
product.local/          →  product-web  (React SPA)
product.local/api/v1    →  product-api  (Quarkus REST)
```
