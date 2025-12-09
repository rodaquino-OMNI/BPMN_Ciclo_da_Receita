# Deployment Guide Overview

## Introduction

This deployment guide provides comprehensive instructions for deploying the Hospital Revenue Cycle BPMN system in production environments.

## Deployment Architecture

### Target Environment

```
┌─────────────────────────────────────────────────────────────────┐
│                        Load Balancer                             │
│                    (NGINX / AWS ALB)                             │
└────────────┬────────────────────────────────────┬────────────────┘
             │                                     │
    ┌────────▼─────────┐                 ┌────────▼─────────┐
    │  Camunda Node 1   │                 │  Camunda Node 2   │
    │   (Primary)       │◄───────────────►│   (Secondary)     │
    └────────┬──────────┘                 └────────┬──────────┘
             │                                      │
             └──────────────┬───────────────────────┘
                            │
                   ┌────────▼─────────┐
                   │   PostgreSQL     │
                   │    (Primary)     │
                   │                  │
                   │   + Read Replica │
                   └──────────────────┘
```

### Component Overview

| Component | Purpose | Technology | Replicas |
|-----------|---------|------------|----------|
| Camunda Platform | Process engine | Java 17, Spring Boot | 2+ |
| PostgreSQL | Process data | PostgreSQL 14+ | 1 primary + 1 replica |
| Redis | Cache & session | Redis 7+ | 1 cluster |
| RabbitMQ | Message queue | RabbitMQ 3.12+ | 1 cluster |
| External Task Workers | Async processing | Java/Node.js | 5+ |
| API Gateway | Load balancing | NGINX/Kong | 2+ |

## Deployment Models

### 1. On-Premise Deployment

**Use Case**: Organizations with strict data residency requirements

**Characteristics**:
- Full control over infrastructure
- Dedicated hardware
- Private network
- Custom security controls

**Minimum Requirements**:
- 4 core CPU per Camunda node
- 16 GB RAM per node
- 500 GB SSD storage
- 1 Gbps network

### 2. Cloud Deployment (AWS)

**Use Case**: Scalable, managed infrastructure

**Services Used**:
- **EKS**: Kubernetes for container orchestration
- **RDS**: Managed PostgreSQL
- **ElastiCache**: Managed Redis
- **AmazonMQ**: Managed RabbitMQ
- **ALB**: Application Load Balancer
- **S3**: Document storage
- **CloudWatch**: Monitoring

### 3. Hybrid Deployment

**Use Case**: Sensitive data on-premise, processing in cloud

**Characteristics**:
- Database on-premise
- Camunda in cloud
- VPN/Direct Connect
- Data encryption in transit

## Prerequisites

### Software Requirements

| Software | Version | Purpose |
|----------|---------|---------|
| Java | 17+ | Runtime environment |
| Docker | 24+ | Containerization |
| Kubernetes | 1.28+ | Orchestration |
| PostgreSQL | 14+ | Database |
| Redis | 7+ | Caching |
| RabbitMQ | 3.12+ | Messaging |
| NGINX | 1.24+ | Load balancing |

### Access Requirements

- Database admin credentials
- Kubernetes cluster access
- Container registry access
- SSL certificates
- TASY API credentials
- Insurance portal credentials
- RPA platform access
- AI/ML service API keys

## Deployment Process Overview

### Phase 1: Infrastructure Setup (Week 1)

1. **Provision Infrastructure**
   - Kubernetes cluster
   - Database instances
   - Cache clusters
   - Storage volumes

2. **Network Configuration**
   - VPC setup
   - Security groups
   - Load balancers
   - DNS configuration

3. **Security Setup**
   - SSL certificates
   - Secrets management
   - IAM roles
   - Network policies

### Phase 2: Database Setup (Week 1-2)

1. **Database Installation**
   - Install PostgreSQL
   - Configure replication
   - Create databases
   - Set up backups

2. **Schema Deployment**
   - Camunda schema
   - Application schema
   - Indexes and constraints
   - Initial data load

