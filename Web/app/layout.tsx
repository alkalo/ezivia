import type { Metadata } from "next";
import type { ReactNode } from "react";
import "./globals.css";

export const metadata: Metadata = {
  title: "Ezivia - Móviles sencillos para mayores",
  description:
    "Ezivia convierte smartphones Android en experiencias accesibles para personas mayores, con navegación clara y texto legible."
};

export default function RootLayout({
  children
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}
