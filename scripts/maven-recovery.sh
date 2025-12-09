#!/bin/bash
###############################################################################
# Maven Dependency Recovery Script
# Purpose: Intelligent recovery logic to fix Maven dependency resolution failures
# Author: Claude Code - Recovery Agent
# Date: 2025-12-09
###############################################################################

set -euo pipefail

# Color codes for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Configuration
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
readonly LOG_FILE="$PROJECT_DIR/logs/maven-recovery-$(date +%Y%m%d_%H%M%S).log"
readonly M2_REPO="${HOME}/.m2/repository"
readonly MAX_RETRIES=3
readonly RETRY_DELAY=5

# Ensure logs directory exists
mkdir -p "$PROJECT_DIR/logs"

###############################################################################
# Logging Functions
###############################################################################

log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${timestamp} [${level}] ${message}" | tee -a "$LOG_FILE"
}

log_info() {
    echo -e "${BLUE}ℹ ${NC}$*" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}✓${NC} $*" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $*" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}✗${NC} $*" | tee -a "$LOG_FILE"
}

###############################################################################
# Validation Functions
###############################################################################

validate_jar() {
    local jar_path=$1

    if [[ ! -f "$jar_path" ]]; then
        return 1
    fi

    # Check if file size is greater than 0
    if [[ ! -s "$jar_path" ]]; then
        log_error "JAR file exists but is empty: $jar_path"
        return 1
    fi

    # Validate JAR structure (ZIP archive)
    if ! jar tf "$jar_path" &>/dev/null; then
        log_error "JAR file is corrupted: $jar_path"
        return 1
    fi

    log_success "JAR validation passed: $jar_path"
    return 0
}

check_maven_installation() {
    if ! command -v mvn &>/dev/null; then
        log_error "Maven is not installed or not in PATH"
        return 1
    fi

    local maven_version=$(mvn -version | head -n 1)
    log_info "Maven installation found: $maven_version"
    return 0
}

check_internet_connectivity() {
    log_info "Checking internet connectivity..."

    # Check Maven Central
    if ! curl -s --head --max-time 5 https://repo.maven.apache.org/maven2/ &>/dev/null; then
        log_warning "Cannot reach Maven Central"
        return 1
    fi

    # Check Camunda repository
    if ! curl -s --head --max-time 5 https://artifacts.camunda.com/artifactory/camunda-bpm/ &>/dev/null; then
        log_warning "Cannot reach Camunda repository"
        return 1
    fi

    log_success "Internet connectivity verified"
    return 0
}

###############################################################################
# Cleanup Functions
###############################################################################

clean_corrupted_metadata() {
    log_info "Cleaning corrupted Maven metadata..."

    local count=0

    # Clean .lastUpdated files in Camunda directory
    if [[ -d "$M2_REPO/org/camunda" ]]; then
        count=$(find "$M2_REPO/org/camunda" -name "*.lastUpdated" -type f 2>/dev/null | wc -l | tr -d ' ')
        if [[ $count -gt 0 ]]; then
            find "$M2_REPO/org/camunda" -name "*.lastUpdated" -type f -delete 2>/dev/null || true
            log_success "Removed $count corrupted metadata files from Camunda directory"
        fi
    fi

    # Clean .lastUpdated files in entire repository (more aggressive)
    count=$(find "$M2_REPO" -name "*.lastUpdated" -type f 2>/dev/null | wc -l | tr -d ' ')
    if [[ $count -gt 0 ]]; then
        find "$M2_REPO" -name "*.lastUpdated" -type f -delete 2>/dev/null || true
        log_success "Removed $count corrupted metadata files globally"
    fi

    # Clean .repositories files
    count=$(find "$M2_REPO" -name "*.repositories" -type f 2>/dev/null | wc -l | tr -d ' ')
    if [[ $count -gt 0 ]]; then
        find "$M2_REPO" -name "*.repositories" -type f -delete 2>/dev/null || true
        log_success "Removed $count repository tracking files"
    fi

    log_success "Metadata cleanup completed"
}

