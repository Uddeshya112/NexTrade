import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "NexTrade — Market workspace",
  description: "A clear, focused workspace for following markets and managing your trading activity.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
