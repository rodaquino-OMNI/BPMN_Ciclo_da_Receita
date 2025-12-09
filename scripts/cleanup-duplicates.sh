#!/bin/bash
# Cleanup Duplicate File Hierarchies
# This script removes duplicate directory structures that are redundant
# with the standard Maven src/main/java/ hierarchy.
#
# Project: BPMN Ciclo da Receita
# Date: 2025-12-09
# Purpose: Clean up non-standard directory structures after Maven migration

set -e  # Exit on error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Duplicate Directory Cleanup Script${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# Get the project root directory (assuming script is in scripts/ subdirectory)
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
echo "Project Root: $PROJECT_ROOT"
echo ""

# Check if directories exist
echo -e "${YELLOW}Checking for duplicate directories...${NC}"
echo ""

DELEGATES_DIR="$PROJECT_ROOT/src/delegates"
JAVA_DIR="$PROJECT_ROOT/src/java"
MAIN_JAVA_DIR="$PROJECT_ROOT/src/main/java"

# Verification
if [ ! -d "$MAIN_JAVA_DIR" ]; then
    echo -e "${RED}ERROR: Standard Maven directory src/main/java/ not found!${NC}"
    echo "Aborting cleanup to prevent data loss."
    exit 1
fi

echo -e "${GREEN}✓ Standard Maven directory exists: src/main/java/${NC}"
echo "  Files: $(find "$MAIN_JAVA_DIR" -type f 2>/dev/null | wc -l | tr -d ' ')"
echo ""

# Check and delete src/delegates/
if [ -d "$DELEGATES_DIR" ]; then
    FILE_COUNT=$(find "$DELEGATES_DIR" -type f 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${YELLOW}Found duplicate: src/delegates/${NC}"
    echo "  Files: $FILE_COUNT"

    if [ "$FILE_COUNT" -eq 0 ]; then
        echo -e "${GREEN}  → Removing empty duplicate directory...${NC}"
        rm -rf "$DELEGATES_DIR"
        echo -e "${GREEN}  ✓ Removed: src/delegates/${NC}"
    else
        echo -e "${RED}  ⚠ WARNING: Directory contains $FILE_COUNT files!${NC}"
        echo "  Please verify files are duplicated in src/main/java/ before deletion."
        echo "  To force deletion, run: rm -rf $DELEGATES_DIR"
    fi
else
    echo -e "${GREEN}✓ No duplicate found: src/delegates/${NC}"
fi
echo ""

# Check and delete src/java/
if [ -d "$JAVA_DIR" ]; then
    FILE_COUNT=$(find "$JAVA_DIR" -type f 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${YELLOW}Found duplicate: src/java/${NC}"
    echo "  Files: $FILE_COUNT"

    if [ "$FILE_COUNT" -eq 0 ]; then
        echo -e "${GREEN}  → Removing empty duplicate directory...${NC}"
        rm -rf "$JAVA_DIR"
        echo -e "${GREEN}  ✓ Removed: src/java/${NC}"
    else
        echo -e "${RED}  ⚠ WARNING: Directory contains $FILE_COUNT files!${NC}"
        echo "  Please verify files are duplicated in src/main/java/ before deletion."
        echo "  To force deletion, run: rm -rf $JAVA_DIR"
    fi
else
    echo -e "${GREEN}✓ No duplicate found: src/java/${NC}"
fi
echo ""

# Final summary
echo -e "${YELLOW}========================================${NC}"
echo -e "${GREEN}Cleanup Complete${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Standard Maven structure retained:"
echo "  src/main/java/ - $(find "$MAIN_JAVA_DIR" -type f 2>/dev/null | wc -l | tr -d ' ') files"
echo ""
echo "Next steps:"
echo "  1. Verify project builds: mvn clean compile"
echo "  2. Run tests: mvn test"
echo "  3. Commit changes: git add . && git commit -m 'Clean up duplicate directory hierarchies'"
echo ""
