#!/bin/bash
set -e

PROJECT_DIR="/home/labuser/IdeaProjects/fraud-detection-service/fraud-detection"

echo "Cleaning up old resources..."
kubectl delete all,configmap,secret,pvc -n fraud-detection --all 2>/dev/null || true

echo "Waiting 10 seconds..."
sleep 10

echo "Fresh Helm install..."
cd "$PROJECT_DIR"
helm install fraud-detection ./helm/fraud-detection-stack/ -n fraud-detection

echo "Waiting 30 seconds for deployment..."
sleep 30

echo "Services deployed:"
kubectl get svc -n fraud-detection -o wide

echo "Pods deployed:"
kubectl get pods -n fraud-detection -o wide

echo "Helm status:"
helm status fraud-detection -n fraud-detection

