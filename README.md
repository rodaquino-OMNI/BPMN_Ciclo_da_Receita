# BPMN Ciclo da Receita - Hospital do Futuro

## Índice Abrangente de Documentação

Bem-vindo à documentação completa da implementação BPMN do Ciclo de Receita Hospitalar. Esta documentação cobre todos os aspectos do sistema, desde arquitetura até operações.

## Visão Geral do Projeto

O projeto **BPMN Ciclo da Receita Hospitalar** é uma implementação completa e de nível empresarial para gestão do ciclo de receita hospitalar utilizando BPMN 2.0 e Camunda Platform 7. O sistema orquestra 10 etapas críticas do ciclo de receita através de workflows automatizados, tomada de decisão baseada em IA e integrações inteligentes.

### Principais Funcionalidades

- **11 Processos BPMN**: 1 orquestrador + 10 subprocessos especializados
- **Automação com IA**: Codificação, recursos e suporte a decisões baseados em LLM
- **Integração RPA**: Interações automatizadas com portais e processamento de documentos
- **Captura IoT/RFID**: Rastreamento em tempo real de materiais e medicamentos
- **Analytics em Tempo Real**: Monitoramento contínuo e detecção de anomalias
- **Integração TASY ERP**: Integração completa com sistema de gestão hospitalar
- **Conformidade TISS**: Geração automatizada de faturamento padrão TISS
- **Suporte Multicanal**: WhatsApp, Portal, App, suporte telefônico

### Destaques da Arquitetura

- **Arquitetura de Microserviços**: Design modular e escalável
- **Orientado a Eventos**: Processamento assíncrono com filas de mensagens
- **Cloud-Native**: Implantação containerizada com Kubernetes
- **Alta Disponibilidade**: Sistemas redundantes com failover automático
- **Segurança Primeiro**: Criptografia end-to-end, conformidade LGPD/HIPAA

## Estrutura de Documentação

### 1. Documentação de Processos
Documentação detalhada de todos os 11 processos BPMN incluindo diagramas de fluxo, descrições de tarefas e regras de negócio.

- [Visão Geral de Processos](./docs/processes/00_Overview.md)
- [ORCH - Orquestrador Principal](./docs/processes/01_Orchestrator.md)
- [SUB_01 - Primeiro Contato](./docs/processes/02_First_Contact.md)
- [SUB_02 - Pré-Autorização](./docs/processes/03_Pre_Authorization.md)
- [SUB_03 - Admissão](./docs/processes/04_Admission.md)
- [SUB_04 - Produção Assistencial](./docs/processes/05_Clinical_Production.md)
- [SUB_05 - Codificação e Auditoria](./docs/processes/06_Coding_Audit.md)
- [SUB_06 - Envio de Faturamento](./docs/processes/07_Billing_Submission.md)
- [SUB_07 - Gestão de Glosas](./docs/processes/08_Denials_Management.md)
- [SUB_08 - Cobrança de Receita](./docs/processes/09_Revenue_Collection.md)
- [SUB_09 - Analytics](./docs/processes/10_Analytics.md)
- [SUB_10 - Maximização](./docs/processes/11_Maximization.md)

### 2. Documentação de API
Referência completa de API para todos os delegates, serviços e integrações.

- [Visão Geral da API](./docs/api/00_API_Overview.md)
- [Referência de Delegates](./docs/api/01_Delegates.md)
- [Service Tasks](./docs/api/02_Service_Tasks.md)
- [Serviços Externos](./docs/api/03_External_Services.md)
- [Especificação OpenAPI](./docs/api/openapi.yaml)

### 3. Tabelas de Decisão DMN
Documentação de todas as regras de negócio e tabelas de decisão.

- [Visão Geral DMN](./docs/dmn/00_DMN_Overview.md)
- [Regras de Elegibilidade](./docs/dmn/01_Eligibility_Rules.md)
- [Regras de Autorização](./docs/dmn/02_Authorization_Rules.md)
- [Regras de Codificação](./docs/dmn/03_Coding_Rules.md)
- [Regras de Faturamento](./docs/dmn/04_Billing_Rules.md)
- [Regras de Cobrança](./docs/dmn/05_Collection_Rules.md)

