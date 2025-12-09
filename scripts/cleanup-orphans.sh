#!/bin/bash

################################################################################
# Orphaned Process Cleanup Script
# Purpose: Safely identify and terminate orphaned Node.js processes and swarm agents
# Author: Hive Mind Coder Agent
# Date: 2025-12-09
################################################################################

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
LOG_FILE="/tmp/cleanup-orphans-$(date +%Y%m%d-%H%M%S).log"
DRY_RUN=false
FORCE=false
VERBOSE=false
PROTECTED_PROCESSES=("launchd" "systemd" "init" "kernel_task" "claude" "Terminal" "iTerm" "vscode")

# Logging function
log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${timestamp} [${level}] ${message}" | tee -a "$LOG_FILE"
}

# Print colored message
print_color() {
    local color=$1
    shift
    echo -e "${color}$*${NC}"
}

# Show usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Safely identify and terminate orphaned Node.js processes and swarm agents.

OPTIONS:
    -d, --dry-run       Show what would be cleaned up without actually doing it
    -f, --force         Force cleanup without confirmation prompts
    -v, --verbose       Enable verbose output
    -h, --help          Show this help message
    --clean-mcp         Clean up MCP server processes
    --clean-swarm       Clean up swarm agent processes
    --clean-node        Clean up orphaned Node.js processes
    --clean-all         Clean up all types of orphaned processes
    --memory-threshold  Set memory threshold (MB) for cleanup (default: 500)

EXAMPLES:
    $0 --dry-run                    # Preview what would be cleaned
    $0 --clean-swarm --force        # Clean up swarm agents without prompts
    $0 --clean-all --dry-run        # Preview cleanup of all orphaned processes

EOF
    exit 0
}

# Check if process is protected
is_protected() {
    local process_name=$1
    for protected in "${PROTECTED_PROCESSES[@]}"; do
        if [[ "$process_name" == *"$protected"* ]]; then
            return 0
        fi
    done
    return 1
}

# Get process memory usage in MB
get_process_memory() {
    local pid=$1
    if [[ "$OSTYPE" == "darwin"* ]]; then
        ps -o rss= -p "$pid" 2>/dev/null | awk '{print int($1/1024)}' || echo "0"
    else
        ps -o rss= -p "$pid" 2>/dev/null | awk '{print int($1/1024)}' || echo "0"
    fi
}

# Find orphaned Node.js processes
find_orphaned_node_processes() {
    log "INFO" "Searching for orphaned Node.js processes..."

    local orphaned_pids=()

    # Find all node processes
    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local ppid=$(echo "$line" | awk '{print $2}')
        local cmd=$(echo "$line" | awk '{$1=$2=""; print $0}' | xargs)

        # Skip if protected
        if is_protected "$cmd"; then
            [[ "$VERBOSE" == "true" ]] && log "DEBUG" "Skipping protected process: $pid ($cmd)"
            continue
        fi

        # Check if parent process exists
        if ! ps -p "$ppid" > /dev/null 2>&1; then
            local mem=$(get_process_memory "$pid")
            orphaned_pids+=("$pid|$ppid|$mem|$cmd")
            log "WARN" "Found orphaned process: PID=$pid PPID=$ppid MEM=${mem}MB CMD=$cmd"
        fi
    done < <(ps -eo pid,ppid,command | grep -i node | grep -v grep)

    echo "${orphaned_pids[@]}"
}

# Find orphaned MCP server processes
find_orphaned_mcp_servers() {
    log "INFO" "Searching for orphaned MCP server processes..."

    local orphaned_pids=()

    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local ppid=$(echo "$line" | awk '{print $2}')
        local cmd=$(echo "$line" | awk '{$1=$2=""; print $0}' | xargs)

        # Check if it's an MCP server
        if [[ "$cmd" == *"mcp start"* ]] || [[ "$cmd" == *"claude-flow"* ]] || [[ "$cmd" == *"ruv-swarm"* ]] || [[ "$cmd" == *"flow-nexus"* ]]; then
            # Check if parent process exists
            if ! ps -p "$ppid" > /dev/null 2>&1; then
                local mem=$(get_process_memory "$pid")
                orphaned_pids+=("$pid|$ppid|$mem|$cmd")
                log "WARN" "Found orphaned MCP server: PID=$pid PPID=$ppid MEM=${mem}MB CMD=$cmd"
            fi
        fi
    done < <(ps -eo pid,ppid,command | grep -E "mcp start|claude-flow|ruv-swarm|flow-nexus" | grep -v grep)

    echo "${orphaned_pids[@]}"
}

# Find orphaned swarm agent processes
find_orphaned_swarm_agents() {
    log "INFO" "Searching for orphaned swarm agent processes..."

    local orphaned_pids=()

    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local ppid=$(echo "$line" | awk '{print $2}')
        local cmd=$(echo "$line" | awk '{$1=$2=""; print $0}' | xargs)

        # Check if it's a swarm agent
        if [[ "$cmd" == *"agent"* ]] || [[ "$cmd" == *"swarm"* ]] || [[ "$cmd" == *"worker"* ]]; then
            # Check if parent process exists or if it's a zombie
            if ! ps -p "$ppid" > /dev/null 2>&1 || ps -o stat= -p "$pid" | grep -q Z; then
                local mem=$(get_process_memory "$pid")
                orphaned_pids+=("$pid|$ppid|$mem|$cmd")
                log "WARN" "Found orphaned swarm agent: PID=$pid PPID=$ppid MEM=${mem}MB CMD=$cmd"
            fi
        fi
    done < <(ps -eo pid,ppid,stat,command | grep -E "agent|swarm|worker" | grep -v grep)

    echo "${orphaned_pids[@]}"
}

