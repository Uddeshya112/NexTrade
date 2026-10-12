import { createClient, type SupabaseClient } from "@supabase/supabase-js";

let client: SupabaseClient | undefined;
let recoverySessionDetected = false;

export function getSupabase() {
  if (client) return client;
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const publishableKey = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
  if (!url || !publishableKey) throw new Error("Supabase is not configured. Set the public Supabase URL and publishable key in Vercel.");
  client = createClient(url, publishableKey, {
    auth: { flowType: "pkce", autoRefreshToken: true, persistSession: true, detectSessionInUrl: true },
  });
  client.auth.onAuthStateChange(event => {
    if (event === "PASSWORD_RECOVERY") recoverySessionDetected = true;
  });
  return client;
}

export function isRecoverySessionDetected() {
  return recoverySessionDetected;
}

export function authRedirect(path: string) {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || (typeof window !== "undefined" ? window.location.origin : "");
  return `${baseUrl.replace(/\/$/, "")}${path}`;
}
