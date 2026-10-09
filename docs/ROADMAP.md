# NexTrade delivery roadmap

## Phase 1 — domain and contracts
Define identifiers, money/price/quantity semantics, order lifecycle, user lifecycle, repository ports, and API DTO contracts. Keep these modules free of Spring and infrastructure dependencies.

## Phase 2 — deterministic execution
Implement price-time order-book matching, FIFO queues, FOK/IOC/GTC/DAY behavior, self-trade prevention, stop-loss/stop-limit activation from actual last-trade executions, stable order identity, partitioned engine lanes, and replay-oriented policies.

## Phase 3 — risk and accounting
Keep reservation, risk, fee, settlement, ledger, portfolio, and reconciliation boundaries explicit. A placement decision must not bypass the persisted reservation/risk boundary, and settlement must be idempotent.

## Phase 4 — authentication and security
Use one HTTP JWT authentication path, kid-based key selection before signature verification, refresh-token rotation by compare-and-swap, atomic password-reset consumption, verification-attempt accounting by challenge selector, and atomic Redis rate limiting.

## Phase 5 — API and terminal
Expose REST and authenticated STOMP boundaries. Keep the JavaFX client isolated from persistence internals and use explicit DTO/API contracts.

## Phase 6 — persistence and delivery infrastructure
Provide PostgreSQL/Flyway mappings, outbox persistence boundaries, Docker topology, CI scaffolding, and operational documentation.

## Phase 7 — verification gate
Run `mvn -U clean verify`, integration tests, Docker startup/health checks, dependency/security scans, and production configuration validation on a machine with Maven, Docker, and dependency-network access.
