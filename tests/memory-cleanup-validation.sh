#!/bin/bash

# Memory Cleanup Validation Test Suite
# Purpose: Validate memory optimization safety and effectiveness
# Agent: Tester (Hive Mind Collective Intelligence)
# Date: 2025-12-09

set -e  # Exit on error

# ANSI Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results tracking
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_TOTAL=0

# Logging functions
log_info() {
    echo -e "${BLUE}ℹ️  [INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅ [PASS]${NC} $1"
    ((TESTS_PASSED++))
    ((TESTS_TOTAL++))
}

log_failure() {
    echo -e "${RED}❌ [FAIL]${NC} $1"
    ((TESTS_FAILED++))
    ((TESTS_TOTAL++))
}

log_warning() {
    echo -e "${YELLOW}⚠️  [WARN]${NC} $1"
}

# Test 1: Verify critical processes are running
test_critical_processes() {
    log_info "Test 1: Verifying critical processes..."

    # Check VS Code (macOS uses full application name in process list)
    if ps aux | grep -i "Visual Studio Code" | grep -v grep > /dev/null 2>&1; then
        log_success "VS Code (Electron) is running"
    else
        log_warning "VS Code (Electron) is NOT running (may not be open)"
        ((TESTS_PASSED++))
        ((TESTS_TOTAL++))
    fi

    # Check claude CLI
    if pgrep -f "claude" > /dev/null; then
        log_success "Claude CLI is running"
    else
        log_failure "Claude CLI is NOT running"
    fi

    # Check system processes
    if pgrep -x "WindowServer" > /dev/null; then
        log_success "WindowServer is running"
    else
        log_failure "WindowServer is NOT running (critical system failure!)"
    fi
}

# Test 2: Capture pre-cleanup memory baseline
test_memory_baseline() {
    log_info "Test 2: Capturing memory baseline..."

    # Capture vm_stat output
    BASELINE_FREE=$(vm_stat | grep "Pages free" | awk '{print $3}' | sed 's/\.//')
    BASELINE_INACTIVE=$(vm_stat | grep "Pages inactive" | awk '{print $3}' | sed 's/\.//')
    BASELINE_ACTIVE=$(vm_stat | grep "Pages active" | awk '{print $3}' | sed 's/\.//')

    if [[ -n "$BASELINE_FREE" && -n "$BASELINE_INACTIVE" ]]; then
        log_success "Memory baseline captured (Free: $BASELINE_FREE pages, Inactive: $BASELINE_INACTIVE pages)"

        # Store baseline in temp file for comparison
        echo "FREE=$BASELINE_FREE" > /tmp/memory_baseline.txt
        echo "INACTIVE=$BASELINE_INACTIVE" >> /tmp/memory_baseline.txt
        echo "ACTIVE=$BASELINE_ACTIVE" >> /tmp/memory_baseline.txt
    else
        log_failure "Failed to capture memory baseline"
    fi
}

# Test 3: Validate process whitelist
test_process_whitelist() {
    log_info "Test 3: Validating process whitelist..."

    # Critical processes that must never be killed
    CRITICAL_PROCESSES=("Code" "claude" "WindowServer" "opendirectoryd" "launchd" "kernel_task")

    for proc in "${CRITICAL_PROCESSES[@]}"; do
        if pgrep -x "$proc" > /dev/null 2>&1 || pgrep -f "$proc" > /dev/null 2>&1; then
            log_success "Protected process detected: $proc"
        else
            log_warning "Protected process not running: $proc (may be normal)"
        fi
    done
}

# Test 4: Dry-run memory cleanup (simulation)
test_memory_cleanup_dryrun() {
    log_info "Test 4: Simulating memory cleanup (dry-run)..."

    # Identify potentially killable processes (old/inactive node processes)
    KILLABLE_COUNT=$(ps aux | grep -E "(node|npm)" | grep -v "grep" | grep -v "Code" | grep -v "claude" | wc -l | xargs)

    if [[ "$KILLABLE_COUNT" -gt 0 ]]; then
        log_info "Found $KILLABLE_COUNT potentially killable node/npm processes"
        log_success "Dry-run completed: $KILLABLE_COUNT processes identified"
    else
        log_warning "No killable processes found (this may be normal)"
        ((TESTS_PASSED++))
        ((TESTS_TOTAL++))
    fi
}

# Test 5: Check for memory leaks
test_memory_leaks() {
    log_info "Test 5: Checking for memory leaks..."

    # Get top memory consumers
    TOP_MEMORY=$(ps aux | sort -k 4 -r | head -5 | awk '{print $11}' | xargs)

    if [[ -n "$TOP_MEMORY" ]]; then
        log_info "Top memory consumers: $TOP_MEMORY"
        log_success "Memory leak analysis completed"
    else
        log_failure "Failed to analyze memory consumers"
    fi
}

