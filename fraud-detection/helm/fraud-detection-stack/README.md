# Fraud Detection Stack - Helm Chart

This Helm chart is provided as-is for the Fraud Detection Service project.

## License

- [Bitnami Helm Charts](https://github.com/bitnami/charts)
- [Grafana Documentation](https://grafana.com/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)

## Support & Resources

   - Setup disaster recovery
   - Implement multi-region deployments
   - Configure cluster autoscaling
4. **High Availability**:

   - Implement automatic updates for dependencies
   - Automate chart deployment with GitOps (ArgoCD, Flux)
3. **CI/CD Integration**:

   - Integrate with external alert systems
   - Set up alerting rules
   - Configure custom Grafana dashboards
2. **Advanced Monitoring**:

   - Enable network policies
   - Use external secrets management (Vault, Sealed Secrets)
   - Implement RBAC policies
1. **Security**:

## Next Steps

```
      innodb_buffer_pool_size=256M
      max_connections=1000
    configuration: |-
  primary:
mysql:
```yaml
### MySQL Tuning

```
    # Adjust num.network.threads, num.io.threads
    heapOpts: "-Xmx2g -Xms2g"
  broker:
kafka:
```yaml
### Kafka Tuning

```
    JAVA_OPTS: "-Xms512m -Xmx1g -XX:+UseG1GC"
  env:
fraudDetection:
```yaml
### JVM Tuning

## Performance Tuning

```
kubectl delete pvc -n fraud-detection --all
# Delete persistent volumes (if not auto-deleted)

kubectl delete namespace fraud-detection
# Delete namespace

helm uninstall fraud-detection -n fraud-detection
# Uninstall the release
```bash

## Uninstall

- Verify application exposes /actuator/prometheus endpoint
- Check Prometheus targets: Visit http://localhost:9090/targets
- Verify ServiceMonitor is created: `kubectl get servicemonitor -n fraud-detection`
#### 4. Prometheus not scraping metrics

- Verify internal DNS resolution
- Check bootstrap servers configuration
- Ensure Kafka pod is running: `kubectl get pods -n fraud-detection -l app=kafka`
#### 3. Kafka connection failed

- Verify connection string in deployment
- Check secrets: `kubectl get secret -n fraud-detection`
- Ensure MySQL pod is running: `kubectl get pods -n fraud-detection -l app=mysql`
#### 2. Database connection failed

```
kubectl describe pod/fraud-detection-XXXXX -n fraud-detection
kubectl logs -n fraud-detection pod/fraud-detection-XXXXX
```bash
#### 1. Pod not starting

### Common Issues

```
kubectl exec -it -n fraud-detection pod/fraud-detection-XXXXX -- nslookup mysql
# Check DNS resolution

kubectl exec -it -n fraud-detection pod/fraud-detection-XXXXX -- nc -zv kafka 9092
# Test Kafka connectivity

kubectl exec -it -n fraud-detection pod/fraud-detection-XXXXX -- nc -zv mysql 3306
# Test MySQL connectivity from pod
```bash

### Debug Connectivity Issues

```
kubectl describe pod/fraud-detection-XXXXX -n fraud-detection
# Describe pod for issues

kubectl get events -n fraud-detection
# Check events

kubectl get pods -n fraud-detection -o wide
# Check specific pods

kubectl get all -n fraud-detection
# Check all resources
```bash

### Check Status

## Troubleshooting

```
kubectl exec -n fraud-detection pod/prometheus-0 -- tar czf - /prometheus > prometheus-backup.tar.gz
```bash
#### Prometheus Data Backup:

```
kubectl exec -i -n fraud-detection pod/mysql-0 -- mysql -uroot -proot fraud_detection_db < backup.sql
```bash
#### MySQL Restore:

```
kubectl exec -n fraud-detection pod/mysql-0 -- mysqldump -uroot -proot fraud_detection_db > backup.sql
```bash
#### MySQL Backup:

### Backup & Restore

- **Kafka**: PVC of 10Gi per broker (configurable via `kafka.persistence.size`)
- **Grafana**: PVC of 10Gi (configurable via `prometheus.grafana.persistence.size`)
- **Prometheus**: PVC of 50Gi (configurable via `prometheus.prometheus.prometheusSpec.storageSpec`)
- **MySQL**: PVC of 20Gi (configurable via `mysql.primary.persistence.size`)

### Data Persistence

## Persistence

- **Loki**: Prometheus-like log aggregation
- **ELK Stack**: Elasticsearch, Logstash, Kibana
For centralized logging, consider adding:

### Log Aggregation (Optional)

```
kubectl logs -n fraud-detection deployment/fraud-detection --previous
# Logs from previous pod (if crashed)

kubectl logs -n fraud-detection deployment/fraud-detection --tail=100
# Last 100 lines

kubectl logs -n fraud-detection fraud-detection-XXXXX
# Logs from specific pod

kubectl logs -f -n fraud-detection deployment/fraud-detection
# Follow logs

kubectl logs -n fraud-detection deployment/fraud-detection
# Current pod logs
```bash

### View Logs

## Logging

```
kubectl get hpa -n fraud-detection -w
```bash
Monitor auto-scaling:

- Target Memory: 80%
- Target CPU: 80%
- Max replicas: 5
- Min replicas: 2
Auto-scaling is configured with HPA (Horizontal Pod Autoscaler):

### Auto-Scaling

```
kubectl scale statefulset/kafka -n fraud-detection --replicas=3
# Scale Kafka

kubectl scale deployment/fraud-detection -n fraud-detection --replicas=5
# Scale Fraud Detection deployment
```bash

### Manual Scaling

## Scaling

```
helm rollback fraud-detection 1 -n fraud-detection
# Rollback to specific revision

helm rollback fraud-detection -n fraud-detection
# Rollback to previous version

helm history fraud-detection -n fraud-detection
# List releases
```bash

### Rollback

```
helm upgrade fraud-detection . --namespace fraud-detection --values values-production.yaml
# Upgrade with custom values

helm upgrade fraud-detection . --namespace fraud-detection
# Perform upgrade

helm upgrade fraud-detection . --namespace fraud-detection --dry-run --debug
# Check what will change
```bash

### Upgrade Chart

## Upgrade

```
      - url: 'http://your-webhook-url'
      webhook_configs:
    - name: 'default'
    receivers:
      receiver: 'default'
    route:
  config:
alertmanager:
```yaml

Configure alert rules in `values.yaml` under `prometheus.alertmanager`:

### Alerting

   - MySQL Performance (ID: 11074)
   - JVM Metrics (ID: 4701)
   - Kubernetes Cluster Monitoring (ID: 7249)
3. **Pre-configured Dashboards**:

   - Set up alerts for critical thresholds
   - Create panels for specific business metrics
   - Import dashboard IDs from Grafana marketplace
2. **Create Custom Dashboards**:

   - Save & Test
   - Access: Server (default)
   - URL: `http://prometheus-operated:9090`
1. **Add Prometheus Data Source**:

### Grafana Dashboard Setup

```
jvm_threads_current
jvm_gc_pause_seconds
jvm_memory_used_bytes
# JVM Metrics

fraud_detection_processing_time_seconds
fraud_detection_fraud_score_distribution
fraud_detection_transactions_total
# Custom Business Metrics

http_request_duration_seconds
http_requests_total
# HTTP Metrics
```

The Fraud Detection application exposes metrics at `/actuator/prometheus` with the following key metrics:

### Application Metrics

## Monitoring & Observability

```
kubectl exec -it -n fraud-detection pod/kafka-0 -- kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group fraud-detection-group --describe
# Monitor topic

kubectl exec -it -n fraud-detection pod/kafka-0 -- kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic transaction-evaluated --from-beginning
# Consume messages

kubectl exec -it -n fraud-detection pod/kafka-0 -- kafka-topics.sh --bootstrap-server localhost:9092 --list
# List topics
```bash

### 5. Kafka Topics

```
SELECT * FROM your_table LIMIT 10;
SHOW TABLES;
USE fraud_detection_db;
# Inside MySQL:

kubectl exec -it -n fraud-detection pod/mysql-0 -- mysql -uroot -proot
# Connect to MySQL
```bash

### 4. MySQL Database

```
# - jvm_memory_used_bytes
# - rate(http_requests_total[5m])
# - up{job="fraud-detection"}
# Query examples:

open http://localhost:9090
# Access Prometheus

kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090
# Port forward Prometheus
```bash

### 3. Prometheus UI

- **Fraud Detection**: Custom application metrics
- **MySQL Performance**: Database performance
- **JVM Metrics**: Java application performance
- **Kubernetes Cluster**: Overall cluster health
#### Available Dashboards:

```
Password: (from values.yaml - default: admin123)
Username: admin
# Login

open http://localhost:3000
# Access Grafana

kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80
# Port forward Grafana
```bash

### 2. Grafana Dashboard

```
open http://localhost:8081/actuator/prometheus
# View metrics

open http://localhost:8081/swagger-ui.html
# View API documentation (Swagger UI)

open http://localhost:8081
# Access the application

kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081
# Port forward to local machine
```bash

### 1. Fraud Detection Application

## Accessing Services

| `prometheus.grafana.adminPassword` | Grafana admin password | `admin123` |
| `mysql.auth.password` | MySQL password | `FraudUser@123` |
| `fraudDetection.image.tag` | Docker image tag | `1.0.0` |
| `fraudDetection.image.repository` | Docker image repository | `your-registry/fraud-detection` |
| `fraudDetection.replicaCount` | App replicas | `2` |
| `namespace` | Kubernetes namespace | `fraud-detection` |
|-----------|-------------|---------|
| Parameter | Description | Default |

### Key Configuration Options

```
    adminPassword: "your-secure-password"
  grafana:
prometheus:

    maxReplicas: 10
    minReplicas: 3
    enabled: true
  autoscaling:
      memory: 1Gi
      cpu: 1000m
    limits:
      memory: 512Mi
      cpu: 500m
    requests:
  resources:
    tag: 1.0.0
    repository: your-registry/fraud-detection
  image:
  replicaCount: 3
fraudDetection:
```yaml
**Custom Production Values** (`values-production.yaml`):

Edit `values.yaml` or create custom value files:

### Override Values

## Configuration

```
  --dry-run --debug
  --values values.yaml \
  --namespace fraud-detection \
helm install fraud-detection . \
```bash
#### Dry Run (Preview what will be deployed):

```
  --set fraudDetection.image.tag=1.0.0
  --set fraudDetection.image.repository=your-registry/fraud-detection \
  --values values-production.yaml \
  --values values.yaml \
  --namespace fraud-detection \
helm install fraud-detection . \
```bash
#### Production Installation with Custom Values:

```
  --values values.yaml
  --namespace fraud-detection \
helm install fraud-detection . \
```bash
#### Development Installation:

### 4. Install the Chart

```
kubectl create namespace fraud-detection
```bash

### 3. Create Namespace

```
helm dependency update
cd helm/fraud-detection-stack
```bash

### 2. Update Helm Dependencies

```
helm repo update
# Update repositories

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
# Add Prometheus community charts

helm repo add bitnami https://charts.bitnami.com/bitnami
# Add Bitnami charts
```bash

### 1. Add Helm Repositories

## Installation

3. **kubectl**: Configured to access your cluster
2. **Helm**: v3.0+
1. **Kubernetes Cluster**: v1.20+

## Prerequisites

- **Grafana**: Metrics visualization and dashboards
- **Prometheus**: Metrics collection and alerting
### Monitoring & Observability

- **Fraud Detection Service**: Spring Boot application running on port 8081
### Application

- **MySQL** (Bitnami): Primary database (v8.0)
- **Kafka** (Bitnami): Event streaming platform (v7.5.0)
- **Zookeeper** (Bitnami): Message queue coordination
### Core Infrastructure

## Chart Components

This Helm chart provides a production-ready Kubernetes deployment for the Fraud Detection Service with integrated Prometheus and Grafana monitoring.

