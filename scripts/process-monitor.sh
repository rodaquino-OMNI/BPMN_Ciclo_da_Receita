#!/bin/bash

################################################################################
# Process Health Monitor
# Purpose: Continuous health monitoring for Node.js processes and MCP servers
# Author: Hive Mind Coder Agent
# Date: 2025-12-09
################################################################################

set -euo pipefail

# Configuration
MONITOR_DIR="/tmp/process-monitor"
HEALTH_LOG="$MONITOR_DIR/health-$(date +%Y%m%d).log"
ALERT_THRESHOLD_CPU=80
ALERT_THRESHOLD_MEM=75
CHECK_INTERVAL=10
ENABLE_ALERTS=true
ENABLE_AUTO_RECOVERY=false

# Create monitor directory
mkdir -p "$MONITOR_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$HEALTH_LOG"
}

print_color() {
    echo -e "${1}${2}${NC}"
}

# Check process health
check_process_health() {
    local pid=$1
    local name=$2

    if ! ps -p "$pid" > /dev/null 2>&1; then
        log "ERROR: Process $name (PID=$pid) is not running"
        return 1
    fi

    # Get CPU and memory usage
    local stats=$(ps -o pid,%cpu,%mem,rss -p "$pid" | tail -n 1)
    local cpu=$(echo "$stats" | awk '{print int($2)}')
    local mem=$(echo "$stats" | awk '{print int($3)}')
    local rss=$(echo "$stats" | awk '{print int($4/1024)}')

    # Check thresholds
    local status="HEALTHY"
    local color="$GREEN"

    if ((cpu > ALERT_THRESHOLD_CPU)) || ((mem > ALERT_THRESHOLD_MEM)); then
        status="WARNING"
        color="$YELLOW"
        log "WARN: $name (PID=$pid) - High resource usage: CPU=${cpu}% MEM=${mem}% RSS=${rss}MB"
    fi

    if ((cpu > 90)) || ((mem > 90)); then
        status="CRITICAL"
        color="$RED"
        log "CRITICAL: $name (PID=$pid) - Critical resource usage: CPU=${cpu}% MEM=${mem}% RSS=${rss}MB"

        if [[ "$ENABLE_AUTO_RECOVERY" == "true" ]]; then
            attempt_recovery "$pid" "$name"
        fi
    fi

    print_color "$color" "[$status] $name (PID=$pid): CPU=${cpu}% MEM=${mem}% RSS=${rss}MB"

    return 0
}

# Attempt process recovery
attempt_recovery() {
    local pid=$1
    local name=$2

    log "INFO: Attempting recovery for $name (PID=$pid)"

    # Send SIGUSR2 for Node.js heap dump
    if kill -USR2 "$pid" 2>/dev/null; then
        log "INFO: Sent USR2 signal to $pid for heap dump"
        sleep 2
    fi

    # Check if process improved
    local stats=$(ps -o %cpu,%mem -p "$pid" 2>/dev/null | tail -n 1)
    local cpu=$(echo "$stats" | awk '{print int($1)}')

    if ((cpu > 90)); then
        log "WARN: Recovery failed, process still critical"
    else
        log "INFO: Recovery successful for $pid"
    fi
}

# Monitor all processes
monitor_all() {
    print_color "$BLUE" "\n🔍 Process Health Monitor - $(date '+%Y-%m-%d %H:%M:%S')"
    print_color "$BLUE" "=============================================="

    # Monitor Node.js processes
    print_color "$BLUE" "\n📦 Node.js Processes"
    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local cmd=$(echo "$line" | awk '{$1=""; print $0}' | xargs)
        local name=$(echo "$cmd" | awk '{print $1}' | xargs basename)

        check_process_health "$pid" "$name" || true
    done < <(ps aux | grep -i node | grep -v grep | awk '{print $2, $0}')

    # Monitor MCP servers
    print_color "$BLUE" "\n📡 MCP Servers"
    for server in "claude-flow" "ruv-swarm" "flow-nexus"; do
        while IFS= read -r line; do
            local pid=$(echo "$line" | awk '{print $1}')
            check_process_health "$pid" "$server" || true
        done < <(ps aux | grep "$server" | grep -v grep | awk '{print $2}')
    done
}

# Generate health report
generate_report() {
    local report_file="$MONITOR_DIR/health-report-$(date +%Y%m%d-%H%M%S).txt"

    {
        echo "Process Health Report"
        echo "Generated: $(date)"
        echo "===================="
        echo ""

        echo "Node.js Processes:"
        ps aux | grep -i node | grep -v grep | awk '{printf "PID: %s | CPU: %s%% | MEM: %s%% | CMD: %s\n", $2, $3, $4, substr($0, index($0,$11))}'

        echo ""
        echo "MCP Servers:"
        ps aux | grep -E "claude-flow|ruv-swarm|flow-nexus" | grep -v grep | awk '{printf "PID: %s | CPU: %s%% | MEM: %s%% | SERVER: %s\n", $2, $3, $4, substr($0, index($0,$11))}'

        echo ""
        echo "Recent Alerts:"
        tail -n 50 "$HEALTH_LOG" | grep -E "WARN|ERROR|CRITICAL" || echo "No recent alerts"

    } > "$report_file"

    print_color "$GREEN" "\n✓ Health report saved to: $report_file"
}

# Main monitoring loop
main() {
    local mode="continuous"

    while [[ $# -gt 0 ]]; do
        case $1 in
            --once)
                mode="once"
                shift
                ;;
            --report)
                generate_report
                exit 0
                ;;
            --interval)
                CHECK_INTERVAL=$2
                shift 2
                ;;
            --auto-recovery)
                ENABLE_AUTO_RECOVERY=true
                shift
                ;;
            --cpu-threshold)
                ALERT_THRESHOLD_CPU=$2
                shift 2
                ;;
            --mem-threshold)
                ALERT_THRESHOLD_MEM=$2
                shift 2
                ;;
            *)
                echo "Usage: $0 [--once|--report|--auto-recovery] [--interval SEC] [--cpu-threshold PCT] [--mem-threshold PCT]"
                exit 1
                ;;
        esac
    done

    log "INFO: Process health monitor started (CPU threshold=${ALERT_THRESHOLD_CPU}%, MEM threshold=${ALERT_THRESHOLD_MEM}%)"

    if [[ "$mode" == "once" ]]; then
        monitor_all
        generate_report
    else
        print_color "$YELLOW" "Press Ctrl+C to stop monitoring\n"
        while true; do
            clear
            monitor_all
            print_color "$YELLOW" "\n⏳ Next check in ${CHECK_INTERVAL} seconds..."
            sleep "$CHECK_INTERVAL"
        done
    fi
}

main "$@"
