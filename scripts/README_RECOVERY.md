# Maven Dependency Recovery Scripts

## Purpose

These scripts provide intelligent recovery logic to fix Maven dependency resolution failures, specifically targeting issues with Camunda BPM dependencies.

## Problem Addressed

**Error**: `Missing: /Users/rodrigo/.m2/repository/org/camunda/bpm/camunda-bpmn-model/7.20.0/camunda-bpmn-model-7.20.0.jar`

This error typically occurs due to:
- Corrupted Maven metadata (`.lastUpdated` files)
- Failed dependency downloads
- Repository synchronization issues
- Network interruptions during dependency resolution
- Cached failed resolution attempts

## Scripts Available

### 1. `maven-recovery.sh` (Linux/macOS)
Full-featured recovery script with:
- Corrupted metadata detection and cleanup
- Intelligent dependency purging
- JAR integrity validation
- Retry logic with exponential backoff
- IDE project refresh support
- Comprehensive logging

### 2. `maven-recovery.bat` (Windows)
Windows-compatible version with:
- Core recovery functionality
- Metadata cleanup
- Dependency purging
- Build verification
- Detailed logging

## Usage

### Linux/macOS

```bash
# Make script executable
chmod +x scripts/maven-recovery.sh

# Run recovery
./scripts/maven-recovery.sh
```

### Windows

```cmd
# Run recovery
scripts\maven-recovery.bat
```

## Recovery Process

The scripts execute the following steps in sequence:

### Step 1: Pre-flight Checks
- Verify Maven installation
- Check internet connectivity to Maven Central and Camunda repositories
- Initialize logging

### Step 2: Clean Corrupted Metadata
- Remove all `.lastUpdated` files from Camunda directory
- Clean `.repositories` tracking files
- Remove failed resolution markers

### Step 3: Purge Problematic Dependencies
Target dependencies:
- `org.camunda.bpm:camunda-bpmn-model`
- `org.camunda.bpm:camunda-engine`
- `org.camunda.commons:camunda-commons-logging`
- `org.camunda.commons:camunda-commons-typed-values`
- `org.camunda.commons:camunda-commons-utils`
- `org.camunda.bpm.model:camunda-cmmn-model`
- `org.camunda.bpm.model:camunda-dmn-model`
- `org.camunda.bpm.model:camunda-xml-model`

### Step 4: Clean Build Cache
- Remove `target/` directory
- Clean Maven wrapper cache

### Step 5: Force Dependency Update
- Execute `mvn clean compile -U` (force update from remote)
- Retry up to 3 times with exponential backoff (5s, 10s, 15s)
- Fallback to direct dependency download if needed

### Step 6: Verify Resolution
- Run `mvn dependency:resolve` to verify all dependencies
- Report any remaining issues

### Step 7: Validate JAR Files
For each critical JAR:
- Check file exists
- Verify file size > 0 bytes
- Validate JAR structure (ZIP archive integrity)
- Test JAR can be read by `jar` tool

### Step 8: Refresh IDE Configuration
- **IntelliJ IDEA**: Regenerate `.idea` configuration
- **Eclipse**: Regenerate `.project` and `.classpath`

## Logging

All recovery operations are logged to:
```
logs/maven-recovery-YYYYMMDD_HHMMSS.log
```

Log includes:
- Timestamp for each operation
- Success/failure status
- Error messages and stack traces
- Validation results
- Recovery metrics

## Return Codes

| Code | Meaning |
|------|---------|
| 0    | Recovery successful |
| 1    | Maven not installed or recovery failed |

## Advanced Features

### Retry Logic
- Automatic retry on failure (max 3 attempts)
- Exponential backoff: 5s → 10s → 15s
- Detailed failure logging for debugging

### JAR Validation
```bash
validate_jar() {
    # Check existence
    # Verify file size > 0
    # Validate JAR structure (ZIP)
    # Test with jar tool
}
```

### Internet Connectivity Check
- Verify access to Maven Central
- Verify access to Camunda repository
- Continue with warnings if offline

### IDE Integration
- Automatic detection of IntelliJ IDEA
- Automatic detection of Eclipse
- Regenerate project configuration files

## Troubleshooting

### If Recovery Fails

1. **Check Maven Installation**
   ```bash
   mvn -version
   ```

2. **Verify Internet Connectivity**
   ```bash
   curl -I https://artifacts.camunda.com/artifactory/camunda-bpm/
   ```

3. **Manual Dependency Download**
   ```bash
   mvn dependency:get \
     -Dartifact=org.camunda.bpm:camunda-bpmn-model:7.20.0 \
     -DremoteRepositories=https://artifacts.camunda.com/artifactory/camunda-bpm/
   ```

4. **Check Log File**
   Review the detailed log at `logs/maven-recovery-*.log`

5. **Nuclear Option: Complete Repository Purge**
   ```bash
   rm -rf ~/.m2/repository/org/camunda
   ./scripts/maven-recovery.sh
   ```

### Common Issues

**Issue**: "Cannot reach Camunda repository"
- **Solution**: Check network proxy settings, VPN, or firewall

**Issue**: "JAR file is corrupted"
- **Solution**: Delete specific JAR and re-run recovery

**Issue**: "Maven build still fails after recovery"
- **Solution**: Check POM.xml for repository configuration

## Integration with CI/CD

The recovery script can be integrated into build pipelines:

```yaml
# GitHub Actions example
- name: Maven Dependency Recovery
  run: |
    chmod +x scripts/maven-recovery.sh
    ./scripts/maven-recovery.sh
  if: failure()
```

## Technical Details

### Dependencies Managed
- Camunda BPM 7.20.0
- Camunda Commons libraries
- Camunda Model APIs (BPMN, CMMN, DMN, XML)

### Maven Commands Used
- `mvn dependency:purge-local-repository` - Remove specific dependencies
- `mvn clean compile -U` - Force update from remote
- `mvn dependency:resolve` - Verify resolution
- `mvn dependency:get` - Download specific artifact

### Validation Strategy
1. File existence check
2. File size validation
3. ZIP archive structure test
4. JAR tool verification

## Performance Metrics

Typical recovery time:
- Fast path (metadata cleanup only): ~30 seconds
- Full recovery (with re-download): 2-5 minutes
- Worst case (multiple retries): ~10 minutes

## Support

For issues or questions:
1. Check the log file at `logs/maven-recovery-*.log`
2. Review Maven error messages
3. Verify network connectivity
4. Check Camunda repository status: https://artifacts.camunda.com/

## Related Documentation

- [Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/)
- [Camunda BPM Platform](https://docs.camunda.org/)
- [Maven Repository Management](https://maven.apache.org/guides/introduction/introduction-to-repositories.html)
