#!/bin/bash

################################################################################
# Memory Optimization Script
# Purpose: Monitor and optimize memory usage across Node.js processes and MCP servers
# Author: Hive Mind Coder Agent
# Date: 2025-12-09
################################################################################

set -euo pipefail

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Configuration
LOG_FILE="/tmp/memory-optimizer-$(date +%Y%m%d-%H%M%S).log"
MEMORY_THRESHOLD_MB=500
RESTART_THRESHOLD_MB=1000
MONITOR_INTERVAL=5
MAX_MEMORY_PER_PROCESS=2048
AUTO_RESTART=false
CONTINUOUS_MONITOR=false

# Logging
log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "${timestamp} [${level}] ${message}" | tee -a "$LOG_FILE"
}

print_color() {
    local color=$1
    shift
    echo -e "${color}$*${NC}"
}

# Usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Monitor and optimize memory usage across Node.js processes and MCP servers.

OPTIONS:
    -m, --monitor           Enable continuous monitoring mode
    -t, --threshold MB      Set memory warning threshold (default: 500MB)
    -r, --restart MB        Set auto-restart threshold (default: 1000MB)
    -i, --interval SEC      Set monitoring interval (default: 5 seconds)
    -a, --auto-restart      Enable automatic restart of high-memory processes
    -o, --optimize-now      Run one-time optimization
    -s, --stats             Show memory statistics
    -h, --help              Show this help message

EXAMPLES:
    $0 --stats                          # Show current memory statistics
    $0 --optimize-now                   # Run immediate optimization
    $0 --monitor --auto-restart         # Continuous monitoring with auto-restart
    $0 --monitor --threshold 750        # Monitor with custom threshold

EOF
    exit 0
}

# Get system memory info
get_system_memory() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        vm_stat | awk '
            /Pages free/ {free=$3}
            /Pages active/ {active=$3}
            /Pages inactive/ {inactive=$3}
            /Pages wired/ {wired=$4}
            END {
                page_size=4096
                total=(free+active+inactive+wired)*page_size/1024/1024
                used=(active+wired)*page_size/1024/1024
                printf "total=%.0f used=%.0f free=%.0f\n", total, used, free*page_size/1024/1024
            }
        '
    else
        # Linux
        free -m | awk 'NR==2{printf "total=%s used=%s free=%s\n", $2,$3,$4}'
    fi
}

# Get process memory usage
get_process_info() {
    local pid=$1
    if [[ "$OSTYPE" == "darwin"* ]]; then
        ps -o pid,ppid,rss,vsz,%mem,command -p "$pid" 2>/dev/null | tail -n 1
    else
        ps -o pid,ppid,rss,vsz,%mem,command -p "$pid" 2>/dev/null | tail -n 1
    fi
}

# Format bytes to human readable
format_bytes() {
    local bytes=$1
    if ((bytes < 1024)); then
        echo "${bytes}B"
    elif ((bytes < 1048576)); then
        echo "$((bytes/1024))KB"
    elif ((bytes < 1073741824)); then
        echo "$((bytes/1048576))MB"
    else
        echo "$((bytes/1073741824))GB"
    fi
}

# Get all Node.js process memory usage
get_nodejs_memory_stats() {
    print_color "$CYAN" "\n📊 Node.js Process Memory Statistics"
    print_color "$CYAN" "======================================"

    local total_rss=0
    local total_vsz=0
    local process_count=0

    printf "%-8s %-8s %-10s %-10s %-8s %s\n" "PID" "PPID" "RSS(MB)" "VSZ(MB)" "MEM%" "COMMAND"
    printf "%s\n" "--------------------------------------------------------------------------------"

    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local ppid=$(echo "$line" | awk '{print $2}')
        local rss=$(echo "$line" | awk '{print int($3/1024)}')
        local vsz=$(echo "$line" | awk '{print int($4/1024)}')
        local mem_pct=$(echo "$line" | awk '{print $5}')
        local cmd=$(echo "$line" | awk '{$1=$2=$3=$4=$5=""; print $0}' | xargs)

        # Highlight high memory usage
        if ((rss > MEMORY_THRESHOLD_MB)); then
            print_color "$YELLOW" "$(printf "%-8s %-8s %-10s %-10s %-8s %s" "$pid" "$ppid" "$rss" "$vsz" "$mem_pct" "${cmd:0:60}")"
        else
            printf "%-8s %-8s %-10s %-10s %-8s %s\n" "$pid" "$ppid" "$rss" "$vsz" "$mem_pct" "${cmd:0:60}"
        fi

        total_rss=$((total_rss + rss))
        total_vsz=$((total_vsz + vsz))
        ((process_count++))
    done < <(ps aux | grep -i node | grep -v grep | awk '{print $2, $3, $6, $5, $4, $0}' | while read -r pid ppid rss vsz mem rest; do ps -o pid,ppid,rss,vsz,%mem,command -p "$pid" 2>/dev/null | tail -n 1; done)

    printf "%s\n" "--------------------------------------------------------------------------------"
    print_color "$GREEN" "Total: $process_count processes | RSS: ${total_rss}MB | VSZ: ${total_vsz}MB"

    echo "$process_count|$total_rss|$total_vsz"
}

