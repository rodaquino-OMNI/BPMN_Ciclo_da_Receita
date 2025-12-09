# 🚀 Deployment Instructions - Hospital Revenue Cycle

## Quick Start (3 Steps)

### 1️⃣ Verify Prerequisites
```bash
# Check Java version (17+ required)
java -version
# Expected output: openjdk version "17.0.17" or higher
```

### 2️⃣ Run the Application
```bash
# Development mode (H2 in-memory database)
java -jar target/revenue-cycle-camunda-1.0.0.jar
```

### 3️⃣ Access Camunda Cockpit
```bash
# Open browser
http://localhost:8080/revenue-cycle/app/cockpit

# Login credentials (dev):
Username: admin
Password: admin
```

---

## 📋 Detailed Deployment Options

### Option A: Development (Local H2 Database)
```bash
# Start with dev profile (default)
java -jar target/revenue-cycle-camunda-1.0.0.jar

# Application will be available at:
# - Camunda Cockpit: http://localhost:8080/revenue-cycle/app/cockpit
# - Camunda Admin: http://localhost:8080/revenue-cycle/app/admin
# - Camunda Tasklist: http://localhost:8080/revenue-cycle/app/tasklist
# - REST API: http://localhost:8080/revenue-cycle/rest
# - H2 Console: http://localhost:8080/revenue-cycle/h2-console
```

### Option B: Production (PostgreSQL Database)
```bash
# Set environment variables
export DATABASE_URL="jdbc:postgresql://db-host:5432/revenue_cycle"
export DATABASE_USER="app_user"
export DATABASE_PASSWORD="your_secure_password"

# Run with production profile
java -jar target/revenue-cycle-camunda-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080 \
  --camunda.bpm.admin-user.id=admin \
  --camunda.bpm.admin-user.password=ChangeMeInProduction
```

### Option C: Docker Container
```dockerfile
# Dockerfile (create in project root)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/revenue-cycle-camunda-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build Docker image
docker build -t hospital-revenue-cycle:1.0.0 .

# Run container
docker run -d \
  --name revenue-cycle \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=${DATABASE_URL} \
  -e DATABASE_USER=${DATABASE_USER} \
  -e DATABASE_PASSWORD=${DATABASE_PASSWORD} \
  hospital-revenue-cycle:1.0.0

# View logs
docker logs -f revenue-cycle
```

### Option D: Kubernetes Deployment
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: revenue-cycle
spec:
  replicas: 2
  selector:
    matchLabels:
      app: revenue-cycle
  template:
    metadata:
      labels:
        app: revenue-cycle
    spec:
      containers:
      - name: app
        image: hospital-revenue-cycle:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        livenessProbe:
          httpGet:
            path: /revenue-cycle/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: revenue-cycle-service
spec:
  selector:
    app: revenue-cycle
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

```bash
# Deploy to Kubernetes
kubectl apply -f deployment.yaml

# Check deployment status
kubectl get pods -l app=revenue-cycle

# View application logs
kubectl logs -f deployment/revenue-cycle
```

---

## 🔧 Configuration

### Environment Variables
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | No | `dev` | Profile: dev/test/prod |
| `SERVER_PORT` | No | `8080` | HTTP port |
| `DATABASE_URL` | Prod | H2 | JDBC connection URL |
| `DATABASE_USER` | Prod | `sa` | Database username |
| `DATABASE_PASSWORD` | Prod | `sa` | Database password |

### Application Properties (Production)
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create schema in prod
    show-sql: false

camunda:
  bpm:
    admin-user:
      id: ${CAMUNDA_ADMIN_USER:admin}
      password: ${CAMUNDA_ADMIN_PASSWORD:ChangeMe}
    authorization:
      enabled: true
    history-level: full
    job-execution:
      core-pool-size: 5
      max-pool-size: 20

logging:
  level:
    root: WARN
    com.hospital: INFO
    org.camunda: WARN
  file:
    name: /var/log/revenue-cycle/application.log
```

---

## ✅ Verification Steps

### 1. Check Application Health
```bash
# Health endpoint
curl http://localhost:8080/revenue-cycle/actuator/health

# Expected response:
# {"status":"UP"}
```

### 2. Verify BPMN Deployment
```bash
# List deployed process definitions
curl http://localhost:8080/revenue-cycle/rest/process-definition

# Expected: 11 process definitions
# - MAIN_HospitalRevenueCycle
# - SUB01_FirstContact
# - SUB02_PreAttendance
# ... (and 8 more)
```

### 3. Verify DMN Deployment
```bash
# List deployed decision definitions
curl http://localhost:8080/revenue-cycle/rest/decision-definition

