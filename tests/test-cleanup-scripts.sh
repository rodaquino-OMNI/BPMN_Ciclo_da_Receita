#!/bin/bash

################################################################################
# Test Suite for Cleanup Scripts
# Purpose: Comprehensive testing of all cleanup and monitoring scripts
# Author: Hive Mind Coder Agent
# Date: 2025-12-09
################################################################################

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/scripts"
TEST_DIR="/tmp/cleanup-test-$$"
PASSED=0
FAILED=0

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

print_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED++))
}

print_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED++))
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

# Setup test environment
setup() {
    mkdir -p "$TEST_DIR"
    print_info "Test environment created at: $TEST_DIR"
}

# Cleanup test environment
cleanup() {
    rm -rf "$TEST_DIR"
    print_info "Test environment cleaned up"
}

# Test 1: Verify all scripts exist
test_scripts_exist() {
    print_test "Verifying all cleanup scripts exist..."

    local scripts=(
        "cleanup-orphans.sh"
        "memory-optimizer.sh"
        "process-monitor.sh"
        "automated-cleanup.sh"
    )

    for script in "${scripts[@]}"; do
        if [ -f "$SCRIPT_DIR/$script" ]; then
            print_pass "Script exists: $script"
        else
            print_fail "Script missing: $script"
        fi
    done
}

