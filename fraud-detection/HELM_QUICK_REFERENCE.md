# Fraud Detection Stack - Quick Reference Card

## 🚀 Installation (Choose One Method)

### Method 1: Windows PowerShell
```powershell
cd helm
.\deploy.ps1 install
```

### Method 2: Linux/macOS Bash
```bash
cd helm
chmod +x deploy.sh
./deploy.sh 1
```

### Method 3: Manual Helm
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
cd helm/fraud-detection-stack
helm dependency update
helm install fraud-detection . --namespace fraud-detection --create-namespace
```

---

## 📊 Access Services (Port Forward)

| Service | Command | URL | Credentials |
|---------|---------|-----|-------------|
| **Grafana** | `kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80` | http://localhost:3000 | admin / admin123 |
| **Prometheus** | `kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090` | http://localhost:9090 | N/A |
| **Application** | `kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081` | http://localhost:8081 | N/A |
| **MySQL** | `kubectl port-forward -n fraud-detection svc/mysql 3306:3306` | localhost:3306 | fraud_service_user / FraudUser@123 |
| **Kafka** | `kubectl port-forward -n fraud-detection pod/kafka-0 9092:9092` | localhost:9092 | N/A |

---

## 🎯 Key Kubernetes DNS Addresses

Use these inside the cluster (in pod environment):

```
MySQL:        mysql.fraud-detection.svc.cluster.local:3306
Kafka:        kafka.fraud-detection.svc.cluster.local:9092
Prometheus:   prometheus-operated.fraud-detection.svc.cluster.local:9090
Grafana:      kube-prometheus-stack-grafana.fraud-detection.svc.cluster.local:3000
Application:  fraud-detection.fraud-detection.svc.cluster.local:8081
```

---

## 🔍 Common kubectl Commands

```bash
# Check pod status
kubectl get pods -n fraud-detection

# View pod logs
kubectl logs -n fraud-detection deployment/fraud-detection
kubectl logs -f -n fraud-detection deployment/fraud-detection  # Follow logs

# Describe pod (debug)
kubectl describe pod/fraud-detection-xxxxx -n fraud-detection

# Execute command in pod
kubectl exec -it -n fraud-detection pod/fraud-detection-xxxxx -- /bin/bash

# Check resource usage
kubectl top pods -n fraud-detection
kubectl top nodes

# View all resources
kubectl get all -n fraud-detection

# View events
kubectl get events -n fraud-detection --sort-by='.lastTimestamp'

# Scale deployment
kubectl scale deployment/fraud-detection -n fraud-detection --replicas=5

# Watch status
kubectl get pods -n fraud-detection -w

# Check persistent volumes
kubectl get pvc -n fraud-detection

# Delete namespace (careful!)
kubectl delete namespace fraud-detection
```

---

## 📈 Monitoring Queries (Prometheus)

Copy-paste these in Prometheus UI (http://localhost:9090):

```promql
# Is application running?
up{job="fraud-detection"}

# Request rate (requests per second)
rate(http_requests_total[5m])

# Error rate
rate(http_requests_total{status=~"5.."}[5m])

# JVM memory usage
jvm_memory_used_bytes

# JVM heap memory
jvm_memory_used_bytes{area="heap"}

# Database connections
mysql_global_status_threads_connected

# Kafka consumer lag
kafka_consumer_lag

# Pod CPU usage
sum(rate(container_cpu_usage_seconds_total[5m])) by (pod_name)

