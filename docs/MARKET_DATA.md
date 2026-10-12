# Market Data Contract

## Current behavior

NexTrade does not currently connect the Spring API to an external quote provider. The Next.js dashboard embeds TradingView widgets directly and attributes those widgets to TradingView; exchange delays and display rights depend on that provider. Do not treat embedded quotes as an executable order-book feed.

The Spring API exposes only values explicitly injected into its in-memory `MarketSimulationEngine`. Every such quote contains `mode: SIMULATED`, `source: NexTrade in-memory simulation engine`, and an `asOf` timestamp. A symbol with no injected price is omitted from the response; no default or synthetic quote is substituted. A process restart clears these in-memory quotes.

## REST

`GET /api/v1/market/quotes?instruments=<instrument-uuid>&instruments=<instrument-uuid>` returns a map keyed by instrument ID. Each value contains instrument ID, price/tick size, mode, source and timestamp. An empty map means the simulator currently has no values for the requested instruments.

The legacy `/api/v1/market/prices` route remains for compatibility and returns price values without metadata. New clients should use `/quotes`.

## Authenticated STOMP

1. Connect to `/ws` with `Authorization: Bearer <Supabase access token>` in the STOMP CONNECT frame.
2. Subscribe to `/topic/market/<instrument-uuid>`.
3. Send JSON `{"instrumentId":"<instrument-uuid>"}` to `/app/market.subscribe`.
4. Read the ack at `/user/queue/market-subscriptions`. It reports the destination and whether a quote is available; an included quote is the current simulator snapshot.
5. Later `MarketSimulationEngine.updatePrice` calls broadcast a metadata-bearing quote to that instrument topic.

Only active, tradeable instrument IDs can be subscribed to. Topic updates contain simulated values only; no first-party provider, candle history, stale-data policy or persistent replay feed is configured.

## Before using external quotes

Choose a provider and verify exchange licensing, public-display rights, latency, coverage, attribution and redistribution terms. Add a provider adapter that supplies the provider's actual mode/source/as-of data. Keep credentials server-side. Do not silently fall back between provider and simulation data.
