import type { Metadata } from "next";

const metadata: Metadata = {
  title: "Ezivia | Móviles Android sencillos para personas mayores",
  description:
    "Ezivia transforma smartphones Android en experiencias accesibles para personas mayores y cuidadores, con menús claros, iconos grandes y comunicación tranquila.",
  keywords: [
    "ezivia",
    "móviles sencillos",
    "smartphone para mayores",
    "android accesible",
    "ayuda para cuidadores",
    "tecnología inclusiva",
    "teléfono fácil",
    "interfaz grande",
    "accesibilidad senior",
    "comunicación familiar"
  ],
  authors: [{ name: "Equipo Ezivia" }],
  applicationName: "Ezivia",
  creator: "Ezivia",
  category: "accessibility",
  openGraph: {
    title: "Ezivia | Móviles Android sencillos para personas mayores",
    description:
      "Simplificamos los móviles Android para que las personas mayores y sus cuidadores disfruten de una experiencia tranquila, clara y segura.",
    url: "https://ezivia.example.com",
    siteName: "Ezivia",
    locale: "es_ES",
    type: "website"
  },
  alternates: {
    canonical: "https://ezivia.example.com"
  },
  icons: {
    icon: [{ url: "/favicon.svg", type: "image/svg+xml", sizes: "any" }],
    apple: [{ url: "/touch-icon.svg", type: "image/svg+xml", sizes: "180x180" }],
    shortcut: [{ url: "/favicon.svg", type: "image/svg+xml", sizes: "any" }]
  }
};

export default metadata;
