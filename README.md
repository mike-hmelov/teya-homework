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

### 1. Accounts

#### Create an Account

```
POST /accounts
Content-Type: application/json
```

**Request body:**

| Field  | Type   | Required | Description          |
|--------|--------|----------|----------------------|
| `name` | string | yes      | Name of the account  |

**Example:**

```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe"}'
```

```json
{
  "id": "a1b2c3d4-...",
  "name": "John Doe",
  "balance": 0
}
```

#### Get Account Details (Balance)

```
GET /accounts/{id}
```

**Example:**

```bash
curl http://localhost:8080/accounts/a1b2c3d4-...
```

```json
{
  "id": "a1b2c3d4-...",
  "name": "John Doe",
  "balance": 3500.50
}
```

---

### 2. Account Transactions

#### Record a Transaction

```
POST /accounts/{id}/transactions
Content-Type: application/json
```

**Request body:**

| Field       | Type   | Required | Description             |
|-------------|--------|----------|-------------------------|
| `direction` | string | yes      | `"IN"` or `"OUT"`       |
| `amount`    | number | yes      | Amount (can be decimal) |

**Example — Deposit:**

```bash
curl -X POST http://localhost:8080/accounts/a1b2c3d4-.../transactions \
  -H "Content-Type: application/json" \
  -d '{"direction":"IN","amount":50.0}'
```

```json
{
  "id": "t1x2y3z4-...",
  "amount": 50.0,
  "direction": "IN",
  "created": "2026-05-25T19:35:00Z"
}
```

**Error — Insufficient funds (428):**

```json
{
  "detail": "Insufficient funds",
  "status": 428
}
```

#### Get Account Transactions

```
GET /accounts/{id}/transactions
```

**Example:**

```bash
curl http://localhost:8080/accounts/a1b2c3d4-.../transactions
```

```json
[
  {
    "id": "t1x2y3z4-...",
    "amount": 50.0,
    "direction": "IN",
    "created": "2026-05-25T19:35:00Z"
  }
]
```

---

### 3. Global Transactions

#### Get Transaction by ID

```
GET /transactions/{id}
```

**Example:**

```bash
curl http://localhost:8080/transactions/t1x2y3z4-...
```

```json
{
  "id": "t1x2y3z4-...",
  "amount": 50.0,
  "direction": "IN",
  "created": "2026-05-25T19:35:00Z"
}
```

#### Search Transactions

```
POST /transactions/search
Content-Type: application/json
```

**Request body (all optional):**

| Field         | Type   | Description                                    | Default |
|---------------|--------|------------------------------------------------|---------|
| `direction`   | string | `"IN"` or `"OUT"`                              | -       |
| `minAmount`   | number | Minimum amount                                 | -       |
| `maxAmount`   | number | Maximum amount                                 | -       |
| `limit`       | number | Maximum number of results to return (max 1000) | 100     |
| `offset`      | number | Number of results to skip                      | 0       |

**Example:**

```bash
curl -X POST http://localhost:8080/transactions/search \
  -H "Content-Type: application/json" \
  -d '{"direction":"OUT","limit":10}'
```

```json
[
  {
    "id": "t9x8y7z6-...",
    "amount": 15.0,
    "direction": "OUT",
    "created": "2026-05-25T19:36:00Z"
  }
]
```

---

## Design Decisions & Assumptions

Simple and straight forward implementation using Spring Boot and Kotlin

### Pros

* Spring - well known framework. May handle security, authN/authZ, db connectivity, transaction management and many more
* Spring Boot - allow to simply create executable binary, dockerize and deploy to any container supported platform
* Spring and Spring Boot stack is well known - so easy to develop and maintain
* Kotlin - language that supports multiple paradigms and allow to write not overbloated and clean code
* Standard "onion" style architecture is applied - also well known to devs around, so easy to onboard new devs and
  maintain

### Cons

* Pretty heavy and resource demanding comparing to other possible solution
* Default performance is not so good comparing to other options

Assumptions
1. **In-memory storage** — Data is stored in a simple `MutableList` and is lost when the server restarts. No database is
   required.
2. **No authentication/authorisation** — All endpoints are publicly accessible.
3. **No concurrent access safety** — The in-memory store is not thread-safe. For a demo application this is acceptable.