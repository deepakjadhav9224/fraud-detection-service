#!/bin/bash
cd /home/labuser/IdeaProjects/fraud-detection-service/fraud-detection

echo "Creating fraud-detection namespace..."
kubectl create namespace fraud-detection 2>/dev/null || echo "Namespace already exists"

echo "Deploying Helm release..."
helm upgrade --install fraud-detection ./helm/fraud-detection-stack/ \
  -n fraud-detection \
  --create-namespace \
  --wait=false

echo "Waiting for deployment..."
sleep 30

echo "Pod Status:"
kubectl get pods -n fraud-detection

echo ""
echo "Service Status (Exposed Services):"
kubectl get svc -n fraud-detection

echo ""
echo "Checking External IPs/Endpoints:"
kubectl get svc -n fraud-detection -o wide

echo ""
echo "Checking Ingress:"
kubectl get ingress -n fraud-detection 2>/dev/null || echo "No Ingress found"

