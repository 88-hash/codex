# Repository Guidelines

## Project Structure & Module Organization
- `backend/`: Spring Boot 3.2 API (`src/main/java/com/leyi`), MyBatis XML in `src/main/resources/mapper`, runtime config in `application.yml`.
- `frontend-admin/`: Vue 3 + Vite admin app (default dev port `3001`).
- `frontend-customer/`: Vue 3 + Vite customer app (default dev port `3000`).
- `database/`: SQL bootstrap and sample data (`init.sql`, `test_data.sql`).
- Root scripts: `start_project.bat` (start existing services) and `one_key_start.ps1` (build + install + launch).

## Build, Test, and Development Commands
- Initialize DB: `mysql -u root -p < database/init.sql`
- Backend dev: `cd backend && mvn spring-boot:run`
- Backend package: `cd backend && mvn clean package` then `java -jar target/leyi-snack-1.0.0.jar`
- Frontend dev (admin): `cd frontend-admin && npm install && npm run dev`
- Frontend dev (customer): `cd frontend-customer && npm install && npm run dev`
- Frontend production build: `npm run build` (run in each frontend module)

## Coding Style & Naming Conventions
- Java: 4-space indentation, `com.leyi` package root, PascalCase classes, camelCase methods/fields.
- Keep backend layering consistent: `*Controller`, `*Service`, `*Mapper`, entity classes in `entity/`.
- Vue: SFCs under `src/views/**/Index.vue`; shared logic in `src/api`, `src/stores`, `src/utils`.
- Use `@` alias for `src` imports; keep SCSS in `src/styles`.
- No repo-level ESLint/Prettier config is committed; match existing style and verify with successful builds.

## Testing Guidelines
- Backend includes `spring-boot-starter-test`, but no project tests are currently committed.
- Add backend tests under `backend/src/test/java` with names ending in `*Test`.
- Frontend has no configured test runner yet; when adding tests, prefer Vitest + Vue Test Utils in `src/**/__tests__/`.
- Minimum pre-PR validation: `mvn clean package` and `npm run build` in both frontend apps.

## Commit & Pull Request Guidelines
- This workspace snapshot has no `.git` metadata, so no historical commit convention can be inferred.
- Adopt Conventional Commits going forward, e.g. `feat(admin): add order verification filter`.
- PRs should include: purpose/scope, affected modules (`backend`, `frontend-admin`, `frontend-customer`, `database`), config/SQL changes, UI screenshots for frontend changes, and manual test steps.

## Security & Configuration Tips
- Do not commit real secrets in `backend/src/main/resources/application.yml`; prefer environment overrides.
- Verify local paths (for example `upload.path`) before running in a new environment.
- Avoid committing generated artifacts (`backend/target/`, `**/node_modules/`).
