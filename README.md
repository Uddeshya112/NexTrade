# NexTrade - Algorithmic Trading Platform

## Overview
NexTrade is a production-grade algorithmic trading platform built with Java 21, Spring Boot 3.2, and JavaFX 21.

## Architecture
- **nextrade-common**: Shared kernel (value objects, identifiers, enums, events)
- **nextrade-contracts**: Canonical API contracts (DTOs only)
- **nextrade-domain**: Pure domain logic (entities, repositories)
- **nextrade-engine**: High-performance matching engine (LMAX Disruptor)
- **nextrade-persistence**: JPA entities, Flyway migrations, repository adapters
- **nextrade-service**: Application services (Auth, Trading, Market Data, Ledger)
- **nextrade-api**: REST/WebSocket API (Spring Boot 3.2)
- **nextrade-fx-client**: JavaFX 21 desktop client
- **nextrade-integration-tests**: Testcontainers integration tests

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (via Docker)
- Redis 7 (via Docker)
- Kafka 3.6 (via Docker)

### Build
```bash
./mvnw clean install -DskipTests
```

### Run Tests
```bash
./mvnw test
./mvnw verify -pl nextrade-integration-tests
```

### Run Locally with Docker
```bash
docker compose -f docker-compose.prod.yml up -d
```

### Run JavaFX Client
```bash
cd nextrade-fx-client
./mvnw javafx:run
```

## Configuration
Environment variables:
- `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASS`
- `JWT_SECRET` (min 32 chars)
- `POSTGRES_PASSWORD` (required by Docker Compose; use a unique strong secret)
- Optional key rotation: `JWT_KEY_ID_CURRENT`, `JWT_PREVIOUS_SECRET`, `JWT_KEY_ID_PREVIOUS`
- `JWT_CURRENT_SECRET`, `JWT_PREVIOUS_SECRET`
- `JWT_KEY_ID_CURRENT`, `JWT_KEY_ID_PREVIOUS`
- `REDIS_HOST`, `REDIS_SESSIONS_HOST`
- `KAFKA_BOOTSTRAP_SERVERS`
- `CORS_ALLOWED_ORIGINS`
- `JWT_SECRET`

## Architecture
- **Domain-Driven Design** with clean layer separation
- **LMAX Disruptor** for high-performance matching engine
- **Transactional Outbox** pattern for reliable event delivery
- **CQRS** for read/write separation
- **Event Sourcing** for audit trail

## Security
- Argon2id password hashing
- JWT with HMAC-SHA256 + key rotation
- Short-lived access tokens (15min) + refresh tokens (30 days)
- Refresh token rotation with replay detection
- Rate limiting (Redis + Lua scripts)
- Account lockout after failed attempts
- Secure token storage (OS credential vaults)

## Testing
```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify -pl nextrade-integration-tests

# Architecture tests
./mvnw test -pl nextrade-integration-tests -Dtest=ArchitectureRulesTest

# Property-based tests
./mvnw test -pl nextrade-engine -Dtest=OrderBookPropertyTest

# Concurrency tests
./mvnw test -pl nextrade-engine -Dtest=MatchingEngineConcurrencyTest
```

## Docker Deployment
```bash
docker compose -f docker-compose.prod.yml up -d
```

## License
Proprietary - NexTrade Inc.

## Repair notes for VS Code diagnostics

The project includes `docs/ERRORS-FIXED-FROM-VSCODE-DIAGNOSTICS.md`, which records the source-level fixes made from the supplied VS Code diagnostics. Open the extracted root folder (the folder containing this `pom.xml`) and run **Maven: Update Project** from the VS Code Command Palette after allowing Maven dependency download. Then run `mvn -U clean verify`. The complete build/test run has not been verified in this repair environment because Maven is not installed here.


**Security note:** the API intentionally refuses to start without `JWT_SECRET` containing at least 32 UTF-8 bytes. Do not commit secrets to this repository. The compose stack also requires `POSTGRES_PASSWORD`.
