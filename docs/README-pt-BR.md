# BPMN Ciclo de Receita - Hospital do Futuro

## Índice Abrangente de Documentação

Bem-vindo à documentação completa do sistema BPMN de Ciclo de Receita Hospitalar. Esta documentação cobre todos os aspectos do sistema, desde arquitetura até operações.

## Visão Geral do Projeto

O projeto **Ciclo de Receita Hospitalar BPMN** é uma implementação completa e de nível empresarial do gerenciamento do ciclo de receita hospitalar usando BPMN 2.0 e Camunda Platform 7. O sistema orquestra 10 estágios críticos do ciclo de receita através de workflows automatizados, tomada de decisão baseada em IA e integrações inteligentes.

### Principais Funcionalidades

- **11 Processos BPMN**: 1 orquestrador + 10 subprocessos especializados
- **Automação com IA**: Codificação, recursos e suporte à decisão baseados em LLM
- **Integração RPA**: Interações automatizadas com portais e processamento de documentos
- **Captura IoT/RFID**: Rastreamento em tempo real de materiais e medicações
- **Analytics em Tempo Real**: Monitoramento contínuo e detecção de anomalias
- **Integração TASY ERP**: Integração completa com sistema de gestão hospitalar
- **Conformidade TISS**: Geração automatizada de faturamento no padrão TISS
- **Suporte Multicanal**: WhatsApp, Portal, App, atendimento telefônico

### Destaques da Arquitetura

- **Arquitetura de Microserviços**: Design modular e escalável
- **Orientada a Eventos**: Processamento assíncrono com filas de mensagens
- **Cloud-Native**: Implantação containerizada com Kubernetes
- **Alta Disponibilidade**: Sistemas redundantes com failover automático
- **Segurança em Primeiro Lugar**: Criptografia ponta a ponta, conformidade LGPD/HIPAA

## Estrutura da Documentação

### 1. Documentação de Processos
Documentação detalhada para todos os 11 processos BPMN, incluindo diagramas de fluxo, descrições de tarefas e regras de negócio.

- [Visão Geral dos Processos](./processes/00_Overview.md)
- [ORCH - Orquestrador Principal](./processes/01_Orchestrator.md)
- [SUB_01 - Primeiro Contato](./processes/02_First_Contact.md)
- [SUB_02 - Pré-Autorização](./processes/03_Pre_Authorization.md)
- [SUB_03 - Admissão](./processes/04_Admission.md)
- [SUB_04 - Produção Assistencial](./processes/05_Clinical_Production.md)
- [SUB_05 - Codificação e Auditoria](./processes/06_Coding_Audit.md)
- [SUB_06 - Envio de Faturamento](./processes/07_Billing_Submission.md)
- [SUB_07 - Gestão de Glosas](./processes/08_Denials_Management.md)
- [SUB_08 - Recebimento de Receita](./processes/09_Revenue_Collection.md)
- [SUB_09 - Analytics](./processes/10_Analytics.md)
- [SUB_10 - Maximização](./processes/11_Maximization.md)

### 2. Documentação de API
Referência completa de API para todos os delegates, serviços e integrações.

- [Visão Geral da API](./api/00_API_Overview.md)
- [Referência de Delegates](./api/01_Delegates.md)
- [Service Tasks](./api/02_Service_Tasks.md)
- [Serviços Externos](./api/03_External_Services.md)
- [Especificação OpenAPI](./api/openapi.yaml)

### 3. Tabelas de Decisão DMN
Documentação de todas as regras de negócio e tabelas de decisão.

- [Visão Geral DMN](./dmn/00_DMN_Overview.md)
- [Regras de Elegibilidade](./dmn/01_Eligibility_Rules.md)
- [Regras de Autorização](./dmn/02_Authorization_Rules.md)
- [Regras de Codificação](./dmn/03_Coding_Rules.md)
- [Regras de Faturamento](./dmn/04_Billing_Rules.md)
- [Regras de Cobrança](./dmn/05_Collection_Rules.md)

### 4. Guia de Implantação
Instruções passo a passo de implantação e configuração.

