# Helm Chart Installation & Deployment Guide

This guide provides step-by-step instructions for deploying the Fraud Detection Stack using Helm Charts.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Installation Steps](#installation-steps)
4. [Configuration](#configuration)
5. [Accessing Services](#accessing-services)
6. [Monitoring Setup](#monitoring-setup)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- **Kubernetes Cluster** (v1.20+)
  - Local: Docker Desktop, Minikube, Kind
  - Cloud: AWS EKS, GKE, AKS
- **Helm** (v3.0+)
- **kubectl** (v1.20+)
- **Docker** (for building your application image)

### Installation Commands

#### macOS
```bash
# Install Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Helm
brew install helm

# Install kubectl
brew install kubectl

# Install Docker Desktop (UI)
brew install --cask docker
```

#### Ubuntu/Debian
```bash
# Install Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install Docker
sudo apt-get update
sudo apt-get install -y docker.io
sudo usermod -aG docker $USER
```

#### Windows (PowerShell)
```powershell
# Install Helm using Chocolatey
choco install helm

# Install kubectl
choco install kubernetes-cli

# Install Docker Desktop
choco install docker-desktop
```

### Verify Installation
```bash
helm version
kubectl version --client
docker version
```

---

## Quick Start

### For Development (Minikube)

```bash
# 1. Start Minikube
minikube start --cpus=4 --memory=8192

# 2. Enable ingress addon
minikube addons enable ingress

# 3. Navigate to chart directory
cd helm/fraud-detection-stack

# 4. Install chart
helm install fraud-detection . --namespace fraud-detection --create-namespace

# 5. Check status
kubectl get all -n fraud-detection

# 6. Access Grafana
kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80
# Open browser: http://localhost:3000
```

### For Production (AWS EKS)

```bash
# 1. Configure kubectl context
aws eks update-kubeconfig --name your-cluster --region us-east-1

# 2. Navigate to chart directory
cd helm/fraud-detection-stack

# 3. Update values for production
# Edit values-production.yaml with your settings

# 4. Install chart
helm install fraud-detection . \
  --namespace fraud-detection \
  --create-namespace \
  --values values.yaml \
  --values values-production.yaml

# 5. Verify deployment
kubectl get pods -n fraud-detection -w
```

---

## Installation Steps

### Step 1: Prepare Your Environment

```bash
# 1.1 Create working directory
mkdir -p ~/fraud-detection-helm
cd ~/fraud-detection-helm

# 1.2 Copy Helm chart
cp -r ./helm/fraud-detection-stack .

# 1.3 Verify chart structure
ls -la fraud-detection-stack/
# Should show: Chart.yaml, values.yaml, templates/, README.md
```

### Step 2: Add Helm Repositories

```bash
# Add all required repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add stable https://charts.helm.sh/stable

# Update repositories
helm repo update

# Verify repositories
helm repo list
```

### Step 3: Update Chart Dependencies

```bash
cd fraud-detection-stack

# Download and update dependencies
helm dependency update

# Verify dependencies are downloaded
ls -la charts/
# Should show: kafka, mysql, kube-prometheus-stack, zookeeper
```

### Step 4: Create Kubernetes Namespace

```bash
# Create namespace
kubectl create namespace fraud-detection

# Verify namespace
kubectl get namespace fraud-detection

# Set as default context (optional)
kubectl config set-context --current --namespace=fraud-detection
```

### Step 5: Configure Values

Edit `values.yaml` for your environment:

```bash
# Copy values for customization
cp values.yaml values-custom.yaml

# Edit with your values
nano values-custom.yaml  # or use your favorite editor
```

**Key values to customize:**

```yaml
# Application image
fraudDetection:
  image:
    repository: "your-registry/fraud-detection"  # Your Docker image
    tag: "1.0.0"

# Database credentials
mysql:
  auth:
    password: "YourSecurePassword123!"  # Change this!

# Grafana admin password
prometheus:
  grafana:
    adminPassword: "YourGrafanaPassword123!"  # Change this!
```

### Step 6: Validate Configuration (Dry Run)

```bash
# Perform dry-run to validate configuration
helm install fraud-detection . \
  --namespace fraud-detection \
  --values values.yaml \
  --dry-run --debug

# This will show all resources that will be created without actually creating them
```

### Step 7: Install the Helm Chart

```bash
# Install the chart
helm install fraud-detection . \
  --namespace fraud-detection \
  --values values.yaml

# Monitor installation
kubectl get pods -n fraud-detection -w

# Check installation status
helm status fraud-detection -n fraud-detection

# View release details
helm get values fraud-detection -n fraud-detection
```

### Step 8: Wait for All Pods to be Ready

```bash
# Wait for all pods to reach Running state
kubectl wait --for=condition=Ready pod --all -n fraud-detection --timeout=600s

# Check detailed pod status
kubectl get pods -n fraud-detection -o wide

# Example output:
# NAME                                 READY   STATUS    RESTARTS   AGE
# fraud-detection-5d7b8c9f6-abc12      1/1     Running   0          2m
# mysql-0                              1/1     Running   0          3m
# kafka-0                              1/1     Running   0          3m
# zookeeper-0                          1/1     Running   0          3m
# prometheus-0                         2/2     Running   0          2m
# grafana-xxxxx                        1/1     Running   0          2m
```

### Step 9: Verify Installation

```bash
# Check all resources
kubectl get all -n fraud-detection

# Check persistent volumes
kubectl get pvc -n fraud-detection

# Check services
kubectl get svc -n fraud-detection

# Check secrets
kubectl get secret -n fraud-detection

# View recent events
kubectl get events -n fraud-detection --sort-by='.lastTimestamp'
```

---

## Configuration

### Common Configuration Scenarios

#### 1. Development Environment (Minikube)

```yaml
# values-dev.yaml
fraudDetection:
  replicaCount: 1
  
mysql:
  primary:
    persistence:
      size: 10Gi

prometheus:
  grafana:
    service:
      type: NodePort
      nodePort: 30300

kafka:
  replicaCount: 1
```

#### 2. Staging Environment

```yaml
# values-staging.yaml
fraudDetection:
  replicaCount: 2
  resources:
    requests:
      cpu: 250m
      memory: 512Mi

mysql:
  primary:
    persistence:
      size: 30Gi
```

#### 3. Production Environment

```yaml
# values-production.yaml
fraudDetection:
  replicaCount: 3
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10

mysql:
  replicaCount: 3
  primary:
    persistence:
      size: 100Gi

kafka:
  replicaCount: 3

prometheus:
  prometheus:
    prometheusSpec:
      retention: 30d
      storageSpec:
        volumeClaimTemplate:
          spec:
            resources:
              requests:
                storage: 200Gi
```

### Custom Database Credentials

```bash
# Update secrets in values.yaml
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set mysql.auth.password=NewPassword123! \
  --set prometheus.grafana.adminPassword=NewGrafanaPass123!
```

### Update Application Image

```bash
# Update application image
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set fraudDetection.image.repository=your-registry/fraud-detection \
  --set fraudDetection.image.tag=2.0.0
```

---

## Accessing Services

### 1. Fraud Detection Application

```bash
# Port forward the service
kubectl port-forward -n fraud-detection svc/fraud-detection 8081:8081

# Access in browser
open http://localhost:8081

# View Swagger API documentation
open http://localhost:8081/swagger-ui.html

# View application metrics
open http://localhost:8081/actuator/prometheus

# Or use curl
curl http://localhost:8081/actuator/health
```

### 2. Grafana Dashboard

```bash
# Port forward Grafana service
kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80

# Login to Grafana
# URL: http://localhost:3000
# Username: admin
# Password: (from values.yaml)

# Get admin password
kubectl get secret -n fraud-detection kube-prometheus-stack-grafana \
  -o jsonpath="{.data.admin-password}" | base64 --decode
```

### 3. Prometheus UI

```bash
# Port forward Prometheus
kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090

# Access Prometheus
# URL: http://localhost:9090

# Query metrics
# Try: up{job="fraud-detection"}
#      rate(http_requests_total[5m])
#      jvm_memory_used_bytes
```

### 4. MySQL Database

```bash
# Port forward MySQL
kubectl port-forward -n fraud-detection svc/mysql 3306:3306

# Connect with MySQL client
mysql -h 127.0.0.1 -u root -proot fraud_detection_db

# Or execute commands directly in pod
kubectl exec -it -n fraud-detection mysql-0 -- \
  mysql -uroot -proot -e "SELECT * FROM your_table LIMIT 10;"
```

### 5. Kafka Topics

```bash
# Port forward Kafka
kubectl port-forward -n fraud-detection pod/kafka-0 9092:9092

# List topics (inside pod)
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list

# Consume messages
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic transaction-evaluated --from-beginning

# Produce test message
kubectl exec -it -n fraud-detection kafka-0 -- \
  kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic customer-action-received
```

---

## Monitoring Setup

### Step 1: Access Grafana

```bash
kubectl port-forward -n fraud-detection svc/kube-prometheus-stack-grafana 3000:80
# Browser: http://localhost:3000
```

### Step 2: Verify Data Source

1. Login to Grafana
2. Navigate to Configuration > Data Sources
3. Verify "Prometheus" data source is configured
4. URL should be: `http://prometheus-operated:9090`

### Step 3: Import Pre-configured Dashboards

In Grafana:
1. Go to Dashboards > Import
2. Enter dashboard ID: 7249 (Kubernetes Cluster)
3. Select Prometheus data source
4. Click Import

### Step 4: Create Custom Dashboard

```bash
# Access Grafana and create new dashboard
# Panel: HTTP Requests
# Metric: rate(http_requests_total[5m])

# Panel: JVM Memory
# Metric: jvm_memory_used_bytes{instance=~"fraud-detection.*"}

# Panel: Database Connections
# Metric: mysql_global_status_threads_connected
```

### Step 5: Setup Alerts

In Grafana:
1. Go to Alerting > Alert Rules
2. Create new alert rule
3. Set condition: `up{job="fraud-detection"} == 0`
4. Condition: If last value is 0 for 5m
5. Configure notification channel

---

## Troubleshooting

### Issue 1: Pods not Starting

```bash
# Check pod status
kubectl describe pod fraud-detection-xxxxx -n fraud-detection

# Check logs
kubectl logs -n fraud-detection fraud-detection-xxxxx

# Check events
kubectl get events -n fraud-detection --sort-by='.lastTimestamp'

# Common causes:
# - Insufficient resources
# - Image pull errors
# - Init container failures
```

### Issue 2: Database Connection Failed

```bash
# Verify MySQL pod is running
kubectl get pods -n fraud-detection -l app=mysql

# Check MySQL logs
kubectl logs -n fraud-detection mysql-0

# Test MySQL connectivity
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- \
  nc -zv mysql.fraud-detection.svc.cluster.local 3306

# Verify credentials in secrets
kubectl get secret -n fraud-detection fraud-detection-secrets -o yaml
```

### Issue 3: Kafka Connection Issues

```bash
# Verify Kafka pod is running
kubectl get pods -n fraud-detection -l app=kafka

# Test Kafka connectivity
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- \
  nc -zv kafka.fraud-detection.svc.cluster.local 9092

# Check Kafka logs
kubectl logs -n fraud-detection kafka-0

# Verify bootstrap servers
kubectl get secret -n fraud-detection fraud-detection-secrets \
  -o jsonpath='{.data.KAFKA_BOOTSTRAP_SERVERS}' | base64 -d
```

### Issue 4: Prometheus Not Scraping Metrics

```bash
# Check ServiceMonitor is created
kubectl get servicemonitor -n fraud-detection

# Verify Prometheus targets
kubectl port-forward -n fraud-detection svc/prometheus-operated 9090:9090
# Visit: http://localhost:9090/targets

# Check application endpoint
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- \
  curl http://localhost:8081/actuator/prometheus

# Verify ServiceMonitor label selector
kubectl get servicemonitor -n fraud-detection fraud-detection -o yaml
```

### Issue 5: Out of Memory / CPU Issues

```bash
# Check resource usage
kubectl top nodes -n fraud-detection
kubectl top pods -n fraud-detection

# Increase resource limits
helm upgrade fraud-detection . \
  --namespace fraud-detection \
  --set fraudDetection.resources.requests.memory=1Gi \
  --set fraudDetection.resources.limits.memory=2Gi

# Check node capacity
kubectl describe nodes
```

### Issue 6: Persistent Volume Issues

```bash
# Check PVC status
kubectl get pvc -n fraud-detection

# Describe PVC to see events
kubectl describe pvc mysql-data -n fraud-detection

# Check PV status
kubectl get pv

# Common solutions:
# - Ensure storage class exists: kubectl get storageclass
# - Check node has available disk space: df -h
# - Ensure PVC is not in Pending: kubectl get pvc -A
```

---

## Useful Commands

```bash
# View helm releases
helm list -n fraud-detection

# View release history
helm history fraud-detection -n fraud-detection

# Rollback to previous version
helm rollback fraud-detection -n fraud-detection

# View manifest of current deployment
helm get manifest fraud-detection -n fraud-detection

# Test chart syntax
helm lint fraud-detection-stack

# Dry-run with debug output
helm install fraud-detection . --dry-run --debug

# Get all resources created by helm
kubectl get all -l app.kubernetes.io/instance=fraud-detection -n fraud-detection

# Stream pod logs
kubectl logs -f -n fraud-detection deployment/fraud-detection

# Execute command in pod
kubectl exec -it -n fraud-detection fraud-detection-xxxxx -- /bin/bash

# Copy files from pod
kubectl cp fraud-detection/fraud-detection-xxxxx:/app/logs ./logs

# Scale deployment
kubectl scale deployment fraud-detection -n fraud-detection --replicas=5
```

---

## Next Steps

1. **Setup CI/CD Pipeline**: Automate deployments with GitOps
2. **Configure Backup/Restore**: Set up automated backups for databases
3. **Implement Network Policies**: Secure communication between pods
4. **Setup Monitoring Alerts**: Configure PagerDuty/Slack integrations
5. **Performance Tuning**: Optimize resource usage based on metrics

---

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Bitnami Charts](https://github.com/bitnami/charts)

---

Last Updated: 2026-02-19

