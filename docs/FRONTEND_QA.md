# NexTrade Frontend Manual QA

## TradingView widgets

- Open the Overview page with network access. Confirm the ticker, chart, market overview and timeline are embedded and the page attributes them to TradingView.
- Confirm the top bar says “TradingView widgets”; it must not imply NexTrade monitors a live provider connection.
- Block `s3.tradingview.com` in browser devtools/content filtering and reload. Each affected widget should expose a visible, screen-reader-readable failure message.
- Check narrow/mobile layout and browser zoom; the provider credit remains reachable and the error text does not cover navigation.
- Confirm exchange delay/source details are provided by the embedded widget and no quote or timestamp is synthesized by NexTrade.

## Account and unsupported features

- Sign in with a confirmed Supabase account; confirm the display name/profile update survives sign-out and sign-in.
- Open Watchlist while signed in; it must still be labeled as a shared public symbol list, not a persisted personal list.
- Open Portfolio, AI Insights and Alerts. Confirm each explains that its data/service is not configured and shows no sample account balance, returns, alerts or model output.
- Test authentication with Supabase configuration absent and with network disconnected; errors should be visible and no secret or token should appear in the interface.

## Verification boundary

Automated checks run for the latest UI change: `npm run lint` and `npx tsc --noEmit` passed. A repeat `npm run build` could not start because another Next.js build process held the build lock; the existing `.next/BUILD_ID` was refreshed during the initial combined lint/build invocation, but that invocation’s output was not captured. Re-run `npm run build` after the existing process exits. No live Vercel deployment or browser session was inspected for this change.
