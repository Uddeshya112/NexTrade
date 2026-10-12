"use client";

import { useEffect, useState } from "react";
import { authRedirect, getSupabase } from "@/lib/supabase";
import { TradingViewWidget } from "@/components/tradingview-widget";

type View = "Overview" | "Markets" | "Watchlist" | "Portfolio" | "News" | "AI Insights" | "Alerts" | "Settings";
const views: View[] = ["Overview", "Markets", "Watchlist", "Portfolio", "News", "AI Insights", "Alerts", "Settings"];
const icons: Record<View, string> = {
  Overview: "⌂", Markets: "⌁", Watchlist: "☆", Portfolio: "▤", News: "▧", "AI Insights": "✳", Alerts: "♧", Settings: "⚙",
};

export default function Home() {
  const [activeView, setActiveView] = useState<View>("Overview");
  const [notice, setNotice] = useState("");
  const [authOpen, setAuthOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [email, setEmail] = useState("");
  const [accountEmail, setAccountEmail] = useState("");
  const [password, setPassword] = useState("");
  const [registering, setRegistering] = useState(false);
  const [verificationRequired, setVerificationRequired] = useState(false);
  const [forgotPassword, setForgotPassword] = useState(false);
  const [signedIn, setSignedIn] = useState(false);

  useEffect(() => {
    let unsubscribe = () => {};
    try {
      const supabase = getSupabase();
      void supabase.auth.getSession().then(({ data }) => {
        const user = data.session?.user;
        setSignedIn(Boolean(user?.email_confirmed_at));
        setAccountEmail(user?.email ?? "");
      });
      const { data: listener } = supabase.auth.onAuthStateChange((_event, session) => {
        const user = session?.user;
        setSignedIn(Boolean(user?.email_confirmed_at));
        setAccountEmail(user?.email ?? "");
      });
      unsubscribe = () => listener.subscription.unsubscribe();
    } catch { /* Auth actions explain when public Supabase config is missing. */ }
    return unsubscribe;
  }, []);

  async function submitAuth(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setNotice("");
    try {
      const supabase = getSupabase();
      if (forgotPassword) {
        const { error } = await supabase.auth.resetPasswordForEmail(email, { redirectTo: authRedirect("/auth/reset-password") });
        if (error) throw error;
        setNotice("If an account exists for that address, Supabase will email a password reset link.");
      } else if (registering) {
        const { error } = await supabase.auth.signUp({ email, password, options: { emailRedirectTo: authRedirect("/auth/callback") } });
        if (error) throw error;
        setNotice("Check your email for a confirmation link. Confirm your address before signing in.");
      } else {
        const { data, error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) throw error;
        if (!data.user?.email_confirmed_at) {
          await supabase.auth.signOut({ scope: "local" });
          setVerificationRequired(true);
          throw new Error("Confirm your email from the link we sent before signing in.");
        }
        setSignedIn(true); setAccountEmail(data.user.email ?? ""); setAuthOpen(false); setNotice("You’re signed in.");
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : "Could not connect to NexTrade.";
      const lowered = message.toLowerCase();
      setNotice(lowered.includes("already registered") || lowered.includes("user already registered")
        ? "If you already have a NexTrade account, sign in or use Forgot password." : message);
      if (!registering && (lowered.includes("confirm") || lowered.includes("not confirmed"))) setVerificationRequired(true);
    } finally { setBusy(false); }
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

  async function signOut() {
    try { await getSupabase().auth.signOut(); } finally { setSignedIn(false); setAccountEmail(""); setNotice("You’ve signed out."); }
  }

  const today = new Intl.DateTimeFormat("en", { weekday: "long", month: "long", day: "numeric" }).format(new Date());

  return <main className="dashboard-shell">
    <aside className="sidebar">
      <button className="dashboard-brand" onClick={() => setActiveView("Overview")} aria-label="NexTrade overview">N<span>.</span></button>
      <div className="sidebar-caption">WORKSPACE</div>
      <nav className="side-nav" aria-label="Main navigation">
        {views.map(view => <button key={view} className={`nav-item${activeView === view ? " selected" : ""}`} onClick={() => setActiveView(view)} aria-current={activeView === view ? "page" : undefined}>
          <span className="nav-icon" aria-hidden="true">{icons[view]}</span>{view}{view === "Alerts" && <span className="nav-soon">Soon</span>}
        </button>)}
      </nav>
      <div className="sidebar-bottom"><span className="service-dot" /> TradingView market widgets <small>Public data view</small></div>
    </aside>

    <section className="dashboard-content">
      <header className="dashboard-topbar"><div className="mobile-brand">NexTrade<span>.</span></div><div className="topbar-date">{today}</div>
        <div className="topbar-actions"><span className="data-status"><i />External market feed</span>
          {signedIn ? <div className="account-chip"><span className="account-avatar">{accountEmail.slice(0, 1).toUpperCase() || "U"}</span><span className="account-address">{accountEmail}</span><button className="subtle-button" onClick={signOut}>Sign out</button></div>
            : <button className="primary-button" onClick={() => { setNotice(""); setAuthOpen(true); }}>Sign in <span>↗</span></button>}
        </div>
      </header>

      <div className="dashboard-main">
        <div className="page-heading"><div><div className="eyebrow"><i /> MARKET WORKSPACE</div><h1>{activeView === "Overview" ? `Good ${new Date().getHours() < 12 ? "morning" : new Date().getHours() < 18 ? "afternoon" : "evening"}${signedIn && accountEmail ? `, ${accountEmail.split("@")[0]}` : ""}` : activeView}</h1>
          <p>{activeView === "Overview" ? "Market information from an attributed third-party feed. Exchange delays may apply." : descriptions[activeView]}</p></div>
          <span className="date-chip">{today}</span></div>

        {activeView === "Overview" && <>
          <TradingViewWidget kind="ticker-tape" title="Market ticker" />
          <div className="dashboard-grid overview-grid">
            <article className="surface-card chart-card"><div className="card-heading"><div><span className="card-kicker">MARKET CHART</span><h2>Follow a symbol</h2></div><span className="source-tag">TradingView</span></div><TradingViewWidget kind="advanced-chart" title="Interactive stock market chart" /></article>
            <article className="surface-card overview-market-card"><div className="card-heading"><div><span className="card-kicker">MARKET OVERVIEW</span><h2>Stocks, indices & crypto</h2></div></div><TradingViewWidget kind="market-overview" title="Stocks, indices and crypto market overview" /></article>
            <article className="surface-card news-card"><div className="card-heading"><div><span className="card-kicker">MARKET NEWS</span><h2>Latest headlines</h2></div><a href="https://www.tradingview.com/news/" target="_blank" rel="noreferrer">Open source ↗</a></div><TradingViewWidget kind="timeline" title="Market news timeline" /></article>
            <article className="truth-card"><span className="truth-icon">i</span><div><strong>Real market feed, honest limits</strong><p>Prices and chart data are served by TradingView. Stock exchange data may be delayed; the widget identifies its sources and availability. NexTrade does not execute trades or connect a brokerage account.</p></div></article>
          </div>
        </>}

        {activeView === "Markets" && <div className="single-market"><div className="surface-card"><div className="card-heading"><div><span className="card-kicker">MARKET OVERVIEW</span><h2>Explore market symbols</h2></div><span className="source-tag">TradingView</span></div><TradingViewWidget kind="market-overview" title="Stocks, indices and crypto market overview" /></div><div className="surface-card"><div className="card-heading"><div><span className="card-kicker">INTERACTIVE CHART</span><h2>Choose a symbol in the chart</h2></div></div><TradingViewWidget kind="advanced-chart" title="Interactive stock market chart" /></div></div>}

        {activeView === "Watchlist" && <div className="surface-card"><div className="card-heading"><div><span className="card-kicker">SYMBOLS</span><h2>Market watch</h2><p>Switch symbols using the embedded widget controls. This is a shared public list, not saved to your account.</p></div><span className="source-tag">TradingView</span></div><TradingViewWidget kind="market-overview" title="Public market symbol list" /></div>}

        {activeView === "News" && <div className="surface-card news-full"><div className="card-heading"><div><span className="card-kicker">THIRD-PARTY FEED</span><h2>Market news</h2><p>Headlines are provided and attributed by TradingView.</p></div></div><TradingViewWidget kind="timeline" title="Market news timeline" /></div>}

        {activeView === "Portfolio" && <UnavailablePanel icon="▤" title="No brokerage or portfolio data is connected" body="NexTrade does not currently connect to a brokerage, hold positions, or track cash. No account balances or returns are shown here. A future portfolio view would need an authorized brokerage integration or positions you enter yourself." />}
        {activeView === "AI Insights" && <UnavailablePanel icon="✳" title="AI analysis is not connected" body="The reference screen’s AI signals and confidence scores were sample content. This project has no verified analysis model connected, so it does not generate or display investment signals." />}
        {activeView === "Alerts" && <UnavailablePanel icon="♧" title="Price alerts are not active" body="This project has no alert scheduler or notification delivery service configured. The sample alerts from the design have been removed so they cannot be mistaken for active notifications." />}
        {activeView === "Settings" && <div className="surface-card settings-card"><span className="card-kicker">ACCOUNT</span><h2>Account settings</h2>{signedIn ? <><p>Signed in as <strong>{accountEmail}</strong></p><p>Authentication is managed by Supabase. This account currently has no brokerage or saved portfolio attached.</p><button className="primary-button" onClick={signOut}>Sign out</button></> : <><p>You are viewing public market widgets. Sign in with your NexTrade account to manage your authentication session.</p><button className="primary-button" onClick={() => { setNotice(""); setAuthOpen(true); }}>Sign in <span>↗</span></button></>}</div>}

        <footer className="dashboard-footer"><span>NexTrade <b>·</b> Market information for observation</span><span>Quotes, charts and headlines by <a href="https://www.tradingview.com/" target="_blank" rel="noreferrer">TradingView</a>. Stock data may be delayed.</span></footer>
      </div>
    </section>

    {notice && !authOpen && <div className="toast" role="status" onClick={() => setNotice("")}>{notice}<button aria-label="Dismiss">×</button></div>}
    {authOpen && <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) setAuthOpen(false); }}><section className="auth-modal" role="dialog" aria-modal="true" aria-labelledby="auth-title"><button className="modal-close" onClick={() => setAuthOpen(false)} aria-label="Close">×</button><div className="modal-brand">N<span>.</span></div><p className="section-kicker">YOUR MARKET WORKSPACE</p><h2 id="auth-title">{forgotPassword ? "Reset your password" : registering ? "Create your account" : "Welcome back"}</h2><p className="modal-copy">{forgotPassword ? "We’ll send a secure password reset link to your email." : registering ? "Set up your NexTrade access." : "Sign in to continue to your workspace."}</p><form onSubmit={submitAuth}><label>Email address<input type="email" autoComplete="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" /></label>{!forgotPassword && <label>Password<input type="password" autoComplete={registering ? "new-password" : "current-password"} required minLength={8} value={password} onChange={event => setPassword(event.target.value)} placeholder="At least 8 characters" /></label>}<button className="button button-dark submit-button" disabled={busy}>{busy ? "Please wait…" : forgotPassword ? "Send reset link ↗" : registering ? "Create account ↗" : "Sign in ↗"}</button></form>{notice && <p className="form-notice" role="status">{notice}</p>}{verificationRequired && !forgotPassword && <p className="switch-auth">Didn’t receive the confirmation email? <button disabled={busy} onClick={resendVerification}>Resend it</button></p>}{forgotPassword ? <p className="switch-auth"><button onClick={() => { setForgotPassword(false); setNotice(""); }}>Back to sign in</button></p> : !registering && <p className="switch-auth"><button onClick={() => { setForgotPassword(true); setNotice(""); }}>Forgot password?</button></p>}<p className="switch-auth">{registering ? "Already have an account?" : "New to NexTrade?"} <button onClick={() => { setRegistering(!registering); setForgotPassword(false); setVerificationRequired(false); setNotice(""); }}>{registering ? "Sign in" : "Create an account"}</button></p>{registering && <small className="verification-note">We’ll email a confirmation link. You must confirm your email before signing in.</small>}</section></div>}
  </main>;
}

const descriptions: Record<View, string> = {
  Overview: "Market information from an attributed third-party feed.",
  Markets: "View market symbols and interactive charts from TradingView.",
  Watchlist: "Browse public market symbols. Personal watchlists are not saved yet.",
  Portfolio: "Brokerage and portfolio tracking are not configured.",
  News: "Explore attributed third-party market headlines.",
  "AI Insights": "Automated market analysis is not configured.",
  Alerts: "Price monitoring and notifications are not configured.",
  Settings: "Manage your account session.",
};

function UnavailablePanel({ icon, title, body }: { icon: string; title: string; body: string }) {
  return <section className="unavailable-panel"><span className="unavailable-icon" aria-hidden="true">{icon}</span><div><span className="card-kicker">NOT CONNECTED</span><h2>{title}</h2><p>{body}</p></div></section>;
}
