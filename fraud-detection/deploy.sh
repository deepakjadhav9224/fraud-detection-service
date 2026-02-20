#!/bin/bash

# Fraud Detection Service - Complete Helm Deployment Script
# This script installs all components including monitoring

set -e

PROJECT_DIR="/home/labuser/IdeaProjects/fraud-detection-service/fraud-detection"
NAMESPACE="default"
RELEASE="fraud-detection"

echo "========================================"
echo "Fraud Detection Service - Helm Deploy"
echo "========================================"

cd "$PROJECT_DIR"

# Step 1: Validate chart
echo "✓ Validating Helm chart..."
helm lint ./helm/fraud-detection-stack/ || exit 1

# Step 2: Check if release exists
echo "✓ Checking for existing releases..."
if helm list -n "$NAMESPACE" | grep -q "$RELEASE"; then
    echo "  Release exists, upgrading..."
    helm upgrade "$RELEASE" ./helm/fraud-detection-stack/ -n "$NAMESPACE" --wait
else
    echo "  Installing fresh release..."
    helm install "$RELEASE" ./helm/fraud-detection-stack/ -n "$NAMESPACE" --create-namespace --wait
fi

# Step 3: Wait for pods to be ready
echo "✓ Waiting for pods to start..."
sleep 15

# Step 4: Check pod status
echo ""
echo "========================================"
echo "POD STATUS"
echo "========================================"
kubectl get pods -n "$NAMESPACE" -o wide

echo ""
echo "========================================"
echo "SERVICES"
echo "========================================"
kubectl get svc -n "$NAMESPACE" -o wide

echo ""
echo "========================================"
echo "DEPLOYMENT STATUS"
echo "========================================"
helm status "$RELEASE" -n "$NAMESPACE"

echo ""
echo "========================================"
echo "✅ DEPLOYMENT COMPLETE"
echo "========================================"
echo ""
echo "Access URLs:"
echo "  • Fraud Detection: http://localhost:8081"
echo "  • Prometheus: http://localhost:9090 (NodePort: 30090)"
echo "  • Grafana: http://localhost:3000 (NodePort: 30000)"
echo ""
echo "Port Forwarding:"
echo "  kubectl port-forward -n $NAMESPACE svc/fraud-detection 8081:8081"
echo "  kubectl port-forward -n $NAMESPACE svc/prometheus 9090:9090"
echo "  kubectl port-forward -n $NAMESPACE svc/grafana 3000:3000"
echo ""


