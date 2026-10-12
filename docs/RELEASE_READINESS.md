# NexTrade release readiness

**Status: NOT READY FOR PUBLIC PAPER-TRADING RELEASE.** This audit is a software status report, not financial, legal, security, or market-data licensing certification.

## Verified in this change

| Area | Result | Evidence |
|---|---|---|
| Maven build and tests | Pass | Final `mvn -B clean test`: 962 tests, 0 failures/errors/skips. |
| Web production build | Pass | Final `npm run build` in `nextrade-web`; Next.js compiled and prerendered the app routes. |
| Web lint | Pass | Final `npm run lint` in `nextrade-web`. |
| Backend quote contract | Implemented | REST and STOMP use the shared contract DTO with mode, source and `asOf`; API module no longer imports the engine quote type. |
| Demo market screen | Implemented | Markets/Watchlist fetch active instruments and quote metadata from the configured API and show loading, empty, unavailable and error states. |
| Demo quote bootstrap | Implemented | Only active `NEX`/`DEMO` instruments receive fixed practice quotes, marked `SIMULATED`; no real symbol receives a generated quote. |
| Executions | Implemented | Trade executions are persisted by the matching-engine callback and an ownership-checked executions endpoint reads them back. |
| Portfolio failure mode | Corrected | Portfolio route returns explicit `501 PORTFOLIO_ACCOUNTING_UNAVAILABLE` rather than throwing an opaque 500 or inventing a balance. |

## Release blockers and unfinished prompt-pack acceptance

- Cash and share reservation, fill settlement into wallet/holdings, release on cancellation, and full cash/position reconciliation are not wired. The current ledger journal is not a substitute for a user portfolio. Keep order trading behind a paper-trading release gate until this is complete and tested against a real database.
- Portfolio balances/holdings and persistent user watchlists are unavailable. `GET /api/v1/portfolio` reports the limitation explicitly.
- There are no persisted candle/history APIs, planned-risk snapshots, deterministic trade reviews, analytics/journal, replay, server-side Pro entitlements, or billing webhook flow.
- The current web client does not complete the full sign-in → paper order → reconciled portfolio → review/journal journey. Authentication uses Supabase; it does not itself complete the backend account/trading workflow.
- No browser end-to-end run or PostgreSQL/Testcontainers run was performed in this environment. Docker is unavailable here. Startup against configured Supabase/PostgreSQL must still be verified.
- The first GitHub Actions run passed Maven, frontend lint and frontend build, then failed in the optional Maven dependency-submission action while parsing its dependency graph. That optional step was removed; follow-up run 33 passed all workflow steps.
- `npm ci` reported 5 high-severity dependency advisories. Review `npm audit` details and update or document affected packages before a public release.
- Live broker execution and live-money trading are out of scope and must remain disabled. Do not enable billing or claim market data is licensed for redistribution without owner and professional review.

## Required before release

1. Implement account bootstrap, idempotent cash/position reservations, fill accounting, cancel/reject release and portfolio snapshots in one consistent persistence model.
2. Add PostgreSQL integration tests for concurrent orders, retries, cancellation races, settlement replay, ownership isolation and ledger/portfolio reconciliation.
3. Complete persisted watchlists and the quote/candle/history contract before connecting charts to NexTrade data.
4. Complete review/journal/analytics only from persisted trades and plans; defer replay, entitlements and billing until their earlier gates pass.
5. Rerun clean Maven tests, frontend lint/build, DB-backed tests and browser journeys; inspect all navigation and mobile/keyboard states.
6. Verify exact production CORS origins, HTTPS, secrets, backups/restore, privacy and deletion/export flows, licensing and legal/payment requirements before deployment.

Passing the current software checks does not establish financial, legal, security or production readiness.
