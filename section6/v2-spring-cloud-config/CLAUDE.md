# CLAUDE.md

Persistent project context for Claude Code sessions working in this repository.

## 1. Project Overview

This is a Spring Boot / Spring Cloud microservices demo modeled on a bank ("EazyBank"),
built to exercise cloud-native patterns: centralized configuration, service discovery,
an API gateway, circuit breakers, event-driven messaging, and observability.

Services, at a glance:
- **Spring-Cloud-config-server** — centralized Git-backed configuration server.
- **Discovery-agent** — Eureka service-discovery server (`spring.application.name: eurekaserver`).
- **Gateway-Server** — reactive Spring Cloud Gateway (WebFlux) with OAuth2/Keycloak-secured
  routes, request/response tracing filters, and a fallback controller.
- **Accounts** (`Accounts/project/project`) — the orchestrating service: owns account/customer
  CRUD, calls the Cards and Loans services via OpenFeign (with Resilience4j fallbacks), and
  publishes/consumes Kafka events for async communication status updates.
- **cards** — card CRUD service.
- **loans** — loan CRUD service.
- **message** — Kafka-driven notification service (`email|sms` function composition via
  Spring Cloud Function); no REST controllers.

There is **no root/aggregator `pom.xml`** — each service is an independent Maven project with
its own `spring-boot-starter-parent`, its own Maven Wrapper (`mvnw`/`mvnw.cmd`), and is built
and versioned on its own.

The repo is currently on branch `ImplementingKubernetes`. A sibling directory,
`kubernetes-discovery-server` (outside this directory, in the parent workspace), is a parallel
variant of this same project that swaps Eureka for Spring Cloud Kubernetes's native
`DiscoveryClient` and adds Helm charts / raw Kubernetes manifests. It is a separate, parallel
tree, not a module of this project — treat it as reference material only unless a task
explicitly asks you to work in it.

## 2. Technology Stack

- **Java 17** across all services.
- **Spring Boot**: version differs per service — this is real drift, not a typo:
  - `3.2.5` — Accounts, cards, loans, Spring-Cloud-config-server
  - `3.3.5` — message
  - `4.0.0` — Gateway-Server
  - `4.0.5` — Discovery-agent
- **Spring Cloud**: `2023.0.3` (Accounts, cards, loans, config server, message),
  `2025.1.0` (Gateway-Server), `2025.1.1` (Discovery-agent).
- **Spring Cloud Config** — server (`spring-cloud-config-server`) + clients
  (`spring-cloud-starter-config`) importing from `http://localhost:8071/`.
- **Netflix Eureka** — server (Discovery-agent) + clients (Accounts, cards, loans,
  Gateway-Server) via `spring-cloud-starter-netflix-eureka-client`.
- **Spring Cloud Gateway** (WebFlux/reactive) with **Spring Security OAuth2 Resource Server**
  validating JWTs against a Keycloak realm, plus reactive Redis.
- **OpenFeign** (Accounts → cards/loans) + **Resilience4j** (circuit breaker, retry, rate
  limiter — configured with `default` config blocks in `application.yml`, with fallback
  methods/classes for Feign clients).
- **Spring Data JPA** + **H2** (in-memory, runtime scope) — Accounts, cards, loans.
- **Spring Cloud Stream + Kafka binder** — Accounts (`updateCommunication` consumer,
  `sendCommunication` producer) and message (`email|sms` function composition), decoupled via
  the `send-communication` / `communication-sent` topics.
- **Jakarta Bean Validation** (`@Valid`, `@NotEmpty`, `@Pattern`, etc.) on DTOs and, in several
  controllers, directly on `@RequestParam`s via class-level `@Validated`.
- **Lombok** — used in the three CRUD services (Accounts, cards, loans) via `@AllArgsConstructor`,
  `@Data`/`@Getter`/`@Setter`/`@ToString`, `@NoArgsConstructor`. **Never `@RequiredArgsConstructor`
  or `@Slf4j`** — this repo consistently uses `@AllArgsConstructor` and manual
  `private static final Logger logger = LoggerFactory.getLogger(...)` instead; match that, don't
  introduce the alternatives.
- **springdoc-openapi** (`2.5.0`, webmvc-ui) for Swagger UI on the CRUD services.
- **Testing**: `spring-boot-starter-test` (JUnit 5, Mockito, MockMvc, AssertJ) is on every
  module's classpath but is essentially unused today — see Testing Guidelines.
- **Docker images**: built via `jib-maven-plugin` (no `docker build` needed), pushed to Docker
  Hub as `pragya24verma/<artifactId>:s6`. A couple of modules (e.g. `loans`, `Accounts`) also
  carry a plain `Dockerfile` alongside jib — treat jib as the actual build path unless told
  otherwise.
