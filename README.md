# Pismo Assessment (Spring Boot)

Implements the **Customer Account & Transactions** API.

- Java 17 + Spring Boot 3
- H2 in-memory DB
- Swagger/OpenAPI UI
- JUnit 5 tests
- Dockerfile

## Run

```bash
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Endpoints

- `POST /accounts` — create account: `{ "document_number": "12345678900" }`
- `GET /accounts/{id}` — fetch account
- `POST /transactions` — create transaction: `{ "account_id": 1, "operation_type_id": 4, "amount": 123.45 }`

Operation types: `1 PURCHASE`, `2 INSTALLMENT PURCHASE`, `3 WITHDRAWAL`, `4 PAYMENT`.

## Tests

```bash
mvn test
```

## Docker

```bash
docker build -t pismo-assessment .
docker run --rm -p 8080:8080 pismo-assessment
```

