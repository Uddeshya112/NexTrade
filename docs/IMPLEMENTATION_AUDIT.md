# NexTrade implementation audit

**Audit date:** 12 October 2026  
**Scope:** Current checked-out repository at audit time; source review and the command results recorded below. This is a baseline audit only. No application code or deployment settings were changed.

## Inputs and confidence

The attached prompt pack asks this audit to use a “NexTrade Startup Report” dated 12 October 2026. That specific report was not present in the accessible attachment directory or `Downloads`. The available related document is the earlier **NexTrade — Overall Project Report**, dated 11 October 2026. Its cautions are treated as historical context, not current evidence. `compile-current.log` is also historical (11 October) and records compile errors that were not reproduced by the clean compile run below. Current claims in this report are based on the checked-out source and commands run on 12 October.

## Verified project shape

| Area | Observed state |
|---|---|
| Java | Root `pom.xml` sets Java 21; local runtime reports Oracle Java 21.0.12.1. |
| Maven | Root reactor has nine modules: `nextrade-common`, `nextrade-contracts`, `nextrade-domain`, `nextrade-engine`, `nextrade-persistence`, `nextrade-service`, `nextrade-api`, `nextrade-fx-client`, and `nextrade-integration-tests`. Spring Boot is 3.2.5. There is no checked-in Maven wrapper; Maven 3.9.9 was available from the local wrapper cache. |
| Browser frontend | The current browser app is Next.js 16 / React 19 in `nextrade-web`, with `/`, `/auth/callback`, and `/auth/reset-password`. It is not a standalone HTML app as described in parts of the prompt pack. There is no HTML file in the repository to validate as a standalone frontend. |
| Desktop frontend | `nextrade-fx-client` is a JavaFX module and compiled in the successful root compile/package attempts before the integration-test failure. A JavaFX GUI launch was not attempted. |
| REST | `AuthController`: `/api/v1/auth` registration, login, refresh, logout, forgot/reset password, verification, change password, logout-all, and `/me`. `TradingController`: `/api/v1/orders` create/list/cancel/modify. `PortfolioController`: `/api/v1/portfolio`. `MarketController`: `/api/v1/market/prices` and `/instruments`. Market routes are public; other routes require authentication in `SecurityConfig`. |
| WebSocket | STOMP endpoint `/ws`; inbound frames are checked by `JwtChannelInterceptor`. The destination handler at `/app/market.subscribe` only checks that `instrumentId` exists; no market subscription/broadcast implementation was found. WebSocket origin patterns are localhost and `*.example.com`, which do not include the documented Vercel production domain. |
| Persistence | PostgreSQL/JPA entities and nine Flyway migrations (V1–V9) are present. V7 seeds `NEX` and `DEMO` instruments. V9 links Supabase identities to NexTrade users. The API’s Supabase provisioner and the email outbox publisher/ledger code have transaction annotations, but the primary `TradingService` order flow has no enclosing transaction. |
| Hosting/config | Root Dockerfile builds the API with Java 21; Docker Compose defines Postgres and Redis plus the API, but not Kafka. `DEPLOYMENT.md` documents Render, Supabase, and Vercel configuration. There is no `vercel.json`; Next.js is expected to be deployed with `nextrade-web` as the root directory. Docker CLI is not installed/available in this environment, so container build and compose startup were not verified. |
| CI | `.github/workflows/maven.yml` runs Maven packaging and Next.js lint/build on pushes and pull requests to `main`. The baseline API jar issue described below has since been corrected and verified. |

## Feature status grounded in source

