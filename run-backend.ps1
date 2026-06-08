# Script to load environment variables from .env and run Aroma Senja Backend

$EnvFile = Join-Path $PSScriptRoot ".env"
$EnvExampleFile = Join-Path $PSScriptRoot ".env.example"

# 1. Check if .env exists, if not copy from .env.example
if (-not (Test-Path $EnvFile)) {
    Write-Host "Creating .env file from .env.example..." -ForegroundColor Yellow
    Copy-Item $EnvExampleFile $EnvFile
    Write-Host "Please edit the newly created .env file with your actual database and JWT credentials before continuing." -ForegroundColor Red
    exit
}

# 2. Load environment variables from .env
Write-Host "Loading environment variables from .env..." -ForegroundColor Cyan
Get-Content $EnvFile | ForEach-Object {
    $Line = $_.Trim()
    if ($Line -and -not $Line.StartsWith("#") -and $Line -match "^([^=]+)=(.*)$") {
        $Key = $Matches[1].Trim()
        $Value = $Matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($Key, $Value, "Process")
    }
}

# 3. Run Spring Boot application
Write-Host "Starting Aroma Senja Backend..." -ForegroundColor Green
$MavenWrapperJar = Join-Path $PSScriptRoot ".mvn\wrapper\maven-wrapper.jar"
$JavaArgs = @(
    "-Dmaven.multiModuleProjectDirectory=$PSScriptRoot",
    "-cp",
    $MavenWrapperJar,
    "org.apache.maven.wrapper.MavenWrapperMain",
    "spring-boot:run"
)
& java @JavaArgs