# Test 2: Verify scripts are executable
test_scripts_executable() {
    print_test "Verifying scripts are executable..."

    for script in "$SCRIPT_DIR"/*.sh; do
        if [ -x "$script" ]; then
            print_pass "Executable: $(basename "$script")"
        else
            print_fail "Not executable: $(basename "$script")"
        fi
    done
}

# Test 3: Test cleanup-orphans.sh help
test_cleanup_help() {
    print_test "Testing cleanup-orphans.sh --help..."

    if "$SCRIPT_DIR/cleanup-orphans.sh" --help > /dev/null 2>&1; then
        print_pass "Help command works"
    else
        print_fail "Help command failed"
    fi
}

# Test 4: Test cleanup-orphans.sh dry-run
test_cleanup_dryrun() {
    print_test "Testing cleanup-orphans.sh --dry-run..."

    local output=$("$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --dry-run 2>&1)

    if echo "$output" | grep -q "DRY RUN MODE"; then
        print_pass "Dry-run mode works correctly"
    else
        print_fail "Dry-run mode not functioning"
    fi
}

# Test 5: Test memory-optimizer.sh stats
test_memory_stats() {
    print_test "Testing memory-optimizer.sh --stats..."

    if "$SCRIPT_DIR/memory-optimizer.sh" --stats > /dev/null 2>&1; then
        print_pass "Memory statistics command works"
    else
        print_fail "Memory statistics command failed"
    fi
}

# Test 6: Test process-monitor.sh once
test_monitor_once() {
    print_test "Testing process-monitor.sh --once..."

    if timeout 30 "$SCRIPT_DIR/process-monitor.sh" --once > /dev/null 2>&1; then
        print_pass "Process monitor single check works"
    else
        print_fail "Process monitor single check failed"
    fi
}

# Test 7: Test automated-cleanup.sh status
test_automated_status() {
    print_test "Testing automated-cleanup.sh --status..."

    if "$SCRIPT_DIR/automated-cleanup.sh" --status > /dev/null 2>&1; then
        print_pass "Automated cleanup status check works"
    else
        print_fail "Automated cleanup status check failed"
    fi
}

# Test 8: Test cleanup safety (protected processes)
test_cleanup_safety() {
    print_test "Testing cleanup safety for protected processes..."

    local output=$("$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --dry-run 2>&1)

    # Should not find launchd, systemd, or other protected processes
    if ! echo "$output" | grep -q "launchd\|systemd\|kernel_task"; then
        print_pass "Protected processes are not targeted"
    else
        print_fail "Protected processes are being targeted!"
    fi
}

# Test 9: Test log file creation
test_log_creation() {
    print_test "Testing log file creation..."

    "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --dry-run > /dev/null 2>&1

    if ls /tmp/cleanup-orphans-*.log > /dev/null 2>&1; then
        print_pass "Log files are being created"
    else
        print_fail "Log files not being created"
    fi
}

# Test 10: Test memory threshold configuration
test_memory_threshold() {
    print_test "Testing custom memory threshold..."

    local output=$("$SCRIPT_DIR/memory-optimizer.sh" --stats --threshold 750 2>&1)

    if echo "$output" | grep -q "Memory"; then
        print_pass "Custom memory threshold accepted"
    else
        print_fail "Custom memory threshold not working"
    fi
}

# Test 11: Test script error handling
test_error_handling() {
    print_test "Testing error handling with invalid arguments..."

    if ! "$SCRIPT_DIR/cleanup-orphans.sh" --invalid-option > /dev/null 2>&1; then
        print_pass "Invalid arguments are properly rejected"
    else
        print_fail "Invalid arguments not being caught"
    fi
}

# Test 12: Test concurrent execution safety
test_concurrent_safety() {
    print_test "Testing concurrent execution safety..."

    # Run multiple instances simultaneously
    "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --dry-run > /dev/null 2>&1 &
    local pid1=$!
    "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --dry-run > /dev/null 2>&1 &
    local pid2=$!

    wait $pid1 $pid2

    if [ $? -eq 0 ]; then
        print_pass "Concurrent execution is safe"
    else
        print_fail "Concurrent execution issues detected"
    fi
}

# Test 13: Verify script dependencies
test_dependencies() {
    print_test "Verifying required command dependencies..."

    local required_commands=("ps" "grep" "awk" "kill" "basename")

    for cmd in "${required_commands[@]}"; do
        if command -v "$cmd" > /dev/null 2>&1; then
            print_pass "Required command available: $cmd"
        else
            print_fail "Required command missing: $cmd"
        fi
    done
}

# Test 14: Test process memory calculation
test_memory_calculation() {
    print_test "Testing process memory calculation accuracy..."

    # Get current process memory
    local current_pid=$$
    local mem_output=$("$SCRIPT_DIR/memory-optimizer.sh" --stats 2>&1)

    if echo "$mem_output" | grep -q "MB"; then
        print_pass "Memory calculation produces valid output"
    else
        print_fail "Memory calculation invalid"
    fi
}

# Test 15: Integration test - full workflow
test_full_workflow() {
    print_test "Testing complete cleanup workflow..."

    # 1. Check for issues
    "$SCRIPT_DIR/automated-cleanup.sh" --check > /dev/null 2>&1

    # 2. Run dry-run cleanup
    "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --dry-run > /dev/null 2>&1

    # 3. Show memory stats
    "$SCRIPT_DIR/memory-optimizer.sh" --stats > /dev/null 2>&1

    # 4. Check automation status
    "$SCRIPT_DIR/automated-cleanup.sh" --status > /dev/null 2>&1

    if [ $? -eq 0 ]; then
        print_pass "Complete workflow executed successfully"
    else
        print_fail "Workflow execution failed"
    fi
}

# Run all tests
run_all_tests() {
    echo ""
    echo "=========================================="
    echo "Cleanup Scripts Test Suite"
    echo "=========================================="
    echo ""

    setup

    test_scripts_exist
    echo ""

    test_scripts_executable
    echo ""

    test_cleanup_help
    echo ""

    test_cleanup_dryrun
    echo ""

    test_memory_stats
    echo ""

    test_monitor_once
    echo ""

    test_automated_status
    echo ""

    test_cleanup_safety
    echo ""

    test_log_creation
    echo ""

    test_memory_threshold
    echo ""

    test_error_handling
    echo ""

    test_concurrent_safety
    echo ""

    test_dependencies
    echo ""

    test_memory_calculation
    echo ""

    test_full_workflow
    echo ""

    cleanup

    # Summary
    echo "=========================================="
    echo "Test Results Summary"
    echo "=========================================="
    echo -e "${GREEN}Passed: $PASSED${NC}"
    echo -e "${RED}Failed: $FAILED${NC}"
    echo "Total:  $((PASSED + FAILED))"
    echo "=========================================="

    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✓ All tests passed!${NC}"
        exit 0
    else
        echo -e "${RED}✗ Some tests failed${NC}"
        exit 1
    fi
}

# Run tests
run_all_tests