| Feature | Status | Evidence / limit |
|---|---|---|
| Supabase browser authentication | **Implemented, locally buildable** | Next.js uses `@supabase/supabase-js`; signup/login/recovery/callback code exists. Live email delivery and deployed redirect behavior were not exercised in this audit. |
| Backend token validation | **Implemented, not production-verified** | `SupabaseTokenVerifier` asks Supabase Auth to validate the bearer token, checks confirmation, and maps the identity; protected REST requests use the filter. No live Supabase request was made. A separate legacy Spring `AuthController`/`AuthService` flow also remains, so there are two auth surfaces to reconcile. |
| User-specific order access | **Partial** | REST derives user identity from authenticated principal. Order listing queries by user, and cancel/modify compare order owner. Cross-account REST isolation was not proved by an end-to-end test. The integration module contains token/auth tests, but the entire system was not run against a database. |
| Order lifecycle | **Partial** | `TradingService` validates user, instrument, side/type, quantity, and required prices, persists the order, submits to the in-memory matching engine, then saves again. A unique database index exists for `(user_id, client_order_id)`, but the service does not look up/replay an existing client order before resubmission. Cash/position reservation and execution settlement are not connected to this flow; the service comment explicitly says those repositories must be supplied before live trading. |
| Portfolio/accounting | **Missing on the API path** | `GET /api/v1/portfolio` calls `TradingService.getPortfolioSummary`, which unconditionally throws `IllegalStateException("Portfolio persistence and cash/position accounting are not implemented yet")`. A `PortfolioService` in the repository is only a diagnostics stub. Domain/JPA portfolio, holding, trade, and ledger types do not make this endpoint operational. |
| Market quotes | **Partial; simulator only in backend** | Backend `MarketDataService` reads an in-memory `MarketSimulationEngine` price map. It returns only `Price`, with no source/mode/as-of/stale metadata. No price update producer was found in the scanned production source; absent a caller, `/prices` can return no values. No backend provider abstraction or historical candles endpoint was found. |
| Browser market UI | **Partial, separate data path** | Current dashboard embeds TradingView widgets and labels them as an external feed, with a note that exchange delay may apply. The frontend does not use a typed API client to connect those widgets to the Spring API. It does not show explicit `LIVE`/`DELAYED`/`SIMULATED` state and as-of timestamp for values. Its Watchlist page is a public TradingView symbol widget, not a personal saved watchlist. |
| WebSocket market data | **Missing / nonfunctional handler** | STOMP authentication/configuration code exists, but `MarketDataWebSocketController.subscribe` only validates a field and never subscribes or emits data. The configured allowed origins also omit the documented Vercel host. |
| Trade plans, journal, reviews, analytics | **Missing** | No corresponding persisted REST resource or usable browser flow was found in the active frontend/API source. |
| Replay/backtesting | **Missing** | No replay/backtest path is exposed through the active API/frontend. Existing isolated engine mechanisms/tests do not establish a replay product flow. |
| Billing / Pro entitlements | **Missing** | No billing provider, webhook lifecycle, subscription store, server entitlement endpoint, or plan enforcement was found. |
| Fake/static frontend values | **Mostly removed from current dashboard; API demos remain** | The Next.js dashboard does not contain `Math.random` or mock prices/portfolio balances/AI scores; it explicitly marks Portfolio, Alerts, and AI as unavailable. Backend migrations seed demo instruments, and `UserControllerSupport` still constructs a hard-coded `{noop}` demo user/instrument as a Spring component; its use by an active controller was not found. Do not treat demo seed instruments as market data. |
| Migrations / runtime schema | **Present, runtime unverified** | Nine Flyway SQL migrations exist and V9 identity linking is visible. No database was started or migrated in this audit, so schema/JPA compatibility and migration behavior against the deployed Supabase schema remain unverified. |

## Build and validation results

Commands were run against the checked-out repository on Java 21.0.12.1. The Maven executable was invoked from the cached Maven 3.9.9 installation because `mvn` is not on PATH and `mvnw` is absent.

