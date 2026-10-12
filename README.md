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
- `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY` (API token verification/Auth proxy)
- `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY`, `NEXT_PUBLIC_SITE_URL` (Vercel browser app)
- `NEXT_PUBLIC_API_URL` (Vercel API address)
- `POSTGRES_PASSWORD` (required by Docker Compose; use a unique strong secret)
- `REDIS_HOST`, `REDIS_SESSIONS_HOST`
- `KAFKA_BOOTSTRAP_SERVERS`
- `CORS_ALLOWED_ORIGINS`

## Architecture
- **Domain-Driven Design** with clean layer separation
- **LMAX Disruptor** for high-performance matching engine
- **Transactional Outbox** pattern for reliable event delivery
- **CQRS** for read/write separation
- **Event Sourcing** for audit trail

## Security and deployment
- Supabase Auth handles passwords, email confirmation, sessions, and password recovery.
- Spring validates bearer tokens against Supabase Auth on protected REST and WebSocket requests.
- The browser uses the Supabase SDK with PKCE and automatic session refresh; only the publishable/anon key is public.
- Never place a Supabase service-role key in the frontend or repository.
- Deployment variables and required Supabase URL configuration are documented in [DEPLOYMENT.md](DEPLOYMENT.md).

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

The Supabase email sender is limited to 2 emails per hour and is best-effort; configure custom SMTP in Supabase Auth for production delivery. Spring Boot SMTP is not used for authentication emails.

The local Docker Compose stack requires `POSTGRES_PASSWORD`, `SUPABASE_URL`, and `SUPABASE_PUBLISHABLE_KEY`. See [DEPLOYMENT.md](DEPLOYMENT.md) for Render, Vercel, Supabase, and local settings.