purge_camunda_dependencies() {
    log_info "Purging Camunda dependencies from local repository..."

    # Specific dependencies that are failing
    local dependencies=(
        "org.camunda.bpm:camunda-bpmn-model"
        "org.camunda.bpm:camunda-engine"
        "org.camunda.commons:camunda-commons-logging"
        "org.camunda.commons:camunda-commons-typed-values"
        "org.camunda.commons:camunda-commons-utils"
        "org.camunda.bpm.model:camunda-cmmn-model"
        "org.camunda.bpm.model:camunda-dmn-model"
        "org.camunda.bpm.model:camunda-xml-model"
    )

    cd "$PROJECT_DIR"

    for dep in "${dependencies[@]}"; do
        log_info "Purging dependency: $dep"
        if mvn dependency:purge-local-repository \
            -DmanualInclude="$dep" \
            -DreResolve=false \
            -q 2>&1 | tee -a "$LOG_FILE"; then
            log_success "Purged: $dep"
        else
            log_warning "Failed to purge (may not exist): $dep"
        fi
    done
}

clean_maven_cache() {
    log_info "Cleaning Maven build cache..."

    cd "$PROJECT_DIR"

    # Clean target directory
    if [[ -d "target" ]]; then
        rm -rf target
        log_success "Removed target directory"
    fi

    # Clean Maven wrapper cache if present
    if [[ -d ".mvn" ]]; then
        find .mvn -name "*.lastUpdated" -delete 2>/dev/null || true
    fi
}

###############################################################################
# Recovery Functions
###############################################################################

force_dependency_update() {
    local retry_count=0

    log_info "Forcing dependency update from remote repositories..."

    cd "$PROJECT_DIR"

    while [[ $retry_count -lt $MAX_RETRIES ]]; do
        log_info "Attempt $((retry_count + 1)) of $MAX_RETRIES"

        if mvn clean compile -U -e 2>&1 | tee -a "$LOG_FILE"; then
            log_success "Maven build completed successfully"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [[ $retry_count -lt $MAX_RETRIES ]]; then
                local wait_time=$((RETRY_DELAY * retry_count))
                log_warning "Build failed, retrying in ${wait_time}s..."
                sleep $wait_time
            fi
        fi
    done

    log_error "Maven build failed after $MAX_RETRIES attempts"
    return 1
}

resolve_dependencies() {
    log_info "Resolving project dependencies..."

    cd "$PROJECT_DIR"

    if mvn dependency:resolve -e 2>&1 | tee -a "$LOG_FILE"; then
        log_success "All dependencies resolved successfully"
        return 0
    else
        log_error "Dependency resolution failed"
        return 1
    fi
}

download_specific_dependency() {
    local group_id=$1
    local artifact_id=$2
    local version=$3

    log_info "Force downloading: $group_id:$artifact_id:$version"

    cd "$PROJECT_DIR"

    mvn dependency:get \
        -Dartifact="$group_id:$artifact_id:$version" \
        -DremoteRepositories="https://artifacts.camunda.com/artifactory/camunda-bpm/" \
        2>&1 | tee -a "$LOG_FILE"
}

###############################################################################
# IDE Integration Functions
###############################################################################

