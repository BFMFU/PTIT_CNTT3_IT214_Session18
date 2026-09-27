# 🏦 RikkeiBank — Backend Microservice System

> Java Spring Boot 4.1.1 | Spring Cloud 2025.1.3 | MySQL | Redis | Kafka | Java 21

---

## 📐 Architecture Overview

```
                        ┌─────────────────────────────────────┐
                        │          API Gateway (:8080)         │
                        │  JWT Filter + Load Balancer           │
                        └──────────────┬──────────────────────┘
                                       │
              ┌────────────────────────┼──────────────────────────┐
              │                        │                          │
    ┌─────────▼────────┐   ┌──────────▼────────┐   ┌────────────▼─────────┐
    │ identity-service  │   │ customer-service   │   │  account-service     │
    │    (:8081)        │   │    (:8082)         │   │     (:8083)          │
    │  JWT/Auth/Tokens  │   │ Customer/Staff/AT  │   │  Accounts + Saga     │
    │  db_identity      │   │  db_customer       │   │  db_account          │
    └──────────────────┘   └───────────────────┘   └──────────────────────┘
                                                              │ Kafka
              ┌───────────────────────────────────────────────┤
              │                        │                          │
    ┌─────────▼────────┐   ┌──────────▼────────┐
    │transaction-service│   │notification-service│
    │    (:8084)        │   │    (:8085)         │
    │  Saga + Records   │   │  Event Consumer    │
    │  db_transaction   │   │  (Kafka Consumer)  │
    └──────────────────┘   └───────────────────┘

Infrastructure:
  config-server   (:8888)  — Spring Cloud Config (native filesystem)
  discovery-server(:8761)  — Eureka Service Registry
  Redis           (:6379)  — Distributed Cache
  Kafka           (:9092)  — Event Broker (KRaft mode)
  MySQL           (:3306)  — Per-service Databases
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- Gradle (or use `./gradlew`)

### Step 1: Start Infrastructure
```bash
docker-compose up -d
```
Starts: MySQL, Redis, Kafka

### Step 2: Start Services (in order)
```bash
# 1. Config Server
cd config-server && ./gradlew bootRun

# 2. Discovery Server (Eureka)
cd discovery-server && ./gradlew bootRun

# 3. API Gateway
cd api-gateway && ./gradlew bootRun

# 4. Identity Service
cd identity-service && ./gradlew bootRun

# 5. Customer Service
cd customer-service && ./gradlew bootRun

# 6. Account Service
cd account-service && ./gradlew bootRun

# 7. Transaction Service
cd transaction-service && ./gradlew bootRun

# 8. Notification Service
cd notification-service && ./gradlew bootRun
```

### Step 3: Verify
- Eureka Dashboard: http://localhost:8761
- Config Server: http://localhost:8888/account-service/default
- API Gateway: http://localhost:8080

---

## 🔑 Authentication

Default credentials (created on first identity-service startup):
| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | ADMIN |

### Login Flow
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGc...",  // Valid 15 minutes
  "refreshToken": "uuid-...",   // Valid 30 days
  "tokenType": "Bearer",
  "expiresIn": 900,
  "role": "ADMIN"
}
```

### Using Tokens
```http
GET http://localhost:8080/api/customers
Authorization: Bearer {accessToken}
```

### Refresh Access Token (seamless experience)
```http
POST http://localhost:8080/api/auth/refresh?refreshToken={refreshToken}
```

### Admin Force Logout
```http
POST http://localhost:8080/api/auth/revoke/{userId}
Authorization: Bearer {adminAccessToken}
```

---

## 💸 Transfer Flow (Saga Choreography)

```
POST /api/transactions/transfer
  → transaction-service: create PENDING transaction
  → Kafka: saga.transfer.initiate
  → account-service: DEBIT from source account
  → Kafka: saga.debit.completed (success) OR saga.debit.failed
  → account-service: CREDIT to destination account
  → Kafka: saga.credit.completed (success) OR saga.credit.failed
  → transaction-service: update to COMPLETED
  → Kafka: notification.transfer.success
  → notification-service: log/notify customer

Rollback Scenario (credit fails):
  → transaction-service: publish saga.compensate.debit
  → account-service: refund source account
  → transaction-service: update to ROLLED_BACK
  → notification-service: notify failure
```

### Transfer API
```http
POST http://localhost:8080/api/transactions/transfer
Authorization: Bearer {customerAccessToken}
Content-Type: application/json

{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 500000,
  "description": "Payment for services"
}
```

Response: `202 Accepted` with pending transaction + sagaId for tracking.

---

## 🛡️ Role-Based Access Control

| Endpoint | ADMIN | TELLER | CUSTOMER |
|---|---|---|---|
| POST /api/auth/register | ✅ | ❌ | ❌ |
| POST /api/auth/revoke/{id} | ✅ | ❌ | ❌ |
| GET /api/customers | ✅ | ❌ | ❌ |
| POST/PUT/DELETE /api/customers | ✅ | ❌ | ❌ |
| GET /api/customers/{id} | ✅ | ✅ | ❌ |
| GET /api/staffs/** | ✅ | ❌ | ❌ |
| GET /api/account-types | ✅ | ✅ | ❌ |
| POST /api/accounts | ✅ | ✅ | ❌ |
| GET /api/accounts/{id} | ✅ | ✅ | Own only |
| GET /api/accounts/my | ❌ | ❌ | ✅ |
| POST /api/transactions/transfer | ❌ | ❌ | ✅ |
| GET /api/transactions | ✅ | ✅ | ❌ |
| GET /api/transactions/my | ❌ | ❌ | ✅ |

---

## ⚡ Redis Cache (Cache-Aside Strategy)

| Cache | Key | TTL | Evicted When |
|---|---|---|---|
| `customers` | `{id}` | 10 min | PUT/DELETE customer |
| `staffs` | `{id}` | 10 min | PUT/DELETE staff |
| `account-types` | `all` / `{id}` | 30 min | Any write on account-type |
| `accounts` | `{id}` | 5 min | Balance update / close |

---

## ⚙️ Circuit Breaker (Resilience4j)

| State | Behavior |
|---|---|
| CLOSED | Normal operation, requests pass through |
| OPEN | Failures ≥ 50% over 10 requests → fast fail, fallback method |
| HALF_OPEN | After 10s: probe with 3 requests to test recovery |

Config in `application.yaml` (shared):
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
```

---

## 🗄️ Databases

| Service | Database |
|---|---|
| identity-service | `db_identity` |
| customer-service | `db_customer` |
| account-service | `db_account` |
| transaction-service | `db_transaction` |

Databases are auto-created by MySQL JDBC URL: `createDatabaseIfNotExist=true`.
Schema is auto-managed by JPA `ddl-auto: update`.

---

## 📋 Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `saga.transfer.initiate` | transaction-service | account-service |
| `saga.debit.completed` | account-service | account-service |
| `saga.debit.failed` | account-service | transaction-service |
| `saga.credit.completed` | account-service | transaction-service |
| `saga.credit.failed` | account-service | transaction-service |
| `saga.compensate.debit` | transaction-service | account-service |
| `notification.transfer.success` | transaction-service | notification-service |
| `notification.transfer.failed` | transaction-service | notification-service |

---

## 🧪 Running Tests

```bash
# Run all tests with coverage
cd account-service && ./gradlew test jacocoTestReport

# Results: build/reports/jacoco/test/html/index.html
```
