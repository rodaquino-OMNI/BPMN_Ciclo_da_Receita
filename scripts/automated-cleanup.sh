#!/bin/bash

################################################################################
# Automated Cleanup Procedures
# Purpose: Scheduled and event-driven cleanup of orphaned processes
# Author: Hive Mind Coder Agent
# Date: 2025-12-09
################################################################################

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="/tmp/automated-cleanup"
SCHEDULE_FILE="$LOG_DIR/cleanup-schedule.conf"

mkdir -p "$LOG_DIR"

# Default configuration
CLEANUP_SCHEDULE="0 */4 * * *"  # Every 4 hours
MEMORY_CHECK_INTERVAL=300        # 5 minutes
MAX_MEMORY_THRESHOLD=1024        # 1GB
AUTO_CLEANUP=false

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG_DIR/automated-cleanup.log"
}

print_color() {
    echo -e "${1}${2}${NC}"
}

# Install cleanup as cron job
install_cron() {
    print_color "$BLUE" "📅 Installing automated cleanup cron job..."

    local cron_command="$SCRIPT_DIR/cleanup-orphans.sh --clean-all --force >> $LOG_DIR/cron-cleanup.log 2>&1"

    # Check if cron job already exists
    if crontab -l 2>/dev/null | grep -q "cleanup-orphans.sh"; then
        print_color "$YELLOW" "⚠️  Cron job already exists"
        read -p "Replace existing cron job? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            return 0
        fi
        # Remove old entry
        crontab -l | grep -v "cleanup-orphans.sh" | crontab -
    fi

    # Add new cron job
    (crontab -l 2>/dev/null; echo "$CLEANUP_SCHEDULE $cron_command") | crontab -

    print_color "$GREEN" "✓ Cron job installed: $CLEANUP_SCHEDULE"
    log "INFO: Cron job installed"
}

# Remove cron job
uninstall_cron() {
    print_color "$BLUE" "🗑️  Removing automated cleanup cron job..."

    if ! crontab -l 2>/dev/null | grep -q "cleanup-orphans.sh"; then
        print_color "$YELLOW" "No cron job found"
        return 0
    fi

    crontab -l | grep -v "cleanup-orphans.sh" | crontab -
    print_color "$GREEN" "✓ Cron job removed"
    log "INFO: Cron job removed"
}

# Create launchd daemon (macOS)
install_launchd() {
    local plist_file="$HOME/Library/LaunchAgents/com.hivemind.cleanup.plist"

    print_color "$BLUE" "📅 Installing LaunchDaemon for macOS..."

    cat > "$plist_file" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.hivemind.cleanup</string>
    <key>ProgramArguments</key>
    <array>
        <string>$SCRIPT_DIR/cleanup-orphans.sh</string>
        <string>--clean-all</string>
        <string>--force</string>
    </array>
    <key>StartInterval</key>
    <integer>14400</integer>
    <key>StandardOutPath</key>
    <string>$LOG_DIR/launchd-cleanup.log</string>
    <key>StandardErrorPath</key>
    <string>$LOG_DIR/launchd-error.log</string>
</dict>
</plist>
EOF

    launchctl load "$plist_file" 2>/dev/null || true

    print_color "$GREEN" "✓ LaunchDaemon installed at: $plist_file"
    log "INFO: LaunchDaemon installed"
}

# Memory watchdog
memory_watchdog() {
    print_color "$BLUE" "🐕 Starting memory watchdog..."
    log "INFO: Memory watchdog started (threshold=${MAX_MEMORY_THRESHOLD}MB, interval=${MEMORY_CHECK_INTERVAL}s)"

    while true; do
        # Check all Node.js processes
        while IFS= read -r line; do
            local pid=$(echo "$line" | awk '{print $1}')
            local rss=$(echo "$line" | awk '{print int($3/1024)}')
            local cmd=$(echo "$line" | awk '{$1=$2=$3=$4=$5=""; print $0}' | xargs)

            if ((rss > MAX_MEMORY_THRESHOLD)); then
                log "ALERT: High memory usage detected - PID=$pid RSS=${rss}MB CMD=$cmd"

                if [[ "$AUTO_CLEANUP" == "true" ]]; then
                    log "INFO: Auto-cleanup triggered for PID=$pid"
                    "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --force
                    break
                else
                    print_color "$RED" "⚠️  High memory alert: PID=$pid using ${rss}MB"
                fi
            fi
        done < <(ps aux | grep -i node | grep -v grep | awk '{print $2, $3, $6, $0}')

        sleep "$MEMORY_CHECK_INTERVAL"
    done
}

