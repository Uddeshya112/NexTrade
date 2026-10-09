# Latest pasted repair material: integration notes

The latest pasted text contains code snippets from a different revision of NexTrade. It was reviewed against the actual checked-out source before integration.

## Applied or reconciled

- Corrected Bucket4j 8.11.0 coordinates to `com.bucket4j:bucket4j_jdk17-core:8.11.0` and centralized the version. Bucket4j renamed the Java-17 artifact in its 8.11 series.
- Corrected `MoneyAttributeConverter` to use the `Money` record accessors (`amount()` and `currency()`) and the existing `Money.of(BigDecimal, String)` factory.
- Rewrote `UserRepositoryAdapter` to implement the actual `UserRepository` interface and current domain model accessors.
- Reconciled `OrderRepositoryAdapter` with the current `OrderRepository` interface, `OrderEntity`, and standalone identifier records. Added `Order.reconstitute(...)` to preserve persisted order identity.
- Added the missing `InstrumentJpaRepository` and `InstrumentRepositoryAdapter` matching the current domain interface.
- Moved the `EventOutbox` port to the domain repository layer so persistence does not import a service-layer interface.
- Added missing Lombok dependencies to modules that use Lombok and removed the wrong Bucket4j coordinate/version from the API POM.
- Removed a duplicate `clientOrderId` field from the legacy `OrderJpaEntity` source.

## Intentionally not copied verbatim

- The pasted `JwtChannelInterceptor` called `AuthService.validateTokenFull(...)` and cast the authentication principal to an `Identifier.UserId`, neither of which matches the current source contract. It also nested SEND payload validation inside the SUBSCRIBE branch, making SEND validation unreachable. The repository's current interceptor already has separate CONNECT, SEND, and SUBSCRIBE handling, so that implementation was retained.
- The pasted SockJS endpoint `/ws/market/websocket` did not match the current raw WebSocket client URL `/ws`. The current endpoint/client pairing was retained rather than introducing an endpoint mismatch.
- The pasted JavaFX snippets call `PasswordField.setShowPasswordToggle(...)`, which is not a standard JavaFX API, and one snippet references `ValidatableViewModel` while the current source classes do not extend it. Those snippets were not copied over the current working-source layout.

## Verification limits

This is a source-level repair pass, not a verified release. Maven is not installed in the build environment, so a full reactor compile, JUnit run, Testcontainers integration run, and runtime Spring context startup were not executed. The repository still needs a full Maven compile to reveal remaining service/domain/controller API mismatches. In particular, this pass does not claim that the complete repository builds cleanly.
