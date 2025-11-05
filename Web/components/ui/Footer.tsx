"use client";

import type { HTMLAttributes, ReactNode } from "react";

export interface FooterLink {
  label: string;
  href: string;
}

export interface FooterSection {
  title: string;
  links: FooterLink[];
}

export interface FooterProps extends HTMLAttributes<HTMLElement> {
  brandName: string;
  brandDescription?: string;
  sections?: FooterSection[];
  accessibilityStatement?: ReactNode;
  legal?: ReactNode;
}

export function Footer({
  brandName,
  brandDescription,
  sections = [],
  accessibilityStatement,
  legal,
  className,
  ...props
}: FooterProps) {
  return (
    <footer
      className={["ez-footer", className].filter(Boolean).join(" ")}
      role="contentinfo"
      {...props}
    >
      <div className="ez-footer__brand" aria-label="Información de la marca">
        <p className="ez-footer__brand-name">{brandName}</p>
        {brandDescription ? (
          <p className="ez-footer__brand-description">{brandDescription}</p>
        ) : null}
      </div>

      {sections.length ? (
        <nav className="ez-footer__navigation" aria-label="Enlaces informativos">
          {sections.map((section) => (
            <section key={section.title} className="ez-footer__section">
              <h3 className="ez-footer__section-title">{section.title}</h3>
              <ul className="ez-footer__links" role="list">
                {section.links.map((link) => (
                  <li key={link.href} className="ez-footer__link-item" role="listitem">
                    <a className="ez-footer__link" href={link.href}>
                      {link.label}
                    </a>
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </nav>
      ) : null}

      {accessibilityStatement ? (
        <section className="ez-footer__accessibility" aria-label="Declaración de accesibilidad">
          {accessibilityStatement}
        </section>
      ) : null}

      {legal ? (
        <section className="ez-footer__legal" aria-label="Información legal">
          {legal}
        </section>
      ) : null}
    </footer>
  );
}

export default Footer;
