# Fraud Detection Stack - Helm Deployment Script for Windows PowerShell
# Run: powershell -ExecutionPolicy Bypass -File .\deploy.ps1

param(
    [Parameter(Position = 0)]
    [ValidateSet('install', 'upgrade', 'uninstall', 'verify', 'dry-run', 'prerequisites', 'repo', 'help')]
    [string]$Action = "help",

    [string]$ValuesFile = "values.yaml",
    [string]$Namespace = "fraud-detection",
    [string]$ReleaseName = "fraud-detection",
    [string]$ChartPath = ".\helm\fraud-detection-stack"
)

# Color output functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Cyan
}

# Check prerequisites
function Check-Prerequisites {
    Write-Info "Checking prerequisites..."

    $missingTools = @()

    # Check Helm
    try {
        $helmVersion = helm version --short 2>$null
        Write-Info "Helm version: $helmVersion"
    } catch {
        $missingTools += "Helm"
    }

    # Check kubectl
    try {
        $kubectlVersion = kubectl version --client --short 2>$null
        Write-Info "kubectl version: $kubectlVersion"
    } catch {
        $missingTools += "kubectl"
    }

    # Check Docker
    try {
        $dockerVersion = docker version --format "{{.Server.Version}}" 2>$null
        Write-Info "Docker version: $dockerVersion"
    } catch {
        Write-Warning "Docker not installed (optional for local testing)"
    }

    if ($missingTools.Count -gt 0) {
        Write-Error "Missing required tools: $($missingTools -join ', ')"
        Write-Host ""
        Write-Host "Installation instructions:"
        Write-Host "1. Helm: https://helm.sh/docs/intro/install/"
        Write-Host "2. kubectl: https://kubernetes.io/docs/tasks/tools/"
        Write-Host "3. Docker Desktop: https://www.docker.com/products/docker-desktop"
        exit 1
    }

    Write-Success "All prerequisites met"
}

# Add Helm repositories
function Add-HelmRepos {
    Write-Info "Adding Helm repositories..."

    helm repo add bitnami https://charts.bitnami.com/bitnami --force-update 2>$null
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts --force-update 2>$null
    helm repo add stable https://charts.helm.sh/stable --force-update 2>$null

    Write-Info "Updating repositories..."
    helm repo update

    Write-Success "Helm repositories added and updated"
}

# Update dependencies
function Update-Dependencies {
    Write-Info "Updating Helm chart dependencies..."

    if (-not (Test-Path $ChartPath)) {
        Write-Error "Chart path not found: $ChartPath"
        exit 1
    }

    Push-Location $ChartPath
    try {
        helm dependency update
        Write-Success "Dependencies updated"
    } finally {
        Pop-Location
    }
}

# Create namespace
function Create-Namespace {
    Write-Info "Creating namespace: $Namespace"

    $namespaceExists = kubectl get namespace $Namespace 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Warning "Namespace $Namespace already exists"
    } else {
        kubectl create namespace $Namespace
        Write-Success "Namespace created"
    }
}

# Install chart
function Install-Chart {
    param([string]$CustomValues = "")

    Write-Info "Installing Helm chart..."
    Write-Info "Release: $ReleaseName"
    Write-Info "Chart: $ChartPath"
    Write-Info "Namespace: $Namespace"

    if (-not (Test-Path "$ChartPath\$ValuesFile")) {
        Write-Error "Values file not found: $ChartPath\$ValuesFile"
        exit 1
    }

    $helmCmd = "helm install $ReleaseName $ChartPath --namespace $Namespace --values $ChartPath\$ValuesFile"

    if ($CustomValues -and (Test-Path $CustomValues)) {
        Write-Info "Using custom values: $CustomValues"
        $helmCmd += " --values $CustomValues"
    }

    Invoke-Expression $helmCmd

    if ($LASTEXITCODE -eq 0) {
        Write-Success "Chart installed successfully"
    } else {
        Write-Error "Chart installation failed"
        exit 1
    }
}

# Upgrade chart
function Upgrade-Chart {
    param([string]$CustomValues = "")

    Write-Info "Upgrading Helm chart..."

    $helmCmd = "helm upgrade $ReleaseName $ChartPath --namespace $Namespace --values $ChartPath\$ValuesFile"

    if ($CustomValues -and (Test-Path $CustomValues)) {
        Write-Info "Using custom values: $CustomValues"
        $helmCmd += " --values $CustomValues"
    }

    Invoke-Expression $helmCmd

    if ($LASTEXITCODE -eq 0) {
        Write-Success "Chart upgraded successfully"
    } else {
        Write-Error "Chart upgrade failed"
        exit 1
    }
}

# Uninstall chart
function Uninstall-Chart {
    $confirm = Read-Host "Are you sure you want to uninstall $ReleaseName? (yes/no)"
    if ($confirm -ne "yes") {
        Write-Warning "Uninstall cancelled"
        return
    }

    Write-Info "Uninstalling Helm chart..."
    helm uninstall $ReleaseName --namespace $Namespace

    if ($LASTEXITCODE -eq 0) {
        Write-Success "Chart uninstalled successfully"
    } else {
        Write-Error "Chart uninstall failed"
        exit 1
    }
}