# Pod memory usage
sum(container_memory_usage_bytes) by (pod_name)
```

---

## 🎨 Grafana Dashboard Setup

1. **Login**: http://localhost:3000 (admin/admin123)
2. **Add Data Source**: 
   - URL: http://prometheus-operated:9090
   - Access: Server
   - Save
3. **Import Dashboards**:
   - Click "+" → Dashboard → Import
   - Dashboard ID: 7249 (Kubernetes)
   - Dashboard ID: 4701 (JVM)
   - Dashboard ID: 11074 (MySQL)

---

## 📁 Chart Files Location

```
fraud-detection/
├── helm/
│   ├── deploy.sh (Linux/macOS)
│   ├── deploy.ps1 (Windows)
│   └── fraud-detection-stack/
│       ├── Chart.yaml
│       ├── values.yaml
│       ├── values-production.yaml
│       ├── README.md
│       └── templates/
│           ├── secrets.yaml
│           ├── fraud-detection-deployment.yaml
│           ├── fraud-detection-service.yaml
│           ├── hpa.yaml
│           ├── pdb.yaml
│           ├── serviceaccount.yaml
│           ├── prometheus-servicemonitor.yaml
│           └── _helpers.tpl
├── HELM_MIGRATION_SUMMARY.md
├── HELM_DEPLOYMENT_GUIDE.md
└── HELM_CHART_STRUCTURE.md
```

---

## 🔧 Helm Commands Reference

```bash
# List releases
helm list -n fraud-detection

# View release status
helm status fraud-detection -n fraud-detection

# View values
helm get values fraud-detection -n fraud-detection

# View manifest
helm get manifest fraud-detection -n fraud-detection

# Upgrade release
helm upgrade fraud-detection . --namespace fraud-detection

# Upgrade with custom values
helm upgrade fraud-detection . --namespace fraud-detection -f values-production.yaml

# Rollback to previous version
helm rollback fraud-detection -n fraud-detection

# View release history
helm history fraud-detection -n fraud-detection

# Uninstall release
helm uninstall fraud-detection -n fraud-detection

# Test chart
helm lint fraud-detection-stack

# Dry-run installation
helm install fraud-detection . --namespace fraud-detection --dry-run --debug

# Update dependencies
helm dependency update fraud-detection-stack
```

---

## 🐛 Troubleshooting Checklist

### Pod won't start?
```bash
kubectl describe pod/fraud-detection-xxxxx -n fraud-detection
kubectl logs -n fraud-detection pod/fraud-detection-xxxxx
```

### Can't reach database?
```bash
# Check MySQL running
kubectl get pods -n fraud-detection -l app=mysql

# Test connection
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- nc -zv mysql 3306
```

### Can't reach Kafka?
```bash
# Check Kafka running
kubectl get pods -n fraud-detection -l app=kafka

# Test connection
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- nc -zv kafka 9092
```

### Prometheus not scraping?
```bash
# Check targets: http://localhost:9090/targets

# Check app endpoint
kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081
# Visit: http://localhost:8081/actuator/prometheus
```

### Out of disk/memory?
```bash
kubectl top nodes
kubectl top pods -n fraud-detection
df -h
```

---

## 📝 Configuration Changes

### Change Grafana Admin Password
```bash
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set prometheus.grafana.adminPassword=NewPassword123!
```

### Change MySQL Password
```bash
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set mysql.auth.password=NewPassword123!
```

### Update Application Image
```bash
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set fraudDetection.image.repository=your-registry/fraud-detection \
  --set fraudDetection.image.tag=2.0.0
```

### Scale Replicas
```bash
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set fraudDetection.replicaCount=5
```

### Increase Resource Limits
```bash
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set fraudDetection.resources.limits.memory=2Gi \
  --set fraudDetection.resources.limits.cpu=1500m
```

---

## 📊 Accessing Specific Data

### View Application Logs (Last 100 lines, following)
```bash
kubectl logs -n fraud-detection deployment/fraud-detection --tail=100 -f
```

### View MySQL Logs
```bash
kubectl logs -n fraud-detection pod/mysql-0
```

### View Kafka Logs
```bash
kubectl logs -n fraud-detection pod/kafka-0
```

### List Kafka Topics
```bash
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Consume Kafka Messages
```bash
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic transaction-evaluated --from-beginning
```

### Query MySQL Database
```bash
kubectl exec -it -n fraud-detection mysql-0 -- \
  mysql -u fraud_service_user -pFraudUser@123 fraud_detection_db \
  -e "SELECT * FROM your_table LIMIT 10;"
```

