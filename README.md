# Online Store IV

A REST service that returns the applicable price for a product of a retail
chain (brand) at a given date and time, resolving overlapping price rates
by priority.

Built as a technical test using **hexagonal architecture** (ports & adapters).

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

  Because this use case only depends on the `LoadPricePort` interface (not
  on JPA or Spring), the algorithm is fully unit-testable with a mocked
  port — no database, no Spring context required.

---

## Running the app

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080` and loads the sample dataset into the `PRICES` table via `data.sql` on startup.

H2 console: `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:pricesdb`, user `sa`, no password).

---

## Endpoint

```
GET /api/v1/prices?applicationDate={ISO_DATE_TIME}&productId={long}&brandId={long}
```

**Example request**

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

**No applicable rate — 404 Not Found**

```json
{
  "timestamp": "2026-09-24T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "No existe tarifa aplicable para brandId=1, productId=35455 en la fecha 2021-01-01T00:00"
}
```

Invalid or missing parameters return `400 Bad Request` with the same error
shape.

---

## Testing strategy

Three isolated test levels, each targeting a different layer:

| Class | Type                                       | What it verifies |
|---|--------------------------------------------|---|
| `PriceServiceTest` | Unit test (Mockito, no Spring context)     | The priority-selection algorithm (streams), with `LoadPricePort` mocked — covers a single candidate, multiple overlapping candidates, three mixed priorities, and the empty-result case |
| `PriceModelPersistenceAdapterTest` | `@DataJpaTest` (JPA slice only)            | The date-range query returns the correct overlapping candidates and the entity→domain mapping is correct |
| `PriceControllerIntegrationTest` | `@SpringBootTest` + `MockMvc` (end-to-end) | The full HTTP contract (all response fields) across all 5 scenarios from the test brief, plus a negative (404) case |

```bash
mvn test
```

**Scenarios from the technical brief** (product `35455`, brand `1`):

| # | Date & time | Expected `priceList` | Expected `price` |
|---|---|---|---|
| 1 | 2020-06-14 10:00 | 1 | 35.50 |
| 2 | 2020-06-14 16:00 | 2 | 25.45 |
| 3 | 2020-06-14 21:00 | 1 | 35.50 |
| 4 | 2020-06-15 10:00 | 3 | 30.50 |
| 5 | 2020-06-16 21:00 | 4 | 38.95 |