# Verify installation
function Verify-Installation {
    Write-Info "Verifying installation..."

    Write-Host ""
    Write-Info "Checking pod status..."
    kubectl get pods -n $Namespace

    Write-Host ""
    Write-Info "Checking service status..."
    kubectl get svc -n $Namespace

    Write-Host ""
    Write-Info "Checking persistent volumes..."
    kubectl get pvc -n $Namespace

    Write-Host ""
    Write-Info "Waiting for deployments to be ready (max 5 minutes)..."

    try {
        kubectl rollout status deployment/fraud-detection -n $Namespace --timeout=5m -ErrorAction SilentlyContinue
    } catch {
        Write-Warning "Some deployments may still be starting"
    }

    Write-Success "Verification complete"
}

# Dry run
function Dry-Run {
    Write-Info "Performing dry-run..."

    helm install $ReleaseName $ChartPath `
        --namespace $Namespace `
        --values "$ChartPath\$ValuesFile" `
        --dry-run `
        --debug
}

# Show access information
function Show-AccessInfo {
    Write-Host ""
    Write-Success "Access Information"
    Write-Host ""
    Write-Host "1. Fraud Detection Application:"
    Write-Host "   kubectl port-forward -n $Namespace svc/fraud-detection 8081:8081"
    Write-Host "   URL: http://localhost:8081"
    Write-Host "   Swagger UI: http://localhost:8081/swagger-ui.html"
    Write-Host ""
    Write-Host "2. Grafana Dashboard:"
    Write-Host "   kubectl port-forward -n $Namespace svc/kube-prometheus-stack-grafana 3000:80"
    Write-Host "   URL: http://localhost:3000"
    Write-Host "   Username: admin"

    $grafanaPassword = kubectl get secret -n $Namespace kube-prometheus-stack-grafana `
        -o jsonpath="{.data.admin-password}" 2>$null | `
        [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_))

    if ($grafanaPassword) {
        Write-Host "   Password: $grafanaPassword"
    } else {
        Write-Host "   Password: (see values.yaml - default: admin123)"
    }

    Write-Host ""
    Write-Host "3. Prometheus UI:"
    Write-Host "   kubectl port-forward -n $Namespace svc/prometheus-operated 9090:9090"
    Write-Host "   URL: http://localhost:9090"
    Write-Host ""
    Write-Host "4. MySQL Database:"
    Write-Host "   kubectl port-forward -n $Namespace svc/mysql 3306:3306"
    Write-Host "   Connection: mysql -h 127.0.0.1 -u fraud_service_user -pFraudUser@123"
    Write-Host ""
    Write-Host "5. Kafka:"
    Write-Host "   kubectl port-forward -n $Namespace pod/kafka-0 9092:9092"
    Write-Host ""
}

# Show help
function Show-Help {
    Write-Host ""
    Write-Host "Fraud Detection Stack - Helm Deployment Script"
    Write-Host ""
    Write-Host "Usage: .\deploy.ps1 [ACTION] [OPTIONS]"
    Write-Host ""
    Write-Host "Actions:"
    Write-Host "  install       Install the Helm chart"
    Write-Host "  upgrade       Upgrade the Helm chart"
    Write-Host "  uninstall     Uninstall the Helm chart"
    Write-Host "  verify        Verify installation status"
    Write-Host "  dry-run       Perform a dry-run (no changes)"
    Write-Host "  prerequisites Check prerequisites"
    Write-Host "  repo          Add/update Helm repositories"
    Write-Host "  help          Show this help message"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -ValuesFile     Custom values file (default: values.yaml)"
    Write-Host "  -Namespace      Kubernetes namespace (default: fraud-detection)"
    Write-Host "  -ReleaseName    Helm release name (default: fraud-detection)"
    Write-Host "  -ChartPath      Path to Helm chart (default: .\helm\fraud-detection-stack)"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\deploy.ps1 install"
    Write-Host "  .\deploy.ps1 install -ValuesFile values-production.yaml"
    Write-Host "  .\deploy.ps1 upgrade"
    Write-Host "  .\deploy.ps1 verify"
    Write-Host "  .\deploy.ps1 dry-run"
    Write-Host ""
}

# Main execution
function Main {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "Fraud Detection Stack - Helm Deployment" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""

    switch ($Action) {
        "install" {
            Check-Prerequisites
            Add-HelmRepos
            Update-Dependencies
            Create-Namespace
            Install-Chart -CustomValues $ValuesFile
            Verify-Installation
            Show-AccessInfo
        }
        "upgrade" {
            Check-Prerequisites
            Add-HelmRepos
            Update-Dependencies
            Upgrade-Chart -CustomValues $ValuesFile
            Verify-Installation
        }
        "uninstall" {
            Uninstall-Chart
        }
        "verify" {
            Verify-Installation
            Show-AccessInfo
        }
        "dry-run" {
            Check-Prerequisites
            Add-HelmRepos
            Update-Dependencies
            Dry-Run
        }
        "prerequisites" {
            Check-Prerequisites
        }
        "repo" {
            Add-HelmRepos
        }
        "help" {
            Show-Help
        }
        default {
            Write-Error "Unknown action: $Action"
            Show-Help
            exit 1
        }
    }

    Write-Host ""
}

# Run main
Main