### Phase 3: Application Deployment (Week 2-3)

1. **Build Docker Images**
   - Camunda application
   - External task workers
   - Custom delegates
   - Integration services

2. **Deploy to Kubernetes**
   - Apply configurations
   - Deploy services
   - Set up ingress
   - Configure monitoring

3. **BPMN Deployment**
   - Deploy process definitions
   - Deploy DMN tables
   - Deploy forms
   - Configure job executor

### Phase 4: Integration Configuration (Week 3-4)

1. **External Systems**
   - TASY integration
   - RPA platform
   - AI/ML services
   - Banking APIs
   - Insurance portals

2. **Testing**
   - Integration tests
   - Load tests
   - Security tests
   - User acceptance testing

### Phase 5: Go-Live (Week 4)

1. **Pre-Production**
   - Data migration
   - Smoke tests
   - Performance validation
   - Security audit

2. **Production Cutover**
   - Blue-green deployment
   - Traffic routing
   - Monitoring activation
   - Support readiness

## Environment Configuration

### Development Environment

```yaml
environment: development
replicas: 1
resources:
  camunda:
    cpu: 2
    memory: 8Gi
  postgres:
    cpu: 1
    memory: 4Gi
features:
  debugging: enabled
  hot-reload: enabled
  mock-integrations: enabled
```

### Staging Environment

```yaml
environment: staging
replicas: 2
resources:
  camunda:
    cpu: 4
    memory: 16Gi
  postgres:
    cpu: 2
    memory: 8Gi
features:
  debugging: enabled
  real-integrations: enabled
  performance-testing: enabled
```

### Production Environment

```yaml
environment: production
replicas: 3
resources:
  camunda:
    cpu: 8
    memory: 32Gi
  postgres:
    cpu: 4
    memory: 16Gi
features:
  debugging: disabled
  high-availability: enabled
  auto-scaling: enabled
  disaster-recovery: enabled
```

## Configuration Management

### Environment Variables

```bash
# Camunda Configuration
CAMUNDA_BPM_DATABASE_TYPE=postgres
CAMUNDA_BPM_DATABASE_SCHEMA_UPDATE=false
CAMUNDA_BPM_HISTORY_LEVEL=FULL
CAMUNDA_BPM_HISTORY_TIME_TO_LIVE=P365D

# Database Configuration
DB_HOST=postgres-primary.internal
DB_PORT=5432
DB_NAME=camunda
DB_USERNAME=camunda_user
DB_PASSWORD=${DB_PASSWORD_SECRET}

# Redis Configuration
REDIS_HOST=redis-cluster.internal
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD_SECRET}

# Integration Configuration
TASY_API_URL=https://tasy-api.hospital.local
TASY_API_KEY=${TASY_API_KEY_SECRET}
RPA_API_URL=https://rpa-platform.hospital.local
AI_SERVICE_URL=https://ai-services.hospital.local
AI_API_KEY=${AI_API_KEY_SECRET}
```

### Secrets Management

Use Kubernetes secrets or HashiCorp Vault:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: camunda-secrets
type: Opaque
data:
  db-password: <base64-encoded>
  tasy-api-key: <base64-encoded>
  ai-api-key: <base64-encoded>
```

## Deployment Checklist

### Pre-Deployment

- [ ] Infrastructure provisioned
- [ ] Network configured
- [ ] Security policies applied
- [ ] Database installed and configured
- [ ] Application images built
- [ ] Configuration files prepared
- [ ] Secrets created
- [ ] SSL certificates installed
- [ ] DNS records created
- [ ] Monitoring tools configured

### Deployment

- [ ] Database schema deployed
- [ ] Application deployed to Kubernetes
- [ ] BPMN processes deployed
- [ ] External workers started
- [ ] Load balancer configured
- [ ] Health checks passing
- [ ] Integration tests passed
- [ ] Smoke tests passed

### Post-Deployment

- [ ] Monitoring dashboards active
- [ ] Alerts configured
- [ ] Backup jobs scheduled
- [ ] Documentation updated
- [ ] Team trained
- [ ] Support procedures established
- [ ] Rollback plan tested

## Rollback Procedures

### Application Rollback

```bash
# Rollback Kubernetes deployment
kubectl rollout undo deployment/camunda-app

