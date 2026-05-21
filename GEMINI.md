# Brisa-ONE Project Instructions

This project is a multi-project workspace for the BRISA system, consisting of a Spring Boot backend and a Vue 3 frontend, along with a separate React prototype.

## Project Overview

- **BRISA-BACKEND**: A Spring Boot 3.1.1 API (Java 21, Maven) providing authentication, CRUD operations, Excel imports, audit logs, and analytics. It uses PostgreSQL as the primary database and JWT for stateless authentication.
- **BRISA-FRONTEND**: A Vue 3 + Vite single-page application (SPA) that consumes the backend API. It features a dashboard, administrative panel, career tracking, and complex program registration workflows.
- **Brisa-ONE**: A separate React + Vite bundle (exported from Figma/Make) used as a visual prototype. It is **not** integrated into the main `BRISA-FRONTEND` application.

## Building and Running

### Backend (BRISA-BACKEND)

- **Requirements**: Java 21+, Maven 3.8+, PostgreSQL 12+.
- **Run Locally**:
  ```powershell
  cd BRISA-BACKEND
  .\mvnw.cmd spring-boot:run
  ```
- **Test**: `.\mvnw.cmd test`
- **Default Port**: `8082` (configured via `server.port`).
- **Database**: Defaults to `jdbc:postgresql://localhost:5432/brisa`.

### Frontend (BRISA-FRONTEND)

- **Requirements**: Node.js 20.19+ or 22.12+.
- **Install Dependencies**: `npm install`
- **Run Dev Server**: `npm run dev`
- **Build**: `npm run build`
- **Environment**: Configure `VITE_API_BASE_URL` in `.env` (default: `http://localhost:8082/api`).

## Development Conventions

### Backend Architecture

- **Domain Model**: The core business graph is **Program -> Class -> Stage**. `PeopleModel` links to classes via `EnrollmentModel` and tracks stage progress via `StageCandidateModel`.
- **Integration Services**: Use `PeopleIntegrationService` and `ProgramIntegrationService` for complex overview/list logic. These services bulk-load data and aggregate DTOs in-memory for performance.
- **Controllers**: Centralize logic in services. Call `LogHelper` after state-changing operations for async audit logging.
- **Security**: JWT-based. `SecurityFilter` handles token extraction and `SecurityContext` population.

### Frontend Architecture

- **Routing**: Meta-driven protection (`meta.requiresAuth`). Enforced in `src/router/index.js`.
- **API Communication**: All shared API calls **must** use the centralized Axios instance in `src/services/api.js` to ensure JWT injection and consistent error handling.
- **Services**: Feature-specific logic is encapsulated in `src/services/` (e.g., `peopleService.js`, `programService.js`).
- **State**: Auth state (token and user) is persisted in `localStorage`.

### General Guidelines

- **Naming**: Mixture of English (identifiers) and Portuguese (UI labels, DTO fields). Follow existing local patterns.
- **Overview Endpoints**: Return composed DTOs (e.g., `PeopleOverviewResponseDTO`) containing summaries, tabs, and filtered items.
- **Imports**: Use multipart form-data with the field name `file`.
- **Audit Logging**: Mandatory for create/update/delete/import actions via `LogHelper`.

## Directory Structure

- `BRISA-BACKEND/`: Java Spring Boot source code.
- `BRISA-FRONTEND/`: Vue 3 frontend source code.
- `Brisa-ONE/`: Independent React prototype.
- `docs/`: Project documentation and requirements.
