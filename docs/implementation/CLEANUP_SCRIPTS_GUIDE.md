# Cleanup Scripts Implementation Guide

## Overview

This guide documents the comprehensive cleanup and memory optimization solution implemented by the Hive Mind Coder Agent. The solution includes four main scripts designed to safely identify, monitor, and terminate orphaned processes while optimizing memory usage.

## Scripts Overview

### 1. cleanup-orphans.sh
**Purpose**: Safely identify and terminate orphaned Node.js processes and swarm agents

**Key Features**:
- Identifies orphaned processes by checking parent process existence
- Protects critical system processes (launchd, systemd, init, etc.)
- Supports dry-run mode for safe previewing
- Graceful termination with SIGTERM before SIGKILL
- Comprehensive logging
- Memory usage tracking

**Usage Examples**:
```bash
# Preview what would be cleaned up
./scripts/cleanup-orphans.sh --dry-run --clean-all

# Clean up swarm agents only
./scripts/cleanup-orphans.sh --clean-swarm --force

# Clean up all orphaned processes with confirmation
./scripts/cleanup-orphans.sh --clean-all

# Clean up MCP servers only
./scripts/cleanup-orphans.sh --clean-mcp --force

# Verbose output with Node.js cleanup
./scripts/cleanup-orphans.sh --clean-node --verbose
```

**Options**:
- `--dry-run, -d`: Preview without executing
- `--force, -f`: Skip confirmation prompts
- `--verbose, -v`: Enable detailed output
- `--clean-mcp`: Clean MCP server processes
- `--clean-swarm`: Clean swarm agent processes
- `--clean-node`: Clean orphaned Node.js processes
- `--clean-all`: Clean all types
- `--memory-threshold MB`: Set memory threshold

### 2. memory-optimizer.sh
**Purpose**: Monitor and optimize memory usage across Node.js processes and MCP servers

**Key Features**:
- Real-time memory monitoring
- Automatic memory statistics
- Process optimization recommendations
- Configurable thresholds
- Auto-restart capability for high-memory processes
- System-wide memory analysis

**Usage Examples**:
```bash
# Show current memory statistics
./scripts/memory-optimizer.sh --stats

# Run one-time optimization
./scripts/memory-optimizer.sh --optimize-now

# Continuous monitoring with auto-restart
./scripts/memory-optimizer.sh --monitor --auto-restart

# Monitor with custom thresholds
./scripts/memory-optimizer.sh --monitor --threshold 750 --restart 1500

# Monitor with custom interval
./scripts/memory-optimizer.sh --monitor --interval 10
```

**Options**:
- `--monitor, -m`: Enable continuous monitoring
- `--threshold MB, -t`: Memory warning threshold (default: 500MB)
- `--restart MB, -r`: Auto-restart threshold (default: 1000MB)
- `--interval SEC, -i`: Monitoring interval (default: 5s)
- `--auto-restart, -a`: Enable automatic restart
- `--optimize-now, -o`: Run immediate optimization
- `--stats, -s`: Show memory statistics

### 3. process-monitor.sh
**Purpose**: Continuous health monitoring for Node.js processes and MCP servers

**Key Features**:
- Real-time health status monitoring
- CPU and memory threshold alerts
- Automatic recovery attempts
- Health report generation
- Configurable alert thresholds
- Critical status detection

**Usage Examples**:
```bash
# Run single health check
./scripts/process-monitor.sh --once

# Generate health report
./scripts/process-monitor.sh --report

# Continuous monitoring with auto-recovery
./scripts/process-monitor.sh --auto-recovery

# Custom thresholds
./scripts/process-monitor.sh --cpu-threshold 85 --mem-threshold 80

# Monitor with custom interval
./scripts/process-monitor.sh --interval 15
```

**Options**:
- `--once`: Run single check and exit
- `--report`: Generate health report
- `--auto-recovery`: Enable automatic recovery
- `--cpu-threshold PCT`: CPU alert threshold (default: 80%)
- `--mem-threshold PCT`: Memory alert threshold (default: 75%)
- `--interval SEC`: Check interval (default: 10s)

### 4. automated-cleanup.sh
**Purpose**: Scheduled and event-driven cleanup procedures

**Key Features**:
- Cron job installation for scheduled cleanup
- LaunchDaemon support for macOS
- Memory watchdog with configurable thresholds
- Event-driven cleanup triggers
- Zombie process detection
- Orphaned process monitoring

**Usage Examples**:
```bash
# Install as cron job (every 4 hours)
./scripts/automated-cleanup.sh --install-cron

# Remove cron job
./scripts/automated-cleanup.sh --uninstall-cron

# Install as LaunchDaemon (macOS)
./scripts/automated-cleanup.sh --install-launchd

# Start memory watchdog with auto-cleanup
./scripts/automated-cleanup.sh --watchdog true

# Event-driven monitoring
./scripts/automated-cleanup.sh --event-driven

# Check for issues now
./scripts/automated-cleanup.sh --check

# Show automation status
./scripts/automated-cleanup.sh --status
```

**Commands**:
- `--install-cron`: Schedule automated cleanup
- `--uninstall-cron`: Remove scheduled cleanup
- `--install-launchd`: Install LaunchDaemon (macOS)
- `--watchdog [auto]`: Start memory watchdog
- `--event-driven`: Start event monitoring
- `--check`: Run immediate check
- `--status`: Show automation status

## Safety Features