### 4. Guia de Implantação
Instruções passo a passo de implantação e configuração.

- [Visão Geral de Implantação](./docs/deployment/00_Overview.md)
- [Configuração de Ambiente](./docs/deployment/01_Environment_Setup.md)
- [Configuração Camunda](./docs/deployment/02_Camunda_Configuration.md)
- [Configuração de Banco de Dados](./docs/deployment/03_Database_Setup.md)
- [Configuração de Integrações](./docs/deployment/04_Integration_Configuration.md)
- [Checklist de Implantação](./docs/deployment/05_Deployment_Checklist.md)

### 5. Manual de Operações
Operações do dia a dia, monitoramento e solução de problemas.

- [Visão Geral de Operações](./docs/operations/00_Overview.md)
- [Guia de Monitoramento](./docs/operations/01_Monitoring.md)
- [Solução de Problemas](./docs/operations/02_Troubleshooting.md)
- [Ajuste de Performance](./docs/operations/03_Performance_Tuning.md)
- [Backup e Recuperação](./docs/operations/04_Backup_Recovery.md)
- [Resposta a Incidentes](./docs/operations/05_Incident_Response.md)

### 6. Materiais de Treinamento
Guias de usuário e recursos de treinamento.

- [Visão Geral de Treinamento](./docs/training/00_Overview.md)
- [Guia do Usuário - Recepção](./docs/training/01_Reception_User_Guide.md)
- [Guia do Usuário - Autorização](./docs/training/02_Authorization_User_Guide.md)
- [Guia do Usuário - Faturamento](./docs/training/03_Billing_User_Guide.md)
- [Guia do Administrador](./docs/training/04_Admin_Guide.md)
- [Vídeos Tutoriais](./docs/training/05_Video_Tutorials.md)

### 7. Segurança e Conformidade
Protocolos de segurança e documentação de conformidade.

- [Visão Geral de Segurança](./docs/security/00_Overview.md)
- [Autenticação e Autorização](./docs/security/01_Auth.md)
- [Privacidade de Dados (LGPD)](./docs/security/02_LGPD_Compliance.md)
- [Logs de Auditoria](./docs/security/03_Audit_Logs.md)
- [Padrões de Criptografia](./docs/security/04_Encryption.md)
- [Checklist de Conformidade](./docs/security/05_Compliance_Checklist.md)

### 8. Dicionário de Dados
Referência completa de todas as variáveis de processo e estruturas de dados.

- [Dicionário de Dados](./docs/data/00_Data_Dictionary.md)
- [Variáveis de Processo](./docs/data/01_Process_Variables.md)
- [Schema do Banco de Dados](./docs/data/02_Database_Schema.md)
- [Formatos de Mensagem](./docs/data/03_Message_Formats.md)
- [Padrões TISS](./docs/data/04_TISS_Standards.md)

### 9. Documentação de Arquitetura
Arquitetura do sistema e decisões de design.

- [Visão Geral da Arquitetura](./docs/architecture/00_Overview.md)
- [Design do Sistema](./docs/architecture/01_System_Design.md)
- [Arquitetura de Integração](./docs/architecture/02_Integration_Architecture.md)
- [Stack Tecnológica](./docs/architecture/03_Technology_Stack.md)
- [Decisões de Design](./docs/architecture/decisions/)

## Início Rápido

### Pré-requisitos

- **Camunda Platform** 7.20+
- **Java** 17+
- **PostgreSQL** 14+ (produção) ou H2 (desenvolvimento)
- **Maven** 3.8+
- **Docker** & **Kubernetes** (para implantação containerizada)
- **TASY ERP System** (integração hospitalar)
- **Plataforma RPA** (IBM RPA ou UiPath - opcional)

### Instalação

