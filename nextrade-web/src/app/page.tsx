"use client";

import { useEffect, useMemo, useState, useSyncExternalStore } from "react";

type Instrument = { id: string; symbol: string; name: string; exchange: string; currency: string; tradeable: boolean };
type AuthResult = { type?: string; accessToken?: string; refreshToken?: string; userId?: string; message?: string; code?: string };

const apiBase = (process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080").replace(/\/$/, "");
const authChanged = "nextrade-auth-change";

function subscribeToAuth(callback: () => void) {
  window.addEventListener(authChanged, callback);
  window.addEventListener("storage", callback);
  return () => {
    window.removeEventListener(authChanged, callback);
    window.removeEventListener("storage", callback);
  };
}

function hasStoredSession() {
  return Boolean(localStorage.getItem("nextrade.accessToken"));
}

async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("nextrade.accessToken") : null;
  const response = await fetch(`${apiBase}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
  });
  const body = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message ?? body?.detail ?? `Request failed (${response.status})`);
  return body as T;
}

export default function Home() {
  const [instruments, setInstruments] = useState<Instrument[]>([]);
  const [apiState, setApiState] = useState<"checking" | "online" | "offline">("checking");
  const [notice, setNotice] = useState("");
  const [authOpen, setAuthOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [registering, setRegistering] = useState(false);
  const signedIn = useSyncExternalStore(subscribeToAuth, hasStoredSession, () => false);

  useEffect(() => {
    fetch(`${apiBase}/api/v1/market/instruments`)
      .then(async response => {
        if (!response.ok) throw new Error("API unavailable");
        setInstruments(await response.json());
        setApiState("online");
      })
      .catch(() => setApiState("offline"));
  }, []);

  const headline = useMemo(() => new Intl.DateTimeFormat("en", { weekday: "long", month: "long", day: "numeric" }).format(new Date()), []);

  async function submitAuth(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setNotice("");
    try {
      const result = await api<AuthResult>(`/api/v1/auth/${registering ? "register" : "login"}`, {
        method: "POST", body: JSON.stringify(registering ? { username: email.split("@")[0], email, password } : { username: email, password }),
      });
      if (result.accessToken) {
        localStorage.setItem("nextrade.accessToken", result.accessToken);
        if (result.refreshToken) localStorage.setItem("nextrade.refreshToken", result.refreshToken);
        window.dispatchEvent(new Event(authChanged)); setAuthOpen(false); setNotice("You’re signed in.");
      } else {
        setNotice(result.message ?? result.code ?? "Check the details and try again.");
      }
    } catch (error) { setNotice(error instanceof Error ? error.message : "Could not connect to NexTrade."); }
    finally { setBusy(false); }
  }

  function signOut() { localStorage.removeItem("nextrade.accessToken"); localStorage.removeItem("nextrade.refreshToken"); window.dispatchEvent(new Event(authChanged)); setNotice("You’ve signed out."); }

  return <main className="shell">
    <aside className="rail">
      <a className="brand-mark" href="#home" aria-label="NexTrade home">N<span>.</span></a>
      <nav aria-label="Main navigation">
        <a className="rail-link active" href="#home" aria-label="Overview"><span>⌂</span></a>
        <a className="rail-link" href="#markets" aria-label="Markets"><span>▥</span></a>
        <a className="rail-link" href="#watchlist" aria-label="Watchlist"><span>☆</span></a>
      </nav>
      <button className="avatar" aria-label={signedIn ? "Sign out" : "Sign in"} onClick={signedIn ? signOut : () => setAuthOpen(true)}>{signedIn ? "✓" : "U"}</button>
    </aside>

    <section className="content" id="home">
      <header className="topbar">
        <a className="wordmark" href="#home">nextrade<span>.</span></a>
        <div className="top-actions"><span className={`api-indicator ${apiState}`}><i />{apiState === "checking" ? "Connecting" : apiState === "online" ? "API connected" : "API offline"}</span>
          {signedIn ? <button className="button button-light" onClick={signOut}>Sign out</button> : <button className="button button-dark" onClick={() => setAuthOpen(true)}>Sign in <span>↗</span></button>}
        </div>
      </header>

      <div className="main-inner">
        <div className="eyebrow"><span className="eyebrow-dot" /> PERSONAL MARKET WORKSPACE <span className="date">{headline.toUpperCase()}</span></div>
        <section className="hero">
          <div className="hero-copy">
            <p className="kicker">See the market clearly.</p>
            <h1>Make your next<br /><em>move</em> with confidence.</h1>
            <p className="hero-sub">A focused place to follow instruments and keep your trading activity in view.</p>
            <div className="hero-ctas"><a className="button button-dark button-large" href="#markets">Explore markets <span>↓</span></a><button className="text-button" onClick={() => setAuthOpen(true)}>{signedIn ? "Account connected" : "Open your workspace"} <span>↗</span></button></div>
          </div>
          <div className="hero-art" aria-label="Abstract market trend illustration" role="img">
            <div className="art-grid" /><div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" />
            <div className="art-label label-top"><span>MARKET PULSE</span><b>+2.48%</b></div>
            <svg viewBox="0 0 500 340" className="trend" aria-hidden="true"><defs><linearGradient id="area" x1="0" x2="0" y1="0" y2="1"><stop offset="0" stopColor="#a8d8b6" stopOpacity=".38"/><stop offset="1" stopColor="#a8d8b6" stopOpacity="0"/></linearGradient></defs><path d="M20 275 C75 250 74 217 125 226 S184 265 224 206 S283 188 311 155 S350 165 382 116 S430 126 480 51 V330 H20Z" fill="url(#area)"/><path d="M20 275 C75 250 74 217 125 226 S184 265 224 206 S283 188 311 155 S350 165 382 116 S430 126 480 51" fill="none" stroke="#17613f" strokeWidth="3" strokeLinecap="round"/><circle cx="480" cy="51" r="6" fill="#17613f"/></svg>
            <div className="art-label label-bottom"><span>YOUR EDGE</span><b>Clarity over noise</b></div>
            <div className="art-stamp">N<span>.</span></div>
          </div>
        </section>

        <section className="market-section" id="markets">
          <div className="section-heading"><div><p className="section-kicker">AT A GLANCE</p><h2>Market watch</h2></div><a href="#watchlist" className="view-link">View watchlist <span>↗</span></a></div>
          {apiState === "offline" ? <div className="state-card"><div className="state-icon">↯</div><div><strong>Can’t reach the market service</strong><p>Check that the NexTrade API is running and NEXT_PUBLIC_API_URL points to it.</p></div><button className="button button-light" onClick={() => location.reload()}>Try again</button></div> : instruments.length ? <div className="market-table"><div className="table-head"><span>INSTRUMENT</span><span>EXCHANGE</span><span>STATUS</span><span> </span></div>{instruments.slice(0, 5).map(item => <div className="market-row" key={item.id}><div className="instrument"><span className="instrument-icon">{item.symbol.slice(0, 1)}</span><div><strong>{item.symbol}</strong><small>{item.name}</small></div></div><span className="exchange">{item.exchange}</span><span className="market-status"><i />{item.tradeable ? "Open" : "Watch"}</span><span className="row-arrow">↗</span></div>)}</div> : <div className="state-card"><div className="state-icon">⌁</div><div><strong>{apiState === "checking" ? "Loading market data" : "Your watchlist starts here"}</strong><p>{apiState === "checking" ? "Connecting to your market service…" : "The API is connected, but no instruments are configured yet."}</p></div></div>}
        </section>

        <section className="bottom-grid" id="watchlist"><div className="note-card"><div className="note-icon">✳</div><div><p className="section-kicker">BUILT FOR FOCUS</p><h3>Less noise.<br />Better decisions.</h3><p>Follow the markets that matter to you, all from one calm workspace.</p></div><span className="note-index">01 / 03</span></div><div className="access-card"><p className="section-kicker">YOUR WORKSPACE</p><h3>{signedIn ? "Welcome back." : "Your view, your pace."}</h3><p>{signedIn ? "Your account is connected. Portfolio and order tools will appear when the account services are configured." : "Sign in to connect your NexTrade account."}</p>{signedIn ? <span className="connected-tag">● &nbsp;Account connected</span> : <button className="button button-dark" onClick={() => setAuthOpen(true)}>Sign in to continue <span>↗</span></button>}</div></section>

        <footer><a className="wordmark footer-mark" href="#home">nextrade<span>.</span></a><span>Markets move. Stay grounded.</span><span>© {new Date().getFullYear()} NEXTRADE</span></footer>
      </div>
    </section>

    {notice && <div className="toast" role="status" onClick={() => setNotice("")}>{notice}<button aria-label="Dismiss">×</button></div>}
    {authOpen && <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) setAuthOpen(false); }}><section className="auth-modal" role="dialog" aria-modal="true" aria-labelledby="auth-title"><button className="modal-close" onClick={() => setAuthOpen(false)} aria-label="Close">×</button><div className="modal-brand">N<span>.</span></div><p className="section-kicker">YOUR MARKET WORKSPACE</p><h2 id="auth-title">{registering ? "Create your account" : "Welcome back"}</h2><p className="modal-copy">{registering ? "Set up your NexTrade access." : "Sign in to continue to your workspace."}</p><form onSubmit={submitAuth}><label>Email address<input type="email" autoComplete="username" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" /></label><label>Password<input type="password" autoComplete={registering ? "new-password" : "current-password"} required minLength={8} value={password} onChange={event => setPassword(event.target.value)} placeholder="At least 8 characters" /></label><button className="button button-dark submit-button" disabled={busy}>{busy ? "Please wait…" : registering ? "Create account ↗" : "Sign in ↗"}</button></form>{notice && <p className="form-notice" role="alert">{notice}</p>}<p className="switch-auth">{registering ? "Already have an account?" : "New to NexTrade?"} <button onClick={() => { setRegistering(!registering); setNotice(""); }}>{registering ? "Sign in" : "Create an account"}</button></p>{registering && <small className="verification-note">Email verification requires an email provider configured by the service administrator.</small>}</section></div>}
  </main>;
}
