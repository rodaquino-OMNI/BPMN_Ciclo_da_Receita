#!/bin/bash
################################################################################
# VS Code & MCP Process Cleanup Script
# Kills orphan processes that cause memory leaks and performance issues
################################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     VS Code & MCP Orphan Process Cleanup Utility             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to count processes
count_processes() {
    local pattern=$1
    ps aux | grep "$pattern" | grep -v grep | wc -l | tr -d ' '
}

# Function to kill processes
kill_processes() {
    local pattern=$1
    local desc=$2
    local count=$(count_processes "$pattern")

    if [ "$count" -gt 0 ]; then
        echo -e "${YELLOW}Found $count $desc processes${NC}"
        ps aux | grep "$pattern" | grep -v grep | awk '{print $2}' | while read pid; do
            echo -e "${RED}  Killing PID $pid${NC}"
            kill -9 "$pid" 2>/dev/null || true
        done
        echo -e "${GREEN}✓ Cleaned up $desc processes${NC}"
    else
        echo -e "${GREEN}✓ No $desc processes found${NC}"
    fi
    echo ""
}

# Display current process status
echo -e "${BLUE}Current Process Status:${NC}"
echo "─────────────────────────────────────────────────────────"
echo -e "Orphan ruv-swarm --version: ${YELLOW}$(count_processes 'ruv-swarm --version')${NC}"
echo -e "MCP server processes: ${YELLOW}$(count_processes 'mcp start')${NC}"
echo -e "Claude Flow processes: ${YELLOW}$(count_processes 'claude-flow')${NC}"
echo -e "Flow Nexus processes: ${YELLOW}$(count_processes 'flow-nexus')${NC}"
echo ""

# Ask for confirmation
read -p "Do you want to proceed with cleanup? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Cleanup cancelled${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}Starting cleanup...${NC}"
echo ""

# Kill orphan ruv-swarm --version processes
kill_processes "ruv-swarm --version" "orphan ruv-swarm --version"

# Kill old MCP server processes (but keep current session)
# Get current session PIDs
CURRENT_PIDS=$(ps aux | grep "mcp start" | grep -v grep | head -3 | awk '{print $2}' | tr '\n' '|' | sed 's/|$//')

if [ ! -z "$CURRENT_PIDS" ]; then
    echo -e "${BLUE}Preserving current MCP session processes: $CURRENT_PIDS${NC}"
    ps aux | grep "mcp start" | grep -v grep | grep -vE "$CURRENT_PIDS" | awk '{print $2}' | while read pid; do
        echo -e "${RED}  Killing old MCP process PID $pid${NC}"
        kill -9 "$pid" 2>/dev/null || true
    done
fi

# Kill orphan npm exec processes
kill_processes "npm exec.*mcp" "orphan npm exec MCP"

# Kill orphan hive-mind spawn processes
kill_processes "hive-mind spawn" "orphan hive-mind spawn"

# Summary
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    Cleanup Complete                           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}Final Process Status:${NC}"
echo "─────────────────────────────────────────────────────────"
echo -e "Orphan ruv-swarm --version: ${GREEN}$(count_processes 'ruv-swarm --version')${NC}"
echo -e "MCP server processes: ${GREEN}$(count_processes 'mcp start')${NC}"
echo -e "Claude Flow processes: ${GREEN}$(count_processes 'claude-flow')${NC}"
echo -e "Flow Nexus processes: ${GREEN}$(count_processes 'flow-nexus')${NC}"
echo ""

# Memory usage report
echo -e "${BLUE}Memory Usage After Cleanup:${NC}"
echo "─────────────────────────────────────────────────────────"
ps aux | awk '{print $2, $4, $11}' | sort -k2 -rn | head -5
echo ""

echo -e "${GREEN}✓ Cleanup completed successfully!${NC}"
echo -e "${YELLOW}Note: Current active MCP servers have been preserved.${NC}"
