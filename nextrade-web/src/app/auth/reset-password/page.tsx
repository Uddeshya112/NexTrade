"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { getSupabase, isRecoverySessionDetected } from "@/lib/supabase";

export default function ResetPasswordPage() {
  const [ready, setReady] = useState(false);
  const [complete, setComplete] = useState(false);
  const [message, setMessage] = useState("Validating your password recovery link…");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const supabase = getSupabase();
    let active = true;
    let checked = false;
    const validateRecovery = async () => {
      if (checked || !isRecoverySessionDetected()) return;
      checked = true;
      const { data: { user }, error } = await supabase.auth.getUser();
      if (!active) return;
      if (error || !user) setMessage("This password reset link is invalid or expired. Request a new one.");
      else { setReady(true); setMessage("Choose a new password for your NexTrade account."); }
    };
    const { data: listener } = supabase.auth.onAuthStateChange(event => {
      if (event === "PASSWORD_RECOVERY") void validateRecovery();
    });
    void supabase.auth.getSession().then(() => { void validateRecovery(); });
    return () => { active = false; listener.subscription.unsubscribe(); };
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (password.length < 8) { setMessage("Use a password with at least 8 characters."); return; }
    if (password !== confirmPassword) { setMessage("The passwords do not match."); return; }
    setBusy(true);
    try {
      const { error } = await getSupabase().auth.updateUser({ password });
      if (error) throw error;
      await getSupabase().auth.signOut({ scope: "local" });
      setComplete(true); setReady(false); setMessage("Your password has been updated. Sign in with your new password.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Could not update your password. Request a fresh reset link.");
    } finally { setBusy(false); }
  }

  return <main className="auth-route"><section className="auth-modal"><div className="modal-brand">N<span>.</span></div><p className="section-kicker">NEXTRADE ACCOUNT</p><h1>{complete ? "Password updated" : "Reset your password"}</h1><p className="modal-copy" role="status">{message}</p>{ready && <form onSubmit={submit}><label>New password<input type="password" autoComplete="new-password" minLength={8} required value={password} onChange={event => setPassword(event.target.value)} /></label><label>Confirm new password<input type="password" autoComplete="new-password" minLength={8} required value={confirmPassword} onChange={event => setConfirmPassword(event.target.value)} /></label><button className="button button-dark submit-button" disabled={busy}>{busy ? "Updating…" : "Update password"}</button></form>}<Link className="button button-dark" href="/">{complete ? "Go to sign in" : "Back to NexTrade"}</Link></section></main>;
}
