$ErrorActionPreference = "Stop"

Write-Host "Compiling main and test sources..."
if (-not (Test-Path "target/test-classes")) {
    New-Item -ItemType Directory -Path "target/test-classes" | Out-Null
}

$mainSources = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
$testSources = Get-ChildItem -Path "src/test/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

javac -d target/test-classes $mainSources $testSources

Write-Host "Running test suite..."
java -cp target/test-classes com.team3n1.smarthome.tests.SmartHomeTestSuite