refresh_ide_project() {
    log_info "Refreshing IDE project configuration..."

    cd "$PROJECT_DIR"

    # IntelliJ IDEA
    if [[ -d ".idea" ]]; then
        log_info "IntelliJ IDEA project detected"

        # Remove cached files
        rm -rf .idea/libraries
        rm -rf .idea/modules
        rm -f .idea/*.iml

        # Reimport Maven project
        mvn idea:idea 2>&1 | tee -a "$LOG_FILE" || true
        log_success "IntelliJ IDEA project refreshed"
    fi

    # Eclipse
    if [[ -f ".project" ]]; then
        log_info "Eclipse project detected"

        # Clean Eclipse configuration
        rm -f .classpath
        rm -f .project
        rm -rf .settings

        # Regenerate Eclipse files
        mvn eclipse:eclipse 2>&1 | tee -a "$LOG_FILE" || true
        log_success "Eclipse project refreshed"
    fi
}

###############################################################################
# Main Recovery Workflow
###############################################################################

run_recovery() {
    log_info "==================================================================="
    log_info "Maven Dependency Recovery Process Started"
    log_info "==================================================================="
    log_info "Project: $PROJECT_DIR"
    log_info "Log file: $LOG_FILE"
    log_info "==================================================================="

    # Pre-flight checks
    if ! check_maven_installation; then
        log_error "Maven installation check failed"
        exit 1
    fi

    check_internet_connectivity || log_warning "Internet connectivity issues detected"

    # Step 1: Clean corrupted metadata
    log_info ""
    log_info "STEP 1: Cleaning corrupted metadata"
    log_info "-------------------------------------------------------------------"
    clean_corrupted_metadata

    # Step 2: Purge specific dependencies
    log_info ""
    log_info "STEP 2: Purging problematic dependencies"
    log_info "-------------------------------------------------------------------"
    purge_camunda_dependencies

    # Step 3: Clean Maven cache
    log_info ""
    log_info "STEP 3: Cleaning Maven build cache"
    log_info "-------------------------------------------------------------------"
    clean_maven_cache

    # Step 4: Force dependency update
    log_info ""
    log_info "STEP 4: Forcing dependency update from remote"
    log_info "-------------------------------------------------------------------"
    if ! force_dependency_update; then
        log_error "Dependency update failed"

        # Fallback: Try to download specific dependency
        log_info "Attempting fallback: direct dependency download"
        download_specific_dependency "org.camunda.bpm" "camunda-bpmn-model" "7.20.0"
    fi

    # Step 5: Verify dependency resolution
    log_info ""
    log_info "STEP 5: Verifying dependency resolution"
    log_info "-------------------------------------------------------------------"
    resolve_dependencies

    # Step 6: Validate critical JARs
    log_info ""
    log_info "STEP 6: Validating critical JAR files"
    log_info "-------------------------------------------------------------------"
    local critical_jars=(
        "$M2_REPO/org/camunda/bpm/camunda-bpmn-model/7.20.0/camunda-bpmn-model-7.20.0.jar"
        "$M2_REPO/org/camunda/bpm/camunda-engine/7.20.0/camunda-engine-7.20.0.jar"
    )

    local validation_failed=false
    for jar in "${critical_jars[@]}"; do
        if ! validate_jar "$jar"; then
            log_error "Validation failed for: $jar"
            validation_failed=true
        fi
    done

    if [[ "$validation_failed" == "true" ]]; then
        log_error "Some JAR validations failed"
        return 1
    fi

    # Step 7: Refresh IDE project
    log_info ""
    log_info "STEP 7: Refreshing IDE project configuration"
    log_info "-------------------------------------------------------------------"
    refresh_ide_project

    # Final summary
    log_info ""
    log_info "==================================================================="
    log_success "Maven Dependency Recovery Process Completed"
    log_info "==================================================================="
    log_info "Next steps:"
    log_info "  1. Reload/reimport Maven project in your IDE"
    log_info "  2. Run 'mvn clean test' to verify the build"
    log_info "  3. Check log file for details: $LOG_FILE"
    log_info "==================================================================="

    return 0
}

###############################################################################
# Entry Point
###############################################################################

main() {
    # Trap errors
    trap 'log_error "Recovery script failed at line $LINENO"' ERR

    run_recovery
    exit_code=$?

    if [[ $exit_code -eq 0 ]]; then
        exit 0
    else
        log_error "Recovery process completed with errors"
        exit 1
    fi
}

# Execute main function
main "$@"