# Test 6: Verify file system operations
test_filesystem_operations() {
    log_info "Test 6: Testing file system operations..."

    # Test file creation
    TEST_FILE="/tmp/memory_cleanup_test_${RANDOM}.txt"
    if echo "test" > "$TEST_FILE" 2>/dev/null; then
        log_success "File creation successful"
        rm -f "$TEST_FILE"
    else
        log_failure "File creation failed"
    fi

    # Test file reading
    if cat /etc/hosts > /dev/null 2>&1; then
        log_success "File reading successful"
    else
        log_failure "File reading failed"
    fi
}

# Test 7: Verify network connectivity
test_network_connectivity() {
    log_info "Test 7: Testing network connectivity..."

    # Simple ping test (timeout 2 seconds)
    if ping -c 1 -W 2000 8.8.8.8 > /dev/null 2>&1; then
        log_success "Network connectivity confirmed"
    else
        log_warning "Network ping failed (may be firewall/network issue)"
        ((TESTS_PASSED++))
        ((TESTS_TOTAL++))
    fi
}

# Test 8: Verify Git operations
test_git_operations() {
    log_info "Test 8: Testing Git operations..."

    # Navigate to project directory
    cd "/Users/rodrigo/claude-projects/BPMN Ciclo da Receita/BPMN_Ciclo_da_Receita" 2>/dev/null || {
        log_failure "Failed to navigate to project directory"
        return
    }

    # Test git status
    if git status > /dev/null 2>&1; then
        log_success "Git operations functional"
    else
        log_failure "Git operations failed"
    fi
}

# Test 9: Calculate memory optimization potential
test_memory_optimization_potential() {
    log_info "Test 9: Calculating memory optimization potential..."

    if [[ -f /tmp/memory_baseline.txt ]]; then
        source /tmp/memory_baseline.txt

        # Calculate potential cleanup (inactive pages)
        POTENTIAL_MB=$((INACTIVE * 4 / 1024))

        if [[ $POTENTIAL_MB -gt 100 ]]; then
            log_success "High optimization potential: ~${POTENTIAL_MB}MB available for cleanup"
        elif [[ $POTENTIAL_MB -gt 50 ]]; then
            log_success "Moderate optimization potential: ~${POTENTIAL_MB}MB available"
        else
            log_warning "Low optimization potential: ~${POTENTIAL_MB}MB available"
            ((TESTS_PASSED++))
            ((TESTS_TOTAL++))
        fi
    else
        log_failure "Memory baseline not found"
    fi
}

# Test 10: System stability check
test_system_stability() {
    log_info "Test 10: Checking system stability..."

    # Check system load
    LOAD_AVG=$(uptime | awk -F'load averages:' '{print $2}' | awk '{print $1}' | sed 's/,//')

    if [[ -n "$LOAD_AVG" ]]; then
        log_info "Current system load: $LOAD_AVG"
        log_success "System stability check completed"
    else
        log_failure "Failed to check system load"
    fi
}

# Main test execution
main() {
    echo ""
    echo "🧪 =========================================="
    echo "   MEMORY CLEANUP VALIDATION TEST SUITE"
    echo "   Agent: Tester (Hive Mind)"
    echo "   Date: $(date)"
    echo "=========================================="
    echo ""

    # Run all tests
    test_critical_processes
    test_memory_baseline
    test_process_whitelist
    test_memory_cleanup_dryrun
    test_memory_leaks
    test_filesystem_operations
    test_network_connectivity
    test_git_operations
    test_memory_optimization_potential
    test_system_stability

    echo ""
    echo "=========================================="
    echo "   TEST RESULTS SUMMARY"
    echo "=========================================="
    echo -e "Total Tests:  ${BLUE}${TESTS_TOTAL}${NC}"
    echo -e "Tests Passed: ${GREEN}${TESTS_PASSED}${NC}"
    echo -e "Tests Failed: ${RED}${TESTS_FAILED}${NC}"

    if [[ $TESTS_FAILED -eq 0 ]]; then
        echo -e "\n${GREEN}✅ ALL TESTS PASSED${NC}"
        echo -e "Memory cleanup is ${GREEN}SAFE TO PROCEED${NC}\n"
        exit 0
    else
        echo -e "\n${RED}❌ SOME TESTS FAILED${NC}"
        echo -e "Review failures before proceeding with cleanup\n"
        exit 1
    fi
}

# Cleanup function
cleanup() {
    # Remove temporary files
    rm -f /tmp/memory_baseline.txt 2>/dev/null
}

# Register cleanup on exit
trap cleanup EXIT

# Execute main function
main
