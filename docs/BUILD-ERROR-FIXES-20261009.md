# Build error fixes — 2026-10-09

Applied against the actual `NexTrade-STRUCTURED` source tree after reviewing the IDE error screenshots.

- Added JUnit Jupiter as a test-scoped dependency to `nextrade-domain` and `nextrade-engine`, where test sources use `org.junit.jupiter.api`.
- Updated invalid `Identifier.UserId`/`Identifier.OrderId` and related nested-type references to the repository's actual top-level identifier record classes (`UserId`, `OrderId`, etc.), across Java sources.
- Added the missing `java.util.UUID` import to `OutboxEventRepository`.
- Removed the unsupported `filter` attribute from the Jakarta Persistence `@Index` declaration in `OutboxEventEntity`.
- Removed a stray `@Column(name = "published_at")` annotation placed on the `@PrePersist` lifecycle method.
- Added explicit queries for outbox unpublished-event retrieval, case-insensitive username/email lookup, and recent verification-token counts, avoiding invalid Spring Data derived-query parsing at startup.

## Verification limitations

The container has Java 21 but no Maven executable or populated local Maven dependency cache, so a full Maven reactor build, JUnit execution, Spring/JPA startup, and integration tests could not be run here. Do not consider the project production-ready until `mvn clean verify` passes in an environment with dependency access.