# Verify rollback
kubectl rollout status deployment/camunda-app
```

### Database Rollback

```bash
# Restore from backup
pg_restore -h postgres-primary -U camunda_user -d camunda backup_file.dump

# Verify data integrity
psql -h postgres-primary -U camunda_user -d camunda -c "SELECT count(*) FROM act_ru_execution;"
```

## Health Checks

### Application Health

```bash
# Health endpoint
curl https://camunda.hospital.com/actuator/health

# Expected response
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "processEngine": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### Process Engine Health

```bash
# Check active process instances
curl -u demo:demo https://camunda.hospital.com/engine-rest/process-instance/count

# Check job executor
curl -u demo:demo https://camunda.hospital.com/engine-rest/metrics
```

## Monitoring and Alerting

### Key Metrics

- **Process Metrics**: Instance count, duration, failure rate
- **System Metrics**: CPU, memory, disk, network
- **Database Metrics**: Connections, query time, deadlocks
- **Integration Metrics**: API response time, error rate

### Alert Rules

```yaml
alerts:
  - name: HighProcessFailureRate
    condition: failure_rate > 5%
    severity: critical

  - name: DatabaseConnectionPoolExhausted
    condition: active_connections > 90%
    severity: warning

  - name: HighResponseTime
    condition: p95_response_time > 5s
    severity: warning
```

## Backup and Disaster Recovery

### Backup Strategy

```yaml
backups:
  database:
    frequency: daily
    retention: 30 days
    type: full + incremental

  bpmn-definitions:
    frequency: on-change
    retention: all versions
    storage: git repository

  configuration:
    frequency: on-change
    retention: all versions
    storage: configuration management
```

### Recovery Procedures

See [Backup and Recovery](../operations/04_Backup_Recovery.md) for detailed procedures.

## Performance Tuning

### Database Optimization

```sql
-- Create indexes
CREATE INDEX idx_act_ru_job_execution ON act_ru_job(execution_id_);
CREATE INDEX idx_act_hi_procinst_business_key ON act_hi_procinst(business_key_);

-- Vacuum and analyze
VACUUM ANALYZE;
```

### JVM Tuning

```bash
JAVA_OPTS="
  -Xms16g
  -Xmx16g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+ParallelRefProcEnabled
"
```

### Camunda Optimization

```yaml
camunda:
  bpm:
    generic-properties:
      properties:
        historyCleanupBatchSize: 500
        historyCleanupDegreeOfParallelism: 4
        jobExecutorAcquireByDueDate: true
        jobExecutorPreferTimerJobs: true
```

## Security Hardening

### Network Security

- Enable TLS 1.3 for all communications
- Configure firewall rules
- Implement network segmentation
- Use VPN for remote access

### Application Security

- Enable authentication and authorization
- Implement rate limiting
- Configure CORS policies
- Enable audit logging
- Encrypt sensitive data

### Database Security

- Use encrypted connections
- Implement row-level security
- Enable audit logging
- Regular security updates

## Support and Escalation

### Support Tiers

| Tier | Response Time | Availability | Issues |
|------|---------------|--------------|--------|
| L1 | 15 minutes | 24/7 | User support, basic troubleshooting |
| L2 | 1 hour | Business hours | Technical issues, configuration |
| L3 | 4 hours | On-call | Complex issues, architecture |

### Escalation Path

1. **L1 Support** → User-reported issues
2. **L2 Support** → Technical analysis
3. **L3 Support** → Deep technical issues
4. **Vendor Support** → Platform issues

---

**Next**: [Environment Setup](./01_Environment_Setup.md)

**Last Updated**: 2025-12-08
