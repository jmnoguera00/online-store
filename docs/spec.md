# Specification

Summary of the technical test requirements this project implements. Written as
a working spec: it is what the code and the tests are checked against.

## Context

The e-commerce database has a `PRICES` table holding the final retail price
(PVP) and the price list that applies to a product of a brand between two dates.

| Field | Meaning |
|---|---|
| `BRAND_ID` | Foreign key of the brand of the group (1) |
| `START_DATE`, `END_DATE` | Date range in which the price list applies |
| `PRICE_LIST` | Identifier of the applicable price list |
| `PRODUCT_ID` | Product code |
| `PRIORITY` | Tie-breaker: if two price lists overlap in a date range, the one with the highest numeric priority applies |
| `PRICE` | Final selling price |
| `CURR` | ISO currency code |

## Functional requirements

1. Expose a REST query endpoint that accepts:
   - application date
   - product identifier
   - brand identifier
2. It returns:
   - product identifier
   - brand identifier
   - price list to apply
   - application dates (start and end)
   - final price to apply
3. When several price lists cover the requested date, the one with the highest
   `PRIORITY` wins.
4. Data is stored in an in-memory database (H2) initialised with the sample data.

## Sample data

| BRAND_ID | START_DATE | END_DATE | PRICE_LIST | PRODUCT_ID | PRIORITY | PRICE | CURR |
|---|---|---|---|---|---|---|---|
| 1 | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1 | 35455 | 0 | 35.50 | EUR |
| 1 | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2 | 35455 | 1 | 25.45 | EUR |
| 1 | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3 | 35455 | 1 | 30.50 | EUR |
| 1 | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4 | 35455 | 1 | 38.95 | EUR |

Field names may change and new fields may be added; data types are a free choice.

## Acceptance scenarios (REST endpoint tests)

All for product `35455` and brand `1`.

| # | Request date and time | Expected price list | Expected price |
|---|---|---|---|
| 1 | 2020-06-14 10:00 | 1 | 35.50 |
| 2 | 2020-06-14 16:00 | 2 | 25.45 |
| 3 | 2020-06-14 21:00 | 1 | 35.50 |
| 4 | 2020-06-15 10:00 | 3 | 30.50 |
| 5 | 2020-06-16 21:00 | 4 | 38.95 |

## Non-functional requirements

- Spring Boot service; quality of design and construction is evaluated.
- Code quality is evaluated.
- Correct results in the tests are evaluated.
- Hexagonal architecture (`infrastructure`, `application`, `domain`).
- Unit tests of the use cases and of the stream-based algorithms.

## Decisions taken where the brief is silent

| Topic | Decision |
|---|---|
| Endpoint | `GET /api/v1/prices` with query parameters (pure query, safe and cacheable) |
| Date format | ISO-8601 date-time, e.g. `2020-06-14T16:00:00` |
| Range boundaries | Inclusive on both ends (`start <= date <= end`) |
| No applicable price | `404 Not Found` with the standard error body |
| Missing or malformed parameter | `400 Bad Request` with the standard error body |
| Response fields | The required ones plus the currency (`curr`) |