### Backup MySQL
```bash
kubectl exec -n fraud-detection mysql-0 -- \
  mysqldump -u fraud_service_user -pFraudUser@123 fraud_detection_db > backup.sql
```

---

## ⚡ Performance Monitoring

### Check Pod Metrics
```bash
# Real-time metrics
kubectl top pods -n fraud-detection

# With sorting
kubectl top pods -n fraud-detection --sort-by=memory
kubectl top pods -n fraud-detection --sort-by=cpu

# All namespaces
kubectl top pods -A
```

### Check Node Metrics
```bash
kubectl top nodes
kubectl top nodes --sort-by=cpu
kubectl top nodes --sort-by=memory
```

### View Resource Requests/Limits
```bash
kubectl describe nodes
kubectl describe node node-name
```

---

## 🔐 Security & Secrets

### View Secrets
```bash
kubectl get secrets -n fraud-detection
kubectl get secret fraud-detection-secrets -n fraud-detection -o yaml
```

### Decode Secret Value
```bash
kubectl get secret fraud-detection-secrets -n fraud-detection \
  -o jsonpath='{.data.MYSQL_PASSWORD}' | base64 --decode
```

### Update Secret
```bash
kubectl create secret generic fraud-detection-secrets \
  --from-literal=MYSQL_PASSWORD=NewPassword123! \
  --namespace fraud-detection \
  --dry-run=client -o yaml | kubectl apply -f -
```

---

## 📈 Auto-Scaling Status

### View HPA Status
```bash
kubectl get hpa -n fraud-detection
kubectl get hpa -n fraud-detection -w  # Watch

# Detailed HPA info
kubectl describe hpa fraud-detection -n fraud-detection
```

### Manual Scaling
```bash
kubectl scale deployment/fraud-detection -n fraud-detection --replicas=10
```

---

## 🎯 Health Checks

### Application Health
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/health/readiness
curl http://localhost:8081/actuator/health/liveness
```

### All Pod Readiness
```bash
kubectl wait --for=condition=Ready pod --all -n fraud-detection --timeout=600s
```

### Deployment Rollout Status
```bash
kubectl rollout status deployment/fraud-detection -n fraud-detection -w
```

---

## 📚 Documentation Files

- **`HELM_MIGRATION_SUMMARY.md`** - Overview and quick start
- **`HELM_DEPLOYMENT_GUIDE.md`** - Detailed deployment guide
- **`HELM_CHART_STRUCTURE.md`** - Technical chart documentation
- **`helm/fraud-detection-stack/README.md`** - Chart-specific README

---

## 🔗 External Resources

- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Helm Docs](https://helm.sh/docs/)
- [Prometheus Docs](https://prometheus.io/docs/)
- [Grafana Docs](https://grafana.com/docs/)
- [Bitnami Helm Charts](https://github.com/bitnami/charts)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

---

## ✅ Pre-Deployment Checklist

- [ ] Helm installed (`helm version`)
- [ ] kubectl installed (`kubectl version`)
- [ ] Kubernetes cluster accessible (`kubectl cluster-info`)
- [ ] Docker image built and pushed
- [ ] values.yaml customized
- [ ] Helm repo added and updated
- [ ] Chart dependencies updated
- [ ] Namespace exists or --create-namespace flag used

---

## 🎓 Quick Tips

1. **Always use namespace**: Add `-n fraud-detection` to kubectl commands
2. **Watch deployments**: Use `-w` flag to watch status: `kubectl get pods -n fraud-detection -w`
3. **Follow logs**: Use `-f` flag: `kubectl logs -f -n fraud-detection deployment/fraud-detection`
4. **Use aliases**: Create aliases for common commands
5. **Dry-run first**: Test changes with `--dry-run --debug` before applying
6. **Check events**: Monitor `kubectl get events -n fraud-detection` for issues
7. **Use labels**: Filter by labels: `kubectl get pods -n fraud-detection -l app=fraud-detection`

---

**Last Updated**: February 19, 2026  
**Chart Version**: 1.0.0  
**Kubernetes Version**: 1.20+  
**Helm Version**: 3.0+

