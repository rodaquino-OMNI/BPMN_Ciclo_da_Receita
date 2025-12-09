#!/bin/bash
################################################################################
# VS Code Memory Optimizer
# Cleans up cache, logs, and optimizes VS Code for better performance
################################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║          VS Code Memory & Performance Optimizer              ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

VSCODE_DATA="$HOME/Library/Application Support/Code"
VSCODE_CACHE="$HOME/Library/Caches/com.microsoft.VSCode"

# Function to get directory size
get_size() {
    du -sh "$1" 2>/dev/null | awk '{print $1}'
}

# Display current status
echo -e "${BLUE}Current Storage Usage:${NC}"
echo "─────────────────────────────────────────────────────────"
echo -e "VS Code Data:         ${YELLOW}$(get_size "$VSCODE_DATA")${NC}"
echo -e "  ├─ User Data:       ${YELLOW}$(get_size "$VSCODE_DATA/User")${NC}"
echo -e "  ├─ CachedData:      ${YELLOW}$(get_size "$VSCODE_DATA/CachedData")${NC}"
echo -e "  ├─ GPUCache:        ${YELLOW}$(get_size "$VSCODE_DATA/GPUCache")${NC}"
echo -e "  └─ logs:            ${YELLOW}$(get_size "$VSCODE_DATA/logs")${NC}"
echo -e "VS Code Cache:        ${YELLOW}$(get_size "$VSCODE_CACHE")${NC}"
echo -e "Extensions:           ${YELLOW}$(get_size "$HOME/.vscode/extensions")${NC}"
echo ""

# Workspace count
WORKSPACE_COUNT=$(find "$VSCODE_DATA/User/workspaceStorage" -type d -maxdepth 1 2>/dev/null | wc -l | tr -d ' ')
echo -e "Workspace Storage:    ${YELLOW}$WORKSPACE_COUNT folders${NC}"
echo ""

# Ask for confirmation
echo -e "${YELLOW}This script will:${NC}"
echo "  1. Clean VS Code cache and temporary files"
echo "  2. Clear old workspace storage"
echo "  3. Clean GPU cache"
echo "  4. Remove old log files (>7 days)"
echo "  5. Optimize extension storage"
echo ""
read -p "Do you want to proceed? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Optimization cancelled${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}Starting optimization...${NC}"
echo ""

# 1. Clear GPU Cache
if [ -d "$VSCODE_DATA/GPUCache" ]; then
    echo -e "${CYAN}[1/6]${NC} Clearing GPU Cache..."
    rm -rf "$VSCODE_DATA/GPUCache"/*
    echo -e "${GREEN}✓ GPU Cache cleared${NC}"
fi

# 2. Clear Code Cache
if [ -d "$VSCODE_DATA/Code Cache" ]; then
    echo -e "${CYAN}[2/6]${NC} Clearing Code Cache..."
    rm -rf "$VSCODE_DATA/Code Cache"/*
    echo -e "${GREEN}✓ Code Cache cleared${NC}"
fi

# 3. Clear old logs (>7 days)
if [ -d "$VSCODE_DATA/logs" ]; then
    echo -e "${CYAN}[3/6]${NC} Removing old log files..."
    find "$VSCODE_DATA/logs" -type f -mtime +7 -delete 2>/dev/null || true
    echo -e "${GREEN}✓ Old logs removed${NC}"
fi

# 4. Clear system cache
if [ -d "$VSCODE_CACHE" ]; then
    echo -e "${CYAN}[4/6]${NC} Clearing system cache..."
    rm -rf "$VSCODE_CACHE"/*
    echo -e "${GREEN}✓ System cache cleared${NC}"
fi

# 5. Clean old workspace storage (keep only recent 10)
if [ -d "$VSCODE_DATA/User/workspaceStorage" ]; then
    echo -e "${CYAN}[5/6]${NC} Cleaning old workspace storage..."
    WORKSPACE_DIR="$VSCODE_DATA/User/workspaceStorage"

    # Keep only the 10 most recently modified workspace folders
    ls -t "$WORKSPACE_DIR" | tail -n +11 | while read folder; do
        rm -rf "$WORKSPACE_DIR/$folder"
    done

    NEW_COUNT=$(find "$WORKSPACE_DIR" -type d -maxdepth 1 2>/dev/null | wc -l | tr -d ' ')
    REMOVED=$((WORKSPACE_COUNT - NEW_COUNT))
    echo -e "${GREEN}✓ Removed $REMOVED old workspace folders${NC}"
fi

# 6. Clean Crashpad dumps
if [ -d "$VSCODE_DATA/Crashpad" ]; then
    echo -e "${CYAN}[6/6]${NC} Clearing crash dumps..."
    find "$VSCODE_DATA/Crashpad" -type f -name "*.dmp" -delete 2>/dev/null || true
    echo -e "${GREEN}✓ Crash dumps cleared${NC}"
fi

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                  Optimization Complete                        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Display new status
echo -e "${GREEN}Storage After Optimization:${NC}"
echo "─────────────────────────────────────────────────────────"
echo -e "VS Code Data:         ${GREEN}$(get_size "$VSCODE_DATA")${NC}"
echo -e "  ├─ User Data:       ${GREEN}$(get_size "$VSCODE_DATA/User")${NC}"
echo -e "  ├─ CachedData:      ${GREEN}$(get_size "$VSCODE_DATA/CachedData")${NC}"
echo -e "  ├─ GPUCache:        ${GREEN}$(get_size "$VSCODE_DATA/GPUCache")${NC}"
echo -e "  └─ logs:            ${GREEN}$(get_size "$VSCODE_DATA/logs")${NC}"
echo -e "VS Code Cache:        ${GREEN}$(get_size "$VSCODE_CACHE")${NC}"
echo ""

FINAL_WORKSPACE=$(find "$VSCODE_DATA/User/workspaceStorage" -type d -maxdepth 1 2>/dev/null | wc -l | tr -d ' ')
echo -e "Workspace Storage:    ${GREEN}$FINAL_WORKSPACE folders${NC}"
echo ""

echo -e "${CYAN}Recommendations:${NC}"
echo "─────────────────────────────────────────────────────────"
echo "1. Restart VS Code to apply changes"
echo "2. Disable unused extensions"
echo "3. Run this script monthly for best performance"
echo "4. Consider using workspace-specific settings"
echo ""

echo -e "${GREEN}✓ Optimization completed successfully!${NC}"
