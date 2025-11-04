import type { ReactNode } from "react";
import { Nunito } from "next/font/google";

import "./globals.css";
import metadata from "./metadata";

const nunito = Nunito({
  subsets: ["latin"],
  weight: ["400", "600", "700"],
  display: "swap",
  variable: "--font-primary"
});

export { metadata };

export default function RootLayout({
  children
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <html lang="es" className={nunito.variable}>
      <body className={`${nunito.className} app-body`}>
        <a className="skip-link" href="#contenido-principal">
          Saltar al contenido principal
        </a>
        <div id="contenido-principal" className="app-shell">
          {children}
        </div>
      </body>
    </html>
  );
}
