# NexTrade update changelog — 2026-10-09

## Changes integrated from the latest submitted fix set

- Corrected `JwtChannelInterceptor` to use this repository's `AccessTokenService` and `SessionRepository` APIs, verify access-token type and active session, authorize `SEND` only to `/app/**`, restrict admin topics, and enforce a 1 MiB STOMP content-length ceiling.
- Aligned `WebSocketConfig` with the endpoint actually used by the JavaFX client, with `/ws`, `/topic`, `/queue`, and `/user` destinations.
- Aligned `StompWebSocketClient` with `/ws` and added cleanup on close.
- Added the missing JavaFX `ComponentFactory` and `NavigationManager` classes referenced by the sign-in flow.
- Repaired the sign-in view's invalid `PasswordField.setShowPasswordToggle` call by implementing an ordinary JavaFX `TextField`/`PasswordField` toggle, and wired the Sign In button.
- Replaced stale dependency-injection registrations that referenced view/viewmodel types absent from this project with registrations for the current login screen.
- Corrected the JavaFX application scene root.
- Added shared auth DTOs and API client methods used by the current login and forgot-password viewmodels, and aligned the auth controller to this repository's `AuthResult`/`AuthService` API.
- Added reset-token validity checks and changed email verification to return the verified user so the domain user can be activated.
- Removed the duplicated `/api` context-path prefix because controllers already declare `/api/v1/...`.
- Fixed the missing semicolon in `MarketController` and declared JJWT dependencies directly in the API module.
- Included the latest submitted patch text under `docs/input/` for provenance.

## Compatibility note

The supplied patch text includes snippets from a different NexTrade API version (for example, an `AuthService.TokenClaims` API and a three-argument `MatchingEngine` constructor). Those snippets were not copied literally where they conflict with this repository's actual API signatures. Compatible intent was adapted to the code in this project.

## Verification status

- Java 21 core compilation (`nextrade-common`, `nextrade-contracts`, `nextrade-domain`, `nextrade-engine`): PASS in this environment.
- Maven POM XML parsing: PASS.
- ZIP integrity check: PASS.
- Full `mvn clean verify`: NOT RUN because Maven is not installed and the environment cannot resolve Maven Central.
- Full API/service/client build: NOT VERIFIED. The code tree still contains cross-module contract drift, especially `TradingService`/`TradingController` versus the current `MatchingEngine`, `RiskEngine`, and `OrderRequest` signatures. Do not treat this package as a certified clean build or production-ready trading system until those compile/runtime gates pass.
- Email delivery is not configured. Password-reset and verification challenges are not delivered to an inbox in this development setup; the API reports this limitation rather than pretending mail was sent.
