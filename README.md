# springboot-microservice-demo

A small, working Spring Boot 3 microservice: an **order service** for an
enterprise-style backend. It covers the pieces a Java / Spring Boot /
microservices role actually asks for — REST API design, JPA persistence,
validation, error handling, caching, tests, and Docker.

## Stack

- Java 17, Spring Boot 3.3.5, Maven
- Spring Web (REST), Spring Data JPA / Hibernate, Bean Validation
- PostgreSQL (persistence), Redis (cache via Spring Cache abstraction)
- JUnit 5 + Mockito (tests), H2 in-memory db for tests

## What it does

Orders with line items. The total is always computed server-side from the
items — never trusted from the client. Status follows a strict state
machine, enforced in the service layer:

```
PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
PENDING -> CANCELLED
CONFIRMED -> CANCELLED
```

Any other transition is a `409 Conflict`. Single-order reads are cached
(`@Cacheable`); a status change evicts the cached copy (`@CacheEvict`).

## Run it

One command (app + PostgreSQL + Redis):

```bash
docker compose up --build
```

The API is then at `http://localhost:8080`.

Without Docker, run the tests (they need nothing external) or run the app
against the in-memory profile:

```bash
mvn test
SPRING_PROFILES_ACTIVE=local java -jar target/springboot-microservice-demo-0.1.0.jar
```

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/orders` | Create an order → `201` |
| `GET` | `/api/orders/{id}` | One order (cached) → `200`, `404` if missing |
| `GET` | `/api/orders?status=PENDING&page=0&size=20` | Paginated list, optional status filter |
| `PUT` | `/api/orders/{id}/status` | Change status → `200`, `409` on illegal transition |

Errors always come back in one JSON shape: `timestamp`, `status`,
`error`, `message`, `path`, plus `details` for validation failures.

```bash
# create
curl -X POST localhost:8080/api/orders -H 'Content-Type: application/json' -d '{
  "customerName": "Ravi Kumar",
  "items": [
    {"productName": "whitening kit", "quantity": 2, "unitPrice": 10.00},
    {"productName": "toothpaste", "quantity": 1, "unitPrice": 5.50}
  ]
}'
# -> 201, totalAmount 25.50

# read (cached)
curl localhost:8080/api/orders/1

# move it along
curl -X PUT localhost:8080/api/orders/1/status -H 'Content-Type: application/json' \
  -d '{"status": "CONFIRMED"}'

# list confirmed orders
curl 'localhost:8080/api/orders?status=CONFIRMED'
```

## Configuration

All of it is environment variables (see `src/main/resources/application.yml`):

| Variable | Default | Meaning |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ordersdb` | Postgres JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `orders` / `orders` | DB credentials |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis |
| `CACHE_TYPE` | `simple` | `redis` under docker compose, `simple` (in-memory) otherwise |

## Tests

```bash
mvn test
```

20 tests: service unit tests (Mockito), repository tests
(`@DataJpaTest` on H2), controller tests (`@WebMvcTest` + MockMvc).
They run with no database, Redis, or Docker — the test profile uses H2
and an in-memory cache.
