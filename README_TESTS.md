# Test Compile and Run Guide

This project includes a plain Java test suite that does not require anything.

## Test Files

- Test suite class: `src/test/java/com/team3n1/smarthome/tests/SmartHomeTestSuite.java`
- Test runner script: `run-tests.ps1`

## Prerequisites

- Java JDK 11 or higher
- PowerShell

Check Java:

```powershell
java -version
javac -version
```

## Option 1: Recommended (PowerShell script)

From the project root:

```powershell
cd C:\Users\umuli\Java_Project3n1
.\run-tests.ps1
```

What this does:

1. Compiles all files in `src/main/java` and `src/test/java`
2. Outputs class files to `target/test-classes`
3. Runs `com.team3n1.smarthome.tests.SmartHomeTestSuite`

Expected result includes lines like:

- `Compiling main and test sources...`
- `Running test suite...`
- `[PASS] ...`
- `TEST SUMMARY`

If all tests pass, PowerShell should end with exit code `0`.

## Common PowerShell Issue

If you see:

- `run-tests.ps1 is not recognized...`

Use:

```powershell
.\run-tests.ps1
```

PowerShell requires `./` or `.\` to run scripts in the current folder.

If script execution is blocked by policy:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run-tests.ps1
```

## Clean Rebuild (Optional)

If you want to force a clean test compile:

```powershell
if (Test-Path "target/test-classes") { Remove-Item -Recurse -Force "target/test-classes" }
.\run-tests.ps1
```
