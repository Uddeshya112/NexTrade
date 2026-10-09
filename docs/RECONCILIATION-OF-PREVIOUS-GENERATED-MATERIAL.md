# Reconciliation of previous generated material

The earlier AI-generated repair documents were used as source material, but they contained mutually inconsistent implementations and unverified claims. This archive consolidates the intended architecture into one canonical source tree.

## Consolidated corrections

- One HTTP JWT mechanism is retained. The API uses `JwtAuthenticationFilter`; OAuth2 resource-server authentication is not configured.
- The matching engine is dependency-isolated from `nextrade-service` and publishes `TradeExecution` through a functional boundary.
- Stop orders mutate in place from STOP_LOSS/STOP_LIMIT to MARKET/LIMIT, preserving the existing `OrderId`.
- Stop activation is driven by recorded execution prices. Matching-book iteration does not use best bid/ask as a substitute for last trade.
- Stop activation scans and orders all hit stop prices deterministically rather than relying on a comparator-direction early-break that could skip valid levels.
- Password-reset consumption has an atomic repository contract and persistence-side update boundary.
- Verification attempts are associated with the challenge selector, so wrong codes do not silently select a different record.
- Refresh-token rotation is modeled as a state transition requiring the presented digest and active status.
- Redis rate limiting has a single Lua-script increment/initial-expiry operation.
- Client/API contracts are kept separate from service-layer internal types.

## Verification honesty

No historical document's claimed test counts, security-scan counts, or Docker results are treated as evidence. Only checks executed while packaging this archive are recorded in `VERIFICATION-STATUS.md`.
