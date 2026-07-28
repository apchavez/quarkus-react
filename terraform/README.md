# Terraform — Clúster EKS

Provisiona la infraestructura de AWS sobre la que se despliega el chart de Helm de este proyecto (`../chart/`): una VPC, un clúster EKS con un managed node group, el driver EBS CSI (para que los PVC de `mongo.yaml`/`redis.yaml` puedan enlazarse), el controlador `ingress-nginx` (`chart/templates/ingress.yaml` tiene hardcodeado `ingressClassName: nginx`), y `cert-manager` (`chart/templates/issuer.yaml` necesita que sus CRDs existan primero).

> **Esto no está conectado al CI.** `deploy.yml` asume que ya existe un clúster y apunta `KUBECONFIG` a él — no ejecuta este Terraform. Provisionar/destruir infraestructura real de AWS es un paso deliberado, manual y con costo, no algo que deba pasar automáticamente en cada push.

---

## ⚠️ Advertencia de costo

Correr `terraform apply` aquí crea **recursos reales y facturables de AWS**: un control plane de EKS (~$0.10/hr), 1 nodo `m7i-flex.large` (~$0.05/hr), un NAT gateway (~$0.045/hr + datos), un Network Load Balancer expuesto a internet para `ingress-nginx`, y volúmenes EBS gp3 para `mongo`/`kafka`. Aproximadamente **$130–170/mes** si se deja corriendo continuamente. A diferencia de los hermanos AWS Lambda y Azure Functions de este portafolio (serverless, costo prácticamente cero en reposo), esta es infraestructura siempre activa. **Siempre correr `terraform destroy` al terminar de evaluarlo.**

> **Nota sobre el tipo de instancia:** `node_instance_types` debe mantenerse dentro de la capa gratuita elegible (verificar con `aws ec2 describe-instance-types --filters Name=free-tier-eligible,Values=true`). Esta cuenta de AWS está en el Plan Gratuito restrictivo de AWS, que rechaza de forma dura el lanzamiento de cualquier tipo de instancia EC2 fuera de esa lista (`t3.medium` falla con `InvalidParameterCombination`, confirmado de la manera difícil). `m7i-flex.large` (2 vCPU/8GB) es el tipo más pequeño elegible para capa gratuita con margen suficiente para el chart completo en un solo nodo — `t3.small`/`t4g.small` (2GB) tuvieron fallas reales de scheduling `0/1 nodes available: Insufficient memory` una vez que todo (kafka+mongo+redis+2x api+2x web+ingress-nginx+cert-manager) estuvo realmente desplegado.

---

## Requisitos previos

- [Terraform](https://developer.hashicorp.com/terraform/install) ≥ 1.5.7
- Credenciales de AWS con permisos para crear VPCs, clústeres EKS, roles IAM e instancias EC2
- `kubectl` y `helm` (para desplegar `../chart/` después)

## Uso

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars   # adjust region/size if needed
terraform init
terraform plan
terraform apply
```

Una vez aplicado, apuntar `kubectl`/`helm` al nuevo clúster y desplegar el chart. `secrets.mongo*`/`secrets.kafka*` y `monitoring.enabled=false` son necesarios en un clúster recién creado sin Prometheus Operator instalado y sin tag de imagen inyectado por CI — `deploy.yml` normalmente provee estos valores desde los secretos del repositorio:

```bash
$(terraform output -raw configure_kubectl)
helm upgrade --install product-management ../chart --namespace product-management --create-namespace \
  --set monitoring.enabled=false \
  --set api.image.tag=latest \
  --set secrets.mongoUsername=demo --set secrets.mongoPassword=demopass123 \
  --set "secrets.mongoConnectionString=mongodb://demo:demopass123@mongo-service:27017/products?authSource=admin" \
  --set secrets.kafkaUser=demo --set secrets.kafkaPassword=demopass123
```

> `mongo`/`kafka` solo aplican las credenciales `secrets.mongo*`/`secrets.kafka*` en su **primer** arranque contra un PVC vacío (las imágenes de Bitnami siembran la autenticación una sola vez, no en cada reinicio) — si haces `helm install` una vez con los valores por defecto (credenciales en blanco) y solo defines las reales en un `helm upgrade` posterior, los pods en ejecución no van a tomar el cambio hasta que además borres `mongo-pvc`/`kafka-pvc` y dejes que se reinicialicen. Definir las credenciales reales desde la primera instalación para evitar esto.

Para destruir todo:

```bash
terraform destroy
```

## Lo que esto *no* hace

- No construye ni publica imágenes Docker — eso lo hace `ci.yml` (jobs `docker-api`/`docker-web`).
- No configura DNS para `product.local` — o editas `/etc/hosts` apuntando a la dirección del load balancer de `ingress-nginx` (`terraform output ingress_nginx_load_balancer_hint`), o usas un dominio real.
- No gestiona el secreto `KUBECONFIG`/entorno `production` de GitHub Actions que usa `deploy.yml` — eso es un paso manual (salida de `aws eks update-kubeconfig`, codificada en base64) si quieres que CI despliegue en este clúster.

## Archivos

| Archivo | Propósito |
|---|---|
| `main.tf` | VPC (un solo NAT gateway) + clúster EKS + managed node group |
| `addons.tf` | Rol IAM del driver EBS CSI (Pod Identity), `StorageClass` `gp3` por defecto, releases de Helm de `ingress-nginx` y `cert-manager` |
| `providers.tf` | Configuración de los providers `aws`/`kubernetes`/`helm`, usando el propio token de auth del clúster EKS |
| `variables.tf` / `outputs.tf` | Inputs y outputs — ver `terraform.tfvars.example` |
