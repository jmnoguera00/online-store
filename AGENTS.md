# AGENTS.md

Instructions for AI coding agents (and human contributors) working on this repository.
Read `docs/spec.md` first: it defines what the service must do.

## Project

Online Store: a Spring Boot 3 / Java 21 REST service that returns the applicable
price of a product at a date, choosing the highest-priority price when several
overlap. Maven project, in-memory H2 database.

## Commands

```bash
mvn test                # run every test
mvn spring-boot:run     # start the API on http://localhost:8080
```

`mvn test` must pass before any commit.

## Architecture rules (hexagonal)

Package root: `com.onlinestore`.

| Package | Contains | May depend on |
|---|---|---|
| `domain` | Domain model and domain exceptions | Nothing (plain Java) |
| `application` | Use cases (`service`), input ports (`port.in`), output ports (`port.out`) | `domain` |
| `infrastructure` | Adapters (`adapter.in.web`, `adapter.out.persistence`) and Spring configuration (`config`) | `application`, `domain`, frameworks |

Rules:

1. `domain` and `application` must not import Spring, JPA, Jackson or Lombok.
   They must compile with plain `javac`.
2. Beans for use cases are declared in `infrastructure/config`, never with
   `@Service`/`@Component` on classes in `application` or `domain`.
3. Business rules (for example which price wins on overlap) live in `application`
   and are written as Java streams over in-memory data. Adapters do not decide business outcomes.
4. JPA entities and HTTP DTOs stay in `infrastructure`. The domain only knows the
   `Price` record. Mapping happens in the adapters.
5. Output ports are consumed by use cases, so they live in `application/port/out`.
6. Persistence details (`PriceRepository`, `PriceAdapter`)
   are package-private.

## Code conventions

- Everything is written in **English**: identifiers, comments, Javadoc, test
  names, exception and error messages, documentation.
- Constructor injection only. Prefer records for immutable data (domain model,
  queries, DTOs).
- `@RequestParam` parameters are required by default; do not add `@Validated`/`@NotNull`
  for that. Binding errors are translated in `GlobalExceptionHandler` into the
  `ErrorResponse` record (`timestamp`, `status`, `error`, `message`).
- Keep the README in sync with the real behaviour whenever an endpoint, status
  code or error body changes.

## Testing conventions

- Business logic: plain unit tests with Mockito, no Spring context.
- Adapters: slice tests (`@DataJpaTest`, `@WebMvcTest`).
- End to end: `@SpringBootTest` + `MockMvc`; use `@ParameterizedTest` when only
  the data changes between cases.
- Mapping code (`PriceResponse.from`, `PriceAdapter.toDomain`) is tested
  with a distinct value in every field so crossed fields are detected.
- Every error path has a test: not found, missing parameter, invalid type.
