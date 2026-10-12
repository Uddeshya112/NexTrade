import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "NexTrade — Market workspace",
  description: "A market observation workspace with attributed market charts, quotes, and headlines.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
