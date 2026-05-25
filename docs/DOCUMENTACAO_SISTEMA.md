# 📚 Documentação Completa do Sistema BRISA

> **Versão:** 0.0.1-SNAPSHOT  
> **Data:** Maio de 2026  
> **Status:** Em desenvolvimento ativo

---

## 📋 Sumário

1. [Visão Geral](#visão-geral)
2. [Arquitetura do Sistema](#arquitetura-do-sistema)
3. [BRISA-BACKEND](#brisa-backend)
   - [Tecnologias e Dependências](#tecnologias-e-dependências-backend)
   - [Estrutura de Pacotes](#estrutura-de-pacotes)
   - [Modelo de Domínio](#modelo-de-domínio)
   - [Autenticação e Segurança](#autenticação-e-segurança)
   - [API REST — Endpoints](#api-rest--endpoints)
   - [Serviços Principais](#serviços-principais)
   - [Auditoria e Logs](#auditoria-e-logs)
   - [Importação de Dados (Excel)](#importação-de-dados-excel)
   - [Configuração](#configuração-do-backend)
4. [BRISA-FRONTEND](#brisa-frontend)
   - [Tecnologias e Dependências](#tecnologias-e-dependências-frontend)
   - [Estrutura de Arquivos](#estrutura-de-arquivos-frontend)
   - [Rotas e Navegação](#rotas-e-navegação)
   - [Serviços (API Layer)](#serviços-api-layer)
   - [Componentes Principais](#componentes-principais)
   - [Views (Telas)](#views-telas)
5. [Banco de Dados](#banco-de-dados)
6. [Implantação (Docker)](#implantação-docker)
7. [Variáveis de Ambiente](#variáveis-de-ambiente)
8. [Guia de Desenvolvimento](#guia-de-desenvolvimento)
9. [Fluxos Funcionais](#fluxos-funcionais)

---

## Visão Geral

O **BRISA** (Sistema de Gestão de Programas de Capacitação) é uma plataforma web completa para gerenciamento de programas educacionais. O sistema permite o controle de programas, turmas, etapas, pessoas (participantes), matrículas, carreira e equipes acadêmicas.

### Subprojetos

| Projeto | Tecnologia | Finalidade |
|---|---|---|
| `BRISA-BACKEND` | Spring Boot 3.1.1 / Java 21 | API REST principal |
| `BRISA-FRONTEND` | Vue 3 + Vite | Interface web (SPA) |
| `Brisa-ONE` | React + Vite | Protótipo visual (Figma export) |

---

## Arquitetura do Sistema

```
┌────────────────────────────────────────────────────────┐
│                    BRISA-FRONTEND                      │
│              Vue 3 + Vite (Port: 5173/4300)            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ Views    │ │ Router   │ │ Services │ │ Components│  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
└────────────────────────┬───────────────────────────────┘
                         │ HTTP (Axios + JWT Bearer Token)
                         ▼
┌────────────────────────────────────────────────────────┐
│                   BRISA-BACKEND                        │
│            Spring Boot 3.1.1 (Port: 8082)              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │Controllers│ │ Services │ │  Models  │ │  DTOs    │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│                    ┌─────────────┐                     │
│                    │  Security   │                     │
│                    │ (JWT Filter)│                     │
│                    └─────────────┘                     │
└────────────────────────┬───────────────────────────────┘
                         │ JDBC / JPA (Hibernate)
                         ▼
┌────────────────────────────────────────────────────────┐
│              PostgreSQL 16 (Port: 5432)                │
│                  Database: brisa                       │
└────────────────────────────────────────────────────────┘
```

---

## BRISA-BACKEND

### Tecnologias e Dependências (Backend)

| Dependência | Versão | Finalidade |
|---|---|---|
| Spring Boot | 3.1.1 | Framework principal |
| Java | 21 | Linguagem |
| Maven | 3.8+ | Build e gerenciamento de dependências |
| PostgreSQL Driver | latest | Banco de dados principal |
| Spring Data JPA | (via Spring Boot) | ORM e acesso a dados |
| Spring Security | (via Spring Boot) | Autenticação e autorização |
| JWT (Auth0 java-jwt) | 4.4.0 | Tokens de autenticação stateless |
| Lombok | 1.18.44 | Redução de boilerplate |
| Apache POI (poi-ooxml) | 5.2.5 | Leitura de arquivos Excel |
| Spring Boot Mail | (via Spring Boot) | Envio de e-mails |
| H2 Database | runtime | Banco em memória (testes/console) |
| Spring Boot Validation | (via Spring Boot) | Validação de entidades e DTOs |

### Estrutura de Pacotes

```
com.example.brisa/
├── Brisa.java                    # Classe principal (SpringApplication)
├── config/                       # Configurações globais
├── controllers/                  # Controladores REST
│   ├── AcademicRoleController.java
│   ├── AdvisorController.java
│   ├── AnalyticsController.java
│   ├── AuthenticationController.java
│   ├── CareerController.java
│   ├── ClassController.java
│   ├── CourseController.java
│   ├── CourseEvaluationController.java
│   ├── EnrollmentController.java
│   ├── ExamController.java
│   ├── InstitutionController.java
│   ├── PeopleController.java
│   ├── ProgramAddendumController.java
│   ├── ProgramController.java
│   ├── ProjectGroupController.java
│   ├── PublicCareerController.java
│   ├── StageController.java
│   ├── SystemLogController.java
│   └── UserController.java
├── dtos/                         # Data Transfer Objects
│   ├── analytics/
│   ├── auth/
│   ├── career/
│   ├── course/
│   ├── exam/
│   ├── imports/
│   ├── institution/
│   ├── log/
│   ├── people/
│   ├── program/
│   ├── stage/
│   └── user/
├── enums/                        # Enumerações do sistema
│   ├── AdvisorRoleType.java
│   ├── GenderRole.java
│   ├── LogAction.java
│   ├── StageStatus.java
│   └── UserRole.java
├── exceptions/                   # Exceções customizadas
├── infra/
│   ├── database/                 # Configurações de banco
│   └── security/                 # Configurações de segurança
│       ├── SecurityConfigurations.java
│       ├── SecurityFilter.java
│       └── TokenService.java
├── init/                         # Inicialização da aplicação
├── models/                       # Entidades JPA
│   ├── auth/                     # Modelos de autenticação
│   ├── course/                   # Modelos de curso
│   ├── exam/                     # Modelos de avaliação
│   └── roles/                    # Modelos de papéis acadêmicos
├── repositories/                 # Interfaces Spring Data JPA
├── seeders/                      # Dados iniciais
└── services/                     # Lógica de negócio
```

---

### Modelo de Domínio

O núcleo do sistema é o grafo: **Programa → Turma → Etapa**

```
ProgramModel (programs)
│   id, code, name, contractNumber, executorName, fundingEntity,
│   generalCoordinator, programValue, startDate, endDate,
│   targetAudience, levelingModality, levelingDuration,
│   immersionDuration, immersionWorkloadHours, quotaCriteria,
│   evaluationCriteria, executor, location, supportEmail,
│   officialWebsite, mainLocality, observations, partnerNames
│
├──▶ ProgramInstitutionModel (many-to-many com InstitutionModel)
│
└──▶ ClassModel (classes)
     │   id, code, program_id, startDate, endDate, locality,
     │   location_id, defaultSelectionCapacity, defaultLevelingCapacity,
     │   defaultImmersionCapacity, immersionTeamSize, qtdVagas,
     │   publicationDate, applicationStartDate, applicationEndDate,
     │   levelingSelectionAnnouncementDate, levelingConfirmationStartDate,
     │   levelingConfirmationEndDate, levelingStartDate, levelingEndDate,
     │   levelingFinalExamDate, immersionSelectionAnnouncementDate,
     │   immersionConfirmationStartDate, immersionConfirmationEndDate,
     │   immersionStartDate, immersionEndDate, partialEvaluationDate,
     │   finalEvaluationDate, certificateIssueDate
     │
     └──▶ StageModel (stages)
              id, name, class_id, description, createdAt

PeopleModel (peoples)
│   id, cpf, name, email, educationLevel, address, addressExtra,
│   city, state, gender, quotaCategory, phone, linkedin, zipCode,
│   birthDate, institutionName, courseName, educationStatus,
│   educationCompletionDate, softDeleted, createdAt, updatedAt
│
├──▶ EnrollmentModel (enrollments)
│        id, people_id, class_id, academic_role_id,
│        enrollmentDate, status, completionDate, grade, frequency
│
└──▶ StageCandidateModel
         Rastreamento do progresso de candidatos por etapa

UserModel (users)
    id (UUID), login, password (BCrypt), email,
    verifiedEmail, enabled, role (ADMIN/USER),
    gender, dateOfBirth, profilePicture

InstitutionModel (institutions)
    Instituições parceiras/executoras dos programas

AdvisorModel
    Orientadores associados a turmas/etapas

ProjectGroupModel
    Grupos de projeto formados dentro de turmas

SystemLogModel (audit_logs)
    Registros de auditoria de todas as ações do sistema
```

#### Entidades Adicionais

| Entidade | Tabela | Descrição |
|---|---|---|
| `CareerAutomationSettingsModel` | — | Configurações de automação de carreira |
| `CareerAutomationDispatchModel` | — | Disparos do sistema de automação de carreira |
| `CareerProgressionModel` | — | Progressão de carreira dos participantes |
| `ProgramAddendumModel` | — | Aditivos de contrato dos programas |
| `ProjectGroupMeetingModel` | — | Reuniões dos grupos de projeto |
| `KnowledgeAreaModel` | — | Áreas de conhecimento |

---

### Autenticação e Segurança

O sistema usa **JWT stateless** com Spring Security.

#### Fluxo de Autenticação

```
1. POST /api/auth/login
   → Valida credenciais via AuthenticationManager
   → Gera JWT com TokenService
   → Retorna { token: "..." }

2. Requisições autenticadas:
   → Authorization: Bearer <token>
   → SecurityFilter extrai e valida o token
   → Popula SecurityContext
```

#### Rotas Públicas (sem autenticação)

| Rota | Método | Descrição |
|---|---|---|
| `api/auth/**` | Todos | Login, registro, verificação de e-mail, reset de senha |
| `api/public/career/**` | Todos | Formulário público de acompanhamento de carreira |
| `/h2-console/**` | Todos | Console H2 (desenvolvimento) |

#### Perfis de Usuário

| Role | Valor | Permissões |
|---|---|---|
| `ADMIN` | `admin` | `ROLE_ADMIN` + `ROLE_USER` — acesso total |
| `USER` | `user` | `ROLE_USER` — acesso padrão |

#### Endpoints de Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Login e geração de JWT |
| `POST` | `/api/auth/register` | Registro de novo usuário |
| `GET` | `/api/auth/verify` | Verificação de e-mail (token UUID) |
| `POST` | `/api/auth/forgot-password` | Solicita reset de senha |
| `POST` | `/api/auth/reset-password` | Redefine senha com token |

---

### API REST — Endpoints

#### Módulo de Pessoas

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/people` | Lista pessoas (com filtros e paginação) |
| `POST` | `/api/people` | Cadastra nova pessoa |
| `GET` | `/api/people/{id}` | Detalhes de uma pessoa |
| `PUT` | `/api/people/{id}` | Atualiza pessoa |
| `DELETE` | `/api/people/{id}` | Remove pessoa (soft delete) |
| `POST` | `/api/people/import` | Importa pessoas via Excel (multipart/form-data, campo `file`) |

#### Módulo de Programas

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/programs` | Lista programas |
| `POST` | `/api/programs` | Cria novo programa |
| `GET` | `/api/programs/{id}` | Detalhes do programa (overview DTO) |
| `PUT` | `/api/programs/{id}` | Atualiza programa |
| `DELETE` | `/api/programs/{id}` | Remove programa |
| `POST` | `/api/programs/import` | Importa programas via Excel |

#### Módulo de Turmas (Classes)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/classes` | Lista turmas |
| `POST` | `/api/classes` | Cria turma |
| `GET` | `/api/classes/{id}` | Detalhes da turma |
| `PUT` | `/api/classes/{id}` | Atualiza turma |
| `DELETE` | `/api/classes/{id}` | Remove turma |
| `POST` | `/api/classes/import` | Importa turmas via Excel |

#### Módulo de Etapas (Stages)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/stages` | Lista etapas |
| `POST` | `/api/stages` | Cria etapa |
| `GET` | `/api/stages/{id}` | Detalhes da etapa |
| `PUT` | `/api/stages/{id}` | Atualiza etapa |
| `DELETE` | `/api/stages/{id}` | Remove etapa |
| `POST` | `/api/stages/{id}/candidates` | Adiciona candidato a etapa |
| `DELETE` | `/api/stages/{id}/candidates/{candidateId}` | Remove candidato de etapa |
| `POST` | `/api/stages/candidates/import` | Importa candidatos via Excel |

#### Módulo de Matrículas (Enrollments)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/enrollments` | Lista matrículas |
| `POST` | `/api/enrollments` | Cria matrícula |
| `PUT` | `/api/enrollments/{id}` | Atualiza matrícula (nota, frequência, status) |
| `DELETE` | `/api/enrollments/{id}` | Remove matrícula |
| `POST` | `/api/enrollments/import` | Importa matrículas via Excel |

#### Módulo de Instituições

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/institutions` | Lista instituições |
| `POST` | `/api/institutions` | Cadastra instituição |
| `PUT` | `/api/institutions/{id}` | Atualiza instituição |
| `DELETE` | `/api/institutions/{id}` | Remove instituição |
| `POST` | `/api/institutions/import` | Importa via Excel |

#### Módulo de Carreira

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/career` | Lista registros de carreira |
| `POST` | `/api/career` | Cria registro de carreira |
| `GET` | `/api/career/{id}` | Detalhe de registro |
| `PUT` | `/api/career/{id}` | Atualiza registro |
| `GET` | `/api/public/career/acompanhamento` | Formulário público (sem auth) |

#### Módulo de Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/users` | Lista usuários do sistema |
| `PUT` | `/api/users/{id}` | Atualiza usuário |
| `DELETE` | `/api/users/{id}` | Remove usuário |

#### Módulo de Grupos de Projeto

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/project-groups` | Lista grupos de projeto |
| `POST` | `/api/project-groups` | Cria grupo |
| `GET` | `/api/project-groups/{id}` | Detalhe do grupo |
| `PUT` | `/api/project-groups/{id}` | Atualiza grupo |
| `DELETE` | `/api/project-groups/{id}` | Remove grupo |

#### Módulo de Analytics

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/analytics` | Dashboard de dados analíticos |
| `GET` | `/api/analytics/programs` | Métricas por programa |

#### Módulo de Logs do Sistema

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/logs` | Lista logs de auditoria |
| `GET` | `/api/logs/{id}` | Detalhe de um log |

#### Outros Módulos

| Controller | Base Path | Descrição |
|---|---|---|
| `AdvisorController` | `/api/advisors` | Orientadores acadêmicos |
| `CourseController` | `/api/courses` | Cursos/disciplinas |
| `CourseEvaluationController` | `/api/course-evaluations` | Avaliações de cursos |
| `ExamController` | `/api/exams` | Provas e avaliações |
| `AcademicRoleController` | `/api/academic-roles` | Papéis acadêmicos |
| `ProgramAddendumController` | `/api/program-addendums` | Aditivos de programas |

---

### Serviços Principais

#### Serviços de Integração (Pattern especial)

| Serviço | Tamanho | Responsabilidade |
|---|---|---|
| `PeopleIntegrationService` | ~42KB | Carrega dados de pessoas em massa e agrega DTOs de overview em memória |
| `ProgramIntegrationService` | ~32KB | Agrega dados de programas, turmas e etapas para views de overview |

> **Padrão**: Esses serviços fazem bulk-load do banco e constroem DTOs compostos (ex: `PeopleOverviewResponseDTO`) em memória para performance, evitando N+1 queries.

#### Outros Serviços de Negócio

| Serviço | Tamanho | Responsabilidade |
|---|---|---|
| `ExamService` | ~48KB | Lógica complexa de avaliações e provas |
| `CourseService` | ~45KB | Gestão de cursos e disciplinas |
| `SpreadsheetImportService` | ~43KB | Processamento de planilhas Excel para importação |
| `CareerService` | ~46KB | Sistema de carreira e automação |
| `StageService` | ~36KB | Gerenciamento de etapas e candidatos |
| `PeopleService` | ~18KB | CRUD de pessoas |
| `ProjectGroupService` | ~18KB | Grupos de projeto |
| `LogHelper` | ~12KB | Utilitário de auditoria assíncrona |
| `SystemLogService` | ~10KB | Consulta de logs |
| `AnalyticsService` | ~8KB | Cálculos de métricas do dashboard |
| `EnrollmentService` | ~4KB | Matrículas |
| `InstitutionService` | ~3KB | Instituições |
| `EmailService` | ~3KB | Envio de e-mails via SMTP |
| `CareerAutomationSchedulerService` | ~1KB | Agendamento via cron (a cada 6h) |

---

### Auditoria e Logs

O sistema possui auditoria completa via `LogHelper` + `SystemLogModel`.

#### Ações Auditadas (LogAction enum)

| Categoria | Ações |
|---|---|
| **Usuário** | `USER_LOGIN`, `USER_LOGOUT`, `USER_REGISTER`, `USER_UPDATE`, `USER_DELETE`, `USER_PASSWORD_RESET`, `USER_EMAIL_VERIFIED` |
| **Pessoas** | `PEOPLE_CREATE`, `PEOPLE_UPDATE`, `PEOPLE_DELETE`, `PEOPLE_IMPORT` |
| **Equipe Acadêmica** | `ADVISOR_CREATE`, `ADVISOR_UPDATE`, `ADVISOR_DELETE` |
| **Programas** | `PROGRAM_CREATE`, `PROGRAM_UPDATE`, `PROGRAM_DELETE`, `PROGRAM_IMPORT` |
| **Turmas** | `CLASS_CREATE`, `CLASS_UPDATE`, `CLASS_DELETE`, `CLASS_IMPORT` |
| **Etapas** | `STAGE_CREATE`, `STAGE_UPDATE`, `STAGE_DELETE`, `STAGE_CANDIDATE_ADD`, `STAGE_CANDIDATE_REMOVE`, `STAGE_CANDIDATE_UPDATE`, `STAGE_CANDIDATES_IMPORT` |
| **Matrículas** | `ENROLLMENT_CREATE`, `ENROLLMENT_UPDATE`, `ENROLLMENT_DELETE`, `ENROLLMENT_IMPORT` |
| **Instituições** | `INSTITUTION_CREATE`, `INSTITUTION_UPDATE`, `INSTITUTION_DELETE`, `INSTITUTION_IMPORT` |
| **Sistema** | `SYSTEM_ERROR`, `SYSTEM_WARNING`, `SYSTEM_INFO` |

> **Implementação**: `LogHelper.logAsync(...)` é chamado após cada operação de estado, garantindo que o log não bloqueie a resposta ao cliente.

---

### Importação de Dados (Excel)

O sistema suporta importação em massa via planilhas `.xlsx` usando **Apache POI**.

- **Serviço**: `SpreadsheetImportService` e `ExcelImportHelper`
- **Formato da requisição**: `multipart/form-data` com campo `file`
- **Tamanho máximo**: 50MB por arquivo
- **Entidades suportadas**: Pessoas, Programas, Turmas, Matrículas, Instituições, Candidatos de Etapa

---

### Configuração do Backend

**Arquivo**: `src/main/resources/application.properties`

| Propriedade | Padrão | Descrição |
|---|---|---|
| `server.port` | `8082` | Porta HTTP do servidor |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/brisa` | URL do banco |
| `spring.datasource.username` | `postgres` | Usuário do banco |
| `spring.datasource.password` | `130680` | Senha do banco |
| `spring.jpa.hibernate.ddl-auto` | `update` | Estratégia DDL (update em dev) |
| `api.security.token.secret` | `my-secret-key` | Segredo para assinar JWTs |
| `spring.mail.host` | `smtp.gmail.com` | Servidor de e-mail |
| `spring.mail.port` | `587` | Porta SMTP (STARTTLS) |
| `backend.url` | `http://localhost:8082` | URL pública do backend |
| `frontend.url` | `http://localhost:4300` | URL pública do frontend |
| `app.cors.allowed-origins` | (múltiplas origens localhost) | CORS permitido |
| `career.automation.cron` | `0 0 */6 * * *` | Cron de automação de carreira (a cada 6h) |

---

## BRISA-FRONTEND

### Tecnologias e Dependências (Frontend)

| Dependência | Versão | Finalidade |
|---|---|---|
| Vue 3 | ^3.5.25 | Framework reativo (Composition API) |
| Vite | ^7.2.4 | Build tool e dev server |
| Vue Router | ^4.6.4 | Roteamento SPA |
| Axios | ^1.13.2 | Requisições HTTP |
| Chart.js | ^4.5.1 | Gráficos e visualizações |
| vue-chartjs | ^5.3.3 | Wrapper Vue para Chart.js |
| Lucide Vue Next | ^1.0.0 | Ícones (baseados em Lucide) |
| @headlessui/vue | ^1.7.23 | Componentes acessíveis sem estilo |
| xlsx | ^0.18.5 | Leitura/escrita de planilhas Excel no cliente |
| Node.js | ^20.19 ou >=22.12 | Ambiente de execução |

### Estrutura de Arquivos (Frontend)

```
BRISA-FRONTEND/
├── src/
│   ├── App.vue                   # Componente raiz
│   ├── main.js                   # Entry point (monta app Vue)
│   ├── assets/                   # Imagens, fonts, CSS global
│   ├── components/               # Componentes reutilizáveis
│   │   ├── ConfirmDialog.vue
│   │   ├── EditPersonModal.vue
│   │   ├── EnrollmentModal.vue
│   │   ├── FollowUpModal.vue
│   │   ├── GroupCreateModal.vue
│   │   ├── NavBar.vue
│   │   ├── NewAdvisorModal.vue
│   │   ├── PeopleChart.vue
│   │   ├── StatCard.vue
│   │   ├── TabFilter.vue
│   │   ├── analytics/            # Componentes de analytics
│   │   ├── dashboard/            # Componentes do dashboard
│   │   └── figma-generated/      # Componentes exportados do Figma
│   ├── router/
│   │   └── index.js              # Configuração de rotas + guards
│   ├── services/                 # Camada de comunicação com a API
│   │   ├── api.js                # Instância Axios centralizada (JWT)
│   │   ├── authService.js        # Login, logout, estado de auth
│   │   ├── peopleService.js      # CRUD de pessoas
│   │   ├── programService.js     # CRUD de programas
│   │   ├── classService.js       # CRUD de turmas
│   │   ├── stageService.js       # CRUD de etapas
│   │   ├── enrollmentService.js  # Matrículas
│   │   ├── careerService.js      # Carreira
│   │   ├── courseService.js      # Cursos
│   │   ├── institutionService.js # Instituições
│   │   ├── logService.js         # Logs de auditoria
│   │   ├── analyticsService.js   # Analytics/dashboard
│   │   ├── advisorService.js     # Orientadores
│   │   ├── groupService.js       # Grupos de projeto
│   │   ├── examService.js        # Avaliações
│   │   ├── courseEvaluationService.js
│   │   ├── programAddendumService.js
│   │   ├── academicRoleService.js
│   │   └── publicApi.js          # Axios sem auth (rotas públicas)
│   ├── utils/                    # Funções utilitárias
│   └── views/                    # Telas da aplicação
│       ├── LoginView.vue
│       ├── ResetPasswordView.vue
│       ├── DashboardView.vue
│       ├── PeopleView.vue
│       ├── PessoaPerfilView.vue
│       ├── PeopleListView.vue
│       ├── ProgramsView.vue
│       ├── ProgramDetailsView.vue
│       ├── ClassDetailsView.vue
│       ├── ClassCoursesView.vue
│       ├── ClassesView.vue
│       ├── StageDetailsView.vue
│       ├── EnrollmentsView.vue
│       ├── InstitutionsView.vue
│       ├── CareerView.vue
│       ├── CareerPublicAccessView.vue
│       ├── AdminPanelView.vue
│       ├── AcademicStaffView.vue
│       ├── CoursesView.vue
│       ├── LogsView.vue
│       └── ProgramRegistration/  # Fluxo multi-step de cadastro
├── public/                       # Assets estáticos
├── index.html                    # HTML raiz (SPA shell)
├── vite.config.js                # Configuração do Vite
├── package.json                  # Dependências e scripts
└── nginx.conf                    # Configuração Nginx (produção)
```

---

### Rotas e Navegação

#### Guard de Autenticação

O router usa **meta-driven protection** com guards `beforeEach`:

```javascript
// Regras aplicadas em ordem:
1. Se rota requer auth (meta.requiresAuth) e usuário não autenticado → redireciona para /
2. Se rota requer admin (meta.requiresAdmin) e usuário não é ADMIN  → redireciona para /dashboard
3. Se usuário autenticado tenta acessar / (login)                   → redireciona para /dashboard
```

#### Tabela de Rotas

| Path | Nome | Componente | Auth | Admin |
|---|---|---|---|---|
| `/` | Login | `LoginView` | ❌ | ❌ |
| `/reset-password` | ResetPassword | `ResetPasswordView` | ❌ | ❌ |
| `/dashboard` | Dashboard | `DashboardView` | ✅ | ❌ |
| `/people` | People | `PeopleView` | ✅ | ❌ |
| `/academic-staff` | AcademicStaff | `AcademicStaffView` | ✅ | ✅ |
| `/people/:id` | PersonDetails | `PessoaPerfilView` | ✅ | ❌ |
| `/institutions` | Institutions | `InstitutionsView` | ✅ | ❌ |
| `/programs` | Programs | `ProgramsView` | ✅ | ❌ |
| `/programs/register` | ProgramRegistration | `ProgramRegistrationView` | ✅ | ❌ |
| `/courses` | Courses | `CoursesView` | ✅ | ❌ |
| `/classes` | Classes | `ClassesView` | ✅ | ❌ |
| `/enrollments` | Enrollments | `EnrollmentsView` | ✅ | ❌ |
| `/admin-panel` | AdminPanel | `AdminPanelView` | ✅ | ❌ |
| `/carreira` | Career | `CareerView` | ✅ | ❌ |
| `/carreira/acompanhamento` | CareerPublicAccess | `CareerPublicAccessView` | ❌ | ❌ |
| `/programs/:id` | ProgramDetails | `ProgramDetailsView` | ✅ | ❌ |
| `/programs/:programId/classes/:classId` | ClassDetails | `ClassDetailsView` | ✅ | ❌ |
| `/programs/:programId/classes/:classId/courses` | ClassCourses | `ClassCoursesView` | ✅ | ❌ |
| `/programs/:programId/classes/:classId/stages/:stageId` | StageDetails | `StageDetailsView` | ✅ | ❌ |
| `/logs` | Logs | `LogsView` | ✅ | ❌ |

---

### Serviços (API Layer)

#### `api.js` — Instância Axios Centralizada

```javascript
// Configurações:
baseURL: VITE_API_BASE_URL || 'http://localhost:8082/api'
Content-Type: 'application/json'

// Request interceptor:
- Injeta "Authorization: Bearer <token>" de localStorage

// Response interceptor:
- Status 401 → limpa token e usuário do localStorage → redireciona para /
```

#### `publicApi.js`

Instância Axios sem interceptor de autenticação, usada para rotas públicas (ex: formulário de carreira).

#### `authService.js`

| Método | Descrição |
|---|---|
| `login(credentials)` | POST `/auth/login`, armazena token e user no localStorage |
| `logout()` | Remove token e user do localStorage |
| `isAuthenticated()` | Verifica presença do token |
| `getUser()` | Retorna objeto user do localStorage |

---

### Componentes Principais

| Componente | Descrição |
|---|---|
| `NavBar.vue` | Barra de navegação lateral com menu e logout |
| `StatCard.vue` | Card de estatística para o dashboard |
| `TabFilter.vue` | Filtro por abas (ex: status de etapas) |
| `PeopleChart.vue` | Gráfico de distribuição de pessoas |
| `ConfirmDialog.vue` | Modal de confirmação de ações destrutivas |
| `EditPersonModal.vue` | Modal de edição de dados de pessoa |
| `EnrollmentModal.vue` | Modal de criação/edição de matrícula |
| `FollowUpModal.vue` | Modal de acompanhamento |
| `GroupCreateModal.vue` | Modal de criação de grupo de projeto |
| `NewAdvisorModal.vue` | Modal de cadastro de orientador |

---

### Views (Telas)

| View | Tamanho | Descrição |
|---|---|---|
| `ClassDetailsView.vue` | ~265KB | Tela mais complexa — detalhes completos de uma turma com todas as sub-seções |
| `PeopleListView.vue` | ~110KB | Lista detalhada de pessoas com filtros, busca e ações em massa |
| `CareerView.vue` | ~88KB | Gestão do módulo de carreira |
| `DashboardView.vue` | ~78KB | Dashboard principal com métricas e gráficos |
| `AdminPanelView.vue` | ~78KB | Painel administrativo de usuários do sistema |
| `ProgramsView.vue` | ~72KB | Lista e gestão de programas |
| `StageDetailsView.vue` | ~48KB | Detalhes de uma etapa com candidatos |
| `ProgramDetailsView.vue` | ~44KB | Detalhes de um programa |
| `ClassCoursesView.vue` | ~39KB | Cursos associados a uma turma |
| `InstitutionsView.vue` | ~31KB | Gestão de instituições |
| `EnrollmentsView.vue` | ~24KB | Gestão de matrículas |
| `LogsView.vue` | ~25KB | Visualização de logs de auditoria |
| `PessoaPerfilView.vue` | ~25KB | Perfil detalhado de uma pessoa |
| `AcademicStaffView.vue` | ~19KB | Equipe acadêmica (acesso ADMIN) |
| `CareerPublicAccessView.vue` | ~19KB | Formulário público de carreira (sem auth) |
| `ClassesView.vue` | ~17KB | Lista de turmas |
| `PersonDetailsView.vue` | ~18KB | Detalhes básicos de pessoa |
| `LoginView.vue` | ~12KB | Tela de login |
| `ResetPasswordView.vue` | ~6KB | Redefinição de senha |

---

## Banco de Dados

### Tecnologia
- **PostgreSQL 16** (produção/desenvolvimento)
- **H2** (em memória — console de desenvolvimento)

### Configuração de Conexão

```
Host:     localhost
Port:     5432
Database: brisa
User:     postgres
DDL:      update (Hibernate gerencia migrações automaticamente)
```

### Principais Tabelas

| Tabela | Entidade | Descrição |
|---|---|---|
| `users` | `UserModel` | Usuários do sistema (UUID como PK) |
| `peoples` | `PeopleModel` | Participantes dos programas |
| `programs` | `ProgramModel` | Programas de capacitação |
| `classes` | `ClassModel` | Turmas de cada programa |
| `stages` | `StageModel` | Etapas de cada turma |
| `enrollments` | `EnrollmentModel` | Matrículas (people × class × role) |
| `institutions` | `InstitutionModel` | Instituições parceiras |
| `program_institutions` | `ProgramInstitutionModel` | Associação programa-instituição |

### Otimizações Configuradas

```properties
# Batch insert/update para performance
spring.jpa.properties.hibernate.jdbc.batch_size=500
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

---

## Implantação (Docker)

O projeto inclui `docker-compose.yml` na raiz para deploy completo.

### Serviços

| Serviço | Imagem/Build | Porta | Descrição |
|---|---|---|---|
| `postgres` | `postgres:16-alpine` | `5432:5432` | Banco de dados PostgreSQL |
| `backend` | `./BRISA-BACKEND/Dockerfile` | `8082:8082` | API Spring Boot |
| `frontend` | `./BRISA-FRONTEND/Dockerfile` | `4300:80` | SPA Vue servida por Nginx |

### Dependências de Startup

```
postgres (healthcheck: pg_isready)
    └──▶ backend (depends_on: postgres healthy)
            └──▶ frontend (depends_on: backend)
```

### Comandos Docker

```bash
# Subir todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Parar todos
docker-compose down

# Parar e remover volumes (apaga dados do banco)
docker-compose down -v
```

### Rede e Volume

- **Rede**: `brisa-network` (bridge)
- **Volume**: `brisa-postgres-data` (persistência do banco)

---

## Variáveis de Ambiente

### Backend (`.env` ou Docker environment)

| Variável | Padrão | Descrição |
|---|---|---|
| `SERVER_PORT` | `8082` | Porta HTTP |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/brisa` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `130680` | Senha do banco |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Estratégia DDL |
| `JWT_SECRET` | `my-secret-key` | Segredo JWT (⚠️ alterar em produção!) |
| `BACKEND_URL` | `http://localhost:8082` | URL pública do backend |
| `FRONTEND_URL` | `http://localhost:4300` | URL pública do frontend |
| `APP_CORS_ALLOWED_ORIGINS` | (múltiplas origens) | Origens CORS permitidas |
| `SPRING_MAIL_USERNAME` | — | E-mail Gmail para envio |
| `SPRING_MAIL_PASSWORD` | — | Senha de app Gmail |
| `SPRING_MAIL_FROM` | — | Endereço de remetente |
| `CAREER_AUTOMATION_CRON` | `0 0 */6 * * *` | Cron de automação (a cada 6h) |
| `POSTGRES_PASSWORD` | `130680` | Senha do container PostgreSQL |

### Frontend (`BRISA-FRONTEND/.env`)

| Variável | Padrão | Descrição |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8082/api` | URL base da API backend |

---

## Guia de Desenvolvimento

### Pré-requisitos

| Ferramenta | Versão Mínima |
|---|---|
| Java | 21+ |
| Maven | 3.8+ |
| Node.js | 20.19+ ou 22.12+ |
| PostgreSQL | 12+ |
| Docker (opcional) | 20+ |

### Rodando Localmente

#### 1. Banco de Dados

```bash
# Opção A: PostgreSQL local
# Crie o banco:
psql -U postgres -c "CREATE DATABASE brisa;"

# Opção B: via Docker apenas o banco
docker-compose up -d postgres
```

#### 2. Backend

```powershell
cd BRISA-BACKEND
.\mvnw.cmd spring-boot:run
# API disponível em: http://localhost:8082
```

#### 3. Frontend

```powershell
cd BRISA-FRONTEND
npm install
npm run dev
# App disponível em: http://localhost:5173
```

### Convenções de Código

#### Backend (Java)

- **Nomenclatura**: Inglês para identificadores, Português para labels de UI e campos de DTOs
- **Controllers**: Lógica centralizada nos Services. Controllers apenas delegam.
- **Audit Log**: Sempre chamar `LogHelper` após operações que alteram estado.
- **Integration Services**: `PeopleIntegrationService` e `ProgramIntegrationService` para overview/list complexos — fazem bulk-load e agregação in-memory.
- **DTOs compostos**: Ex: `PeopleOverviewResponseDTO` com resumos, abas e itens filtrados.
- **Imports**: `multipart/form-data` com campo `file`.

#### Frontend (Vue 3)

- **API calls**: Sempre usar a instância centralizada de `src/services/api.js` para injeção automática de JWT.
- **Serviços**: Lógica de API encapsulada em `src/services/` por domínio.
- **Estado de auth**: Token e user no `localStorage` (chaves: `token`, `user`).
- **Proteção de rotas**: Via `meta.requiresAuth` e `meta.requiresAdmin` no router.

### Build de Produção

```powershell
# Backend
cd BRISA-BACKEND
.\mvnw.cmd clean package -DskipTests
# JAR gerado em: target/brisa-0.0.1-SNAPSHOT.jar

# Frontend
cd BRISA-FRONTEND
npm run build
# Build gerado em: dist/
```

### Testes

```powershell
# Backend
cd BRISA-BACKEND
.\mvnw.cmd test
```

---

## Fluxos Funcionais

### Fluxo de Autenticação Completo

```
1. Usuário acessa /
2. Preenche login e senha
3. POST /api/auth/login → retorna JWT
4. Token armazenado em localStorage
5. Redirecionamento para /dashboard
6. Toda requisição seguinte injeta Bearer token automaticamente
7. Token expirado/inválido → 401 → limpa storage → redireciona para /
```

### Fluxo de Registro de Novo Usuário

```
1. POST /api/auth/register
2. Sistema cria UserModel (role=USER, enabled=true, verifiedEmail=false)
3. Gera token de verificação UUID
4. Envia e-mail de verificação via SMTP Gmail
5. Usuário clica no link → GET /api/auth/verify?token=<uuid>
6. Sistema marca verifiedEmail=true
7. Usuário pode fazer login
```

### Fluxo de Cadastro de Programa (Multi-step)

```
View: /programs/register (ProgramRegistrationView)

Step 1: Dados básicos do programa
    → nome, código, contrato, executor, financiador
Step 2: Configurações de turma e etapas
    → capacidades, datas, modalidades
Step 3: Instituições parceiras
    → seleção de instituições
Step 4: Revisão e confirmação
    → POST /api/programs
```

### Fluxo de Importação de Pessoas via Excel

```
1. Usuário faz upload de planilha .xlsx
2. Frontend envia: POST /api/people/import
   Content-Type: multipart/form-data
   Campo: file
3. Backend: SpreadsheetImportService processa via Apache POI
4. Valida e insere registros em batch (500 por batch)
5. LogHelper.logAsync(PEOPLE_IMPORT, ...) — registra auditoria
6. Retorna relatório de sucesso/erros
```

### Fluxo de Carreira Público

```
1. Participante recebe link: /carreira/acompanhamento
2. Rota pública (sem autenticação)
3. GET /api/public/career/acompanhamento
4. Preenche formulário de acompanhamento de carreira
5. Dados salvos associados ao CPF/e-mail
6. Staff acessa /carreira para visualizar e gerenciar
```

### Fluxo de Automação de Carreira

```
Agendamento: CAREER_AUTOMATION_CRON = "0 0 */6 * * *" (a cada 6 horas)

CareerAutomationSchedulerService:
1. Verifica CareerAutomationSettingsModel
2. Identifica participantes com pendências
3. Envia e-mails automáticos via EmailService
4. Registra CareerAutomationDispatchModel
```

---

## Estrutura de Arquivos do Repositório Raiz

```
Brisa-ONE/ (raiz do repositório)
├── BRISA-BACKEND/          # Backend Spring Boot
├── BRISA-FRONTEND/         # Frontend Vue 3
├── docs/                   # Documentação do projeto
│   ├── DOCUMENTACAO_SISTEMA.md  # Este arquivo
│   └── CHECKLIST-SISTEMA-BRISA.md
├── planilhas/              # Planilhas de exemplo para importação
├── docker-compose.yml      # Orquestração Docker completa
├── .env                    # Variáveis de ambiente (não commitado)
├── .env.example            # Template de variáveis de ambiente
├── GEMINI.md               # Instruções de projeto para agentes de IA
├── README.md               # README principal do repositório
└── LICENSE                 # Licença do projeto
```

---

> 📌 **Nota**: Esta documentação foi gerada automaticamente com base na análise do código-fonte em **Maio de 2026**. Para manter a documentação atualizada, revise este arquivo sempre que houver mudanças significativas na arquitetura ou novos módulos forem adicionados.
