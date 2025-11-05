import type { Metadata } from "next";

const siteUrl = "https://www.eazivi.com";

const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
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
    url: siteUrl,
    siteName: "Ezivia",
    locale: "es_ES",
    type: "website",
    images: [
      {
        url: "/illustration-care.svg",
        width: 1200,
        height: 630,
        alt: "Persona mayor utilizando Ezivia en un teléfono Android"
      }
    ]
  },
  alternates: {
    canonical: siteUrl
  },
  icons: {
    icon: [{ url: "/favicon.svg", type: "image/svg+xml", sizes: "any" }],
    apple: [{ url: "/touch-icon.svg", type: "image/svg+xml", sizes: "180x180" }],
    shortcut: [{ url: "/favicon.svg", type: "image/svg+xml", sizes: "any" }]
  },
  twitter: {
    card: "summary_large_image",
    site: "@eziviaapp",
    creator: "@eziviaapp",
    title: "Ezivia | Android simple para personas mayores",
    description:
      "Convierte cualquier Android en un móvil sencillo y humano para personas mayores y sus cuidadores.",
    images: ["/illustration-care.svg"]
  }
};

export default metadata;
