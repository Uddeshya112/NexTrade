"use client";

import { useEffect, useRef, useState } from "react";

type WidgetKind = "market-overview" | "advanced-chart" | "ticker-tape" | "timeline";

const scriptByKind: Record<WidgetKind, string> = {
  "market-overview": "https://s3.tradingview.com/external-embedding/embed-widget-market-overview.js",
  "advanced-chart": "https://s3.tradingview.com/external-embedding/embed-widget-advanced-chart.js",
  "ticker-tape": "https://s3.tradingview.com/external-embedding/embed-widget-ticker-tape.js",
  timeline: "https://s3.tradingview.com/external-embedding/embed-widget-timeline.js",
};

const symbols = [
  { s: "NASDAQ:AAPL", d: "Apple" },
  { s: "NASDAQ:MSFT", d: "Microsoft" },
  { s: "NASDAQ:NVDA", d: "NVIDIA" },
  { s: "NASDAQ:GOOGL", d: "Alphabet" },
  { s: "NYSE:KO", d: "Coca-Cola" },
];

function configFor(kind: WidgetKind, symbol?: string) {
  if (kind === "ticker-tape") return {
    symbols: symbols.map(({ s, d }) => ({ proName: s, title: d })),
    showSymbolLogo: true, colorTheme: "light", isTransparent: true, displayMode: "adaptive", locale: "en",
  };
  if (kind === "market-overview") return {
    colorTheme: "light", dateRange: "1D", showChart: true, locale: "en", isTransparent: false,
    showSymbolLogo: true, showFloatingTooltip: false, width: "100%", height: "100%",
    tabs: [{ title: "Stocks", symbols }, { title: "Indices", symbols: [
      { s: "FOREXCOM:SPXUSD", d: "S&P 500" }, { s: "NASDAQ:NDX", d: "Nasdaq 100" }, { s: "DJ:DJI", d: "Dow Jones" },
    ] }, { title: "Crypto", symbols: [
      { s: "BITSTAMP:BTCUSD", d: "Bitcoin" }, { s: "BITSTAMP:ETHUSD", d: "Ethereum" },
    ] }],
  };
  if (kind === "timeline") return {
    feedMode: "all_symbols", isTransparent: false, displayMode: "regular", width: "100%", height: "100%",
    colorTheme: "light", locale: "en",
  };
  return {
    autosize: true, symbol: symbol ?? "NASDAQ:AAPL", interval: "D", timezone: "Etc/UTC", theme: "light",
    style: "1", locale: "en", allow_symbol_change: true, hide_side_toolbar: true, hide_top_toolbar: false,
    save_image: false, calendar: false, support_host: "https://www.tradingview.com",
  };
}

export function TradingViewWidget({ kind, symbol, title }: { kind: WidgetKind; symbol?: string; title: string }) {
  const root = useRef<HTMLDivElement>(null);
  const [loadError, setLoadError] = useState(false);

  useEffect(() => {
    const target = root.current;
    if (!target) return;
    setLoadError(false);
    target.replaceChildren();
    const container = document.createElement("div");
    container.className = "tradingview-widget-container__widget";
    container.style.width = "100%";
    container.style.height = "100%";
    const script = document.createElement("script");
    script.src = scriptByKind[kind];
    script.async = true;
    script.textContent = JSON.stringify(configFor(kind, symbol));
    script.onerror = () => setLoadError(true);
    target.append(container, script);
    return () => { script.onerror = null; target.replaceChildren(); };
  }, [kind, symbol]);

  return <section className={`tv-panel tv-${kind}`} aria-label={title}>
    <div ref={root} className="tradingview-widget-container" />
    {loadError && <p className="tv-load-error" role="status">TradingView could not load this market widget. Check your connection or browser content blocker, then reload.</p>}
    <a className="tv-credit" href="https://www.tradingview.com/" target="_blank" rel="noreferrer">Data and charts by TradingView ↗</a>
  </section>;
}
