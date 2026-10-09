# NexTrade source reconciliation changelog

Date: 2026-10-09

This is a partial repair snapshot based on the latest uploaded code suggestions and the actual contents of the current NexTrade archive. The pasted snippets were compared to current APIs rather than blindly pasted, because several snippets referenced a different version of the project.

## Changes made

- Corrected Bucket4j Maven coordinates for version 8.11.0 to `com.bucket4j:bucket4j_jdk17-core` and centralized its version in the parent POM. Bucket4j's 8.11 release renamed the Java 17 artifact; see https://bucket4j.com/8.11.0/toc.html.
- Removed the redundant `nextrade-service` dependency version in the API module and removed its child-level Spring Boot plugin version override.
- Added Lombok as a provided dependency to the API, service, and persistence modules that use Lombok annotations.
- Added the missing contracts-module dependency to the engine module because the engine strategy interfaces refer to `com.nextrade.contracts.strategy.StrategySignal`.
- Rewrote `MoneyAttributeConverter` to use the actual `Money` record accessors and `Money.of(BigDecimal, String)` factory; added clear invalid-persisted-value handling.
- Rewrote `UserRepositoryAdapter` to match the actual `UserRepository` contract and current domain model API.
- Added `Order.reconstitute(...)` so restored orders can retain the persisted `OrderId` and state.
- Reworked `OrderRepositoryAdapter` to match the actual `OrderRepository` methods and the current `OrderEntity` / identifier model.
- Added `InstrumentJpaRepository` and `InstrumentRepositoryAdapter` to match the current `InstrumentRepository` interface.
- Moved the `EventOutbox` interface to the domain repository layer and updated service/persistence imports so persistence no longer imports a service-layer port.
- Added audit timestamps to `OrderEntity` and a forward migration that adds them to the `orders` table.
- Removed duplicate Java field declarations from the legacy `OrderJpaEntity`, `OutboxEventEntity`, `OutboxEntity`, `IdempotencyEntity`, `PasswordResetTokenEntity`, `EmailVerificationEntity`, and `UserSessionEntity` sources; corrected malformed JPA table names in legacy entities.
- Added the QuickTheories test dependency to the integration-test module.
- Added a source reconciliation document explaining which pasted snippets were intentionally not copied because they do not match the current project APIs.

## Not claimed

This changelog does not imply that the entire platform is now build-clean. The current source still contains larger cross-module inconsistencies, including older/newer parallel persistence entities and service methods that do not align with the current domain APIs. Those require a full reactor repair cycle with Maven available.
## 2026-10-09 — targeted safety patch

- Prevent public registration from granting privileged roles.
- Fail closed when JWT secret configuration is absent/weak; remove fixed development signing key.
- Check active session status during REST access-token authentication.
- Enforce positive cash movements and positive/tick-aligned order prices.
- Correct sell-side self-trade handling, FOK liquidity precheck, and stop-trigger recursion depth.
- Add focused wallet, price, and matching regression tests.
- Full Maven/application verification remains pending.
