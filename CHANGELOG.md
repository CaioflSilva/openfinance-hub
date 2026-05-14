# Changelog

All notable changes to **OpenFinance Hub** are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).  
Versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.6.0] — 2026-05-13

### Added

- **Async flow completed** — `TransactionListener` now fetches the transaction by ID after consuming the RabbitMQ event, sets `status=COMPLETED` and `processedAt=now()`, and persists the update. Logs a warning and continues if the transaction is not found, avoiding dead-lettering valid messages (`TransactionListener`)
- **Pagination on transaction listings** — `GET /api/transactions` and `GET /api/transactions/account/{id}` now accept `page`, `size` and `sort` query parameters via Spring `Pageable`. Default: `size=20`, `sort=createdAt,DESC`. Both endpoints return `Page<TransactionResponse>` with `content`, `totalElements`, `totalPages` and pagination metadata (`TransactionController`, `TransactionService`, `TransactionRepository`)
- **Swagger Authorize button** — `@SecurityScheme(bearerAuth)` added via `OpenApiConfig` so the Swagger UI shows the JWT authorization dialog (`OpenApiConfig`)
- **`@ApiResponse` on all controllers** — every endpoint now documents its possible HTTP response codes (200/201/400/401/403/404/409/422) directly in the OpenAPI spec (`AuthController`, `BankAccountController`, `TransactionController`, `DashboardController`)

### Fixed

- **Race condition on balance update** — `BankAccountRepository` now exposes `findByIdForUpdate()` with `@Lock(PESSIMISTIC_WRITE)`. `TransactionService.create()` uses this method so concurrent requests on the same account serialize at the database level instead of reading stale balance (`BankAccountRepository`, `TransactionService`)
- **Internal error details leaked in 500 responses** — the generic exception handler now logs the full stack trace internally via `log.error()` and returns only `"Erro interno do servidor"` to the client, preventing exposure of class names or exception messages (`GlobalExceptionHandler`)
- **`ddl-auto: update` in production config** — changed to `validate` so Hibernate verifies the schema on startup without mutating it. The test profile (`application-test.yml`) retains `create-drop` so the test suite continues to work without Flyway (`application.yml`)

### Docs

- README project structure corrected to match the actual source tree — removed non-existent `messaging/producer/` and `messaging/consumer/` subdirectories, added `dto/event/` (`README.md`)

---

## [0.5.0] — 2026-05-10

### Added

- **JWT authentication** — `POST /api/auth/register` and `POST /api/auth/login` endpoints with BCrypt password hashing and HMAC-SHA256 signed tokens (`AuthController`, `AuthService`)
- **Bank account management** — full CRUD at `/api/bank-accounts` with per-user ownership validation (`BankAccountController`, `BankAccountService`)
- **Async transaction processing** — `POST /api/transactions` publishes to RabbitMQ; `TransactionListener` consumes events and logs transaction details (foundation for future notification/processing logic) (`TransactionController`, `TransactionService`, `TransactionListener`)
- **Transaction queries** — `GET /api/transactions`, `GET /api/transactions/{id}`, `GET /api/transactions/account/{accountId}`
- **Redis cache on Dashboard** — `GET /api/dashboard/summary` cached with 5-minute TTL via `@Cacheable`; cache evicted on every new transaction via `@CacheEvict` (`DashboardService`, `DashboardController`)
- **Global exception handler** — `ResourceNotFoundException` (404) and `ForbiddenException` (403) handled centrally (`GlobalExceptionHandler`)
- **Domain models** — `User`, `BankAccount`, `Transaction` entities with enums: `Role`, `AccountType`, `TransactionType`, `Category`
- **JPA repositories** — `UserRepository`, `BankAccountRepository`, `TransactionRepository`
- **RabbitMQ configuration** — direct exchange, dead-letter queue, Jackson message converter (`RabbitMQConfig`)
- **Redis configuration** — `@EnableCaching` with 5-minute TTL cache manager (`RedisConfig`)
- **spring-dotenv** — `me.paulschwarz:spring-dotenv:4.0.0` added so `./mvnw spring-boot:run` reads `.env` automatically without manual `export`

### Security

- Secrets removed from `application.yml` and `docker-compose.yml`; replaced with environment variable references (`${DB_PASSWORD:postgres}`, `${RABBITMQ_PASSWORD:guest}`, `${JWT_SECRET:fallback-only-for-tests-do-not-use-in-prod}`, `${JWT_EXPIRATION:86400000}`)
- `.env.example` added with placeholder values for developer onboarding
- `.env` and `.claude/` added to `.gitignore` to prevent accidental secret leakage
- `docker-compose.yml` updated to load passwords via `env_file: .env`

### Tests

- **`AuthServiceTest`** — 3 unit tests covering register (happy path, duplicate email) and login
- **`BankAccountServiceTest`** — 4 unit tests covering create, list, getById (ownership), and delete
- **`TransactionServiceTest`** — 3 unit tests covering create, list, and getById
- **`AuthFlowIntegrationTest`** — 3 integration tests against a real PostgreSQL container (register, login, duplicate email rejection)
- **`TransactionFlowIntegrationTest`** — 1 integration test for transaction creation flow
- **`OpenfinanceHubApplicationTests`** — context loads test
- **Testcontainers** — PostgreSQL container spun up per integration test suite (no mocks for persistence layer)

### CI/CD

- GitHub Actions workflow (`.github/workflows/ci.yml`) runs `./mvnw test` on every push and pull request to `main`
- Java 21 (Temurin) with Maven dependency cache
- Surefire test reports uploaded as artifact on failure

### Docs

- README: added `🚀 Tech Stack` section with shields.io badges for all 10 technologies
- README: updated `✨ Features` with accurate bullet list (removed unimplemented items)
- README: replaced `📋 Endpoints` with `📡 API Endpoints` — correct routes, descriptions and JWT column
- README: updated `🗺️ Roadmap` marking all shipped items as done
- README: added `Setup` step documenting `.env` configuration and JWT secret generation commands

### Fixed

- `JwtService` was reading `${security.jwt.secret}` and `${security.jwt.expiration}` but `application.yml` defines the properties under `app.jwt.*` — aligned `@Value` annotations to `${app.jwt.secret}` and `${app.jwt.expiration}`, fixing a startup failure
- JWT filter was being registered twice by Spring Boot's auto-configuration (once via `SecurityFilterChain`, once via `FilterRegistrationBean`) — disabled the duplicate registration

---

[0.5.0]: https://github.com/CaioflSilva/openfinance-hub/commits/main
