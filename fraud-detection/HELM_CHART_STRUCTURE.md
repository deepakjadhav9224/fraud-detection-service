# Helm Chart Structure Documentation

## Overview

This document describes the Helm Chart structure and the purpose of each file created for the Fraud Detection Stack.

## Chart Directory Structure

```
fraud-detection-stack/
├── Chart.yaml                          # Chart metadata and dependencies
├── values.yaml                         # Default configuration values
├── values-production.yaml              # Production-specific overrides
├── README.md                           # Chart documentation
├── templates/
│   ├── NOTES.txt                       # Post-installation notes
│   ├── _helpers.tpl                    # Template helper functions
│   ├── secrets.yaml                    # Kubernetes Secrets (DB credentials, Kafka config)
│   ├── fraud-detection-deployment.yaml # Application deployment
│   ├── fraud-detection-service.yaml    # Application service
│   ├── serviceaccount.yaml             # Service account for RBAC
│   ├── hpa.yaml                        # Horizontal Pod Autoscaler
│   ├── pdb.yaml                        # Pod Disruption Budget
│   └── prometheus-servicemonitor.yaml  # Prometheus ServiceMonitor for scraping
└── charts/                             # Downloaded dependency charts
    ├── kafka/
    ├── zookeeper/
    ├── mysql/
    └── kube-prometheus-stack/
```

---

## File Descriptions

### 1. Chart.yaml

**Purpose**: Defines chart metadata and dependencies

**Key Sections**:
- `name`: Chart name (fraud-detection-stack)
- `version`: Chart version (1.0.0)
- `appVersion`: Application version (1.0.0)
- `dependencies`: External charts to include
  - Kafka (Bitnami, v26+)
  - Zookeeper (Bitnami, v12+)
  - MySQL (Bitnami, v9+)
  - kube-prometheus-stack (Prometheus Community, v56+)

**When to modify**:
- When updating chart version
- When updating dependencies
- When changing maintained by information

### 2. values.yaml

**Purpose**: Default configuration values for the entire stack

**Key Sections**:

#### Global Settings
```yaml
namespace: fraud-detection              # Kubernetes namespace
storageClass: standard                  # Storage class for PVCs
```

#### Zookeeper Configuration
- replicaCount: 1 (development)
- persistence: 8Gi
- resources: CPU 250m, memory 256Mi

#### Kafka Configuration
- Version: 7.5.0 (matches docker-compose)
- replicaCount: 1
- persistence: 10Gi
- Metrics enabled with JMX

#### MySQL Configuration
- Version: 8.0 (matches docker-compose)
- Credentials: fraud_service_user / FraudUser@123
- persistence: 20Gi
- Metrics enabled with ServiceMonitor

#### Fraud Detection App
- replicaCount: 2
- Service: ClusterIP on port 8081
- Autoscaling: 2-5 replicas, 80% CPU/Memory threshold
- Liveness & Readiness probes configured

