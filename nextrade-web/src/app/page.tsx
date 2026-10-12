"use client";

import { useEffect, useMemo, useState } from "react";
import { authRedirect, getSupabase } from "@/lib/supabase";

type Instrument = { id: string; symbol: string; name: string; exchange: string; currency: string; tradeable: boolean };

const defaultApiBase = process.env.NODE_ENV === "production"
  ? "https://nextrade-6iia.onrender.com"
  : "http://localhost:8080";
const apiBase = (process.env.NEXT_PUBLIC_API_URL ?? defaultApiBase).replace(/\/$/, "");
export default function Home() {
  const [instruments, setInstruments] = useState<Instrument[]>([]);
  const [apiState, setApiState] = useState<"checking" | "online" | "offline">("checking");
  const [notice, setNotice] = useState("");
  const [authOpen, setAuthOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [registering, setRegistering] = useState(false);
  const [verificationRequired, setVerificationRequired] = useState(false);
  const [forgotPassword, setForgotPassword] = useState(false);
  const [signedIn, setSignedIn] = useState(false);

  useEffect(() => {
    let unsubscribe = () => {};
    try {
      const supabase = getSupabase();
      void supabase.auth.getSession().then(({ data }) => setSignedIn(Boolean(data.session?.user.email_confirmed_at)));
      const { data: listener } = supabase.auth.onAuthStateChange((_event, session) => {
        setSignedIn(Boolean(session?.user.email_confirmed_at));
      });
      unsubscribe = () => listener.subscription.unsubscribe();
    } catch { /* Auth actions show the missing configuration when the user opens the sign-in form. */ }
    fetch(`${apiBase}/api/v1/market/instruments`)
      .then(async response => {
        if (!response.ok) throw new Error("API unavailable");
        setInstruments(await response.json());
        setApiState("online");
      })
      .catch(() => setApiState("offline"));
    return unsubscribe;
  }, []);

  const headline = useMemo(() => new Intl.DateTimeFormat("en", { weekday: "long", month: "long", day: "numeric" }).format(new Date()), []);

  async function submitAuth(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setNotice("");
    try {
      const supabase = getSupabase();
      if (forgotPassword) {
        const { error } = await supabase.auth.resetPasswordForEmail(email, { redirectTo: authRedirect("/auth/reset-password") });
        if (error) throw error;
        setNotice("If an account exists for that address, Supabase will email a password reset link.");
        return;
      }
      if (registering) {
        const { error } = await supabase.auth.signUp({ email, password, options: { emailRedirectTo: authRedirect("/auth/callback") } });
        if (error) throw error;
        setNotice("Check your email for next steps. If you already have an account, sign in or use Forgot password.");
      } else {
        const { data, error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) throw error;
        if (!data.user?.email_confirmed_at) {
          await supabase.auth.signOut({ scope: "local" });
          setVerificationRequired(true);
          throw new Error("Confirm your email from the link we sent before signing in.");
        }
        setSignedIn(true); setAuthOpen(false); setNotice("You’re signed in.");
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : "Could not connect to NexTrade.";
      const lowered = message.toLowerCase();
      setNotice(lowered.includes("already registered") || lowered.includes("user already registered")
        ? "If you already have a NexTrade account, sign in or use Forgot password."
        : message);
      if (!registering && (lowered.includes("confirm") || lowered.includes("not confirmed"))) setVerificationRequired(true);
    }
    finally { setBusy(false); }
  }

  async function resendVerification() {
    setBusy(true); setNotice("");
    try {
      const { error } = await getSupabase().auth.resend({ type: "signup", email, options: { emailRedirectTo: authRedirect("/auth/callback") } });
      if (error) throw error;
      setNotice("If the account needs confirmation, Supabase will send a new email.");
    } catch (error) { setNotice(error instanceof Error ? error.message : "Could not resend the confirmation email."); }
    finally { setBusy(false); }
  }

  async function signOut() { await getSupabase().auth.signOut(); setSignedIn(false); setNotice("You’ve signed out."); }

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
    {authOpen && <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) setAuthOpen(false); }}><section className="auth-modal" role="dialog" aria-modal="true" aria-labelledby="auth-title"><button className="modal-close" onClick={() => setAuthOpen(false)} aria-label="Close">×</button><div className="modal-brand">N<span>.</span></div><p className="section-kicker">YOUR MARKET WORKSPACE</p><h2 id="auth-title">{forgotPassword ? "Reset your password" : registering ? "Create your account" : "Welcome back"}</h2><p className="modal-copy">{forgotPassword ? "We’ll send a secure password reset link to your email." : registering ? "Set up your NexTrade access." : "Sign in to continue to your workspace."}</p><form onSubmit={submitAuth}><label>Email address<input type="email" autoComplete="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" /></label>{!forgotPassword && <label>Password<input type="password" autoComplete={registering ? "new-password" : "current-password"} required minLength={8} value={password} onChange={event => setPassword(event.target.value)} placeholder="At least 8 characters" /></label>}<button className="button button-dark submit-button" disabled={busy}>{busy ? "Please wait…" : forgotPassword ? "Send reset link ↗" : registering ? "Create account ↗" : "Sign in ↗"}</button></form>{notice && <p className="form-notice" role="status">{notice}</p>}{verificationRequired && !forgotPassword && <p className="switch-auth">Didn’t receive the confirmation email? <button disabled={busy} onClick={resendVerification}>Resend it</button></p>}{forgotPassword ? <p className="switch-auth"><button onClick={() => { setForgotPassword(false); setNotice(""); }}>Back to sign in</button></p> : !registering && <p className="switch-auth"><button onClick={() => { setForgotPassword(true); setNotice(""); }}>Forgot password?</button></p>}<p className="switch-auth">{registering ? "Already have an account?" : "New to NexTrade?"} <button onClick={() => { setRegistering(!registering); setForgotPassword(false); setVerificationRequired(false); setNotice(""); }}>{registering ? "Sign in" : "Create an account"}</button></p>{registering && <small className="verification-note">We’ll email a confirmation link. You must confirm your email before signing in.</small>}</section></div>}
  </main>;
}
