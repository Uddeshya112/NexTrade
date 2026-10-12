"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getSupabase } from "@/lib/supabase";

export default function AuthCallbackPage() {
  const [message, setMessage] = useState("Checking your confirmation link…");
  const [complete, setComplete] = useState(false);

  useEffect(() => {
    let active = true;
    void (async () => {
      try {
        const supabase = getSupabase();
        const { data: { session }, error } = await supabase.auth.getSession();
        if (error) throw error;
        if (!session?.user.email_confirmed_at) throw new Error("The link is invalid or expired. Request a new confirmation email and try again.");
        const { data, error: userError } = await supabase.auth.getUser();
        if (userError || !data.user?.email_confirmed_at) throw userError ?? new Error("Supabase did not confirm this email address.");
        if (active) { setMessage("Your email is confirmed. Your NexTrade session is ready."); setComplete(true); }
      } catch (error) {
        if (active) setMessage(error instanceof Error ? error.message : "We could not verify this link. Request a new confirmation email.");
      }
    })();
    return () => { active = false; };
  }, []);

  return <main className="auth-route"><section className="auth-modal"><div className="modal-brand">N<span>.</span></div><p className="section-kicker">NEXTRADE ACCOUNT</p><h1>{complete ? "Email confirmed" : "Confirming your email"}</h1><p className="modal-copy" role="status">{message}</p><Link className="button button-dark" href="/">{complete ? "Open NexTrade" : "Back to NexTrade"}</Link></section></main>;
}