| Command | Result |
|---|---|
| `java -version` | **PASS** — Java 21.0.12.1. |
| `mvn -version` | **Not available by command name** — Maven is not on PATH. Cached Maven 3.9.9 was used directly. |
| `mvn -B validate` (cached Maven 3.9.9) | **PASS** — all nine modules validated. The first sandboxed attempt could not reach Maven Central; the actual validation was rerun with dependency network access. |
| `mvn -B clean compile` (cached Maven 3.9.9) | **PASS** — all nine modules compiled with Java release 21. |
| `mvn -B test` (cached Maven 3.9.9) | **PASS** — Surefire reports: 959 tests, 0 failures, 0 errors, 0 skipped. This is not a database-backed or deployed end-to-end result. |
| `mvn -B verify -pl nextrade-integration-tests -am` | **FAIL** — test compilation cannot resolve `com.nextrade.api.security` from `SupabaseTokenVerifierTest` and `SupabaseAuthenticationFilterTest`. The selected reactor reaches the API module’s Spring Boot repackaging before compiling the dependent tests, so the executable API jar is not usable as a normal dependency. |
| CI’s exact `mvn -B package` | **FAIL** — same integration-test compilation errors for missing `com.nextrade.api.security` classes. This is a confirmed CI blocker even though `mvn test` passes. |
| `npm run lint` in `nextrade-web` | **PASS** — ESLint completed successfully. |
| `npm run build` in `nextrade-web` | **PASS** — Next.js 16.4.0 production build and TypeScript pass; routes `/`, `/auth/callback`, `/auth/reset-password` generated. |
| HTML duplicate-ID / inline-JS validation | **Not applicable to the active app** — no standalone HTML file exists in the repo; frontend is Next.js TSX. |
| Docker/Compose build or runtime | **Not run** — Docker command is unavailable in this environment. |
| API startup, migration against Supabase, production Vercel/Render smoke test | **Not run** — no runtime services or live deployment access used in this audit. |

Build warnings observed include Java platform-default UTF-8 encoding and JavaFX shade-plugin overlapping resources. They did not fail `clean compile`; the Maven package CI failure above is separate and must be fixed.

## Baseline blockers and risks

1. **CI package failure (resolved):** the executable Spring Boot artifact now uses a classifier, leaving the plain API jar usable by downstream modules. `mvn -B package` passes with all tests enabled.
2. **Portfolio endpoint is guaranteed to fail:** `TradingService.getPortfolioSummary` throws unconditionally. No portfolio UI should imply account values until the ledger/positions path is implemented and reconciled.
3. **Orders are not a complete paper-trading account flow:** the current order path lacks connected cash/position reservation and persisted fill/settlement reconciliation. The unique index alone does not make client-order retries return the original order.
4. **Market data paths do not join up:** frontend widgets are third-party embeds; backend prices are in-memory simulation values without mode/source/time fields; no candles or functioning WebSocket publisher were found.
5. **WebSocket production origin mismatch (resolved):** REST and WebSocket now use the same explicit `CORS_ALLOWED_ORIGINS` configuration. Render still needs the exact production domain set in its environment.
6. **Deployment remains unproven:** Docker and live database startup/migrations, Supabase auth/email, Vercel deployment, Render health, and end-to-end user flows were not exercised. A green frontend build does not verify these services.
7. **Documentation/config mismatch:** `README.md` refers to `docker-compose.prod.yml`, which is absent; the present compose file is `docker-compose.yml`. Docker Compose also does not provision Kafka although `application.yml` has a Kafka localhost default.
8. **Legacy demo bean:** `UserControllerSupport` constructs a no-op-password demo identity. It is not referenced by the active controller code found, but should be removed or isolated before treating the API as production-ready.

## Dependency-ordered implementation plan

