# Cleanup Scripts Quick Reference

## 🚀 Quick Start

### Immediate Actions

```bash
# Check what's wrong
./scripts/automated-cleanup.sh --check

# Clean up everything (with confirmation)
./scripts/cleanup-orphans.sh --clean-all

# See memory usage
./scripts/memory-optimizer.sh --stats
```

## 📋 Command Cheat Sheet

### cleanup-orphans.sh

| Command | Description |
|---------|-------------|
| `--dry-run --clean-all` | Preview cleanup without executing |
| `--clean-swarm --force` | Clean swarm agents without prompts |
| `--clean-mcp --force` | Clean MCP servers without prompts |
| `--clean-node --verbose` | Clean Node.js processes with details |
| `--clean-all --force` | Clean everything without confirmation |

### memory-optimizer.sh

| Command | Description |
|---------|-------------|
| `--stats` | Show current memory statistics |
| `--optimize-now` | Run immediate optimization |
| `--monitor` | Start continuous monitoring |
| `--monitor --auto-restart` | Monitor with auto-restart enabled |
| `--threshold 750` | Set 750MB warning threshold |

### process-monitor.sh

| Command | Description |
|---------|-------------|
| `--once` | Single health check |
| `--report` | Generate health report |
| `--auto-recovery` | Enable automatic recovery |
| `--cpu-threshold 85` | Alert at 85% CPU |
| `--mem-threshold 80` | Alert at 80% memory |

### automated-cleanup.sh

| Command | Description |
|---------|-------------|
| `--install-cron` | Schedule cleanup every 4 hours |
| `--uninstall-cron` | Remove scheduled cleanup |
| `--watchdog true` | Start watchdog with auto-cleanup |
| `--check` | Check for issues now |
| `--status` | Show automation status |

## 🎯 Common Scenarios

### Scenario 1: System Running Slow

```bash
# Check memory usage
./scripts/memory-optimizer.sh --stats

# Find high-memory processes
./scripts/process-monitor.sh --once

# Clean up orphans
./scripts/cleanup-orphans.sh --clean-all --force

# Verify improvement
./scripts/memory-optimizer.sh --stats
```

### Scenario 2: Too Many Processes

```bash
# Preview what will be cleaned
./scripts/cleanup-orphans.sh --dry-run --clean-all

# Clean up if safe
./scripts/cleanup-orphans.sh --clean-all

# Monitor for new issues
./scripts/process-monitor.sh --interval 5
```

### Scenario 3: Setup Automation

```bash
# Install scheduled cleanup
./scripts/automated-cleanup.sh --install-cron

# Start memory watchdog (background)
nohup ./scripts/automated-cleanup.sh --watchdog true &

# Start health monitoring (background)
nohup ./scripts/process-monitor.sh --auto-recovery &

# Verify setup
./scripts/automated-cleanup.sh --status
```

### Scenario 4: Emergency Response

```bash
# Quick check
./scripts/automated-cleanup.sh --check

# Force immediate cleanup
./scripts/cleanup-orphans.sh --clean-all --force

# Restart high-memory processes
./scripts/memory-optimizer.sh --optimize-now

# Generate report
./scripts/process-monitor.sh --report
```

## 🔧 Configuration

### Memory Thresholds

```bash
# Low threshold (more aggressive)
--threshold 250 --restart 500

# Medium threshold (balanced)
--threshold 500 --restart 1000

# High threshold (conservative)
--threshold 1000 --restart 2000
```

### Monitoring Intervals

```bash
# Frequent checks (high overhead)
--interval 5

# Moderate checks (balanced)
--interval 10

# Infrequent checks (low overhead)
--interval 30
```

## 📊 Interpreting Output

### Memory Statistics

```
PID     PPID    RSS(MB)  VSZ(MB)  MEM%    COMMAND
12345   1       850      2048     15.2    node server.js
```

- **RSS**: Actual memory used (Resident Set Size)
- **VSZ**: Virtual memory allocated
- **MEM%**: Percentage of system memory

### Health Status

- **HEALTHY** (Green): Normal operation
- **WARNING** (Yellow): High resource usage
- **CRITICAL** (Red): Critical resource usage

### Log Entries

```
[2025-12-09 10:00:00] [INFO] Starting cleanup operation
[2025-12-09 10:00:01] [WARN] Found orphaned process: PID=12345
[2025-12-09 10:00:02] [INFO] Successfully terminated PID 12345
```

## 🛡️ Safety Checklist

Before running cleanup:
- [ ] Check dry-run output first
- [ ] Verify no critical processes will be affected
- [ ] Ensure important work is saved
- [ ] Have backup plan if issues occur
- [ ] Review recent logs

After running cleanup:
- [ ] Verify system stability
- [ ] Check application functionality
- [ ] Review cleanup logs
- [ ] Monitor for new issues
- [ ] Document any problems

## 🐛 Troubleshooting

### Script won't execute
```bash
chmod +x scripts/*.sh
```

### Process not terminating
```bash
# Check if process exists
ps aux | grep [PID]

# Force kill
kill -9 [PID]
```

### Permission errors
```bash
# Run with sudo if needed
sudo ./scripts/cleanup-orphans.sh --clean-all
```

### Logs not appearing
```bash
# Check log directory
ls -la /tmp/cleanup-*
ls -la /tmp/process-monitor/

# Create directory if missing
mkdir -p /tmp/process-monitor
```

### Cron job not running
```bash
# List current cron jobs
crontab -l

# Reinstall
./scripts/automated-cleanup.sh --uninstall-cron
./scripts/automated-cleanup.sh --install-cron
```

## 📂 File Locations

| File | Location |
|------|----------|
| Scripts | `/scripts/` |
| Tests | `/tests/` |
| Logs | `/tmp/cleanup-*` |
| Docs | `/docs/implementation/` |

## ⚡ Performance Tips

1. **Use --dry-run first**: Preview before executing
2. **Set appropriate thresholds**: Match your system capacity
3. **Schedule during low usage**: Off-peak hours for cleanup
4. **Monitor continuously**: Catch issues early
5. **Keep logs**: Historical data helps patterns
6. **Test regularly**: Ensure scripts work when needed
7. **Automate routine tasks**: Reduce manual intervention

## 🔗 Related Commands

### View running processes
```bash
ps aux | grep node
ps aux | grep mcp
```

### Check system memory
```bash
# macOS
vm_stat

# Linux
free -m
```

### Kill process by name
```bash
pkill -f "process-name"
```

### View process tree
```bash
pstree -p [PID]
```

## 📞 Need Help?

1. **Check logs**: Look in `/tmp/` directory
2. **Run tests**: `./tests/test-cleanup-scripts.sh`
3. **Read guide**: `docs/implementation/CLEANUP_SCRIPTS_GUIDE.md`
4. **Review status**: `./scripts/automated-cleanup.sh --status`

---

**Quick tip**: Bookmark this file for fast reference during emergencies!