# Event-driven cleanup
event_driven_cleanup() {
    print_color "$BLUE" "⚡ Setting up event-driven cleanup..."

    # Monitor for process crashes and cleanup
    log "INFO: Event-driven cleanup monitoring started"

    while true; do
        # Check for zombie processes
        local zombie_count=$(ps aux | awk '$8=="Z" {print $0}' | wc -l | xargs)

        if ((zombie_count > 0)); then
            log "ALERT: Detected $zombie_count zombie processes"
            "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --force
        fi

        # Check for orphaned processes
        local orphan_count=$(ps -eo pid,ppid,stat | awk '$2==1 && $3!~/s/ {print $1}' | wc -l | xargs)

        if ((orphan_count > 5)); then
            log "ALERT: Detected $orphan_count orphaned processes"
            "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --force
        fi

        sleep 60
    done
}

# Run immediate cleanup check
run_check() {
    print_color "$BLUE" "🔍 Running cleanup check..."

    local issues_found=0

    # Check for high memory processes
    print_color "$YELLOW" "\nChecking for high memory usage..."
    while IFS= read -r line; do
        local pid=$(echo "$line" | awk '{print $1}')
        local rss=$(echo "$line" | awk '{print int($3/1024)}')

        if ((rss > MAX_MEMORY_THRESHOLD)); then
            print_color "$RED" "⚠️  High memory: PID=$pid RSS=${rss}MB"
            ((issues_found++))
        fi
    done < <(ps aux | grep -i node | grep -v grep | awk '{print $2, $3, $6}')

    # Check for orphaned processes
    print_color "$YELLOW" "\nChecking for orphaned processes..."
    local orphan_count=$(ps -eo pid,ppid | awk '$2==1 {print $1}' | wc -l | xargs)
    if ((orphan_count > 0)); then
        print_color "$RED" "⚠️  Found $orphan_count potentially orphaned processes"
        ((issues_found++))
    fi

    # Check for zombie processes
    print_color "$YELLOW" "\nChecking for zombie processes..."
    local zombie_count=$(ps aux | awk '$8=="Z"' | wc -l | xargs)
    if ((zombie_count > 0)); then
        print_color "$RED" "⚠️  Found $zombie_count zombie processes"
        ((issues_found++))
    fi

    if ((issues_found == 0)); then
        print_color "$GREEN" "\n✓ No issues found"
    else
        print_color "$YELLOW" "\n⚠️  Found $issues_found issue(s)"
        read -p "Run cleanup now? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            "$SCRIPT_DIR/cleanup-orphans.sh" --clean-all --force
        fi
    fi
}

# Show status
show_status() {
    print_color "$BLUE" "📊 Automated Cleanup Status"
    print_color "$BLUE" "============================"

    # Check cron
    echo -e "\n${YELLOW}Cron Jobs:${NC}"
    if crontab -l 2>/dev/null | grep -q "cleanup-orphans.sh"; then
        print_color "$GREEN" "✓ Cron job installed"
        crontab -l | grep "cleanup-orphans.sh"
    else
        print_color "$YELLOW" "✗ No cron job found"
    fi

    # Check LaunchDaemon (macOS)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo -e "\n${YELLOW}LaunchDaemon:${NC}"
        local plist_file="$HOME/Library/LaunchAgents/com.hivemind.cleanup.plist"
        if [ -f "$plist_file" ]; then
            print_color "$GREEN" "✓ LaunchDaemon installed"
        else
            print_color "$YELLOW" "✗ No LaunchDaemon found"
        fi
    fi

    # Recent logs
    echo -e "\n${YELLOW}Recent Cleanup Activity:${NC}"
    if [ -f "$LOG_DIR/automated-cleanup.log" ]; then
        tail -n 10 "$LOG_DIR/automated-cleanup.log"
    else
        echo "No recent activity"
    fi
}

# Main execution
main() {
    case "${1:-}" in
        --install-cron)
            install_cron
            ;;
        --uninstall-cron)
            uninstall_cron
            ;;
        --install-launchd)
            install_launchd
            ;;
        --watchdog)
            AUTO_CLEANUP="${2:-false}"
            memory_watchdog
            ;;
        --event-driven)
            event_driven_cleanup
            ;;
        --check)
            run_check
            ;;
        --status)
            show_status
            ;;
        *)
            cat << EOF
Usage: $0 [COMMAND]

Automated cleanup procedures for orphaned processes.

COMMANDS:
    --install-cron      Install as cron job (every 4 hours)
    --uninstall-cron    Remove cron job
    --install-launchd   Install as LaunchDaemon (macOS)
    --watchdog [auto]   Start memory watchdog (optional: auto cleanup)
    --event-driven      Start event-driven cleanup monitoring
    --check             Run immediate cleanup check
    --status            Show automation status

EXAMPLES:
    $0 --install-cron           # Schedule automated cleanup
    $0 --watchdog true          # Start watchdog with auto-cleanup
    $0 --check                  # Check for issues now

EOF
            exit 0
            ;;
    esac
}

main "$@"
