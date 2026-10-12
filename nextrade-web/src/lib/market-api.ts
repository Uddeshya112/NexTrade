export type Instrument = {
  id: string;
  symbol: string;
  name: string;
  exchange: string;
  currency: string;
  tradeable: boolean;
};

export type MarketQuote = {
  instrumentId: string;
  bid: string | null;
  ask: string | null;
  last: string;
  mode: "SIMULATED" | "DELAYED" | "LIVE" | string;
  source: string;
  asOf: string;
};

const apiBase = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/\/$/, "");

async function getJson<T>(path: string, signal: AbortSignal): Promise<T> {
  const response = await fetch(`${apiBase}${path}`, {
    headers: { Accept: "application/json" },
    signal,
    cache: "no-store",
  });
  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      throw new Error("The market API requires an authenticated session.");
    }
    throw new Error(`Market API returned ${response.status}.`);
  }
  return response.json() as Promise<T>;
}

export async function getMarketSnapshot(signal: AbortSignal) {
  const requestSignal = AbortSignal.any([signal, AbortSignal.timeout(8000)]);
  const instruments = await getJson<unknown>("/api/v1/market/instruments", requestSignal);
  if (!Array.isArray(instruments) || instruments.some(item => !item || typeof item !== "object"
    || typeof item.id !== "string" || typeof item.symbol !== "string" || typeof item.name !== "string"
    || typeof item.exchange !== "string" || typeof item.currency !== "string" || typeof item.tradeable !== "boolean")) {
    throw new Error("Market API returned an invalid instruments response.");
  }
  if (!instruments.length) return { instruments, quotes: {} as Record<string, MarketQuote> };

  const params = new URLSearchParams();
  for (const instrument of instruments) params.append("instruments", instrument.id);
  const quotes = await getJson<unknown>(`/api/v1/market/quotes?${params.toString()}`, requestSignal);
  if (!quotes || typeof quotes !== "object" || Array.isArray(quotes)) {
    throw new Error("Market API returned an invalid quotes response.");
  }
  for (const [id, quote] of Object.entries(quotes)) {
    if (!quote || typeof quote !== "object") throw new Error("Market API returned invalid quote metadata.");
    const value = quote as Record<string, unknown>;
    if (value.instrumentId !== id || typeof value.last !== "string"
      || !Number.isFinite(Number(value.last)) || typeof value.mode !== "string"
      || typeof value.source !== "string" || typeof value.asOf !== "string"
      || !Number.isFinite(Date.parse(value.asOf))) {
      throw new Error("Market API returned invalid quote metadata.");
    }
  }
  return { instruments: instruments as Instrument[], quotes: quotes as Record<string, MarketQuote> };
}