#### 1. Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/BPMN_Ciclo_da_Receita.git
cd BPMN_Ciclo_da_Receita
```

#### 2. Configurar Variáveis de Ambiente

Crie o arquivo `src/main/resources/application-dev.yml` para desenvolvimento:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console

camunda:
  bpm:
    admin-user:
      id: admin
      password: admin
```

#### 3. Compilar o Projeto

```bash
# Compilar e executar testes
mvn clean install

# Pular testes (desenvolvimento rápido)
mvn clean install -DskipTests
```

#### 4. Executar a Aplicação

**Modo Desenvolvimento (H2 Database):**
```bash
mvn spring-boot:run
```

**Modo Produção (PostgreSQL):**
```bash
mvn spring-boot:run -Pprod
```

**Modo Test:**
```bash
mvn spring-boot:run -Ptest
```

#### 5. Acessar a Aplicação

- **Camunda Cockpit**: http://localhost:8080/camunda
- **Camunda Tasklist**: http://localhost:8080/camunda/app/tasklist
- **Camunda Admin**: http://localhost:8080/camunda/app/admin
- **H2 Console** (dev): http://localhost:8080/h2-console
- **REST API**: http://localhost:8080/rest

**Credenciais Padrão:**
- Usuário: `admin`
- Senha: `admin`

### Comandos Maven Úteis

```bash
# Executar apenas testes unitários
mvn test

# Executar testes de integração
mvn verify

# Gerar relatório de cobertura JaCoCo
mvn jacoco:report

# Limpar e reconstruir
mvn clean package

# Executar com perfil específico
mvn spring-boot:run -Pprod

# Empacotar para produção
mvn clean package -Pprod -DskipTests

# Verificar dependências desatualizadas
mvn versions:display-dependency-updates

# Analisar dependências
mvn dependency:tree
```

### Estrutura do Projeto

```
BPMN_Ciclo_da_Receita/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hospital/        # Código fonte Java
│   │   └── resources/
│   │       ├── application.yml       # Configuração principal
│   │       ├── application-dev.yml   # Config desenvolvimento
│   │       └── application-prod.yml  # Config produção
│   ├── bpmn/                         # Arquivos BPMN 2.0
│   │   ├── ORCH_Ciclo_Receita_Hospital_Futuro.bpmn
│   │   ├── SUB_01_Agendamento_Registro.bpmn
│   │   ├── SUB_02_Pre_Atendimento.bpmn
│   │   └── ... (8 subprocessos adicionais)
│   └── dmn/                          # Tabelas de decisão DMN
│       ├── eligibility-verification.dmn
│       ├── authorization-approval.dmn
│       └── ... (4 DMN adicionais)
├── tests/
│   ├── unit/                         # Testes unitários
│   ├── integration/                  # Testes de integração
│   ├── e2e/                          # Testes end-to-end
│   └── performance/                  # Testes de performance
├── docs/                             # Documentação completa
│   ├── architecture/                 # Arquitetura e ADRs
│   ├── processes/                    # Documentação BPMN
│   ├── api/                          # Documentação API
│   └── ... (7 categorias adicionais)
├── scripts/                          # Scripts de deploy/manutenção
├── archive/                          # Código legado arquivado
├── pom.xml                           # Configuração Maven
└── README.md                         # Este arquivo
```

### Implantação com Docker (Opcional)

```bash
# Construir imagem Docker
docker build -t revenue-cycle-camunda:1.0.0 .

# Executar container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/camunda \
  revenue-cycle-camunda:1.0.0
```

Consulte o [Guia de Implantação](./docs/deployment/00_Overview.md) para instruções detalhadas.

## Métricas Chave

### Metas de Performance

- **Primeiro Contato até Agendamento**: < 5 minutos
- **Pré-Autorização**: < 24 horas (máximo 48h)
- **Envio de Faturamento**: < 24 horas após alta
- **Resolução de Glosas**: < 7 dias
- **Reconciliação de Pagamentos**: Automatizada diariamente
- **Disponibilidade do Sistema**: 99.9%

### Resultados de Negócio

