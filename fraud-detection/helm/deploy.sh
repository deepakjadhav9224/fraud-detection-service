#!/bin/bash
# Helm deployment helper script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHART_PATH="$SCRIPT_DIR/fraud-detection-stack"
NAMESPACE="fraud-detection"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    if ! command -v helm &> /dev/null; then
        print_error "Helm is not installed. Please install Helm v3.0+"
        exit 1
    fi

    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install kubectl"
        exit 1
    fi

    print_info "Helm version: $(helm version --short)"
    print_info "kubectl version: $(kubectl version --short --client)"
}

# Add Helm repositories
add_helm_repos() {
    print_info "Adding Helm repositories..."

    helm repo add bitnami https://charts.bitnami.com/bitnami || true
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
    helm repo update

    print_info "Helm repositories updated"
}

# Update dependencies
update_dependencies() {
    print_info "Updating Helm dependencies..."
    cd "$CHART_PATH"
    helm dependency update
    cd - > /dev/null
    print_info "Dependencies updated"
}

# Create namespace
create_namespace() {
    print_info "Creating namespace: $NAMESPACE"
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    print_info "Namespace ready"
}

# Install chart
install_chart() {
    local release_name="fraud-detection"
    local values_file="values.yaml"
    local custom_values="${1:-}"

    print_info "Installing Helm chart..."
    print_info "Release: $release_name"
    print_info "Chart: $CHART_PATH"
    print_info "Namespace: $NAMESPACE"

    local helm_cmd="helm install $release_name $CHART_PATH --namespace $NAMESPACE"
    helm_cmd="$helm_cmd --values $CHART_PATH/$values_file"

    if [ -n "$custom_values" ] && [ -f "$custom_values" ]; then
        print_info "Using custom values: $custom_values"
        helm_cmd="$helm_cmd --values $custom_values"
    fi

    eval "$helm_cmd"

    print_info "Chart installed successfully"
}

# Upgrade chart
upgrade_chart() {
    local release_name="fraud-detection"
    local custom_values="${1:-}"

    print_info "Upgrading Helm chart..."

    local helm_cmd="helm upgrade $release_name $CHART_PATH --namespace $NAMESPACE"
    helm_cmd="$helm_cmd --values $CHART_PATH/values.yaml"

    if [ -n "$custom_values" ] && [ -f "$custom_values" ]; then
        print_info "Using custom values: $custom_values"
        helm_cmd="$helm_cmd --values $custom_values"
    fi

    eval "$helm_cmd"

    print_info "Chart upgraded successfully"
}

# Verify installation
verify_installation() {
    print_info "Verifying installation..."

    print_info "Waiting for deployments to be ready..."
    kubectl rollout status deployment/fraud-detection -n "$NAMESPACE" --timeout=5m || true
    kubectl rollout status statefulset/mysql -n "$NAMESPACE" --timeout=5m || true

    print_info "Pod status:"
    kubectl get pods -n "$NAMESPACE"

    print_info "Service status:"
    kubectl get svc -n "$NAMESPACE"
}

# Show port-forward commands
show_access_info() {
    print_info "To access services, use the following commands:"
    echo ""
    echo "Fraud Detection Application:"
    echo "  kubectl port-forward -n $NAMESPACE svc/fraud-detection 8081:8081"
    echo "  URL: http://localhost:8081"
    echo ""
    echo "Grafana Dashboard:"
    echo "  kubectl port-forward -n $NAMESPACE svc/kube-prometheus-stack-grafana 3000:80"
    echo "  URL: http://localhost:3000"
    echo "  Default credentials: admin / $(grep 'adminPassword:' $CHART_PATH/values.yaml | awk '{print $2}')"
    echo ""
    echo "Prometheus UI:"
    echo "  kubectl port-forward -n $NAMESPACE svc/prometheus-operated 9090:9090"
    echo "  URL: http://localhost:9090"
    echo ""
}

# Uninstall chart
uninstall_chart() {
    local release_name="fraud-detection"

    print_warning "Uninstalling Helm chart..."
    read -p "Are you sure? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        helm uninstall "$release_name" -n "$NAMESPACE"
        print_info "Chart uninstalled"
    else
        print_info "Uninstall cancelled"
    fi
}

# Dry run
dry_run() {
    local custom_values="${1:-}"

    print_info "Performing dry-run..."

    local helm_cmd="helm install fraud-detection $CHART_PATH --namespace $NAMESPACE --dry-run --debug"
    helm_cmd="$helm_cmd --values $CHART_PATH/values.yaml"

    if [ -n "$custom_values" ] && [ -f "$custom_values" ]; then
        helm_cmd="$helm_cmd --values $custom_values"
    fi

    eval "$helm_cmd"
}

# Main menu
show_menu() {
    echo ""
    echo "======================================"
    echo "Fraud Detection Stack - Helm Deployment"
    echo "======================================"
    echo "1. Install chart (with prerequisites)"
    echo "2. Upgrade chart"
    echo "3. Uninstall chart"
    echo "4. Verify installation"
    echo "5. Show access information"
    echo "6. Dry-run installation"
    echo "7. Check prerequisites"
    echo "8. Add/Update Helm repositories"
    echo "9. Exit"
    echo "======================================"
    echo ""
}

# Main execution
main() {
    if [ $# -eq 0 ]; then
        show_menu
        read -p "Select an option (1-9): " choice
    else
        choice=$1
    fi

    case $choice in
        1)
            check_prerequisites
            add_helm_repos
            update_dependencies
            create_namespace
            install_chart "${2:-}"
            verify_installation
            show_access_info
            ;;
        2)
            upgrade_chart "${2:-}"
            verify_installation
            ;;
        3)
            uninstall_chart
            ;;
        4)
            verify_installation
            ;;
        5)
            show_access_info
            ;;
        6)
            check_prerequisites
            add_helm_repos
            update_dependencies
            dry_run "${2:-}"
            ;;
        7)
            check_prerequisites
            ;;
        8)
            add_helm_repos
            ;;
        9)
            print_info "Exiting..."
            exit 0
            ;;
        *)
            print_error "Invalid option"
            show_menu
            ;;
    esac
}

# Run main
main "$@"

