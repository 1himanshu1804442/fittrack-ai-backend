# AGENTS.md — Quick onboarding for AI coding agents

This file captures the minimal, project-specific knowledge an automated coding agent needs to be productive in this repository.

## Quick checklist for an agent
- Understand the app shape: Spring Boot 3, layered (controller → service → repository), PostgreSQL, JWT-based auth, and a Google Gemini integration. Includes exercise logging and user statistics features.
- Key files to read first: `src/main/java/com/example/demo/DemoApplication.java`, `security/JwtUtil.java`, `controller/{UserController,ExerciseController,StatsController}.java`, `service/{RecommendationService,UserService,StatsService}.java`, `repository/*`, `entity/*`.
- For external integrations, inspect `src/main/resources/application.properties` (DB and Gemini keys).
- Use the provided wrapper for builds/tests on Windows: `./mvnw.cmd` (PowerShell examples below).

## Big picture (short)
- Runtime: Spring Boot application (`DemoApplication`). Controllers expose REST endpoints under `/api/*` (see `controller/{UserController,ExerciseController,StatsController}.java`).
- Business logic: `service/*` (e.g., `RecommendationService` calls external AI; `UserService` implements CRUD and workout association; `StatsService` computes user metrics from exercise logs).
- Persistence: Spring Data JPA repositories in `repository/*` (e.g., `UserRepository`, `WorkoutRepository`, `ExerciseLogRepository`). The DB is PostgreSQL configured in `application.properties`.
- Security: JWT utilities live in `security/JwtUtil.java` (token creation/validation). `JwtRequestFilter.java` and `SecurityConfig.java` exist but are empty in this snapshot — search these before changing auth behavior.

## Project-specific patterns & pitfalls (concrete)
- Controllers call service methods by exact names/signatures. Example: `UserController` calls `userService.createNewUser(User)` and `userService.addWorkoutToUser(Integer, Workout)` — keep signatures stable when editing.
- Entities map to explicit DB columns: `User.userId` maps to `user_id` (see `entity/User.java`). `WorkoutRepository.findUserHistory` uses a native SQL query; when changing schema or column names, update this query.
- Exercise logging: `ExerciseController` endpoints are `/api/users/{userId}/exercises` (POST creates, GET retrieves). Logs persist to `ExerciseLog` entity with columns: `exerciseName`, `weight`, `sets`, `reps`, `dateLogged`. `ExerciseLogRepository.calculateTotalVolumeSince()` computes volume (weight × sets × reps) for the past 7 days, used by `StatsService`.
- User goals: `User.goal` is a `Goal` enum (WEIGHT_LOSS, MUSCLE_GAIN, MAINTENANCE). Update this field via `UserService.updateUser()` or `UserController` endpoints.
- Stats endpoint: `StatsController` exposes `GET /api/users/{userId}/stats` returning `UserStatsDTO` with `workoutStreak`, `weeklyVolume`, `recoveryScore`, and `currentWeight`. The `StatsService` assembles these from `User` and `ExerciseLog` data.
- AI integration: `RecommendationService.generatePlan(Integer)` constructs a single-string prompt and calls an HTTP endpoint via `RestTemplate`. The Gemini URL and key are assembled from properties: `gemini.api.url` + `gemini.api.key` (see `application.properties`). Handle JSON parsing exactly as implemented (the code expects `candidates[0].content.parts[0].text`).
- Secrets: JWT secret is now read from the `jwt.secret` property (backed by `JWT_SECRET` env var) in `security/JwtUtil.java`, initialized at bean startup via `@PostConstruct`. Falls back to a default for local testing; always set `JWT_SECRET` for production. Update tests if they rely on a specific token value.

## Build, run, debug (commands)
- Build & package (Windows PowerShell):
  $env:DB_PASSWORD='your_db_password'; $env:GEMINI_API_KEY='your_key'; $env:JWT_SECRET='your_jwt_secret'; .\mvnw.cmd clean package
- Run locally (Spring Boot) in PowerShell:
  $env:DB_PASSWORD='...'; $env:GEMINI_API_KEY='...'; $env:JWT_SECRET='...'; .\mvnw.cmd spring-boot:run
