# NexTrade repair status

Status: **targeted security and invariant patch; whole application still unverified**.

This pass changed the actual repository sources, not the revision-specific pasted snippets. See `PATCHSET-20261009.md` for the exact scope.

The common/domain/engine production sources compile directly under Java 21, and a standalone smoke test passed four targeted regression scenarios. The full Maven reactor was not run because Maven is not installed here. Framework-dependent modules, migrations, persistence wiring, UI, Docker, and end-to-end behavior remain unverified.

Known blockers remain in service/domain/contracts APIs and persistence wiring. The current app still uses in-memory repositories and a no-op event outbox; do not use it for real-money trading.
