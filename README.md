# NexTrade

## Overview
NexTrade is a paper-trading workspace with a Java 21 / Spring Boot API, a Next.js web client, and a JavaFX desktop client. Market data is simulated or supplied by an attributed third-party widget; the app does not connect to a brokerage or place live trades.

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
- Maven 3.9+ (the repository does not include Maven Wrapper scripts)
- Node.js 22 and npm for the web client
- Docker & Docker Compose
- PostgreSQL 16 (provided by Docker Compose)
- A Supabase project for web authentication

### Build
```bash
mvn clean verify
```

### Run Tests
```bash
mvn test
mvn verify -pl nextrade-integration-tests -am
```

### Run the API locally with Docker

Copy `.env.example` to `.env`, set a unique `POSTGRES_PASSWORD`, and provide the Supabase project URL and publishable key before starting the stack:

```bash
docker compose up -d --build
```

### Run the web client

In a second terminal:

```bash
cd nextrade-web
cp .env.example .env.local
npm ci
npm run dev
```

### Run JavaFX Client
```bash
cd nextrade-fx-client
mvn javafx:run
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
mvn test

# Integration tests (requires Docker)
mvn verify -pl nextrade-integration-tests -am

# Architecture tests
mvn test -pl nextrade-integration-tests -Dtest=ArchitectureRulesTest

# Property-based tests
mvn test -pl nextrade-engine -Dtest=OrderBookPropertyTest

# Concurrency tests
mvn test -pl nextrade-engine -Dtest=MatchingEngineConcurrencyTest
```

## Docker Deployment
```bash
docker compose up -d --build
```

## License
Proprietary - NexTrade Inc.

## Repair notes for VS Code diagnostics

The Supabase email sender is limited to 2 emails per hour and is best-effort; configure custom SMTP in Supabase Auth for production delivery. Spring Boot SMTP is not used for authentication emails.

The local Docker Compose stack requires `POSTGRES_PASSWORD`, `SUPABASE_URL`, and `SUPABASE_PUBLISHABLE_KEY`. See [DEPLOYMENT.md](DEPLOYMENT.md) for Render, Vercel, Supabase, and local settings.