- [Visão Geral da Implantação](./deployment/00_Overview.md)
- [Configuração do Ambiente](./deployment/01_Environment_Setup.md)
- [Configuração do Camunda](./deployment/02_Camunda_Configuration.md)
- [Configuração do Banco de Dados](./deployment/03_Database_Setup.md)
- [Configuração de Integrações](./deployment/04_Integration_Configuration.md)
- [Checklist de Implantação](./deployment/05_Deployment_Checklist.md)

### 5. Manual de Operações
Operações do dia a dia, monitoramento e resolução de problemas.

- [Visão Geral de Operações](./operations/00_Overview.md)
- [Guia de Monitoramento](./operations/01_Monitoring.md)
- [Resolução de Problemas](./operations/02_Troubleshooting.md)
- [Ajuste de Performance](./operations/03_Performance_Tuning.md)
- [Backup e Recuperação](./operations/04_Backup_Recovery.md)
- [Resposta a Incidentes](./operations/05_Incident_Response.md)

### 6. Materiais de Treinamento
Guias do usuário e recursos de treinamento.

- [Visão Geral do Treinamento](./training/00_Overview.md)
- [Guia do Usuário - Recepção](./training/01_Reception_User_Guide.md)
- [Guia do Usuário - Autorização](./training/02_Authorization_User_Guide.md)
- [Guia do Usuário - Faturamento](./training/03_Billing_User_Guide.md)
- [Guia do Administrador](./training/04_Admin_Guide.md)
- [Tutoriais em Vídeo](./training/05_Video_Tutorials.md)

### 7. Segurança e Conformidade
Protocolos de segurança e documentação de conformidade.

- [Visão Geral de Segurança](./security/00_Overview.md)
- [Autenticação e Autorização](./security/01_Auth.md)
- [Privacidade de Dados (LGPD)](./security/02_LGPD_Compliance.md)
- [Logs de Auditoria](./security/03_Audit_Logs.md)
- [Padrões de Criptografia](./security/04_Encryption.md)
- [Checklist de Conformidade](./security/05_Compliance_Checklist.md)

### 8. Dicionário de Dados
Referência completa de todas as variáveis de processo e estruturas de dados.

- [Dicionário de Dados](./data/00_Data_Dictionary.md)
- [Variáveis de Processo](./data/01_Process_Variables.md)
- [Schema do Banco de Dados](./data/02_Database_Schema.md)
- [Formatos de Mensagem](./data/03_Message_Formats.md)
- [Padrões TISS](./data/04_TISS_Standards.md)

### 9. Documentação de Arquitetura
Arquitetura do sistema e decisões de design.

- [Visão Geral da Arquitetura](./architecture/00_Overview.md)
- [Design do Sistema](./architecture/01_System_Design.md)
- [Arquitetura de Integração](./architecture/02_Integration_Architecture.md)
- [Stack Tecnológico](./architecture/03_Technology_Stack.md)
- [Decisões de Design](./architecture/decisions/)

## Início Rápido

### Pré-requisitos

- Camunda Platform 7.20+
- Java 17+
- PostgreSQL 14+
- Docker & Kubernetes
- Sistema TASY ERP
- Plataforma RPA (IBM RPA ou UiPath)

### Instalação

```bash
# Clonar o repositório
git clone <repository-url>
cd BPMN_Ciclo_da_Receita

# Compilar o projeto
mvn clean install

# Executar testes
mvn test

# Construir o pacote
mvn package

# Executar a aplicação
java -jar target/revenue-cycle-camunda-1.0.0.jar

# Ou usando Spring Boot Maven Plugin
mvn spring-boot:run

# Para Docker
docker build -t hospital-revenue-cycle:1.0.0 .
docker-compose up -d
```

### Perfis de Execução

```bash
# Desenvolvimento (H2 em memória)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Produção (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Testes
mvn test -Ptest
```

Consulte o [Guia de Implantação](./deployment/00_Overview.md) para instruções detalhadas.

## Métricas-Chave

### Metas de Performance

- **Primeiro Contato até Agendamento**: < 5 minutos
- **Pré-Autorização**: < 24 horas (máx 48h)
- **Envio de Faturamento**: < 24 horas pós-alta
- **Resolução de Glosas**: < 7 dias
- **Reconciliação de Pagamentos**: Automação diária
- **Disponibilidade do Sistema**: 99.9%

