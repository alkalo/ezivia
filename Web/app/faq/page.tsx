import type { Metadata } from "next";

import { MarkdownRenderer } from "../components/MarkdownRenderer";
import { ScreenGuide } from "../components/ScreenGuide";
import { readMarkdown } from "../lib/readMarkdown";

export const metadata: Metadata = {
  title: "Preguntas frecuentes | Ezivia",
  description:
    "Resuelve las dudas habituales sobre Ezivia y descubre cómo simplifica los móviles Android para personas mayores.",
};

export default async function FaqPage() {
  const faq = await readMarkdown("content/faq.md");

  return (
    <main className="content-page" aria-labelledby="faq-title">
      <ScreenGuide
        title="Guía de preguntas frecuentes"
        message="Aquí puedes leer respuestas rápidas sobre Ezivia. Recorre la lista de preguntas y usa el botón de ayuda para escuchar las indicaciones si necesitas apoyo."
      />
      <header className="content-page__header">
        <h1 id="faq-title">Preguntas frecuentes</h1>
        <p>
          Esta guía reúne respuestas rápidas sobre compatibilidad, instalación y
          seguridad de Ezivia.
        </p>
      </header>
      <section aria-label="Listado de preguntas frecuentes">
        <MarkdownRenderer
          content={faq}
          className="markdown markdown--faq"
        />
      </section>
    </main>
  );
}
