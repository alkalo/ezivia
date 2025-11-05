import Link from "next/link";
import type { Metadata } from "next";

import { MarkdownRenderer } from "../components/MarkdownRenderer";
import { readMarkdown } from "../lib/readMarkdown";

export const metadata: Metadata = {
  title: "Información legal | Ezivia",
  description:
    "Consulta la política de privacidad y los términos de servicio oficiales de Ezivia, revisados por el equipo legal.",
};

export default async function LegalPage() {
  const [privacy, terms] = await Promise.all([
    readMarkdown("content/legal/politica-privacidad.md"),
    readMarkdown("content/legal/terminos-servicio.md"),
  ]);

  return (
    <main className="content-page" aria-labelledby="legal-title">
      <header className="content-page__header">
        <h1 id="legal-title">Documentación legal de Ezivia</h1>
        <p>
          Aquí encontrarás la información revisada junto a nuestro equipo legal.
          Si tienes dudas adicionales, puedes escribirnos a {" "}
          <a className="markdown-link" href="mailto:hola@ezivia.com">
            hola@ezivia.com
          </a>
          {" "}
          o visitar las <Link href="/faq">Preguntas frecuentes</Link>.
        </p>
        <nav aria-label="Índice legal" className="content-page__toc">
          <ul>
            <li>
              <a href="#politica-privacidad">Política de privacidad</a>
            </li>
            <li>
              <a href="#terminos-servicio">Términos de servicio</a>
            </li>
          </ul>
        </nav>
      </header>
      <section id="politica-privacidad" aria-labelledby="privacy-heading">
        <MarkdownRenderer
          content={privacy}
          labelledBy="privacy-heading"
          className="markdown markdown--legal"
        />
      </section>
      <section id="terminos-servicio" aria-labelledby="terms-heading">
        <MarkdownRenderer
          content={terms}
          labelledBy="terms-heading"
          className="markdown markdown--legal"
        />
      </section>
    </main>
  );
}