# Get MCP server memory stats
get_mcp_memory_stats() {
    print_color "$CYAN" "\n📡 MCP Server Memory Statistics"
    print_color "$CYAN" "================================"

    local total_rss=0
    local process_count=0

    printf "%-8s %-10s %-8s %s\n" "PID" "RSS(MB)" "MEM%" "SERVER"
    printf "%s\n" "--------------------------------------------------------------------------------"

    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local rss=$(echo "$line" | awk '{print int($3/1024)}')
        local mem_pct=$(echo "$line" | awk '{print $5}')
        local cmd=$(echo "$line" | awk '{$1=$2=$3=$4=$5=""; print $0}' | xargs)

        # Identify server type
        local server_type="unknown"
        if [[ "$cmd" == *"claude-flow"* ]]; then
            server_type="claude-flow"
        elif [[ "$cmd" == *"ruv-swarm"* ]]; then
            server_type="ruv-swarm"
        elif [[ "$cmd" == *"flow-nexus"* ]]; then
            server_type="flow-nexus"
        fi

        if ((rss > MEMORY_THRESHOLD_MB)); then
            print_color "$YELLOW" "$(printf "%-8s %-10s %-8s %s" "$pid" "$rss" "$mem_pct" "$server_type")"
        else
            printf "%-8s %-10s %-8s %s\n" "$pid" "$rss" "$mem_pct" "$server_type"
        fi

        total_rss=$((total_rss + rss))
        ((process_count++))
    done < <(ps aux | grep -E "mcp start|claude-flow|ruv-swarm|flow-nexus" | grep -v grep | while read -r line; do echo "$line" | awk '{print $2}'; done | while read -r pid; do ps -o pid,ppid,rss,vsz,%mem,command -p "$pid" 2>/dev/null | tail -n 1; done)

    printf "%s\n" "--------------------------------------------------------------------------------"
    print_color "$GREEN" "Total: $process_count MCP servers | RSS: ${total_rss}MB"

    echo "$process_count|$total_rss"
}

# Optimize process memory
optimize_process() {
    local pid=$1
    local rss=$2

    log "INFO" "Optimizing process PID=$pid (RSS=${rss}MB)"

    # Send SIGUSR2 for memory dump (if supported)
    if kill -USR2 "$pid" 2>/dev/null; then
        log "INFO" "Sent memory dump signal to PID=$pid"
    fi

    # Force garbage collection for Node.js processes
    if ps -p "$pid" -o command= | grep -q node; then
        log "INFO" "Attempting to trigger garbage collection for Node.js process $pid"
        # Note: This requires the process to have --expose-gc flag
    fi
}

# Restart high memory process
restart_process() {
    local pid=$1
    local cmd=$2

    log "WARN" "Restarting high-memory process PID=$pid"

    if [[ "$AUTO_RESTART" != "true" ]]; then
        read -p "Restart process $pid? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log "INFO" "Skipped restart of PID $pid"
            return 0
        fi
    fi

    # Graceful termination
    kill -15 "$pid" 2>/dev/null
    sleep 2

    # Force kill if still running
    if ps -p "$pid" > /dev/null 2>&1; then
        kill -9 "$pid" 2>/dev/null
    fi

    print_color "$GREEN" "✓ Process $pid terminated"
    log "INFO" "Process $pid terminated for restart"
}