# Expected: 6 decision definitions
# - EligibilityVerification
# - ReimbursementCalculation
# - GlosaClassification
# - PaymentAllocation
# - ProvisionRules
# - RecoveryPriority
```

### 4. Start a Test Process Instance
```bash
# Start main process
curl -X POST http://localhost:8080/revenue-cycle/rest/process-definition/key/MAIN_HospitalRevenueCycle/start \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "patientId": {"value": "12345", "type": "String"},
      "insuranceProvider": {"value": "123456", "type": "String"},
      "procedureCode": {"value": "TUSS001", "type": "String"}
    }
  }'

# Expected: Process instance created with ID
```

---

## 🔍 Monitoring

### Application Logs
```bash
# View real-time logs
tail -f logs/revenue-cycle.log

# Filter by level
grep ERROR logs/revenue-cycle.log

# Camunda engine logs
grep "org.camunda" logs/revenue-cycle.log
```

### Prometheus Metrics
```bash
# Metrics endpoint
curl http://localhost:8080/revenue-cycle/actuator/prometheus

# Key metrics:
# - process_engine_job_acquisition_duration_seconds
# - process_engine_root_process_instances_total
# - jvm_memory_used_bytes
# - http_server_requests_seconds
```

### Database Monitoring
```sql
-- Active process instances
SELECT COUNT(*) FROM ACT_RU_EXECUTION WHERE PARENT_ID_ IS NULL;

-- Completed process instances (last 24h)
SELECT COUNT(*) FROM ACT_HI_PROCINST
WHERE END_TIME_ >= NOW() - INTERVAL '24 hours';

-- Failed jobs
SELECT * FROM ACT_RU_JOB WHERE RETRIES_ = 0;
```

---

## 🚨 Troubleshooting

### Issue: Application won't start
```bash
# Check Java version
java -version  # Must be 17+

# Check port availability
lsof -i :8080

# View detailed logs
java -jar revenue-cycle-camunda-1.0.0.jar --debug
```

### Issue: Database connection failed
```bash
# Test database connectivity
psql -h db-host -U app_user -d revenue_cycle

# Check database URL format
# Correct: jdbc:postgresql://host:5432/database
# Wrong: postgresql://host:5432/database (missing jdbc:)
```

### Issue: BPMN processes not deployed
```bash
# Verify BPMN files in JAR
jar tf revenue-cycle-camunda-1.0.0.jar | grep .bpmn

# Check deployment resources
curl http://localhost:8080/revenue-cycle/rest/deployment

# Force redeployment
# Delete old deployments via Cockpit, restart application
```

### Issue: OutOfMemoryError
```bash
# Increase heap size
java -Xmx2g -Xms1g -jar revenue-cycle-camunda-1.0.0.jar
```

---

## 📊 Performance Tuning

### JVM Options (Production)
```bash
java \
  -Xms2g \                              # Initial heap
  -Xmx4g \                              # Maximum heap
  -XX:+UseG1GC \                        # G1 garbage collector
  -XX:MaxGCPauseMillis=200 \            # Max GC pause
  -XX:+HeapDumpOnOutOfMemoryError \     # Dump on OOM
  -XX:HeapDumpPath=/var/log/heap \      # Dump location
  -Dserver.tomcat.max-threads=200 \     # Max HTTP threads
  -Dserver.tomcat.accept-count=100 \    # Connection queue
  -jar revenue-cycle-camunda-1.0.0.jar
```

### Camunda Job Executor Tuning
```yaml
camunda:
  bpm:
    job-execution:
      core-pool-size: 10     # Core thread count
      max-pool-size: 50      # Max thread count
      queue-capacity: 20     # Job queue size
      lock-time-in-millis: 600000  # 10 minutes
```

---

## 🔐 Security Checklist

- [ ] Change default admin password
- [ ] Enable HTTPS/TLS
- [ ] Configure authentication (LDAP/OAuth2)
- [ ] Enable authorization
- [ ] Restrict REST API access
- [ ] Configure CORS policies
- [ ] Enable audit logging
- [ ] Encrypt database credentials
- [ ] Use secrets management (Vault/AWS Secrets)
- [ ] Regular security updates

---

## 📞 Support Contacts

**Development Team:** Hospital Revenue Cycle Development Team
**Operations:** DevOps Team
**Emergency:** On-call Engineer

**Documentation:**
- API Documentation: `/revenue-cycle/rest/openapi.json`
- Camunda Docs: https://docs.camunda.org/manual/7.20/
- Spring Boot Docs: https://docs.spring.io/spring-boot/

---

**Deployment Version:** 1.0.0
**Last Updated:** 2025-12-09
**Deployment Ready:** ✅ YES
