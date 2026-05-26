# Tiny Ledger 🏦

A simple REST API for recording financial transactions (deposits and withdrawals), viewing account balance, and browsing
transaction history.

## Prerequisites

- **Java 17** (or later)

No other software is required. The project uses the Gradle wrapper, so you don't need to install Gradle separately.

## Getting Started

```bash
# Clone the repository
git clone <repository-url>
cd tiny-ledger

# Run the application
./gradlew bootRun
```

The server starts on **http://localhost:8080**.

Swagger UI is available at  **http://localhost:8080/swagger-ui/index.html**.

`http` folder contains [Bruno](https://www.usebruno.com/) project to test API

## Running Tests

```bash
./gradlew test
```

## API Reference

All endpoints are scoped to a specific account. Create an account first, then use its `id` for subsequent requests.

---

### Create an Account

```
POST /accounts
Content-Type: application/json
```

**Request body:**

| Field  | Type   | Required | Description             |
|--------|--------|----------|-------------------------|
| `name` | string | yes      | Display name of account |

**Example:**

```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice"}'
```

**Response — 201 Created** (with `Location: /accounts/{id}` header):

```json
{
  "id": "a1b2c3d4-e5f6-...",
  "name": "Alice",
  "balance": 0,
  "transactions": []
}
```

**Error — 400 Bad Request** (validation failure):

```json
{
  "error": "validation_error",
  "details": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ]
}
```

---

### Get an Account

```
GET /accounts/{id}
```

**Path parameters:**

| Parameter | Description  |
|-----------|--------------|
| `id`      | Account UUID |

**Example:**

```bash
curl http://localhost:8080/accounts/a1b2c3d4-e5f6-...
```

**Response — 200 OK:**

```json
{
  "id": "a1b2c3d4-e5f6-...",
  "name": "Alice",
  "balance": 350.00,
  "transactions": [
    {
      "amount": 500.00,
      "direction": "IN",
      "timestamp": "2026-05-27T19:35:00+00:00"
    },
    {
      "amount": 150.00,
      "direction": "OUT",
      "timestamp": "2026-05-27T19:36:00+00:00"
    }
  ]
}
```

**Error — 404 Not Found:**

```json
{
  "error": "Account not found"
}
```

---

### Get Account Transactions

```
GET /accounts/{id}/transactions
```

Returns the transaction list for the specified account as a JSON array (streamed via `Flux`).

**Example:**

```bash
curl http://localhost:8080/accounts/a1b2c3d4-e5f6-.../transactions
```

**Response — 200 OK:**

```json
[
  {
    "amount": 500.00,
    "direction": "IN",
    "timestamp": "2026-05-27T19:35:00+00:00"
  },
  {
    "amount": 150.00,
    "direction": "OUT",
    "timestamp": "2026-05-27T19:36:00+00:00"
  }
]
```

**Error — 404 Not Found:**

```json
{
  "error": "Account not found"
}
```

---

### Create a Transaction

```
POST /accounts/{id}/transactions
Content-Type: application/json
```

**Path parameters:**

| Parameter | Description  |
|-----------|--------------|
| `id`      | Account UUID |

**Request body:**

| Field       | Type          | Required | Description                              |
|-------------|---------------|----------|------------------------------------------|
| `direction` | string (enum) | yes      | `"IN"` (deposit) or `"OUT"` (withdrawal) |
| `amount`    | decimal       | yes      | Positive monetary amount (e.g. `150.00`) |

**Example — Deposit (IN):**

```bash
curl -X POST http://localhost:8080/accounts/a1b2c3d4-e5f6-.../transactions \
  -H "Content-Type: application/json" \
  -d '{"direction":"IN","amount":500.00}'
```

**Response — 200 OK:**

```json
{
  "amount": 500.00,
  "direction": "IN",
  "timestamp": "2026-05-27T19:35:00+00:00"
}
```

**Example — Withdrawal (OUT):**

```bash
curl -X POST http://localhost:8080/accounts/a1b2c3d4-e5f6-.../transactions \
  -H "Content-Type: application/json" \
  -d '{"direction":"OUT","amount":150.00}'
```

**Error — 422 Unprocessable Entity** (insufficient funds):

```json
{
  "error": "Insufficient funds"
}
```

**Error — 404 Not Found** (account does not exist):

```json
{
  "error": "Account not found"
}
```

**Error — 400 Bad Request** (validation failure):

```json
{
  "error": "validation_error",
  "details": [
    {
      "field": "amount",
      "message": "must be greater than 0"
    }
  ]
}
```

---

## Design Decisions & Assumptions

A bit less naive implementation comparing to master branch

### Pros

1. Still Spring and Spring boot and kotlin is used including all advantages, however
1. **Reactive Spring Boot (WebFlux)** - a bit more advanced stack is used - more performance, more throughput out the
   box
1. **CQRS** pattern

### Cons

1. More advanced stack require more skilled stuff to develop and support
2. Onboarding is a bit more complex

### Assumptions

1. **In-memory storage** — Data is stored in a simple `MutableList` and is lost when the server restarts. No database is
   required.
2. **No authentication/authorisation** — All endpoints are publicly accessible.
3. **No concurrent access safety** — The in-memory store is not thread-safe. For a demo application this is acceptable.