- **docker-compose** (`docker-compose/{default,qa,prod}/docker-compose.yml`) for local
  orchestration. `default` runs the full stack: Kafka, Keycloak, the app services, and a full
  observability stack (Prometheus, Grafana, Loki [read/write/backend+gateway], Tempo, Alloy,
  MinIO). `qa`/`prod` run only the app services (configserver, eurekaserver, accounts, loans,
  cards) with no observability/Kafka/Keycloak.
- **Kubernetes/Helm**: not present in this directory. Helm charts and raw manifests exist only
  in the sibling `kubernetes-discovery-server` tree mentioned above.

## 3. Repository Structure

```
v2-spring-cloud-config/
├── Spring-Cloud-config-server/     # Config server (port 8071), git-backed, @EnableConfigServer
├── Discovery-agent/                # Eureka server, @EnableEurekaServer, no business code
├── Gateway-Server/                 # Reactive gateway, OAuth2/Keycloak security, trace filters
├── Accounts/project/project/       # Accounts service (port 8080) — see note below
├── cards/                          # Cards CRUD service (port 9000)
├── loans/                          # Loans CRUD service (port 8090)
├── message/                        # Kafka notification functions (port 9010)
└── docker-compose/                 # default/qa/prod compose stacks + observability configs
```

Note: the Accounts module lives at the nested path `Accounts/project/project` (artifact
`com.accounts:project`) — that nesting is intentional/existing, not a mistake to "fix."

Each of the three CRUD services (Accounts, cards, loans) follows the same internal package
layout — reuse this layout for any new code in those services:
`controller`, `service` (+ `service.impl`), `repository`, `entity`, `dto`, `mapper`,
`exception`, `constants`, `audit`. Accounts additionally has `service.client` (Feign clients +
fallbacks for calling cards/loans) and `functions` (the Kafka consumer bean) — it's the only
service that orchestrates the other two.

Gateway-Server, Discovery-agent, Spring-Cloud-config-server, and message do **not** follow the
CRUD layout — they're infrastructure/functional services (gateway routing+security,
discovery-registry, config-serving, and Kafka function composition, respectively) and have no
controller/service/repository/entity layers. Don't force that layout onto them.

One naming inconsistency to be aware of (existing, not to be "corrected" incidentally):
Accounts' custom exceptions have no `Exception` suffix (`CustomerAlreadyExists`,
`ResourceNotFound`), while cards/loans use the suffix (`CardAlreadyExistsException`,
`ResourceNotFoundException`, `LoanAlreadyExistsException`). Match whichever service you're
editing.

## 4. Build and Run Commands

There is no root aggregator — run Maven from inside each service's own directory. Every
service has its own wrapper, so prefer `./mvnw` (or `mvnw.cmd` on Windows) over a system `mvn`.

```
cd Accounts/project/project
./mvnw clean install        # build + run tests
./mvnw test                 # tests only
./mvnw spring-boot:run       # run the service locally
```

Same pattern for `cards`, `loans`, `Gateway-Server`, `Discovery-agent`,
`Spring-Cloud-config-server`, `message` (substitute the directory).

Typical local startup order (each depends on config server / Eureka being up first, per each
service's `application.yml`): **Spring-Cloud-config-server → Discovery-agent → cards/loans →
Accounts → Gateway-Server / message**.

Docker images are built via Jib, not `docker build`:
```
cd <service-dir>
./mvnw compile jib:build     # builds & pushes pragya24verma/<artifactId>:s6
./mvnw compile jib:dockerBuild   # builds to local Docker daemon only, no push
```

Local multi-service stack via Docker Compose:
```
cd docker-compose/default
docker compose up -d         # full stack incl. Kafka/Keycloak/observability
```
(`docker-compose/qa` and `docker-compose/prod` bring up just the app services, no
observability/Kafka/Keycloak.)

## 5. Coding Guidelines

- Match the package layout already used in the service you're touching (see Repository
  Structure above) — don't introduce new top-level packages without a clear reason.
- Keep controllers thin: request mapping, `@Valid`/`@Validated` input validation, and delegating
  to a service interface (`IAccountsServices`, `ICardsService`, `ILoansService`, ...). Business
  logic belongs in the `service.impl` class, not the controller.
  - Note: existing controllers mix constructor injection (`@AllArgsConstructor`) with field-level
    `@Autowired` for secondary dependencies (e.g. `Environment`, contact-info DTOs). This is the
    existing pattern; don't feel obligated to refactor it away as part of an unrelated task.
