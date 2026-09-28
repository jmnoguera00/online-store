# Online Store

A REST service that returns the applicable price of a product of a retail
brand at a given date and time, resolving overlapping price lists by priority.

Built as a technical test using **hexagonal architecture** and developed with AI assistance
(see [AI-assisted development](#ai-assisted-development)).

---

## Tech stack

| | |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.3.5 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Boilerplate reduction | Lombok (infrastructure layer only) |
| Testing | JUnit 5, Mockito, AssertJ, MockMvc |
| Build | Maven |

**Why Java 21 + Spring Boot 3.3.x?** Java 21 is the most widely adopted LTS
release today (support until ~2031), giving the best balance between modern
language features and ecosystem maturity. Spring Boot 3.3.x is a stable,
production-proven line built on top of it — a safer choice for a timed
technical test than bleeding-edge versions with less community support.

---

## Architecture

The project follows **hexagonal architecture** (ports & adapters), split
into three packages with a strict dependency rule: 
- `domain` knows nothing about `application` or `infrastructure`
- `application` only depends on`domain`
- adapters in `infrastructure` depend on `application` and `domain`


### Where each decision lives

- **The SQL query** (`PriceRepository`) returns **every** rate
  whose date range covers the requested application date — overlaps are
  expected and allowed at this level.
- **The business rule** (which rate wins when several overlap) lives in
  `PriceService`, resolved with a stream:

  ```java
  candidates.stream()
      .max(Comparator.comparingInt(Price::priority))
      .orElseThrow(() -> new PriceNotFoundException(...));
  ```

- **Bean wiring** lives in `infrastructure/config/UseCaseConfiguration`. The
  use case class carries no Spring annotation, so `domain` and `application`
  have no framework dependency at all and can be compiled with plain `javac`.

---

## Running the app

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080` and loads the sample dataset into the `PRICES` table via `data.sql` on startup.

H2 console: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:pricesdb`, user `sa`, empty password).

---

## Endpoint

```
GET /api/v1/prices?applicationDate={ISO-8601 date-time}&productId={number}&brandId={number}
```

All three parameters are required.

**Example Request**

```
GET /api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

**Example response — 200 OK**

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "curr": "EUR"
}
```

### Errors

| Status | When | Example `message` |
|---|---|---|
| `400 Bad Request` | A required parameter is missing | `Required parameter 'productId' is missing` |
| `400 Bad Request` | A parameter cannot be converted to its type | `Parameter 'productId' has an invalid value 'abc': expected a whole number` |
| `404 Not Found` | No price is applicable for the given criteria | `No applicable price found for brandId=1, productId=35455 at 2021-01-01T00:00` |

Both `400` and `404` responses share the same body:

```json
{
  "timestamp": "2026-09-28T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Required parameter 'productId' is missing"
}
```

Errors raised by the framework itself for other reasons (for example `405 Method
Not Allowed`) keep Spring Boot's default error format.

---

## Testing

```bash
mvn test
```

| Class | Type | What it verifies |
|---|---|---|
| `GetApplicablePriceServiceTest` | Unit (Mockito, no Spring) | The priority-selection algorithm with `LoadPricePort` mocked: single candidate, overlapping candidates, input order independence, mixed priorities, empty result |
| `PricePersistenceAdapterTest` | `@DataJpaTest` | Date-range query (overlaps, inclusive boundaries, other product/brand) and the entity to domain mapping of every field |
| `PriceControllerTest` | `@WebMvcTest` (use case mocked) | Request binding: each missing parameter, invalid types and malformed dates return `400` with the error body; `404` mapping; mapping of the response |
| `PriceResponseTest` | Unit | Domain to `PriceResponse` mapping of every field |
| `PriceControllerIntegrationTest` | `@SpringBootTest` + `MockMvc` | End to end: the 5 scenarios of the brief (one `@ParameterizedTest`) plus `404` and `400` |

Scenarios of the technical brief (product `35455`, brand `1`):

| # | Date and time | Expected `priceList` | Expected `price` |
|---|---|---|---|
| 1 | 2020-06-14 10:00 | 1 | 35.50 |
| 2 | 2020-06-14 16:00 | 2 | 25.45 |
| 3 | 2020-06-14 21:00 | 1 | 35.50 |
| 4 | 2020-06-15 10:00 | 3 | 30.50 |
| 5 | 2020-06-16 21:00 | 4 | 38.95 |

---

## Design notes

- **Separate models per boundary.** `PriceResponse` (HTTP) and `PriceEntity`
  (JPA) are infrastructure concerns; the domain only knows the `Price` record.
- **Anemic domain model on purpose.** All orchestration, including the overlap
  resolution rule, lives in the use case. That is why `LoadPricePort` belongs
  to `application`: nothing in the domain model itself calls it.
- **Encapsulated persistence.** `PriceRepository` and
  `PriceAdapter` are package-private; the rest of the application
  only sees the `LoadPricePort` interface.
- **Required parameters by default.** `@RequestParam` is mandatory unless stated
  otherwise, so no extra validation annotations are needed; binding errors are
  translated in `GlobalExceptionHandler`.

---

## AI-assisted development

This project was built with the help of an AI assistant (Claude Code). The
supporting material is part of the repository:

- [`docs/spec.md`](docs/spec.md): the requirements and acceptance scenarios the work was driven by.
- [`AGENTS.md`](AGENTS.md): architecture rules, conventions and commands that any AI agent (or contributor) must follow.