# Terminate process safely
terminate_process() {
    local pid=$1
    local process_info=$2

    if [[ "$DRY_RUN" == "true" ]]; then
        print_color "$YELLOW" "DRY RUN: Would terminate PID $pid"
        return 0
    fi

    if [[ "$FORCE" == "false" ]]; then
        read -p "Terminate process $pid? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log "INFO" "Skipped termination of PID $pid"
            return 0
        fi
    fi

    log "INFO" "Attempting to terminate PID $pid gracefully..."

    # Try SIGTERM first (graceful shutdown)
    if kill -15 "$pid" 2>/dev/null; then
        sleep 2
        if ! ps -p "$pid" > /dev/null 2>&1; then
            print_color "$GREEN" "✓ Successfully terminated PID $pid"
            log "INFO" "Successfully terminated PID $pid"
            return 0
        fi
    fi

    # If still running, try SIGKILL
    log "WARN" "Process $pid did not respond to SIGTERM, using SIGKILL..."
    if kill -9 "$pid" 2>/dev/null; then
        print_color "$GREEN" "✓ Force killed PID $pid"
        log "INFO" "Force killed PID $pid"
        return 0
    else
        print_color "$RED" "✗ Failed to terminate PID $pid"
        log "ERROR" "Failed to terminate PID $pid"
        return 1
    fi
}

# Main cleanup function
cleanup_orphaned_processes() {
    local process_type=$1
    local processes=()

    case "$process_type" in
        "node")
            processes=($(find_orphaned_node_processes))
            ;;
        "mcp")
            processes=($(find_orphaned_mcp_servers))
            ;;
        "swarm")
            processes=($(find_orphaned_swarm_agents))
            ;;
        "all")
            processes=($(find_orphaned_node_processes))
            processes+=($(find_orphaned_mcp_servers))
            processes+=($(find_orphaned_swarm_agents))
            ;;
        *)
            log "ERROR" "Unknown process type: $process_type"
            return 1
            ;;
    esac

    if [[ ${#processes[@]} -eq 0 ]]; then
        print_color "$GREEN" "✓ No orphaned $process_type processes found"
        log "INFO" "No orphaned $process_type processes found"
        return 0
    fi

    print_color "$BLUE" "Found ${#processes[@]} orphaned $process_type process(es)"

    local total_memory=0
    local terminated_count=0

    for process_info in "${processes[@]}"; do
        IFS='|' read -r pid ppid mem cmd <<< "$process_info"
        total_memory=$((total_memory + mem))

        if [[ "$VERBOSE" == "true" ]]; then
            echo "  PID: $pid | PPID: $ppid | Memory: ${mem}MB"
            echo "  Command: $cmd"
            echo ""
        fi

        if terminate_process "$pid" "$process_info"; then
            ((terminated_count++))
        fi
    done

    print_color "$GREEN" "✓ Terminated $terminated_count/$((${#processes[@]})) processes"
    print_color "$GREEN" "✓ Freed approximately ${total_memory}MB of memory"
    log "INFO" "Cleanup complete: Terminated $terminated_count processes, freed ${total_memory}MB"
}

# Main execution
main() {
    local clean_type=""
    local memory_threshold=500

    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -d|--dry-run)
                DRY_RUN=true
                shift
                ;;
            -f|--force)
                FORCE=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -h|--help)
                usage
                ;;
            --clean-mcp)
                clean_type="mcp"
                shift
                ;;
            --clean-swarm)
                clean_type="swarm"
                shift
                ;;
            --clean-node)
                clean_type="node"
                shift
                ;;
            --clean-all)
                clean_type="all"
                shift
                ;;
            --memory-threshold)
                memory_threshold=$2
                shift 2
                ;;
            *)
                print_color "$RED" "Unknown option: $1"
                usage
                ;;
        esac
    done

    # Start cleanup
    print_color "$BLUE" "========================================="
    print_color "$BLUE" "Orphaned Process Cleanup Script"
    print_color "$BLUE" "========================================="
    echo ""

    if [[ "$DRY_RUN" == "true" ]]; then
        print_color "$YELLOW" "DRY RUN MODE - No processes will be terminated"
        echo ""
    fi

    log "INFO" "Starting cleanup operation (type: $clean_type)"
    log "INFO" "Log file: $LOG_FILE"

    if [[ -z "$clean_type" ]]; then
        print_color "$YELLOW" "No cleanup type specified. Use --help for usage information."
        exit 0
    fi

    cleanup_orphaned_processes "$clean_type"

    echo ""
    print_color "$GREEN" "Cleanup complete! Log saved to: $LOG_FILE"
}

# Run main function
main "$@"