- Persistence goes through the `repository` layer (`JpaRepository` interfaces) — don't put
  `EntityManager`/JDBC calls directly in services.
- Follow the existing exception-handling pattern: a per-service `exception/GlobalExceptionHandler`
  (`@ControllerAdvice extends ResponseEntityExceptionHandler`) with custom exceptions mapped to
  specific HTTP statuses (`@ResponseStatus` or an explicit `@ExceptionHandler`). Add new
  exceptions the same way rather than introducing a new handling mechanism.
- Use Lombok (`@AllArgsConstructor`, `@Data`/`@Getter`/`@Setter`) and manual SLF4J
  `Logger`/`LoggerFactory` in the CRUD services, matching what's already there — don't switch to
  `@RequiredArgsConstructor` or `@Slf4j` partway through a file/service.
- Use `jakarta.validation` annotations on DTOs/`@RequestParam`s for input validation, consistent
  with existing DTOs.
- Do not change a controller's REST contract (path, method, request/response shape, status
  codes) unless the task explicitly calls for it — other services and the gateway depend on
  these contracts.
- Prefer existing utilities/mappers (e.g. `AccountsMapper`, `CustomerMapper`) over writing new
  mapping code inline.
- Don't introduce a new library/framework (e.g. MapStruct, a different validation framework, a
  different logging facade) when the existing hand-rolled pattern already does the job.

## 6. Testing Guidelines

**Current state, stated plainly**: every one of the seven services has exactly one test class,
always named `<Service>ApplicationTests`, always just `@SpringBootTest` + an empty
`contextLoads()` method. There are no unit tests, no `MockMvc` tests, no Mockito-based service
tests, and **no coverage tool** (no JaCoCo, no custom Surefire config) configured anywhere. So
there is no existing *test* convention to mimic beyond "JUnit 5 + Mockito + MockMvc are already
on the classpath via `spring-boot-starter-test` — use those, don't add new test dependencies."

When adding tests:
- Use JUnit 5 (`@Test`, `@ExtendWith(MockitoExtension.class)`) and Mockito for service-layer
  tests — mock repositories, Feign clients, and `StreamBridge`/messaging components.
- Use `@WebMvcTest` + `MockMvc` + `@MockBean` for controller-layer tests (Accounts, cards, loans
  controllers are synchronous Spring MVC, not WebFlux).
- Cover success paths, validation failures (400s), not-found/exception paths (404s/custom
  exceptions), and — for services — repository/dependency failure paths.
- Never delete or disable an existing test (including the trivial `contextLoads()` ones) to make
  a build pass — fix the actual failure instead.
- Don't write assertion-free or trivially-true tests just to inflate a coverage number if one is
  ever measured; every test should verify real behavior.
- If a task requires measuring coverage, note that no coverage plugin exists yet in any
  `pom.xml` — adding one (e.g. `jacoco-maven-plugin`) is a build-tooling change and should be
  called out to the user before doing it, same as adding a dependency.

## 7. Change Safety Rules

- Stay inside the microservice(s) the task actually concerns — don't touch unrelated services
  "while you're in there."
- Don't bump a Spring Boot/Spring Cloud/library version without an explicit reason tied to the
  task (the existing version drift across services is known and pre-existing — don't try to
  unify it as a side effect of unrelated work).
- Don't add a new Maven dependency without explaining why it's needed and what it replaces/adds
  — `spring-boot-starter-test` already covers JUnit 5/Mockito/MockMvc/AssertJ for testing work.
- Don't change production behavior just to make a test easier to write; if a test genuinely
  requires a small testability change (e.g. extracting a seam), say so and ask first rather than
  changing it silently.
- Never commit real secrets. Note: `Spring-Cloud-config-server/application.yml` currently has an
  `encrypt.key` value checked in — treat this as existing, known configuration; do not copy this
  pattern into new config, and flag it rather than silently "fixing" or removing it.
- Preserve REST contracts and Kafka topic/message shapes (`AccountsMsgDto`, topics
  `send-communication`/`communication-sent`) unless a task explicitly asks to change them —
  other services and the message service depend on them.

## 8. Working Style

- For anything touching more than one file or one service, briefly state the approach before
  making changes.
- Investigate existing controllers/services/DTOs/mappers in the target service before writing
  new code — reuse what's there.
- Keep changes scoped to what was asked; don't drive-by refactor naming inconsistencies,
  version drift, or the mixed DI style noted above unless asked to.
- After making changes, run that service's tests (`./mvnw test` from its own directory) and
  report the actual result — including failures — rather than assuming success.
