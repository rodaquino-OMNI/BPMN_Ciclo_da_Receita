# Quick Recovery Guide

## 🚨 Maven Dependency Issues? Run This:

```bash
# One-line fix
./scripts/maven-recovery.sh
```

---

## 📋 Common Maven Problems & Solutions

### Problem: "Missing JAR file in .m2/repository"
```bash
./scripts/maven-recovery.sh
```

### Problem: "Could not resolve dependencies"
```bash
find ~/.m2/repository -name "*.lastUpdated" -delete
mvn clean compile -U
```

### Problem: "Build stuck at downloading"
```bash
# Kill Maven process, then:
rm -rf ~/.m2/repository/org/camunda
./scripts/maven-recovery.sh
```

### Problem: "IDE not recognizing dependencies"
```bash
./scripts/maven-recovery.sh
# Then in your IDE:
# IntelliJ: File → Reload from Disk → Reimport Maven Project
# Eclipse: Project → Update Maven Project (Alt+F5)
```

---

## 🛠️ Manual Recovery Steps

If automated script fails:

### 1. Clean Corrupted Metadata
```bash
find ~/.m2/repository -name "*.lastUpdated" -delete
find ~/.m2/repository -name "*.repositories" -delete
```

### 2. Purge Specific Dependency
```bash
mvn dependency:purge-local-repository \
    -DmanualInclude=org.camunda.bpm:camunda-engine \
    -DreResolve=false
```

### 3. Force Update
```bash
mvn clean compile -U
```

### 4. Verify Resolution
```bash
mvn dependency:resolve
mvn dependency:tree
```

---

## 📊 Validation Commands

### Check Maven Installation
```bash
mvn -version
```

### List All Dependencies
```bash
mvn dependency:list
```

### Analyze Dependency Tree
```bash
mvn dependency:tree > dependency-tree.txt
```

### Check for Unused Dependencies
```bash
mvn dependency:analyze
```

### Validate POM
```bash
mvn validate
```

---

## 🔍 Diagnostic Commands

### Check Internet Connectivity
```bash
curl -I https://artifacts.camunda.com/artifactory/public/
curl -I https://repo.maven.apache.org/maven2/
```

### View Recovery Logs
```bash
tail -f logs/maven-recovery-*.log
```

### Check Disk Space
```bash
df -h ~/.m2/repository
```

### Check .m2 Repository Size
```bash
du -sh ~/.m2/repository
```

---

## ⚡ Quick Fixes

### Build Failed Once
```bash
mvn clean compile
```

### Build Failed Multiple Times
```bash
./scripts/maven-recovery.sh
```

### IDE Not Syncing
```bash
# IntelliJ
rm -rf .idea/libraries .idea/modules
mvn idea:idea

# Eclipse
rm -f .classpath .project
mvn eclipse:eclipse
```

### Nuclear Option (Last Resort)
```bash
# WARNING: Deletes entire Maven cache (~GB of data)
rm -rf ~/.m2/repository
mvn clean install
```

---

## 📁 Important Files

| File | Purpose |
|------|---------|
| `scripts/maven-recovery.sh` | Automated recovery (Linux/macOS) |
| `scripts/maven-recovery.bat` | Automated recovery (Windows) |
| `scripts/README_RECOVERY.md` | Detailed documentation |
| `logs/maven-recovery-*.log` | Recovery execution logs |
| `pom.xml` | Project dependencies configuration |

---

## 🎯 Best Practices

### Before Building
```bash
# Clean workspace
mvn clean

# Update dependencies
mvn clean compile -U
```

### After Recovery
```bash
# Verify build
mvn clean test

# Package application
mvn package

# Run application
mvn spring-boot:run
```

### Regular Maintenance
```bash
# Weekly cleanup
find ~/.m2/repository -name "*.lastUpdated" -delete

# Monthly deep clean
mvn dependency:purge-local-repository
```

---

## 🆘 When to Use Each Script

### Use `maven-recovery.sh` When:
- ✅ Dependency resolution fails
- ✅ Missing JAR files
- ✅ Corrupted Maven cache
- ✅ Build fails after network interruption
- ✅ IDE not recognizing dependencies

### Manual Recovery When:
- ✅ Need specific dependency purge
- ✅ Debugging a single dependency
- ✅ Testing different Maven commands

### Nuclear Option When:
- ✅ Everything else failed
- ✅ Suspected corrupt .m2 repository
- ✅ Fresh start needed

---

## 📞 Support

### Check Status
```bash
# Build health
mvn clean compile

# Dependency health
mvn dependency:resolve

# Project validation
mvn validate
```

### Get Help
1. Check recovery log: `logs/maven-recovery-*.log`
2. Review detailed docs: `scripts/README_RECOVERY.md`
3. Validate POM: `mvn help:effective-pom`
4. Check Maven settings: `~/.m2/settings.xml`

---

## ✅ Success Indicators

### Build is Healthy When:
- ✅ `mvn clean compile` succeeds
- ✅ `mvn dependency:resolve` shows no errors
- ✅ All JAR files exist in `~/.m2/repository`
- ✅ IDE recognizes all dependencies
- ✅ No red underlines in Java files

### Recovery is Complete When:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

---

**Keep this guide handy for quick reference during development!**