#### Prometheus & Grafana
- kube-prometheus-stack enabled
- Grafana datasources: Prometheus (http://prometheus-operated:9090)
- Pre-configured dashboards: Kubernetes, JVM, MySQL
- Retention: 15 days
- Storage: 50Gi

### 3. values-production.yaml

**Purpose**: Production-specific configuration overrides

**Common Overrides**:
- Higher replica counts (3 instead of 1-2)
- Increased resource limits
- Larger persistence volumes
- Autoscaling with higher max replicas
- Extended metrics retention (30 days)
- Production registry and image tags

**Usage**:
```bash
helm install fraud-detection . \
  --values values.yaml \
  --values values-production.yaml
```

### 4. templates/secrets.yaml

**Purpose**: Creates Kubernetes Secrets for sensitive data

**Contains**:
- MySQL connection details (host, port, database, credentials)
- Kafka bootstrap servers and advertised listeners
- Spring datasource URL and credentials
- Base64 encoded values

**Key Feature**: Uses internal Kubernetes DNS:
```
mysql.fraud-detection.svc.cluster.local:3306
kafka.fraud-detection.svc.cluster.local:9092
```

**Usage**: Referenced by deployment via `envFrom.secretKeyRef`

### 5. templates/fraud-detection-deployment.yaml

**Purpose**: Kubernetes Deployment for the fraud detection application

**Components**:
- **Init Containers**: Wait for MySQL and Kafka to be ready
- **Main Container**: Spring Boot application
- **Environment Variables**: From ConfigMap and Secrets
- **Probes**:
  - Liveness: `/actuator/health` (30s initial delay)
  - Readiness: `/actuator/health/readiness` (20s initial delay)
- **Resource Limits**: CPU and memory constraints
- **Volume Mounts**: Log volume
- **Pod Anti-Affinity**: Spreads pods across nodes

**Key Features**:
- Prometheus annotations for scraping
- Init containers for dependency checking
- Health checks for reliability
- Security context (non-root user)

### 6. templates/fraud-detection-service.yaml

**Purpose**: Kubernetes Service to expose the application

**Configuration**:
- Type: ClusterIP (internal only)
- Port: 8081
- Prometheus annotations for metrics scraping

**Service Discovery**:
- DNS: `fraud-detection.fraud-detection.svc.cluster.local:8081`
- Can be changed to LoadBalancer or NodePort for external access

### 7. templates/serviceaccount.yaml

**Purpose**: Creates a ServiceAccount for RBAC

**Use Cases**:
- Pod authentication to Kubernetes API
- Applying RBAC policies
- Audit logging

**Permissions**: Currently minimal (can be extended with ClusterRoles/Roles)

### 8. templates/hpa.yaml

**Purpose**: Horizontal Pod Autoscaler configuration

**Scaling Logic**:
- Min replicas: 2
- Max replicas: 5
- CPU threshold: 80%
- Memory threshold: 80%

**Behavior**: When metrics exceed thresholds, new pods are created (up to max)

### 9. templates/pdb.yaml

**Purpose**: Pod Disruption Budget for high availability

**Configuration**:
- Minimum available pods: 1
- Ensures at least 1 pod remains during node maintenance

### 10. templates/prometheus-servicemonitor.yaml

**Purpose**: Tells Prometheus how to scrape application metrics

**Configuration**:
- Targets: fraud-detection service
- Endpoint: `/actuator/prometheus`
- Interval: 30 seconds
- Timeout: 10 seconds

**Note**: Requires Prometheus Operator (included in kube-prometheus-stack)

### 11. templates/_helpers.tpl

**Purpose**: Reusable template functions

**Functions**:
- `fraud-detection-stack.name`: Chart name
- `fraud-detection-stack.fullname`: Full release name
- `fraud-detection-stack.chart`: Chart label
- `fraud-detection-stack.labels`: Common labels
- `fraud-detection-stack.selectorLabels`: Selector labels

**Usage**: Referenced in all templates via `{{ include "..." . }}`

### 12. templates/NOTES.txt

**Purpose**: Post-installation instructions

**Shows Users**:
- How to access each service
- Port-forward commands
- Default credentials
- Swagger UI URL
- Troubleshooting commands

### 13. README.md

**Purpose**: Complete Helm Chart documentation

**Sections**:
- Installation instructions
- Configuration guide
- Service access methods
- Monitoring setup
- Scaling instructions
- Troubleshooting guide
- Performance tuning tips

---

## Configuration Flow

```
values.yaml (defaults)
    ↓
values-production.yaml (overrides)
    ↓
Command-line --set flags (highest priority)
    ↓
Rendered templates
    ↓
Kubernetes resources
```

---

## Environment Variables Mapping

### From Secrets (Sensitive Data)
```yaml
MYSQL_HOST → mysql.fraud-detection.svc.cluster.local
MYSQL_DATABASE → fraud_detection_db
SPRING_DATASOURCE_URL → jdbc:mysql://mysql.fraud-detection.svc.cluster.local:3306/fraud_detection_db
SPRING_KAFKA_BOOTSTRAP_SERVERS → kafka.fraud-detection.svc.cluster.local:9092
```

### From ConfigMap (Non-sensitive)
```yaml
SPRING_PROFILES_ACTIVE → kubernetes
SERVER_PORT → 8081
LOGGING_LEVEL_COM_GMB → INFO
```

---

## Networking

### Service Discovery

Internal communication uses Kubernetes DNS:
```
Service: fraud-detection.fraud-detection.svc.cluster.local:8081
MySQL: mysql.fraud-detection.svc.cluster.local:3306
Kafka: kafka.fraud-detection.svc.cluster.local:9092
Prometheus: prometheus-operated.fraud-detection.svc.cluster.local:9090
Grafana: kube-prometheus-stack-grafana.fraud-detection.svc.cluster.local:3000
```

### Port Forwarding for External Access

```bash
# Application
kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081

# Grafana
kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80

# Prometheus
kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090
```

---

## Persistence

### Storage Requirements

| Component | Size | Purpose |
|-----------|------|---------|
| MySQL | 20Gi | Database storage |
| Prometheus | 50Gi | Metrics storage (15 days) |
| Grafana | 10Gi | Dashboard configuration |
| Kafka | 10Gi per broker | Message logs |

### Storage Classes

- Default: `standard` (can be changed to `fast`, `gp3`, etc.)
- Ensure your cluster has storage class available: `kubectl get storageclass`

---

## Monitoring Stack

### Components
1. **Prometheus**: Metrics collection and storage
2. **Grafana**: Metrics visualization
3. **AlertManager**: Alert routing and management
4. **Node Exporter**: Node-level metrics
5. **Kube-State-Metrics**: Kubernetes object metrics

### Data Flow
```
Application (/actuator/prometheus)
    ↓
Prometheus (scrapes every 30s)
    ↓
Grafana (queries and visualizes)
    ↓
User Dashboard (http://localhost:3000)
```

---

## Dependency Management

### Chart Dependencies Resolution

When you run `helm dependency update`:
1. Helm reads `Chart.yaml` dependencies
2. Fetches charts from repositories
3. Stores in `charts/` directory
4. Creates `Chart.lock` file (version lock)

### Dependency Versions
```yaml
kafka: "26.x.x"           # Latest 26.x version
mysql: "9.x.x"            # Latest 9.x version
zookeeper: "12.x.x"       # Latest 12.x version
kube-prometheus-stack: "56.x.x"  # Latest 56.x version
```

---

## Customization Examples

### 1. Change Application Replicas
```bash
helm install fraud-detection . \
  --set fraudDetection.replicaCount=5
```

### 2. Use Different Docker Registry
```bash
helm install fraud-detection . \
  --set fraudDetection.image.repository=my-registry.com/fraud-detection \
  --set fraudDetection.image.tag=v2.0.0
```

### 3. Enable Ingress
```bash
helm install fraud-detection . \
  --set fraudDetection.ingress.enabled=true \
  --set fraudDetection.ingress.hosts[0].host=fraud-detection.example.com
```

### 4. Disable Prometheus
```bash
helm install fraud-detection . \
  --set prometheus.enabled=false
```

### 5. Use Custom Storage Class
```bash
helm install fraud-detection . \
  --set global.storageClass=fast-ssd
```

---

## Best Practices

1. **Version Control**: Keep Chart.yaml versions updated
2. **Secrets Management**: Use external secret managers (Vault, Sealed Secrets)
3. **Resource Limits**: Always set resource requests/limits
4. **Monitoring**: Always enable Prometheus for observability
5. **Backup**: Regular backups of persistent data
6. **Documentation**: Keep values-production.yaml documented
7. **Testing**: Test in staging before production deployment

---

This documentation should help you understand and manage the Helm Chart effectively.

