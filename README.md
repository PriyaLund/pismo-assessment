# Accounts and Transactions API

This project implements the **Pismo coding assessment **

## Endpoints

- **POST** `/accounts` – create a new account  
- **GET** `/accounts/{accountId}` – fetch account by ID  
- **POST** `/transactions` – create a transaction  
  - Purchases, installment purchases, withdrawals → stored as **negative** amounts  
  - Payments → stored as **positive** amounts  

---

## Run locally

```bash
mvn clean package
java -jar target/pismo-assessment-1.0.0.jar
```

Server runs at: [http://localhost:8080](http://localhost:8080)

---

## Swagger UI

API documentation is available at:  
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## H2 Database Console

The project uses an **in-memory H2 database** (reset on restart).

- Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
- **JDBC URL:** `jdbc:h2:mem:pismo`  
- **Username:** `sa`  
- **Password:** *(leave blank)*  

Example queries:
```sql
SELECT * FROM accounts;
SELECT * FROM transactions;
```

---

## Example Usage

### 1. Create Account
**Request**
```http
POST /accounts
Content-Type: application/json

{
  "document_number": "12345678900"
}
```

**Response**
```json
{
  "account_id": 1,
  "document_number": "12345678900"
}
```

---

### 2. Get Account
**Request**
```http
GET /accounts/1
```

**Response**
```json
{
  "account_id": 1,
  "document_number": "12345678900"
}
```

---

### 3. Create Transaction (Payment)
**Request**
```http
POST /transactions
Content-Type: application/json

{
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 100.00
}
```

**Response**
```json
{
  "transaction_id": 1,
  "account_id": 1,
  "operation_type_id": 4,
  "amount": 100.00
}
```

---

### 4. Create Transaction (Purchase)
**Request**
```http
POST /transactions
Content-Type: application/json

{
  "account_id": 1,
  "operation_type_id": 1,
  "amount": 40.00
}
```

**Response**
```json
{
  "transaction_id": 2,
  "account_id": 1,
  "operation_type_id": 1,
  "amount": -40.00,
  "event_date": "2025-09-11T10:31:00.456Z"
}
```

---

## Docker

Build and run the app in Docker:

```bash
docker build -t pismo-assessment .
docker run --rm -p 8080:8080 pismo-assessment
```