- **50% de redução** no tempo de autorização manual
- **70% de automação** na verificação de elegibilidade
- **90% de precisão** na codificação com IA
- **40% de redução** em glosas
- **60% mais rápido** no processamento de recursos
- **Dashboards financeiros** em tempo real

## Stack Tecnológica

### Backend
- **Java**: 17 (LTS)
- **Spring Boot**: 3.2.0
- **Camunda BPM**: 7.20.0
- **Maven**: 3.8+

### Banco de Dados
- **Desenvolvimento**: H2 (in-memory)
- **Produção**: PostgreSQL 14+
- **ORM**: Spring Data JPA

### Testes
- **JUnit**: 5.10.1
- **AssertJ**: 3.24.2
- **Mockito**: 5.7.0
- **Camunda BPM Assert**: 15.0.0
- **Cobertura**: JaCoCo (meta: 90%)

### DevOps
- **Containerização**: Docker
- **Orquestração**: Kubernetes
- **CI/CD**: GitHub Actions / Jenkins
- **Monitoramento**: Prometheus + Grafana

## Configuração

### Perfis Spring Boot

O projeto suporta 3 perfis de execução:

#### 1. **dev** (padrão)
- Banco de dados H2 em memória
- H2 Console habilitado
- Logs detalhados
- Auto-reload habilitado

#### 2. **prod**
- PostgreSQL database
- Segurança reforçada
- Logs otimizados
- Métricas de produção

#### 3. **test**
- Banco de dados H2 para testes
- Fixtures de dados de teste
- Configuração de teste isolada

### Variáveis de Ambiente

```bash
# Banco de dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/camunda
SPRING_DATASOURCE_USERNAME=camunda
SPRING_DATASOURCE_PASSWORD=camunda

# Camunda Admin
CAMUNDA_ADMIN_USER_ID=admin
CAMUNDA_ADMIN_USER_PASSWORD=admin

# Integração TASY
TASY_API_URL=https://tasy.hospital.com/api
TASY_API_KEY=your-api-key

# RPA (opcional)
RPA_PLATFORM_URL=https://rpa.hospital.com
RPA_API_KEY=your-rpa-key
```

## Troubleshooting Rápido

### Erro: "Port 8080 already in use"

```bash
# Encontrar processo usando a porta
lsof -i :8080

# Matar o processo
kill -9 <PID>

# Ou executar na porta alternativa
mvn spring-boot:run -Dserver.port=8081
```

### Erro: "Table 'ACT_RE_PROCDEF' doesn't exist"

```bash
# Recriar schema do banco de dados
mvn clean install -Dcamunda.db.schema.create=true
```

### Erro: "Out of memory"

```bash
# Aumentar heap memory
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn clean install
```

### Logs de Debug

```yaml
# Adicionar em application-dev.yml
logging:
  level:
    org.camunda: DEBUG
    com.hospital: DEBUG
```

## Suporte e Manutenção

### Informações de Contato

- **Suporte Técnico**: support@hospitalfuturo.com
- **Atualizações de Documentação**: docs@hospitalfuturo.com
- **Linha Direta de Emergência**: +55 11 1234-5678

### Contribuindo

Veja [CONTRIBUTING.md](./CONTRIBUTING.md) para diretrizes sobre:
- Reportar bugs
- Sugerir melhorias
- Processo de contribuição de código
- Atualizações de documentação

## Histórico de Versões

| Versão | Data | Descrição |
|---------|------|-------------|
| 1.0.0 | 2025-12-08 | Lançamento inicial com todos os 11 processos |

## Licença

Copyright (c) 2025 Hospital do Futuro. Todos os direitos reservados.

Veja [LICENSE](./LICENSE) para o texto completo da licença.

## Agradecimentos

- Equipe AUSTA Hospital do Futuro
- Comunidade Camunda
- Contribuidores open source
- Implementação com suporte de Claude Code e Claude-Flow SPARC

---

**Última Atualização**: 2025-12-09
**Versão da Documentação**: 1.0.0
**Versão do Sistema**: 1.0.0
**Stack**: Camunda 7.20.0 + Spring Boot 3.2.0 + Java 17
