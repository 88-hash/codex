# One Key Start Script
# Function: Download Maven, Build Backend, Install Frontend Deps, Start Services

$ErrorActionPreference = "Stop"
$scriptPath = $PSScriptRoot

# --- Config ---
$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$toolsDir = Join-Path $scriptPath ".tools"
$mavenDir = Join-Path $toolsDir "apache-maven-$mavenVersion"
$mavenBin = Join-Path $mavenDir "bin\mvn.cmd"

# --- 1. Check and Download Maven ---
Write-Host "=== 1. Checking Maven Environment ===" -ForegroundColor Cyan
if (-not (Test-Path $mavenBin)) {
    Write-Host "Maven not found, downloading..." -ForegroundColor Yellow
    if (-not (Test-Path $toolsDir)) { New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null }
    
    $zipFile = Join-Path $toolsDir "maven.zip"
    try {
        Invoke-WebRequest -Uri $mavenUrl -OutFile $zipFile -UserAgent "Mozilla/5.0"
        Write-Host "Download complete, extracting..." -ForegroundColor Yellow
        Expand-Archive -Path $zipFile -DestinationPath $toolsDir -Force
        Remove-Item $zipFile
    } catch {
        Write-Error "Failed to download Maven. Please check network."
        exit 1
    }
} else {
    Write-Host "Maven exists." -ForegroundColor Green
}

# --- 2. Build Backend ---
Write-Host "`n=== 2. Building Backend Project (This may take a while) ===" -ForegroundColor Cyan
$backendDir = Join-Path $scriptPath "backend"
$backendJar = Join-Path $backendDir "target\leyi-snack-1.0.0.jar"

# Set JAVA_HOME explicitly
$env:JAVA_HOME = "D:\program tool\java\jdk"

# Build with Maven
& $mavenBin -f (Join-Path $backendDir "pom.xml") clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Error "Backend build failed. Check logs."
    exit 1
}
Write-Host "Backend build success!" -ForegroundColor Green

# --- 3. Install Frontend Dependencies ---
Write-Host "`n=== 3. Preparing Frontend Environment ===" -ForegroundColor Cyan

function Install-Deps {
    param ($path, $name)
    Write-Host "Installing dependencies for $name..." -ForegroundColor Yellow
    Push-Location $path
    try {
        npm install
    } finally {
        Pop-Location
    }
}

Install-Deps (Join-Path $scriptPath "frontend-admin") "Admin Frontend"
Install-Deps (Join-Path $scriptPath "frontend-customer") "Customer Frontend"

# --- 4. Start Services ---
Write-Host "`n=== 4. Starting All Services ===" -ForegroundColor Cyan
Write-Host "Launching 3 new windows..." -ForegroundColor Yellow

# Start Backend
Start-Process -FilePath "java" -ArgumentList "-jar", "$backendJar" -WorkingDirectory $backendDir

# Start Admin Frontend
$adminDir = Join-Path $scriptPath "frontend-admin"
Start-Process -FilePath "npm" -ArgumentList "run", "dev" -WorkingDirectory $adminDir

# Start Customer Frontend
$customerDir = Join-Path $scriptPath "frontend-customer"
Start-Process -FilePath "npm" -ArgumentList "run", "dev" -WorkingDirectory $customerDir

Write-Host "`n=== Project Started! ===" -ForegroundColor Green
Write-Host "Backend: http://localhost:8080"
Write-Host "Admin:   http://localhost:3001"
Write-Host "Customer: http://localhost:3000"
Write-Host "`nDo not close the popped up windows."
# Pause
