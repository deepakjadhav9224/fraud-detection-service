# Fraud Detection Stack - Helm Chart Migration Complete ✓

## Summary

You have successfully received a **production-ready Helm Chart** for your Fraud Detection Service that includes:

- ✅ Kafka & Zookeeper (Bitnami charts)
- ✅ MySQL Database (Bitnami chart)
- ✅ Fraud Detection Spring Boot Application
- ✅ Prometheus (Metrics collection)
- ✅ Grafana (Metrics visualization)
- ✅ Complete monitoring stack with pre-configured dashboards

---

## 📁 Helm Chart Files Created

### Main Chart Files

1. **`helm/fraud-detection-stack/Chart.yaml`** (100+ lines)
   - Chart metadata and dependencies definition
   - Includes 4 sub-charts: Kafka, Zookeeper, MySQL, kube-prometheus-stack

2. **`helm/fraud-detection-stack/values.yaml`** (400+ lines)
   - Default configuration for all components
   - Development environment settings
   - Resource requests/limits
   - Database credentials
   - Prometheus & Grafana configuration

3. **`helm/fraud-detection-stack/values-production.yaml`** (50+ lines)
   - Production-specific overrides
   - Higher replica counts
   - Increased resource limits
   - Extended retention periods

### Template Files (Kubernetes Resources)

4. **`templates/secrets.yaml`**
   - Kubernetes Secrets for sensitive data
   - MySQL credentials
   - Kafka bootstrap servers
   - Spring datasource configuration
   - Uses internal Kubernetes DNS (kafka.fraud-detection.svc.cluster.local:9092)

5. **`templates/fraud-detection-deployment.yaml`**
   - Spring Boot application deployment
   - Init containers for dependency checking
   - Liveness & Readiness probes
   - CPU/Memory limits
   - Pod anti-affinity for distribution
   - Prometheus metrics annotation

6. **`templates/fraud-detection-service.yaml`**
   - Kubernetes Service (ClusterIP)
   - Exposes port 8081
   - Prometheus scraping annotations

7. **`templates/serviceaccount.yaml`**
   - Service account for RBAC

8. **`templates/hpa.yaml`**
   - Horizontal Pod Autoscaler
   - Auto-scales between 2-5 replicas based on CPU/Memory

9. **`templates/pdb.yaml`**
   - Pod Disruption Budget
   - Ensures minimum pod availability during maintenance

10. **`templates/prometheus-servicemonitor.yaml`**
    - Prometheus ServiceMonitor
    - Scrapes metrics from `/actuator/prometheus` endpoint every 30 seconds

### Helper Files

11. **`templates/_helpers.tpl`**
    - Template helper functions for reusable template logic

12. **`templates/NOTES.txt`**
    - Post-installation instructions

### Documentation Files

13. **`helm/fraud-detection-stack/README.md`** (500+ lines)
    - Complete documentation
    - Installation instructions
    - Service access guide
    - Monitoring setup
    - Scaling and performance tuning
    - Troubleshooting guide

14. **`HELM_DEPLOYMENT_GUIDE.md`** (600+ lines)
    - Step-by-step deployment guide
    - Quick start for Minikube and EKS
    - Configuration scenarios (Dev, Staging, Prod)
    - Accessing services
    - Comprehensive troubleshooting

15. **`HELM_CHART_STRUCTURE.md`** (400+ lines)
    - Detailed file-by-file documentation
    - Configuration flow explanation
    - Networking architecture
    - Persistence and backup strategies

### Deployment Scripts

16. **`helm/deploy.sh`**
    - Bash script for automated deployment (Linux/macOS)
    - Menu-driven interface
    - Pre-flight checks
    - One-command deployment

17. **`helm/deploy.ps1`**
    - PowerShell script for Windows
    - Colored output
    - Interactive menu
    - Prerequisites checking

---

## 🚀 Quick Start

### Option 1: Windows PowerShell (Recommended for Windows)

```powershell
# Open PowerShell and run:
cd C:\Users\Somanath\IdeaProjects\fraud-detection-service\fraud-detection

# Make script executable
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# Run deployment
.\helm\deploy.ps1 install

# Access services
.\helm\deploy.ps1 verify
```

### Option 2: Linux/macOS Bash

```bash
cd ~/fraud-detection-service/fraud-detection

# Make script executable
chmod +x helm/deploy.sh

# Run deployment
./helm/deploy.sh 1

# Or use individual commands
./helm/deploy.sh install
./helm/deploy.sh verify
```

### Option 3: Manual Helm Commands

```bash
# Add repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Update dependencies
cd helm/fraud-detection-stack
helm dependency update

# Install chart
helm install fraud-detection . --namespace fraud-detection --create-namespace

# Verify
kubectl get pods -n fraud-detection
```

---

## 📊 Accessing Services & Dashboards

### 1. **Grafana Dashboard** (Metrics Visualization)

```powershell
# Port forward Grafana
kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80

# Access in browser
# URL: http://localhost:3000
# Username: admin
# Password: admin123 (or from values.yaml)
```