### Resultados de Negócio

- **Redução de 50%** no tempo manual de autorização
- **Automação de 70%** na verificação de elegibilidade
- **Precisão de 90%** na codificação com IA
- **Redução de 40%** em glosas
- **Processamento 60% mais rápido** de recursos
- **Dashboards financeiros** em tempo real

## Estrutura do Projeto

```
BPMN_Ciclo_da_Receita/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hospital/
│   │   │       ├── RevenueCycleApplication.java
│   │   │       ├── config/
│   │   │       ├── delegates/
│   │   │       ├── services/
│   │   │       └── models/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── processes/
│   ├── bpmn/                  # Arquivos BPMN
│   │   ├── ORCH_Ciclo_Receita_Hospital_Futuro.bpmn
│   │   ├── SUB_01_First_Contact.bpmn
│   │   └── ... (demais subprocessos)
│   ├── dmn/                   # Tabelas de decisão DMN
│   └── test/
│       ├── java/
│       └── resources/
├── tests/                     # Testes de integração
├── docs/                      # Documentação
│   ├── requirements/
│   │   └── ORIGINAL_REQUIREMENTS.md
│   ├── processes/
│   ├── api/
│   ├── dmn/
│   ├── deployment/
│   ├── operations/
│   ├── training/
│   ├── security/
│   ├── data/
│   └── architecture/
├── pom.xml                    # Configuração Maven
├── README.md                  # Este arquivo (English)
└── CLAUDE.md                  # Configuração Claude Code
```

## Stack Tecnológico

### Core
- **BPMN Engine**: Camunda Platform 7.20.0
- **Framework**: Spring Boot 3.2.0
- **Linguagem**: Java 17
- **Build Tool**: Maven 3.9+

### Banco de Dados
- **Desenvolvimento**: H2 (em memória)
- **Produção**: PostgreSQL 14+

### Testes
- **Framework**: JUnit 5.10.1
- **Assertions**: AssertJ 3.24.2
- **Mocking**: Mockito 5.7.0
- **Coverage**: JaCoCo 0.8.11 (meta: 90%)

### Integrações
- **ERP**: TASY (API REST)
- **RPA**: IBM RPA / UiPath
- **IoT/RFID**: Diversos readers
- **Portais**: Operadoras de saúde (web scraping)

## Suporte e Manutenção

### Informações de Contato

- **Suporte Técnico**: support@hospitalfuturo.com
- **Atualizações de Documentação**: docs@hospitalfuturo.com
- **Linha Emergencial**: +55 11 1234-5678

### Contribuindo

Consulte [CONTRIBUTING.md](../CONTRIBUTING.md) para diretrizes sobre:
- Reportar bugs
- Sugerir melhorias
- Processo de contribuição de código
- Atualizações de documentação

## Histórico de Versões

| Versão | Data | Descrição |
|--------|------|-----------|
| 1.0.0 | 2025-12-08 | Lançamento inicial com todos os 11 processos |

## Comandos Maven Úteis

```bash
# Compilar o projeto
mvn clean compile

# Executar testes unitários
mvn test

# Executar testes de integração
mvn verify

# Gerar relatório de cobertura
mvn jacoco:report

# Construir pacote JAR
mvn package

# Instalar no repositório local
mvn install

# Pular testes (não recomendado)
mvn package -DskipTests

# Limpar build anterior
mvn clean

# Verificar dependências atualizadas
mvn versions:display-dependency-updates
```

## Acessando a Aplicação

Após iniciar a aplicação:

- **Camunda Cockpit**: http://localhost:8080/camunda
- **Camunda Admin**: http://localhost:8080/camunda/app/admin
- **Camunda Tasklist**: http://localhost:8080/camunda/app/tasklist
- **REST API**: http://localhost:8080/rest
- **H2 Console** (dev): http://localhost:8080/h2-console

### Credenciais Padrão (Desenvolvimento)

- **Usuário**: admin
- **Senha**: admin

⚠️ **IMPORTANTE**: Alterar credenciais em ambiente de produção!

## Configuração

### Variáveis de Ambiente

