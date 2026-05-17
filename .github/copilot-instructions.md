# Copilot Instructions for Brisa-ONE

## Repository shape

This repository is a multi-project workspace:

- `BRISA-BACKEND`: Spring Boot 3.1.1 API (Java 21, Maven, local H2 by default, JWT auth)
- `BRISA-FRONTEND`: Vue 3 + Vite SPA that consumes the backend API
- `Brisa ONE`: separate React + Vite bundle (Figma/Make export), not wired into `BRISA-FRONTEND`

When implementing features, confirm which project is in scope before editing.

## Build, run, and test commands

### BRISA-BACKEND (`BRISA-BACKEND`)

- Build/package: `.\mvnw.cmd clean install`
- Run API locally: `.\mvnw.cmd spring-boot:run`
- Full test suite: `.\mvnw.cmd test`
- Single test class: `.\mvnw.cmd -Dtest=AuthApplicationTests test`
- Single test method: `.\mvnw.cmd -Dtest=AuthApplicationTests#contextLoads test`
- API runs on port `8083` by default.

### BRISA-FRONTEND (`BRISA-FRONTEND`)

- Install dependencies: `npm install`
- Run dev server: `npm run dev`
- Build production bundle: `npm run build`
- Preview production bundle: `npm run preview`
- No test or lint scripts are currently defined in `package.json`.

### Brisa ONE (`Brisa ONE`)

- Install dependencies: `npm i`
- Run dev server: `npm run dev`
- Build production bundle: `npm run build`
- No test or lint scripts are currently defined in `package.json`.

## High-level architecture

### Backend API (Spring Boot)

- Core business graph is **Program -> Class -> Stage**.
- `PeopleModel` links into classes through `EnrollmentModel` and stage progress through `StageCandidateModel`.
- Read models in `models/` first, then controllers in `controllers/`, then integration services in `services/` to trace list/overview behavior.
- Overview/list endpoints (`/api/people/overview`, `/api/programs/overview`) are composed in `PeopleIntegrationService` and `ProgramIntegrationService`, which bulk-load with repository `findAllWithRelations()` methods and aggregate in-memory.
- Controllers call `LogHelper` after state-changing operations (create/update/delete/import); logging is async and intentionally non-blocking.

### Security/auth

- JWT-based stateless auth:
  - `AuthenticationController` issues tokens
  - `SecurityFilter` reads `Authorization: Bearer <token>` and loads user into `SecurityContext`
  - `SecurityConfigurations` enables stateless security and allows auth endpoints + protects the rest

### BRISA-FRONTEND (Vue 3)

- Vue app entry: `src/main.js` -> `App.vue` -> `router/index.js`.
- Route protection is meta-driven (`meta.requiresAuth`) and enforced in a global `beforeEach` guard.
- API access is centralized in `src/services/api.js`, driven by `VITE_API_BASE_URL` and defaulting to `http://localhost:8083/api`, with an Axios request interceptor that injects JWT from `localStorage`.
- Feature services (`peopleService`, `programService`, etc.) wrap API endpoints and are the boundary for view components.
- Program registration is a large wizard-style flow under `src/views/ProgramRegistration/`.
- `authService` persists both `token` and serialized `user` in `localStorage`.
- Playwright MCP is useful here for browser-level validation of Vue screens, routing, and form flows when browser automation is available.

### Brisa ONE (React + Vite)

- Separate bundle exported from Figma/Make (`main.tsx` entry).
- Not wired into the Vue frontend routing or services; treat as independent unless the task explicitly bridges them.

### Seed/bootstrap behavior

- `DataSeeder` creates default admin/user on startup when user table is empty.
- `StageSelectionSeedRunner` (guarded by `brisa.seed.enable=true`) creates deterministic seed data for selection stages.

## Key conventions in this codebase

- **Language/domain naming is mixed**: code identifiers are often English, while DTO fields/messages/UI labels are Portuguese. Keep existing wording style per module instead of force-translating.
- **Overview endpoints return composed DTOs**, not raw entities (`PeopleOverviewResponseDTO`, `ProgramOverviewResponseDTO` with summary + tabs + filtered items). Preserve this shape when expanding filters.
- **Import endpoints use multipart with field name `file`** (frontend services and backend controllers are aligned on this contract).
- **Repository optimization pattern**: integration services prefer bulk loads (`findAllWithRelations`, grouped maps) and in-memory aggregation over repeated per-item queries.
- **Audit logging pattern**: controllers call `logHelper` after state-changing operations; log failures are intentionally non-blocking for request success.
- **Frontend auth conventions**:
  - protected routes must include `meta.requiresAuth: true`
  - login route is `/` and redirects to `/home` when authenticated
  - shared API calls must go through the Axios `api` instance to preserve JWT injection.