- Run the produced jar (artifactId/version from `pom.xml` -> `demo-0.0.1-SNAPSHOT.jar`):
  $env:DB_PASSWORD='...'; $env:GEMINI_API_KEY='...'; $env:JWT_SECRET='...'; java -jar target\demo-0.0.1-SNAPSHOT.jar
- Tests: $env:JWT_SECRET='test'; .\mvnw.cmd test

## Environment variables & config to set for successful runs
- `DB_PASSWORD` — used by `spring.datasource.password` in `src/main/resources/application.properties` (DB URL: `jdbc:postgresql://localhost:5432/fittrack_db`).
- `GEMINI_API_KEY` — used to build the Gemini request URL. Confirm `gemini.api.url` in `application.properties` before calling the AI.
- `JWT_SECRET` — used to sign and validate JWT tokens. Read by `security/JwtUtil.java` via the `jwt.secret` property. Falls back to an embedded default for local testing, but production must set this env var.

## Files to inspect for change-impact analysis
- Security: `src/main/java/com/example/demo/security/JwtUtil.java` (token format, expiration), `JwtRequestFilter.java`, `SecurityConfig.java`.
- AI: `src/main/java/com/example/demo/service/RecommendationService.java` (prompt format, JSON parsing, request shape).
- Controllers & Services: `src/main/java/com/example/demo/controller/{UserController,ExerciseController,StatsController}.java`, `src/main/java/com/example/demo/service/{UserService,StatsService}.java`. Note: `ExerciseController` uses constructor injection for repositories; `StatsService` aggregates metrics from `ExerciseLogRepository`.
- Exercise logging: `src/main/java/com/example/demo/entity/ExerciseLog.java` (has `@ManyToOne` to `User`), `src/main/java/com/example/demo/repository/ExerciseLogRepository.java` (defines `calculateTotalVolumeSince` query), `src/main/java/com/example/demo/dto/ExerciseLogRequest.java`.
- Persistence & schema: `src/main/java/com/example/demo/entity/*` (User now references `Goal` enum and `ExerciseLog`), `src/main/java/com/example/demo/repository/*`. Note native query in `WorkoutRepository.findUserHistory`.

## Testing & modification guidance (concrete examples)
- When changing `User.bodyWeight` type or column name, update: `entity/User.java`, any DTOs (`dto/UserUpdateDTO.java`), `RecommendationService` (prompt formatting uses `user.getBodyWeight()`), and database migration or `spring.jpa.hibernate.ddl-auto` behavior.
- When modifying `User.goal` (e.g., adding new goals), update: `entity/Goal.java` (enum), any business logic that checks goal values (e.g., in `RecommendationService` prompt, `StatsService`), and tests.
- When adding new fields to `ExerciseLog`, update: `entity/ExerciseLog.java`, `dto/ExerciseLogRequest.java` (request mapping), `ExerciseController.addExerciseLog()` (field assignment), and `ExerciseLogRepository` queries if they reference the field.
- When modifying volume calculation or stat aggregation, update: `ExerciseLogRepository.calculateTotalVolumeSince()` query and `StatsService.getUserStats()` logic to match. Note: `StatsService` currently hard-codes `workoutStreak` (12) and `recoveryScore` (72) as placeholder values.
- When modifying AI prompt or output parsing, update `RecommendationService.generatePlan` and the code that persists `Workout.aiResponse`.
- JWT secret is now configuration-backed via `JWT_SECRET` env var. If modifying token validation or expiration logic, update `JwtUtil` and ensure tests use the same secret mechanism.

## Useful quick-search globs for an agent
- Source root: `src/main/java/com/example/demo/**`
- Controllers: `**/controller/**`
- Services: `**/service/**`
- Security: `**/security/**`
- Entities/Repos: `**/entity/**`, `**/repository/**`
- Config: `src/main/resources/application.properties`

## When to ask a human
- If you need to enforce stricter JWT secret validation (e.g., minimum length, reject startup if not set) — this would be a security decision best confirmed first.
- If you need access to a real Gemini key or a Postgres instance — ask for credentials or a local dev DB snapshot.

---
Reference files mentioned above live under `src/main/java/com/example/demo/` and `src/main/resources/application.properties`.
