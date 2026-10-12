# NexTrade local run and deployment

## Run locally

Requirements: Java 21, Maven 3.9+, Node.js 20.9+, npm, and Docker Compose.

1. Copy `.env.example` to `.env` and replace both placeholder secrets. `JWT_SECRET` must be at least 32 UTF-8 bytes.
2. Start the backend dependencies and API from the repository root:

   ```sh
   docker compose up --build
   ```

3. In another terminal, run the browser app:

   ```sh
   cd nextrade-web
   cp .env.example .env.local
   npm install
   npm run dev
   ```

4. Open [http://localhost:3000](http://localhost:3000). The API health check is [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health).

The API runs Flyway migrations on startup and seeds two clearly labelled demo instruments. Market data, email delivery, account balances, and order settlement are not production integrations yet. Do not use this build for real-money trading.

## Render API service

Create a Render Web Service from this repository and use the repository root as its root directory/build context so Render finds the root `Dockerfile` and the full Maven workspace. Do not set the root directory to `nextrade-api`. The container defaults to the `prod` Spring profile and writes logs to stdout/stderr for Render to collect. Configure the environment variables below in Render and use the generated HTTPS domain as `NEXT_PUBLIC_API_URL` in Vercel.

## Vercel frontend

Import this GitHub repository in Vercel and set **Root Directory** to `nextrade-web`. Use Node.js 20.x or later and add this build environment variable:

```text
NEXT_PUBLIC_API_URL=https://<your-api-host>
```

The public API URL is compiled into the browser bundle; set it before the Vercel build. Add the Vercel origin to the API host's `CORS_ALLOWED_ORIGINS`, for example `https://nextrade-web.vercel.app` (comma-separated origins are supported). Vercel hosts the Next.js frontend; the Java API must run on a Java/Docker host separately.

## Supabase Postgres and Java API

Create a Supabase project, then use its **Session pooler** connection values for a typical IPv4 application host. Configure the API host with:

```text
SPRING_PROFILES_ACTIVE=prod
FLYWAY_BASELINE_VERSION=7
SPRING_DATASOURCE_URL=jdbc:postgresql://<session-pooler-host>:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.cepissjsdjypatskmoku
SPRING_DATASOURCE_PASSWORD=<database-password>
JWT_SECRET=<at-least-32-random-bytes>
JWT_KEY_ID_CURRENT=key-1
CORS_ALLOWED_ORIGINS=https://<your-vercel-domain>
MANAGEMENT_HEALTH_DETAILS=never
```

Use the exact session-pooler host from the Supabase dashboard's **Connect** dialog; the shared session pooler uses port `5432`, database `postgres`, and username `postgres.<project-ref>`. The Docker entrypoint accepts either the JDBC URL shown above or Supabase's native `postgres://` / `postgresql://` connection URI, converting it to JDBC form and removing any embedded user info. Keep the username and password in the separate required environment variables, and keep credentials and JWT secrets only in the API host's secret manager. The NexTrade Supabase project has migrations V1–V8 recorded and its existing schema has no Flyway history table. The production profile therefore baselines an existing schema at V7, then applies V8 (which creates its indexes with `IF NOT EXISTS`); on a fresh empty database Flyway runs V1 onward. Do not point this service at another existing database without reviewing its schema and migration history first.

## Current service limits

The UI exposes the parts that currently work: API status, configured instruments, and account sign-in. Email verification/reset has no email transport, auth sessions/challenges are in memory and are lost on restart, prices have no external provider, portfolio accounting is incomplete, and order execution does not reserve/settle cash or positions. Registration therefore cannot complete through a normal user email flow, and order placement is not safe for live use. Add durable session/challenge stores, an email provider, a market data provider, and transactional account/risk settlement before production trading.
