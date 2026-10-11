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

## Railway API service

Create a Railway service from this repository with the repository root as its build context. The root `Dockerfile` builds the Java API from the full Maven workspace; do not set the service's root directory to `nextrade-api`. Configure the environment variables below in Railway and use the generated HTTPS domain as `NEXT_PUBLIC_API_URL` in Vercel.

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
SPRING_DATASOURCE_URL=jdbc:postgresql://<pooler-host>:<pooler-port>/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
SPRING_DATASOURCE_PASSWORD=<database-password>
JWT_SECRET=<at-least-32-random-bytes>
JWT_KEY_ID_CURRENT=key-1
CORS_ALLOWED_ORIGINS=https://<your-vercel-domain>
MANAGEMENT_HEALTH_DETAILS=never
```

Use the exact host, port, database, and username from the Supabase dashboard's **Connect** dialog; pooler usernames include the project reference. Keep database credentials and JWT secrets only in the API host's secret manager. The NexTrade Supabase project has already been initialized through migration V7, so set `FLYWAY_BASELINE_VERSION=7` on that API host; Flyway will record the existing schema as its baseline and then apply future migrations. For a fresh empty database, leave this variable unset so Flyway runs V1 onward. Do not point this service at another existing database without reviewing its schema and migration history first.

## Current service limits

The UI exposes the parts that currently work: API status, configured instruments, and account sign-in. Email verification/reset has no email transport, auth sessions/challenges are in memory and are lost on restart, prices have no external provider, portfolio accounting is incomplete, and order execution does not reserve/settle cash or positions. Registration therefore cannot complete through a normal user email flow, and order placement is not safe for live use. Add durable session/challenge stores, an email provider, a market data provider, and transactional account/risk settlement before production trading.
