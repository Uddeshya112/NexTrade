# Verification status

**Current status: targeted patch applied; full build remains unverified.**

Passed in this patch pass:

- Direct Java 21 compilation of common/domain/engine production sources (including the engine's strategy contract).
- Syntax compilation of newly added domain/engine regression test sources using temporary JUnit API stubs. This is not a test-runner result.
- Standalone executable smoke assertions covering negative wallet operations, FOK with own-side liquidity, sell-side self-trade handling, and zero-price orders.

Not yet verified:

- Full Maven reactor compile/test/verify; Maven is not installed in this environment.
- API, service, JavaFX, and integration-test compilation with their external dependencies.
- Spring Boot context startup, JWT environment wiring, database migrations, persistence mapping, Docker, and E2E flows.
- Dependency, secret, and container security scans.

See `PATCHSET-20261009.md` and `FINAL-VERIFICATION-REPORT.md`. Do not treat this archive as production-ready.
