# Error resolution report — partial pass

## Resolved in this snapshot

| Reported/root issue | Change |
|---|---|
| `io.github.bucket4j:bucket4j-core:8.11.0` could not be resolved | Changed to the valid Java 17 artifact coordinates `com.bucket4j:bucket4j_jdk17-core:8.11.0` and centrally managed the version. |
| `MoneyAttributeConverter` calls missing getters and wrong factory overload | Switched to the current record accessors `amount()` and `currency()` and `Money.of(BigDecimal, String)`. |
| `UserRepositoryAdapter` has wrong identifier/enums/methods and extra interface methods | Reimplemented only the actual `UserRepository` interface methods using current `UserId`, `UserRole`, `UserStatus`, `Username`, and `Email` APIs. |
| `OrderRepositoryAdapter` uses nested identifiers, wrong entity type and non-existent methods | Aligned it to `OrderRepository`, standalone ID records and `OrderEntity`; added a domain restoration factory. |
| Engine strategy code imports `StrategySignal` from contracts but engine POM omitted contracts | Added `nextrade-contracts` to engine dependencies. |
| Persistence imported a service-layer outbox interface | Moved the port to the domain repository layer and updated service/persistence imports. |
| Duplicate fields / malformed legacy table names in JPA entity source | Removed duplicate declarations and normalized table names in the affected legacy entities. |

## Not verified / remaining blockers

- Full Maven reactor compilation and test execution were not run because Maven is not installed and network DNS is unavailable in this environment.
- `nextrade-service` still has API mismatches against the current domain and contracts, including record-style versus JavaBean-style accessors and old identifier/type names.
- Persistence contains parallel legacy and newer entity families. Their table mappings and migrations need a single canonical strategy; do not treat this package as database/runtime verified.
- Runtime wiring for wallet, portfolio, trade, and outbox persistence must be verified, and the current outbox adapter is an in-process development adapter, not a durable production outbox.
- JavaFX, Spring API, security integration, and all integration tests remain unverified as a complete build.