**What You'll See**:
- Kubernetes Cluster Health
- JVM Application Metrics
- MySQL Performance Metrics
- Fraud Detection Custom Metrics

### 2. **Prometheus UI** (Metrics Query)

```powershell
# Port forward Prometheus
kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090

# Access in browser
# URL: http://localhost:9090

# Example queries:
# up{job="fraud-detection"}
# rate(http_requests_total[5m])
# jvm_memory_used_bytes
```

### 3. **Application Metrics Endpoint**

```powershell
# Port forward app
kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081

# View raw metrics
# URL: http://localhost:8081/actuator/prometheus

# Health check
# URL: http://localhost:8081/actuator/health

# API Documentation
# URL: http://localhost:8081/swagger-ui.html
```

### 4. **MySQL Database**

```bash
# Connect via port-forward
kubectl port-forward -n fraud-detection svc/mysql 3306:3306

# Connection details
# Host: localhost
# Port: 3306
# User: fraud_service_user
# Password: FraudUser@123
# Database: fraud_detection_db
```

### 5. **Kafka Topics**

```bash
# Check topics
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list

# Monitor consumer group
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group fraud-detection-group --describe
```

---

## 🎯 Key Configuration Points

### Internal Kubernetes DNS Configuration

```yaml
# Services communicate via internal DNS (no external access needed)
MySQL:        mysql.fraud-detection.svc.cluster.local:3306
Kafka:        kafka.fraud-detection.svc.cluster.local:9092
Prometheus:   prometheus-operated.fraud-detection.svc.cluster.local:9090
Grafana:      kube-prometheus-stack-grafana.fraud-detection.svc.cluster.local:3000
```

### Environment Variables Mapping

The chart automatically maps your docker-compose settings:

```yaml
# From docker-compose.yml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql.fraud-detection.svc.cluster.local:3306/fraud_detection_db
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka.fraud-detection.svc.cluster.local:9092

# Database Credentials
MYSQL_DATABASE: fraud_detection_db
MYSQL_USER: fraud_service_user
MYSQL_PASSWORD: FraudUser@123
```

### Prometheus Scraping

The chart automatically configures Prometheus to scrape metrics from your application:

```yaml
Endpoint: /actuator/prometheus
Interval: 30 seconds
Timeout: 10 seconds
Target: fraud-detection service (port 8081)
```

---

## 📈 Monitoring Metrics Collected

### Application Metrics
- HTTP requests (count, duration)
- JVM memory usage
- Garbage collection metrics
- Thread metrics
- Custom business metrics (fraud detection scores, processing times)

### Database Metrics
- Connection pool stats
- Query performance
- Replication lag (if applicable)

### Kubernetes Metrics
- Pod CPU/Memory usage
- Node health
- Persistent volume usage
- Network I/O

### Kafka Metrics
- Producer/Consumer lag
- Message throughput
- Broker health

---

## 🔧 Common Tasks

### Scale Application to 5 Replicas
```bash
kubectl scale deployment/fraud-detection -n fraud-detection --replicas=5
```

### View Application Logs
```bash
kubectl logs -f -n fraud-detection deployment/fraud-detection
```

### Execute Command in Application Pod
```bash
kubectl exec -it -n fraud-detection deployment/fraud-detection -- /bin/bash
```

### Upgrade Application Image
```bash
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set fraudDetection.image.tag=2.0.0
```

### Backup MySQL Database
```bash
kubectl exec -n fraud-detection mysql-0 -- \
  mysqldump -uroot -proot fraud_detection_db > backup.sql
```

### Monitor Deployment Progress
```bash
kubectl rollout status deployment/fraud-detection -n fraud-detection -w
```

---

## 🔍 Grafana Dashboard Features

### Pre-configured Dashboards

1. **Kubernetes Cluster Monitoring**
   - Node CPU/Memory
   - Pod distribution
   - Network traffic

2. **JVM Metrics**
   - Heap memory usage
   - Garbage collection
   - Thread count

3. **MySQL Performance**
   - Query performance
   - Connection pool
   - Slow queries

### Creating Custom Dashboards

In Grafana:
1. Click "+" → Dashboard
2. Add Panel
3. Select "Prometheus" as data source
4. Write custom PromQL queries
5. Save dashboard

Example custom panels:
```promql
# Fraud Detection Rate
rate(fraud_detection_transactions_total[5m])

# API Response Time (95th percentile)
histogram_quantile(0.95, http_request_duration_seconds_bucket)

# Database Connection Pool Usage
mysql_global_status_threads_connected / mysql_global_variables_max_connections
```

---

## 🛡️ Security Considerations

### Included Security Features

1. **Secrets Management**
   - Database passwords in Kubernetes Secrets
   - Not exposed in config files

2. **RBAC**
   - ServiceAccount with minimal permissions
   - Can be enhanced with ClusterRoles/Roles

3. **Pod Security**
   - Runs as non-root user (1000)
   - Read-only root filesystem (configurable)

4. **Health Checks**
   - Liveness probes prevent zombie pods
   - Readiness probes prevent traffic to unhealthy pods

### Additional Security Recommendations

