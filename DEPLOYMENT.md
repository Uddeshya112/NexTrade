# NexTrade deployment and Supabase Auth

## Deployment layout

- The Next.js frontend is hosted by Vercel from `nextrade-web`.
- The Spring Boot API remains a Docker web service on Render, built from the repository root.
- Supabase provides Auth and PostgreSQL. The API accepts a bearer token only after asking Supabase Auth to validate it and confirm the user's email. The API maps the verified Supabase identity to an existing NexTrade account by email, or creates a linked account once. The identity link is stored in Flyway migration V9 so existing order and portfolio ownership IDs remain intact.
- The API uses only Supabase's publishable/anon key for Auth verification and the desktop Auth proxy. Never put a service-role key in this repository, Vercel, or the browser.

## Supabase dashboard setup

In **Authentication → URL Configuration** set:

- **Site URL:** `https://nex-trade-eight.vercel.app`
- **Redirect URLs:** `https://nex-trade-eight.vercel.app/auth/callback`, `https://nex-trade-eight.vercel.app/auth/reset-password`, `http://localhost:3000/auth/callback`, and `http://localhost:3000/auth/reset-password`.

In **Authentication → Providers → Email**, enable email/password, require email confirmations, and set the minimum password length to at least 8 characters. Leave password recovery enabled. Use the standard confirmation and recovery templates, which must preserve Supabase's confirmation URL/action link. Test any customized template with both signup and recovery links.

Supabase's default email service is limited to 2 emails per hour and is best-effort. For real users, configure a custom SMTP provider in the Supabase Auth SMTP settings and verify the sender/domain there. Render does not send these messages; the API's Spring SMTP dependency and mail health check were removed because they served only the old custom verification flow. See [Supabase password-based Auth and email sending](https://supabase.com/docs/guides/auth/passwords) and [custom SMTP setup](https://supabase.com/docs/guides/auth/auth-smtp).

## Vercel frontend

Import the GitHub repository, set **Root Directory** to `nextrade-web`, and use Node.js 20.9 or later. Configure the following for **Production**, **Preview**, and **Development** as appropriate, then redeploy so the public values are included in the client build:

```text
NEXT_PUBLIC_API_URL=https://nextrade-6iia.onrender.com
NEXT_PUBLIC_SITE_URL=https://nex-trade-eight.vercel.app
NEXT_PUBLIC_SUPABASE_URL=https://<project-ref>.supabase.co
NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY=<publishable-key-or-legacy-anon-key>
```

The Supabase URL and publishable/anon key are public client configuration. They are not service-role secrets. Restrict production redirects in Supabase to the exact Vercel production routes above. For preview deploys, use a separate safe preview configuration or add only the specific preview origins you intend to use.

## Render API

Keep the existing Docker Web Service root/build context at the repository root. Keep these variables in Render:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<session-pooler-host>:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
SPRING_DATASOURCE_PASSWORD=<Supabase-database-password>
FLYWAY_BASELINE_VERSION=7
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_PUBLISHABLE_KEY=<publishable-key-or-legacy-anon-key>
NEXTRADE_WEB_URL=https://nex-trade-eight.vercel.app
CORS_ALLOWED_ORIGINS=https://nex-trade-eight.vercel.app
MANAGEMENT_HEALTH_DETAILS=never
```

Use the exact database host, username, and password shown in the Supabase **Connect** dialog. The current deployment uses the Supabase session pooler on port 5432; keep `sslmode=require`. Store the database password only in Render's secret environment settings. Do not configure `JWT_SECRET`, Spring SMTP variables, or `NEXTRADE_EMAIL_VERIFICATION_ENABLED` for this Auth implementation. After the V9 migration is deployed, confirm Render logs show Flyway applying V9 before testing a protected API.

For local development, copy root `.env.example` to `.env`; set the local Postgres password, Supabase URL, and publishable/anon key. Copy `nextrade-web/.env.example` to `nextrade-web/.env.local` and set the matching `NEXT_PUBLIC_` values. Then run `docker compose up --build` from the repo root and `npm install && npm run dev` in `nextrade-web`.

## Authentication behavior

- Signup, confirmation email, resend, login, refresh, logout, and password recovery use Supabase Auth.
- The browser uses the official Supabase JavaScript SDK with PKCE, session persistence, automatic token refresh, and confirmation/recovery callback routes.
- The API verifies every supplied access token by calling Supabase Auth's `/auth/v1/user` endpoint with the bearer token and publishable key. It rejects invalid, expired, and unconfirmed identities before protected REST or WebSocket requests are accepted. It does not trust the browser's signed-in flag or user ID.
- Existing NexTrade users are linked by verified email on their first Supabase-authenticated request. New identities receive a NexTrade account and durable Supabase-to-NexTrade ID mapping. Do not delete or re-create `app_user`, order, wallet, or portfolio data during this migration.
- Password reset requires the Supabase-issued recovery session at `/auth/reset-password`; the page verifies the session with Supabase before calling `updateUser`.

## What still needs live verification

Local builds cannot prove Supabase SMTP delivery or a production callback without dashboard credentials and a mailbox you can access. After setting dashboard/env values, test with a new test mailbox: signup and email confirmation, login before/after confirmation, bad credentials, forgot-password email, recovery link and password update, logout/session refresh, and an authenticated request with valid, expired, and malformed bearer tokens. Also call `/api/v1/market/instruments` without a token to confirm the existing public market API still responds. The built-in Supabase email sender may throttle these tests; configure custom SMTP for production.

NexTrade's market data is still demo data and its order/portfolio accounting is not suitable for real-money trading; this Auth migration does not change those capabilities.