### Protected Processes
The scripts automatically protect critical system processes:
- `launchd` (macOS init)
- `systemd` (Linux init)
- `init` (Unix init)
- `kernel_task` (macOS kernel)
- `claude` (Claude Code)
- `Terminal`, `iTerm` (Terminal applications)
- `vscode` (VS Code)

### Graceful Termination
All scripts follow a graceful termination pattern:
1. Send SIGTERM (signal 15) for graceful shutdown
2. Wait 2 seconds for process to terminate
3. If still running, send SIGKILL (signal 9)
4. Verify termination and log results

### Dry-Run Mode
Preview all changes before execution:
```bash
./scripts/cleanup-orphans.sh --dry-run --clean-all
```

### Confirmation Prompts
Interactive mode asks for confirmation before terminating processes unless `--force` is used.

## Testing

Run the comprehensive test suite:
```bash
# Run all tests
./tests/test-cleanup-scripts.sh

# Tests include:
# - Script existence verification
# - Executable permissions check
# - Help command functionality
# - Dry-run mode testing
# - Safety feature validation
# - Log file creation
# - Error handling
# - Concurrent execution safety
# - Dependency verification
# - Memory calculation accuracy
# - Full workflow integration
```

## Logging

All scripts create detailed logs:
- **cleanup-orphans.sh**: `/tmp/cleanup-orphans-YYYYMMDD-HHMMSS.log`
- **memory-optimizer.sh**: `/tmp/memory-optimizer-YYYYMMDD-HHMMSS.log`
- **process-monitor.sh**: `/tmp/process-monitor/health-YYYYMMDD.log`
- **automated-cleanup.sh**: `/tmp/automated-cleanup/automated-cleanup.log`

## Common Workflows

### Daily Maintenance
```bash
# Morning: Check system health
./scripts/process-monitor.sh --once --report

# Midday: Optimize memory
./scripts/memory-optimizer.sh --optimize-now

# Evening: Clean up orphans
./scripts/cleanup-orphans.sh --clean-all --force
```

### Automation Setup
```bash
# Set up automated cleanup
./scripts/automated-cleanup.sh --install-cron

# Start memory watchdog
./scripts/automated-cleanup.sh --watchdog true &

# Enable process monitoring
./scripts/process-monitor.sh --auto-recovery --interval 15 &
```

### Emergency Cleanup
```bash
# Quick check
./scripts/automated-cleanup.sh --check

# Immediate cleanup if needed
./scripts/cleanup-orphans.sh --clean-all --force

# Show what freed up
./scripts/memory-optimizer.sh --stats
```

## Troubleshooting

### Script Won't Execute
```bash
# Make executable
chmod +x scripts/*.sh
```

### Permission Denied
```bash
# Run with appropriate privileges
sudo ./scripts/cleanup-orphans.sh --clean-all
```

### Process Still Running
```bash
# Check process status
ps aux | grep [process-name]

# Manual termination
kill -9 [PID]
```

### High Memory Not Detected
```bash
# Check current thresholds
./scripts/memory-optimizer.sh --stats

# Adjust threshold
./scripts/memory-optimizer.sh --monitor --threshold 250
```

## Best Practices

1. **Always test with --dry-run first**
2. **Review logs regularly** for patterns
3. **Set appropriate thresholds** for your system
4. **Use automation** for regular maintenance
5. **Monitor continuously** in production
6. **Keep logs** for troubleshooting
7. **Test recovery procedures** regularly
8. **Document custom configurations**

## Performance Impact

- **cleanup-orphans.sh**: Minimal impact, runs on-demand
- **memory-optimizer.sh**: Low impact during monitoring
- **process-monitor.sh**: Very low impact, configurable intervals
- **automated-cleanup.sh**: Negligible impact when scheduled

## Integration with Hive Mind

All scripts integrate with the Hive Mind coordination system:

```bash
# Before running cleanup
npx claude-flow@alpha hooks pre-task --description "Running cleanup"

# After cleanup operations
npx claude-flow@alpha hooks post-edit --file "cleanup.log" --memory-key "swarm/coder/cleanup"

# Store results
npx claude-flow@alpha hooks notify --message "Cleanup completed"
```

## File Locations

```
/Users/rodrigo/claude-projects/BPMN Ciclo da Receita/BPMN_Ciclo_da_Receita/
├── scripts/
│   ├── cleanup-orphans.sh          # Main cleanup script
│   ├── memory-optimizer.sh         # Memory monitoring
│   ├── process-monitor.sh          # Health monitoring
│   └── automated-cleanup.sh        # Automation setup
├── tests/
│   └── test-cleanup-scripts.sh     # Test suite
└── docs/
    └── implementation/
        └── CLEANUP_SCRIPTS_GUIDE.md # This guide
```

## Future Enhancements

- [ ] Add support for Docker container cleanup
- [ ] Implement machine learning for pattern detection
- [ ] Add Prometheus metrics export
- [ ] Create web dashboard for monitoring
- [ ] Add Slack/email notifications
- [ ] Implement rollback functionality
- [ ] Add cleanup history tracking
- [ ] Create cleanup scheduling profiles

## Support

For issues or questions:
1. Check logs in `/tmp/` directory
2. Run test suite for diagnostics
3. Review this guide for common issues
4. Check Hive Mind coordination logs

---

**Author**: Hive Mind Coder Agent
**Date**: 2025-12-09
**Version**: 1.0.0
**Status**: Production Ready