1. Fix the Maven module/package boundary and CI so the actual checked-in `mvn -B package` and `mvn verify -pl nextrade-integration-tests -am` commands pass without exclusions or skipped tests.
2. Decide and document the canonical product/runtime architecture (active Next.js browser frontend plus Spring API/JavaFX, or the standalone HTML described in the supplied pack) and the canonical authentication surface. Update stale README/deployment instructions accordingly.
3. Complete backend paper-account accounting: persisted fills, cash ledger, position/lot updates, reservation/release, transactional/idempotent settlement and a working portfolio response. Add ownership, retry, and hand-calculated reconciliation tests before exposing account totals.
4. Implement a normalized quote/candle provider contract and source/mode/as-of/staleness metadata; label simulation explicitly. Connect quote updates to a functioning WebSocket stream only after verifying auth and production origins.
5. Add a typed browser API layer and wire the primary user flow to real backend contracts. Implement saved watchlists and supported order/portfolio UI with loading, empty, offline, and error states. Keep unsupported journal/review/AI/alerts clearly unavailable.
6. Implement trade plan, review, journal, and analytics only after persisted order/fill/portfolio accounting is verified. Consider replay/backtesting only after historical data and deterministic fill tests. Billing and Pro entitlements come after product/legal/provider checks and must be enforced server-side.
7. Add true service/database integration tests and browser smoke coverage; verify migrations against an isolated Supabase/Postgres-compatible test database, Docker image startup, Render health, Vercel root route and cross-origin requests. Then produce a separate release-readiness report; this audit is not a production-readiness approval.

## Implementation progress after the baseline audit

The initial CI blocker was fixed after the audit, without changing test behavior:

- Spring Boot now attaches its executable artifact as `nextrade-api-1.0.0-SNAPSHOT-exec.jar`, preserving the normal API jar for downstream test compilation.
- Both Dockerfiles now copy the executable `-exec.jar` artifact.
- GitHub Actions still runs the full Maven `package` lifecycle and now also installs the locked frontend dependencies and runs `npm run lint` plus `npm run build`.
- Re-run result after the fix: `mvn -B package` **PASS**, with 959 tests passed, 0 failures/errors/skips. The Maven `verify -pl nextrade-integration-tests -am` selection has not yet been rerun after the fix.

The rows in **Build and validation results** and **Baseline blockers and risks** document the pre-fix snapshot and should be read as historical baseline evidence; the CI package blocker is resolved in the current working tree.

## Audit conclusion

The repository is a substantial Java 21/Spring multi-module paper-trading codebase with a Next.js frontend. The Maven package and integration verification commands currently pass. Order retry idempotency and original-quantity persistence are implemented. It is **not yet a working end-to-end trading workspace**: the portfolio endpoint deliberately throws, account cash/position accounting and persisted execution settlement are not connected, the UI does not call the backend market/order APIs, and production deployment/database behavior is unverified. This document now records implementation progress beyond the original baseline audit.

## Current implementation milestone (12 October 2026)

- Fixed order retry idempotency at the service boundary. Requests require a non-empty client order ID; exact retries return the existing order, changed payloads using the same key receive HTTP 409, and a concurrent insert collision is re-read before matching.
- Added `original_quantity` through additive Flyway migration V10, backfilled from the existing `quantity` column. The persistence adapter now round-trips original and working quantities separately, retaining original size after order modification or partial fill.
- Added tests for retry identity, no duplicate matching, and changed-payload conflict.
- Verification: `mvn -B package` passes with **961 tests, 0 failures, 0 errors, 0 skipped**. `git diff --check` passes.
- These changes do not implement account cash/position reservation or settlement. Full account balances and live trading must remain unavailable; market simulation/data, profile, and deployment checks remain outstanding.
## Frontend status correction (12 October 2026)

The active frontend is Next.js (not the standalone HTML described by the pack). The account dashboard already attributes market widgets to TradingView and labels portfolio, personal watchlists, AI insights and alerts as unavailable/shared. The top-bar label was corrected from a generic “External market feed” green indicator to “TradingView widgets”; widget script load failures now produce a visible accessible message. See `docs/FRONTEND_QA.md` for manual checks. `npm run lint` and `npx tsc --noEmit` passed. The most recent repeat `npm run build` was blocked by an existing Next build lock; the initial combined command output was not captured, so build status for this edit is unverified.
- Follow-up regression: added a database unique-key race test; the order idempotency test class now passes 3/3 tests. The complete Maven package suite passes after this addition (962 total tests, 0 failures, 0 errors, 0 skipped).
