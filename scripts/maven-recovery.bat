@echo off
REM ###############################################################################
REM Maven Dependency Recovery Script - Windows Version
REM Purpose: Intelligent recovery logic to fix Maven dependency resolution failures
REM Author: Claude Code - Recovery Agent
REM Date: 2025-12-09
REM ###############################################################################

setlocal enabledelayedexpansion

REM Configuration
set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
set "M2_REPO=%USERPROFILE%\.m2\repository"
set "LOG_DIR=%PROJECT_DIR%\logs"
set "LOG_FILE=%LOG_DIR%\maven-recovery-%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.log"
set "MAX_RETRIES=3"

REM Create logs directory
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo ===================================================================
echo Maven Dependency Recovery Process Started
echo ===================================================================
echo Project: %PROJECT_DIR%
echo Log file: %LOG_FILE%
echo ===================================================================

REM Redirect output to both console and log file
call :log_info "Starting Maven recovery process..."

REM Step 1: Check Maven installation
call :check_maven
if errorlevel 1 (
    call :log_error "Maven is not installed or not in PATH"
    exit /b 1
)

REM Step 2: Clean corrupted metadata
call :log_info ""
call :log_info "STEP 1: Cleaning corrupted metadata"
call :log_info "-------------------------------------------------------------------"
call :clean_metadata

REM Step 3: Clean Maven cache
call :log_info ""
call :log_info "STEP 2: Cleaning Maven build cache"
call :log_info "-------------------------------------------------------------------"
if exist "%PROJECT_DIR%\target" (
    rmdir /s /q "%PROJECT_DIR%\target"
    call :log_success "Removed target directory"
)

REM Step 4: Purge Camunda dependencies
call :log_info ""
call :log_info "STEP 3: Purging Camunda dependencies"
call :log_info "-------------------------------------------------------------------"
cd /d "%PROJECT_DIR%"

call :purge_dependency "org.camunda.bpm:camunda-bpmn-model"
call :purge_dependency "org.camunda.bpm:camunda-engine"
call :purge_dependency "org.camunda.commons:camunda-commons-logging"

REM Step 5: Force dependency update
call :log_info ""
call :log_info "STEP 4: Forcing dependency update from remote"
call :log_info "-------------------------------------------------------------------"
call :force_update

REM Step 6: Verify dependencies
call :log_info ""
call :log_info "STEP 5: Verifying dependency resolution"
call :log_info "-------------------------------------------------------------------"
cd /d "%PROJECT_DIR%"
call mvn dependency:resolve >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    call :log_error "Dependency resolution failed"
) else (
    call :log_success "All dependencies resolved successfully"
)

REM Step 7: Validate critical JARs
call :log_info ""
call :log_info "STEP 6: Validating critical JAR files"
call :log_info "-------------------------------------------------------------------"
call :validate_jar "%M2_REPO%\org\camunda\bpm\camunda-bpmn-model\7.20.0\camunda-bpmn-model-7.20.0.jar"
call :validate_jar "%M2_REPO%\org\camunda\bpm\camunda-engine\7.20.0\camunda-engine-7.20.0.jar"

REM Final summary
call :log_info ""
call :log_info "==================================================================="
call :log_success "Maven Dependency Recovery Process Completed"
call :log_info "==================================================================="
call :log_info "Next steps:"
call :log_info "  1. Reload/reimport Maven project in your IDE"
call :log_info "  2. Run 'mvn clean test' to verify the build"
call :log_info "  3. Check log file for details: %LOG_FILE%"
call :log_info "==================================================================="

exit /b 0

REM ###############################################################################
REM Functions
REM ###############################################################################

:log_info
echo [INFO] %~1
echo [INFO] %~1 >> "%LOG_FILE%"
exit /b 0

:log_success
echo [SUCCESS] %~1
echo [SUCCESS] %~1 >> "%LOG_FILE%"
exit /b 0

:log_error
echo [ERROR] %~1
echo [ERROR] %~1 >> "%LOG_FILE%"
exit /b 0

:log_warning
echo [WARNING] %~1
echo [WARNING] %~1 >> "%LOG_FILE%"
exit /b 0

:check_maven
where mvn >nul 2>&1
if errorlevel 1 exit /b 1
call :log_info "Maven installation verified"
exit /b 0

:clean_metadata
call :log_info "Cleaning .lastUpdated files..."
if exist "%M2_REPO%\org\camunda" (
    for /r "%M2_REPO%\org\camunda" %%f in (*.lastUpdated) do (
        del /q "%%f" 2>nul
    )
    call :log_success "Removed corrupted metadata files from Camunda directory"
)

call :log_info "Cleaning .repositories files..."
for /r "%M2_REPO%" %%f in (*.repositories) do (
    del /q "%%f" 2>nul
)
call :log_success "Metadata cleanup completed"
exit /b 0

:purge_dependency
set "DEP=%~1"
call :log_info "Purging dependency: %DEP%"
cd /d "%PROJECT_DIR%"
call mvn dependency:purge-local-repository -DmanualInclude="%DEP%" -DreResolve=false -q >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    call :log_warning "Failed to purge (may not exist): %DEP%"
) else (
    call :log_success "Purged: %DEP%"
)
exit /b 0

:force_update
set "RETRY=0"
:retry_loop
set /a RETRY+=1
call :log_info "Attempt %RETRY% of %MAX_RETRIES%"
cd /d "%PROJECT_DIR%"
call mvn clean compile -U -e >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    if %RETRY% LSS %MAX_RETRIES% (
        call :log_warning "Build failed, retrying..."
        timeout /t 5 /nobreak >nul
        goto :retry_loop
    ) else (
        call :log_error "Maven build failed after %MAX_RETRIES% attempts"
        exit /b 1
    )
)
call :log_success "Maven build completed successfully"
exit /b 0

:validate_jar
set "JAR_PATH=%~1"
if not exist "%JAR_PATH%" (
    call :log_error "JAR file not found: %JAR_PATH%"
    exit /b 1
)

REM Check if file size is greater than 0
for %%A in ("%JAR_PATH%") do set "SIZE=%%~zA"
if %SIZE% EQU 0 (
    call :log_error "JAR file is empty: %JAR_PATH%"
    exit /b 1
)

call :log_success "JAR validation passed: %JAR_PATH%"
exit /b 0