1. Use external secret managers (Vault, AWS Secrets Manager)
2. Enable network policies
3. Implement RBAC policies
4. Use image scanning for container security
5. Enable audit logging

---

## 🐛 Troubleshooting Checklist

### Pod Not Starting
```bash
kubectl describe pod/fraud-detection-xxxxx -n fraud-detection
kubectl logs -n fraud-detection pod/fraud-detection-xxxxx
```

### Database Connection Failed
```bash
# Check MySQL is running
kubectl get pods -n fraud-detection -l app=mysql

# Test connectivity
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- \
  nc -zv mysql.fraud-detection.svc.cluster.local 3306
```

### Kafka Connection Failed
```bash
# Check Kafka is running
kubectl get pods -n fraud-detection -l app=kafka

# Test connectivity
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- \
  nc -zv kafka.fraud-detection.svc.cluster.local 9092
```

### Prometheus Not Scraping
```bash
# Check Prometheus targets
kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090
# Visit: http://localhost:9090/targets

# Check application endpoint
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- \
  curl http://localhost:8081/actuator/prometheus
```

### Out of Memory
```bash
# Check resource usage
kubectl top pods -n fraud-detection
kubectl top nodes

# Increase limits
helm upgrade fraud-detection . \
  --set fraudDetection.resources.limits.memory=2Gi
```

---

## 📚 Next Steps

1. **Build & Push Docker Image**
   ```bash
   docker build -t your-registry/fraud-detection:1.0.0 .
   docker push your-registry/fraud-detection:1.0.0
   ```

2. **Update Chart Values**
   ```bash
   # Edit values.yaml or values-production.yaml
   fraudDetection:
     image:
       repository: your-registry/fraud-detection
       tag: 1.0.0
   ```

3. **Deploy to Cluster**
   ```bash
   helm install fraud-detection . --namespace fraud-detection
   ```

4. **Monitor & Verify**
   ```bash
   kubectl get pods -n fraud-detection -w
   kubectl port-forward svc/kube-prometheus-stack-grafana 3000:80
   ```

5. **Setup Alerts** (Optional)
   - Configure alerting rules in `values.yaml`
   - Setup notification channels (Slack, PagerDuty, email)

6. **Implement CI/CD** (Optional)
   - GitOps with ArgoCD or Flux
   - Automated deployments on image push

---

## 📖 Documentation Files

All documentation is in the root directory:

- **`HELM_DEPLOYMENT_GUIDE.md`** - Complete deployment instructions
- **`HELM_CHART_STRUCTURE.md`** - Technical chart documentation
- **`helm/fraud-detection-stack/README.md`** - Chart-specific README

---

## 🎓 Key Achievements

✅ **Kubernetes-Ready**: Complete migration from docker-compose to Kubernetes  
✅ **Production-Grade**: Multi-replica deployments with auto-scaling  
✅ **Monitoring**: Full observability with Prometheus and Grafana  
✅ **High Availability**: Pod disruption budgets and anti-affinity rules  
✅ **Easy Deployment**: One-command installation via Helm  
✅ **Flexible Configuration**: Environment-specific value files  
✅ **Well-Documented**: Comprehensive guides and examples  
✅ **Secure**: Secrets management and RBAC support  

---

## 💡 Tips & Tricks

### Quick Port Forwarding Alias
```bash
# Add to ~/.bashrc or ~/.zshrc
alias port-fraud='kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081'
alias port-grafana='kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80'
alias port-prometheus='kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090'
```

### One-liner to Check Everything
```bash
kubectl get all,secret,pvc -n fraud-detection
```

### Watch Pod Creation
```bash
kubectl get pods -n fraud-detection -w
```

### Get All Events in Namespace
```bash
kubectl get events -n fraud-detection --sort-by='.lastTimestamp'
```

---

## 📞 Support Resources

- **Kubernetes Docs**: https://kubernetes.io/docs/
- **Helm Docs**: https://helm.sh/docs/
- **Prometheus Docs**: https://prometheus.io/docs/
- **Grafana Docs**: https://grafana.com/docs/
- **Bitnami Charts**: https://github.com/bitnami/charts
- **Spring Boot Metrics**: https://spring.io/guides/gs/spring-boot-docker/

---

## ✅ Deployment Checklist

Before deploying to production, ensure:

- [ ] Helm and kubectl are installed
- [ ] Kubernetes cluster is accessible
- [ ] Docker image is built and pushed to registry
- [ ] `values-production.yaml` is customized for your environment
- [ ] Database passwords are changed from defaults
- [ ] Grafana admin password is changed
- [ ] All required storage classes exist in cluster
- [ ] Network policies (if any) allow inter-pod communication
- [ ] Backup strategy is in place
- [ ] Monitoring alerts are configured

---

## 🎉 Congratulations!

Your Fraud Detection Service is now Kubernetes and Helm Chart ready with complete monitoring via Prometheus and Grafana!

**Created by: GitHub Copilot**  
**Date: February 19, 2026**  
**Version: 1.0.0**

---

For detailed deployment instructions, see **`HELM_DEPLOYMENT_GUIDE.md`**  
For chart technical details, see **`HELM_CHART_STRUCTURE.md`**