```bash
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/revenue_cycle
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha

# Camunda
CAMUNDA_BPM_ADMIN_USER_ID=admin
CAMUNDA_BPM_ADMIN_USER_PASSWORD=senha_segura

# TASY ERP
TASY_API_URL=https://tasy.hospitalfuturo.com/api
TASY_API_KEY=sua_chave_api

# RPA
RPA_PLATFORM_URL=https://rpa.hospitalfuturo.com
RPA_API_KEY=sua_chave_rpa
```

### Arquivos de Configuração

- **application.yml**: Configurações gerais
- **application-dev.yml**: Configurações de desenvolvimento
- **application-prod.yml**: Configurações de produção
- **application-test.yml**: Configurações de teste

## Licença

Copyright (c) 2025 Hospital do Futuro. Todos os direitos reservados.

Consulte [LICENSE](../LICENSE) para o texto completo da licença.

## Agradecimentos

- Equipe AUSTA Hospital do Futuro
- Comunidade Camunda
- Contribuidores open source

## Recursos Adicionais

### Documentação Externa

- [Camunda Platform 7 Docs](https://docs.camunda.org/manual/7.20/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
- [BPMN 2.0 Specification](https://www.omg.org/spec/BPMN/2.0/)
- [DMN Specification](https://www.omg.org/spec/DMN/)
- [Padrão TISS](http://www.ans.gov.br/prestadores/tiss-troca-de-informacao-de-saude-suplementar)

### Ferramentas Recomendadas

- **Modelagem BPMN**: Camunda Modeler 5.20+
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **API Testing**: Postman / Insomnia
- **Container**: Docker Desktop / Rancher Desktop
- **Orquestração**: Kubernetes / OpenShift

## Status do Build

```bash
# Verificar compilação
mvn clean compile

# Executar todos os testes
mvn clean verify

# Gerar relatórios
mvn clean verify site
```

## Monitoramento e Observabilidade

O sistema inclui:

- **Métricas**: Camunda Metrics (JMX/REST)
- **Logs**: SLF4J + Logback
- **Health Checks**: Spring Boot Actuator
- **Tracing**: Suporte para OpenTelemetry (opcional)

Consulte [Guia de Monitoramento](./operations/01_Monitoring.md) para detalhes.

## Segurança

- ✅ Autenticação baseada em usuário/senha
- ✅ Autorização baseada em grupos
- ✅ Criptografia de dados sensíveis
- ✅ Logs de auditoria completos
- ✅ Conformidade LGPD
- ✅ Proteção contra OWASP Top 10

Consulte [Visão Geral de Segurança](./security/00_Overview.md) para detalhes.

## Performance

### Benchmarks

- **Throughput**: 1000+ processos/hora
- **Latência**: < 100ms para service tasks
- **Concorrência**: Suporte para 500+ instâncias simultâneas
- **Escalabilidade**: Horizontal com cluster Camunda

Consulte [Ajuste de Performance](./operations/03_Performance_Tuning.md) para otimizações.

## Roadmap

### Versão 1.1 (Q1 2026)
- [ ] Dashboard mobile
- [ ] Notificações push
- [ ] Integração WhatsApp Business
- [ ] Machine Learning para previsão de glosas

### Versão 2.0 (Q2 2026)
- [ ] Migração para Camunda Platform 8
- [ ] Arquitetura event-driven com Kafka
- [ ] API GraphQL
- [ ] Integração blockchain para auditoria

## Troubleshooting Rápido

### Problemas Comuns

**Erro: "Process definition not found"**
```bash
# Verificar se BPMN foi deployado
curl http://localhost:8080/rest/process-definition
```

**Erro: "Database connection failed"**
```bash
# Verificar configuração do datasource
mvn spring-boot:run -Ddebug
```

**Erro: "TASY API timeout"**
```bash
# Verificar conectividade
curl -I https://tasy.hospitalfuturo.com/api/health
```

Consulte [Resolução de Problemas](./operations/02_Troubleshooting.md) completo.

---

**Última Atualização**: 2025-12-09
**Versão da Documentação**: 1.0.0
**Versão do Sistema**: 1.0.0
**Traduzido por**: Claude Code (Coder Agent)