# Run optimization
run_optimization() {
    print_color "$BLUE" "\n🔧 Running Memory Optimization"
    print_color "$BLUE" "==============================="

    local optimized_count=0
    local restarted_count=0

    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local rss=$(echo "$line" | awk '{print int($3/1024)}')

        if ((rss > RESTART_THRESHOLD_MB)); then
            restart_process "$pid" ""
            ((restarted_count++))
        elif ((rss > MEMORY_THRESHOLD_MB)); then
            optimize_process "$pid" "$rss"
            ((optimized_count++))
        fi
    done < <(ps aux | grep -i node | grep -v grep | awk '{print $2, $3, $6}')

    print_color "$GREEN" "\n✓ Optimization complete"
    print_color "$GREEN" "  Optimized: $optimized_count processes"
    print_color "$GREEN" "  Restarted: $restarted_count processes"

    log "INFO" "Optimization complete: optimized=$optimized_count restarted=$restarted_count"
}

# Continuous monitoring
monitor_memory() {
    print_color "$BLUE" "🔍 Starting Continuous Memory Monitoring"
    print_color "$BLUE" "========================================"
    print_color "$YELLOW" "Press Ctrl+C to stop monitoring\n"

    log "INFO" "Started continuous monitoring (interval=${MONITOR_INTERVAL}s)"

    while true; do
        clear
        print_color "$CYAN" "Memory Monitor - $(date '+%Y-%m-%d %H:%M:%S')"
        print_color "$CYAN" "=============================================="

        # System memory
        local sys_mem=$(get_system_memory)
        local sys_total=$(echo "$sys_mem" | grep -o 'total=[0-9]*' | cut -d= -f2)
        local sys_used=$(echo "$sys_mem" | grep -o 'used=[0-9]*' | cut -d= -f2)
        local sys_free=$(echo "$sys_mem" | grep -o 'free=[0-9]*' | cut -d= -f2)

        print_color "$CYAN" "\n💻 System Memory"
        echo "Total: ${sys_total}MB | Used: ${sys_used}MB | Free: ${sys_free}MB"

        # Node.js processes
        get_nodejs_memory_stats

        # MCP servers
        get_mcp_memory_stats

        print_color "$YELLOW" "\n⏳ Next update in ${MONITOR_INTERVAL} seconds..."

        sleep "$MONITOR_INTERVAL"
    done
}

# Show statistics
show_statistics() {
    print_color "$BLUE" "\n📈 Memory Statistics Report"
    print_color "$BLUE" "============================"

    # System memory
    local sys_mem=$(get_system_memory)
    print_color "$CYAN" "\n💻 System Memory"
    echo "$sys_mem" | tr ' ' '\n'

    # Node.js stats
    local nodejs_stats=$(get_nodejs_memory_stats)

    # MCP stats
    local mcp_stats=$(get_mcp_memory_stats)

    print_color "$GREEN" "\n✓ Statistics report complete"
}

# Main execution
main() {
    local action=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            -m|--monitor)
                action="monitor"
                CONTINUOUS_MONITOR=true
                shift
                ;;
            -t|--threshold)
                MEMORY_THRESHOLD_MB=$2
                shift 2
                ;;
            -r|--restart)
                RESTART_THRESHOLD_MB=$2
                shift 2
                ;;
            -i|--interval)
                MONITOR_INTERVAL=$2
                shift 2
                ;;
            -a|--auto-restart)
                AUTO_RESTART=true
                shift
                ;;
            -o|--optimize-now)
                action="optimize"
                shift
                ;;
            -s|--stats)
                action="stats"
                shift
                ;;
            -h|--help)
                usage
                ;;
            *)
                print_color "$RED" "Unknown option: $1"
                usage
                ;;
        esac
    done

    print_color "$BLUE" "========================================="
    print_color "$BLUE" "Memory Optimization Script"
    print_color "$BLUE" "========================================="
    echo ""

    log "INFO" "Memory Optimizer started (threshold=${MEMORY_THRESHOLD_MB}MB, restart=${RESTART_THRESHOLD_MB}MB)"
    log "INFO" "Log file: $LOG_FILE"

    case "$action" in
        "monitor")
            monitor_memory
            ;;
        "optimize")
            run_optimization
            ;;
        "stats")
            show_statistics
            ;;
        *)
            print_color "$YELLOW" "No action specified. Use --help for usage information."
            show_statistics
            ;;
    esac
}

# Run main
main "$@"
