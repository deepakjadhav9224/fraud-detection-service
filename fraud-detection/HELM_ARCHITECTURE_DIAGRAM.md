# Fraud Detection Stack - Architecture & Components

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        KUBERNETES CLUSTER                                   │
│                     (fraud-detection namespace)                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER                                     │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Fraud Detection Spring Boot Application (Deployment)               │  │
│  │  ├─ Replicas: 2 (scalable 2-5)                                     │  │
│  │  ├─ Port: 8081                                                      │  │
│  │  ├─ Health Checks: Liveness & Readiness probes                     │  │
│  │  ├─ Metrics: /actuator/prometheus                                  │  │
│  │  └─ Pods spread across nodes (anti-affinity)                       │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                              │ │ │ │                                        │
│                              ↓ ↓ ↓ ↓                                        │
│  ┌────────────────────────────────────────┐  ┌──────────────────────────┐ │
│  │     Kubernetes Service                 │  │  HorizontalPodAutoscaler │ │
│  │  (fraud-detection)                     │  │  Scales 2-5 based on CPU │ │
│  │  ├─ Type: ClusterIP                    │  │  and Memory usage        │ │
│  │  ├─ Port: 8081                         │  │  (80% thresholds)        │ │
│  │  └─ Internal DNS:                      │  │                          │ │
│  │    fraud-detection.fraud-detection     │  └──────────────────────────┘ │
│  │    .svc.cluster.local:8081             │                                │
│  └────────────────────────────────────────┘                                │
│                                                                              │
│          ServiceMonitor (for Prometheus scraping)                          │
│          ├─ Endpoint: /actuator/prometheus                                │
│          ├─ Interval: 30 seconds                                          │
│          └─ Timeout: 10 seconds                                           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                                     │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  KAFKA (Message Queue)                                                 │ │
│  │  ├─ Version: 7.5.0                                                    │ │
│  │  ├─ Replicas: 1 (configurable for HA)                                │ │
│  │  ├─ Internal DNS: kafka.fraud-detection.svc.cluster.local:9092      │ │
│  │  ├─ Persistence: 10Gi                                                │ │
│  │  ├─ Topics: transaction-evaluated, transaction-status-updated,      │ │
│  │  │           customer-action-received                                │ │
│  │  └─ JMX Metrics: Enabled (port 9999)                                │ │
│  │      └─ Scraped by Prometheus for Kafka metrics                     │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  ZOOKEEPER (Coordination)                                              │ │
│  │  ├─ Version: Bitnami default                                          │ │
│  │  ├─ Replicas: 1                                                       │ │
│  │  ├─ Internal DNS: zookeeper.fraud-detection.svc.cluster.local:2181  │ │
│  │  └─ Persistence: 8Gi                                                 │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  MySQL (Database)                                                      │ │
│  │  ├─ Version: 8.0                                                      │ │
│  │  ├─ Database: fraud_detection_db                                      │ │
│  │  ├─ User: fraud_service_user                                          │ │
│  │  ├─ Password: FraudUser@123 (in Secret)                              │ │
│  │  ├─ Internal DNS: mysql.fraud-detection.svc.cluster.local:3306      │ │
│  │  ├─ Persistence: 20Gi (configurable)                                │ │
│  │  └─ Metrics: Enabled                                                │ │
│  │      └─ Scraped by Prometheus for DB metrics                        │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  Kubernetes Secrets (Configuration)                                    │ │
│  │  ├─ MYSQL_HOST, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD          │ │
│  │  ├─ KAFKA_BOOTSTRAP_SERVERS                                          │ │
│  │  ├─ SPRING_DATASOURCE_URL                                            │ │
│  │  └─ SPRING_KAFKA_BOOTSTRAP_SERVERS                                   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  Kubernetes ConfigMap (Non-sensitive Configuration)                    │ │
│  │  ├─ SPRING_PROFILES_ACTIVE: kubernetes                               │ │
│  │  ├─ SERVER_PORT: 8081                                                │ │
│  │  ├─ Kafka topics and consumer groups                                │ │
│  │  └─ Logging levels                                                  │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                   MONITORING & OBSERVABILITY LAYER                          │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  PROMETHEUS (Metrics Collection)                                       │ │
│  │  ├─ Deployment: 1 replica                                             │ │
│  │  ├─ Internal DNS: prometheus-operated:9090                            │ │
│  │  ├─ Persistence: 50Gi (15-day retention)                             │ │
│  │  ├─ Scrape Targets:                                                  │ │
│  │  │  ├─ Fraud Detection App (/actuator/prometheus)                   │ │
│  │  │  ├─ Kafka (JMX via exporter)                                    │ │
│  │  │  ├─ MySQL (MySQL exporter)                                      │ │
│  │  │  ├─ Kubernetes nodes (Node Exporter)                            │ │
│  │  │  └─ Kubernetes objects (Kube-State-Metrics)                     │ │
│  │  └─ Port: 9090                                                      │ │
│  │      └─ Access via: kubectl port-forward svc/prometheus-operated   │ │
│  │         9090:9090                                                   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  GRAFANA (Visualization & Dashboards)                                  │ │
│  │  ├─ Deployment: 1 replica                                             │ │
│  │  ├─ Port: 3000                                                        │ │
│  │  ├─ Admin: admin / admin123                                           │ │
│  │  ├─ Datasources:                                                      │ │
│  │  │  └─ Prometheus (http://prometheus-operated:9090)                 │ │
│  │  ├─ Pre-configured Dashboards:                                       │ │
│  │  │  ├─ Kubernetes Cluster Monitoring                                │ │
│  │  │  ├─ JVM Metrics                                                 │ │
│  │  │  └─ MySQL Performance                                           │ │
│  │  ├─ Persistence: 10Gi                                               │ │
│  │  └─ Access via: kubectl port-forward svc/                          │ │
│  │     kube-prometheus-stack-grafana 3000:80                          │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  ALERTMANAGER (Alert Routing)                                          │ │
│  │  ├─ Part of: kube-prometheus-stack                                    │ │
│  │  ├─ Handles: Alert routing, deduplication, grouping                 │ │
│  │  ├─ Persistence: 10Gi                                                │ │
│  │  └─ Configurable via: values.yaml                                   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  NODE EXPORTER (Node-level Metrics)                                    │ │
│  │  ├─ DaemonSet: Runs on every node                                     │ │
│  │  ├─ Collects: CPU, Memory, Disk, Network metrics                     │ │
│  │  └─ Scraped by: Prometheus                                           │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │  KUBE-STATE-METRICS (Kubernetes Object Metrics)                        │ │
│  │  ├─ Deployment: 1 replica                                             │ │
│  │  ├─ Collects: Pod, Deployment, Node, PVC, etc. metrics              │ │
│  │  └─ Scraped by: Prometheus                                           │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    STORAGE & PERSISTENCE LAYER                              │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Persistent Volume Claims (PVCs)                                     │  │
│  │  ├─ MySQL Data: 20Gi                                                │  │
│  │  ├─ Prometheus: 50Gi                                                │  │
│  │  ├─ Grafana: 10Gi                                                  │  │
│  │  ├─ Kafka: 10Gi per broker                                         │  │
│  │  └─ Zookeeper: 8Gi                                                 │  │
│  │                                                                     │  │
│  │  All backed by cluster storage class: standard (or configured)     │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                  DATA FLOW & COMMUNICATION                                  │
│                                                                              │
│  Application → Kafka                                                        │
│  ├─ Topics: transaction-evaluated, customer-action-received                │
│  ├─ Bootstrap Server: kafka.fraud-detection.svc.cluster.local:9092        │
│  └─ Messages: JSON serialized events                                       │
│                                                                              │
│  Application → MySQL                                                        │
│  ├─ Connection URL: jdbc:mysql://mysql.fraud-detection.svc:3306/          │
│  │                 fraud_detection_db                                      │
│  ├─ User: fraud_service_user                                              │
│  └─ Operations: Read/Write fraud detection data                           │
│                                                                              │
│  Application → Prometheus                                                   │
│  ├─ Endpoint: /actuator/prometheus                                        │
│  ├─ Scrape Interval: 30 seconds                                           │
│  └─ Metrics: HTTP, JVM, Custom business metrics                           │
│                                                                              │
│  Prometheus → Grafana                                                       │
│  ├─ Data Source: http://prometheus-operated:9090                          │
│  ├─ Query: PromQL queries from dashboard panels                           │
│  └─ Visualization: Real-time dashboards                                   │
│                                                                              │
│  Prometheus → AlertManager (if alerts configured)                          │
│  ├─ Alert Rules: Fired based on thresholds                               │
│  ├─ Notification: Routed to configured channels                          │
│  └─ Channels: Slack, PagerDuty, Email, Webhook, etc.                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Component Dependency Graph

```
User/Client
    ↓
    └─→ Fraud Detection Service (Port 8081)
        ├─→ MySQL Database
        │   └─ fraud_detection_db (credentials in Secret)
        │
        ├─→ Kafka Cluster
        │   ├─→ Zookeeper (coordination)
        │   └─ Topics: transaction-evaluated, etc.
        │
        └─→ Prometheus Metrics Endpoint
            └─→ Prometheus Scraper
                ├─→ AlertManager (if alerts triggered)
                │   └─ Notification channels
                │
                └─→ Grafana Dashboard
                    └─ User Views Metrics
```

---

## 🔄 Traffic Flow

### Incoming Traffic
```
External Request
    ↓
kubectl port-forward (or LoadBalancer/Ingress)
    ↓
Kubernetes Service (fraud-detection:8081)
    ↓
Pod 1/2/3/4/5 (fraud-detection deployment, 2-5 replicas)
    ↓
Spring Boot Application (8081)
```

### Database Traffic
```
Application
    ↓
Kubernetes DNS Resolution: mysql.fraud-detection.svc.cluster.local
    ↓
Service: mysql (port 3306)
    ↓
StatefulSet Pod: mysql-0
    ↓
MySQL Server
    ↓
Persistent Volume
```

### Kafka Traffic
```
Application Producer
    ↓
Kubernetes DNS: kafka.fraud-detection.svc.cluster.local:9092
    ↓
Service: kafka (port 9092)
    ↓
Kafka Broker Pod: kafka-0
    ↓
Topic Storage

Application Consumer
    ↓
Kafka Consumer Group: fraud-detection-group
    ↓
Topic Offset Tracking (in Zookeeper)
```

### Metrics Collection
```
Application (/actuator/prometheus)
    ↓
Prometheus Scraper (every 30s)
    ↓
Prometheus Storage (TSDB, 50Gi PVC)
    ↓
Grafana Queries (PromQL)
    ↓
Visualization in Dashboard
    ↓
User Views Metrics (http://localhost:3000)
```

---

## 🔐 Security Zones

```
┌─────────────────────────────────────────────────────┐
│ Public Zone (External)                              │
│ - Port-forwarded via kubectl (development)          │
│ - LoadBalancer/Ingress (production)                 │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│ Kubernetes Service Layer (ClusterIP)                │
│ - Service-to-Pod communication                      │
│ - Internal DNS resolution                           │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│ Pod Network (Internal)                              │
│ - Pod-to-Pod communication via internal IPs         │
│ - Network policies (optional)                       │
│ - Secrets mounted as volumes (not env vars)         │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────┐
│ Container Layer                                     │
│ - Application running as non-root (UID 1000)        │
│ - Read-only root filesystem (configurable)          │
│ - Resource limits enforced                          │
└─────────────────────────────────────────────────────┘
```

---

## 💾 Data Persistence Strategy

```
Application Pod
    ↓
Volume Mount: /app/logs
    ↓
EmptyDir (ephemeral, lost on pod restart)


MySQL Pod
    ↓
Volume Mount: /var/lib/mysql
    ↓
PVC: mysql-data (20Gi)
    ↓
Persistent Volume
    ↓
Cluster Storage Backend (varies: EBS, GCE PD, etc.)


Prometheus Pod
    ↓
Volume Mount: /prometheus
    ↓
PVC: prometheus-data (50Gi)
    ↓
Persistent Volume
    ↓
Cluster Storage Backend


Grafana Pod
    ↓
Volume Mount: /var/lib/grafana
    ↓
PVC: grafana-storage (10Gi)
    ↓
Persistent Volume
    ↓
Cluster Storage Backend
```

---

## 📈 Scaling Architecture

```
┌──────────────────────────────────────┐
│  Horizontal Pod Autoscaler (HPA)    │
│  ├─ Min Replicas: 2                 │
│  ├─ Max Replicas: 5                 │
│  ├─ CPU Target: 80%                 │
│  └─ Memory Target: 80%              │
└────────┬─────────────────────────────┘
         │ Monitors Metrics from Prometheus
         ↓
┌──────────────────────────────────────┐
│  Deployment: fraud-detection         │
│                                      │
│  Pod 1  Pod 2  Pod 3  Pod 4  Pod 5  │
│  (initially 2, scales 2-5)          │
│                                      │
│  Each pod:                           │
│  ├─ CPU request: 500m limit 1000m  │
│  ├─ Mem request: 512Mi limit 1Gi   │
│  └─ Anti-affinity: spread across   │
│     different nodes                 │
└──────────────────────────────────────┘
```

---

## 🎯 Network Communication Patterns

### Within Cluster (Pod-to-Pod)
```
fraud-detection-pod-1:8081
    → mysql.fraud-detection.svc.cluster.local:3306
    → kafka.fraud-detection.svc.cluster.local:9092
    → prometheus-operated.fraud-detection.svc.cluster.local:9090
```

### Service Discovery via DNS
```
my-service.my-namespace.svc.cluster.local

In our case:
fraud-detection.fraud-detection.svc.cluster.local
mysql.fraud-detection.svc.cluster.local
kafka.fraud-detection.svc.cluster.local
```

### External Access (Development/Debugging)
```
kubectl port-forward svc/fraud-detection 8081:8081
→ localhost:8081 maps to service:8081

kubectl port-forward svc/kube-prometheus-stack-grafana 3000:80
→ localhost:3000 maps to service:80
```

---

## 📝 Resource Allocation Summary

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit | Storage |
|-----------|------------|-----------|----------------|-------------|---------|
| App (each) | 500m | 1000m | 512Mi | 1Gi | - |
| MySQL | 250m | 500m | 256Mi | 512Mi | 20Gi |
| Kafka | 250m | 500m | 512Mi | 1Gi | 10Gi |
| Zookeeper | 250m | 500m | 256Mi | 512Mi | 8Gi |
| Prometheus | - | - | 512Mi | 1Gi | 50Gi |
| Grafana | - | - | - | - | 10Gi |

---

## 🚀 Deployment Workflow

```
1. Developer Pushes Code
        ↓
2. CI/CD Pipeline Builds Docker Image
        ↓
3. Image Pushed to Registry
        ↓
4. Helm Chart Updated with New Image Tag
        ↓
5. helm upgrade fraud-detection . (executed)
        ↓
6. Kubernetes Rolling Update
   ├─ New pod starts (readiness probe)
   ├─ Old pod drains connections
   └─ Old pod terminates
        ↓
7. HPA monitors new metrics
        ↓
8. Prometheus scrapes metrics from new pods
        ↓
9. Grafana displays updated metrics
        ↓
10. Application Running with Monitoring
```

---

This architecture provides:
- ✅ **High Availability**: Multiple replicas with auto-scaling
- ✅ **Observability**: Full metrics and dashboard visibility
- ✅ **Resilience**: Health checks and auto-restart
- ✅ **Scalability**: Auto-scales based on demand
- ✅ **Persistence**: Data survives pod restarts
- ✅ **Security**: Secrets management and RBAC ready
- ✅ **Production-Ready**: All components clustered (configurable)

