"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { getMarketSnapshot, type Instrument, type MarketQuote } from "@/lib/market-api";

type Snapshot = { instruments: Instrument[]; quotes: Record<string, MarketQuote> };

export function MarketDataPanel({ compact = false }: { compact?: boolean }) {
  const [snapshot, setSnapshot] = useState<Snapshot | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");

  const refresh = useCallback(async (signal: AbortSignal) => {
    setLoading(true);
    setError("");
    try {
      setSnapshot(await getMarketSnapshot(signal));
    } catch (cause) {
      if (!signal.aborted) setError(cause instanceof Error ? cause.message : "Could not load market data.");
    } finally {
      if (!signal.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const timer = window.setTimeout(() => void refresh(controller.signal), 0);
    return () => { window.clearTimeout(timer); controller.abort(); };
  }, [refresh]);

  const instruments = useMemo(() => {
    const all = snapshot?.instruments ?? [];
    const term = query.trim().toLowerCase();
    return term ? all.filter(item => `${item.symbol} ${item.name} ${item.exchange}`.toLowerCase().includes(term)) : all;
  }, [query, snapshot]);

  function reload() {
    const controller = new AbortController();
    void refresh(controller.signal);
  }

  return <section className={`surface-card api-market-card${compact ? " compact" : ""}`} aria-label="NexTrade backend market data">
    <div className="card-heading api-market-heading">
      <div><span className="card-kicker">NEXTRADE API</span><h2>Paper market instruments</h2><p>Quotes from the configured backend only. No local sample prices are generated.</p></div>
      <span className="source-tag">{snapshot ? "API connected" : "API status"}</span>
    </div>
    <div className="api-market-tools">
      <label className="market-search">Search instruments<input value={query} onChange={event => setQuery(event.target.value)} placeholder="Symbol, name or exchange" /></label>
      <button className="subtle-button" onClick={reload} disabled={loading}>{loading ? "Loading…" : "Refresh"}</button>
    </div>
    {error && <div className="market-api-message" role="status"><strong>Market data unavailable</strong><p>{error} Start the API and database, then refresh. TradingView widgets below remain a separate third-party feed.</p></div>}
    {!error && loading && <p className="market-api-muted" role="status">Loading instruments and quote metadata…</p>}
    {!error && !loading && snapshot && snapshot.instruments.length === 0 && <p className="market-api-muted">The backend returned no active instruments.</p>}
    {!error && !loading && snapshot && snapshot.instruments.length > 0 && instruments.length === 0 && <p className="market-api-muted">No instruments match “{query}”.</p>}
    {!error && !loading && instruments.length > 0 && <div className="api-market-table-wrap"><table className="api-market-table"><thead><tr><th>Instrument</th><th>Exchange</th><th>Last quote</th><th>Mode / source</th><th>As of</th></tr></thead><tbody>
      {instruments.map(instrument => {
        const quote = snapshot?.quotes[instrument.id];
        return <tr key={instrument.id}><th scope="row"><strong>{instrument.symbol}</strong><small>{instrument.name} · {instrument.currency}</small></th><td>{instrument.exchange}</td><td>{quote ? <strong>{new Intl.NumberFormat(undefined, { style: "currency", currency: instrument.currency, maximumFractionDigits: 4 }).format(Number(quote.last))}</strong> : <span className="market-api-muted">No quote</span>}</td><td>{quote ? <><span className={`quote-mode ${quote.mode.toLowerCase()}`}>{quote.mode}</span><small>{quote.source}</small></> : <span className="market-api-muted">No backend quote available</span>}</td><td>{quote ? <time dateTime={quote.asOf}>{new Date(quote.asOf).toLocaleString()}</time> : "—"}</td></tr>;
      })}
    </tbody></table></div>}
    <p className="market-api-footnote">This panel reports backend API data. SIMULATED quotes are practice data and are not exchange prices or a trading recommendation.</p>
  </section>;
}
