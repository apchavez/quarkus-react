# Product Management Platform

Aplicación fullstack para administración de productos, construida como portafolio técnico para demostrar experiencia en desarrollo backend con Java, APIs REST modernas, arquitectura hexagonal e integración con frontend React.

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21 · Quarkus 3 · MongoDB · Redis · MapStruct · Lombok |
| Frontend | React 18 · TypeScript · Vite · Material UI |
| Infraestructura | Docker · Kubernetes · GitHub Actions |

---

## Estructura del repositorio

```text
product-management/
├── product-management-api/         Backend Java + Quarkus
│   ├── src/
│   ├── k8s/                        Manifests de Kubernetes
│   ├── Dockerfile
│   └── build.gradle
├── product-management-web/         Frontend React + Vite
│   ├── src/
│   ├── Dockerfile
│   └── nginx.conf
├── docker/
│   └── gateway.conf                nginx gateway (Docker Compose)
├── .github/workflows/
│   ├── docker-publish.yml          CI/CD backend
│   └── docker-publish-web.yml      CI/CD frontend
├── docker-compose.yml
├── product-management.postman_collection.json
└── README.md
```

---

## Documentación detallada

Ver [`product-management-api/README.md`](product-management-api/README.md) para instrucciones completas de instalación, ejecución, endpoints y despliegue.
