#!/bin/bash
set -e

echo "Building Docker image..."
docker build -t fraud-detection:latest .

echo "Applying Kubernetes configuration..."
kubectl apply -f deployment.yaml --validate=false

echo "Deployment completed successfully!"
echo "You can check the status of your pods using: kubectl get pods"
