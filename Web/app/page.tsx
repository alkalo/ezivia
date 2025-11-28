import Image from "next/image";
import Link from "next/link";
import content from "../content/home.json";
import { CTAButton, CTAButtons } from "./components/CTAButtons";
import { ScreenGuide } from "./components/ScreenGuide";
import WaitlistForm from "./components/WaitlistForm";

type HomeContent = {
  hero: {
    eyebrow?: string;
    title: string;
    description: string;
    primaryCta: CTAButton;
    secondaryCta?: CTAButton;
    image: {
      src: string;
      alt: string;
    };
  };
  benefits: {
    title: string;
    description: string;
    items: Array<{
      title: string;
      description: string;
    }>;
  };
  howItWorks: {
    title: string;
    description: string;
    steps: Array<{
      title: string;
      description: string;
    }>;
    note?: string;
  };
  testimonials: {
    title: string;
    description: string;
    items: Array<{
      quote: string;
      author: string;
      role: string;
    }>;
  };
  waitlist: {
    title: string;
    description: string;
    privacyNote: string;
  };
  ctaDownload: {
    title: string;
    description: string;
    buttons: CTAButton[];
    footnote?: string;
  };
};

type EssentialAction = {
  id: "call" | "video" | "settings" | "sos";
  title: string;
  description: string;
  accent: string;
  glow: string;
};

const essentialActions: EssentialAction[] = [
  {
    id: "call",
    title: "Llamadas claras",
    description:
      "Botón de llamada con trazo grueso y feedback inmediato para hablar con la familia favorita.",
    accent: "#ffe7c4",
    glow: "rgba(255, 140, 66, 0.3)",
  },
  {
    id: "video",
    title: "Videollamadas cálidas",
    description:
      "Cámara enmarcada en color coral y contornos redondeados que invitan a conversar cara a cara.",
    accent: "#fff4e5",
    glow: "rgba(47, 42, 31, 0.12)",
  },
  {
    id: "settings",
    title: "Ajustes guiados",
    description:
      "Engrane grueso y amable que resume los accesos a tamaño de letra, brillo, sonido y favoritos.",
    accent: "#fefbf6",
    glow: "rgba(31, 26, 18, 0.12)",
  },
  {
    id: "sos",
    title: "SOS visible",
    description:
      "Escudo de seguridad en alto contraste con señal luminosa para pedir ayuda sin dudas.",
    accent: "#ffe0bf",
    glow: "rgba(255, 140, 66, 0.35)",
  },
];

const homeContent = content as HomeContent;

function EssentialIcon({ id }: { id: EssentialAction["id"] }) {
  const stroke = "#1f1a12";
  const accent = "#ff8c42";

  if (id === "call") {
    return (
      <svg
        aria-hidden
        viewBox="0 0 64 64"
        role="img"
        className="action-panel__icon-svg"
      >
        <rect
          x="10"
          y="8"
          width="44"
          height="48"
          rx="12"
          fill="#fff9f1"
          stroke={stroke}
          strokeWidth="3"
        />
        <path
          d="M25 28.5c2 3.4 5.2 6.6 8.6 8.6l4.6-4.6a3.5 3.5 0 0 1 3.6-.85l6 1.9c1 .33 1.7 1.27 1.7 2.34v5.77c0 1.38-1.25 2.4-2.57 2.04-15.35-4.3-27-15.96-31.3-31.3C14.37 11.1 15.4 9.86 16.77 9.86h5.77c1.07 0 2 .7 2.34 1.71l1.9 6a3.5 3.5 0 0 1-.86 3.6Z"
          fill="#ffe7c4"
          stroke={stroke}
          strokeWidth="3"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M33.5 16.5c2.2-.8 5.2-.5 6.7 1"
          stroke={accent}
          strokeWidth="3"
          strokeLinecap="round"
        />
      </svg>
    );
  }

  if (id === "video") {
    return (
      <svg
        aria-hidden
        viewBox="0 0 64 64"
        role="img"
        className="action-panel__icon-svg"
      >
        <rect
          x="9"
          y="14"
          width="35"
          height="36"
          rx="10"
          fill="#fff4e5"
          stroke={stroke}
          strokeWidth="3"
        />
        <path
          d="M44 24.5 54 20c1.2-.5 2.5.4 2.5 1.7v20.6c0 1.3-1.3 2.2-2.5 1.7L44 39.5Z"
          fill="#ffe0bf"
          stroke={stroke}
          strokeWidth="3"
          strokeLinejoin="round"
        />
        <rect
          x="17.5"
          y="22"
          width="16"
          height="13"
          rx="3.5"
          fill="#fefbf6"
          stroke={accent}
          strokeWidth="3"
        />
      </svg>
    );
  }

  if (id === "settings") {
    return (
      <svg
        aria-hidden
        viewBox="0 0 64 64"
        role="img"
        className="action-panel__icon-svg"
      >
        <circle
          cx="32"
          cy="32"
          r="14"
          fill="#fefbf6"
          stroke={accent}
          strokeWidth="3"
        />
        <path
          d="m32 13 2.4 4.8c.24.48.72.8 1.26.86l5.35.57c1.26.13 2.14 1.34 1.86 2.57l-1.06 4.8c-.11.52.03 1.05.37 1.46l3.2 3.86c.84 1.02.63 2.55-.45 3.34l-4.28 3.12c-.43.32-.68.84-.65 1.38l.24 5.1c.06 1.32-1.06 2.36-2.32 2.12l-5.2-1c-.51-.1-1.04.03-1.45.35L27 49.8c-1.05.82-2.55.6-3.33-.47l-3.12-4.28a1.9 1.9 0 0 0-1.38-.66l-5.1.24c-1.32.06-2.36-1.06-2.12-2.32l1-5.2c.1-.51-.04-1.04-.35-1.45L13 27a2 2 0 0 1 .47-2.83l4.28-3.12c.43-.32.68-.84.66-1.38l-.24-5.1c-.06-1.32 1.06-2.36 2.32-2.12l5.2 1c.51.1 1.04-.04 1.45-.35Z"
          fill="#fff4e5"
          stroke={stroke}
          strokeWidth="3"
          strokeLinejoin="round"
        />
        <circle cx="32" cy="32" r="5.5" fill="#ffe7c4" stroke={stroke} strokeWidth="3" />
      </svg>
    );
  }

  return (
    <svg
      aria-hidden
      viewBox="0 0 64 64"
      role="img"
      className="action-panel__icon-svg"
    >
      <path
        d="M12 28.5c0-5.6 3.3-10.6 8.5-12.8l10.7-4.7a6 6 0 0 1 4.6 0l10.7 4.7c5.2 2.3 8.5 7.3 8.5 12.8v9.6c0 2.6-1.1 5-3 6.8l-8.3 7.7a10 10 0 0 1-13.6 0l-8.3-7.7c-1.9-1.8-3-4.2-3-6.8Z"
        fill="#ffe0bf"
        stroke={stroke}
        strokeWidth="3"
        strokeLinejoin="round"
      />
      <path
        d="M26 33.5h12.25"
        stroke="#b53d3d"
        strokeWidth="3.5"
        strokeLinecap="round"
      />
      <path
        d="M32 27.25V40"
        stroke="#b53d3d"
        strokeWidth="3.5"
        strokeLinecap="round"
      />
      <path
        d="M18.5 33.75c1.4-5.1 5.5-9.05 10.8-10.28"
        stroke={accent}
        strokeWidth="3"
        strokeLinecap="round"
      />
      <path
        d="M45.5 22.5c2.1 1.8 3.5 4.4 3.5 7.7"
        stroke={accent}
        strokeWidth="3"
        strokeLinecap="round"
      />
    </svg>
  );
}

function FriendlyActionsIllustration() {
  return (
    <svg
      aria-hidden
      className="action-panel__illustration-figure"
      viewBox="0 0 320 220"
      role="img"
    >
      <defs>
        <linearGradient id="bubbleGradient" x1="0" x2="1" y1="0" y2="1">
          <stop stopColor="#fff4e5" offset="0%" />
          <stop stopColor="#ffe7c4" offset="100%" />
        </linearGradient>
        <linearGradient id="shine" x1="0" x2="1" y1="0" y2="1">
          <stop stopColor="#ffb074" offset="0%" />
          <stop stopColor="#ff8c42" offset="100%" />
        </linearGradient>
      </defs>
      <rect
        x="14"
        y="20"
        width="292"
        height="180"
        rx="44"
        fill="url(#bubbleGradient)"
        stroke="#1f1a12"
        strokeWidth="4"
      />
      <rect
        x="70"
        y="42"
        width="180"
        height="136"
        rx="28"
        fill="#fefbf6"
        stroke="#2f2a1f"
        strokeWidth="4"
      />
      <rect
        x="95"
        y="62"
        width="130"
        height="96"
        rx="18"
        fill="#fff4e5"
        stroke="#2f2a1f"
        strokeWidth="4"
      />
      <circle cx="120" cy="92" r="16" fill="#ffe0bf" stroke="#2f2a1f" strokeWidth="4" />
      <circle cx="198" cy="92" r="16" fill="#ffe7c4" stroke="#2f2a1f" strokeWidth="4" />
      <path
        d="M124 94c6 12 22 25 35 25 8.5 0 14.5-4.4 19-10"
        fill="none"
        stroke="#2f2a1f"
        strokeWidth="4"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <rect
        x="122"
        y="120"
        width="76"
        height="14"
        rx="7"
        fill="#fff"
        stroke="#2f2a1f"
        strokeWidth="4"
      />
      <path
        d="M240 176c10 0 18-8 18-18 0-3.3-1-6.5-2.8-9.2l-14.2-21.3a4 4 0 0 0-6.7 0l-14.2 21.3A17.2 17.2 0 0 0 218 158c0 10 8 18 18 18Z"
        fill="#ffe7c4"
        stroke="#2f2a1f"
        strokeWidth="4"
      />
      <path
        d="M240 168c5.5 0 10-4.5 10-10 0-1.9-.5-3.7-1.5-5.3l-8.3-12.5a2.2 2.2 0 0 0-3.6 0l-8.2 12.5c-1 1.6-1.6 3.4-1.6 5.3 0 5.5 4.5 10 10 10Z"
        fill="#ffb074"
        stroke="#1f1a12"
        strokeWidth="3.5"
      />
      <path
        d="M68 156c-9.9 0-18-8-18-18 0-3.3 1-6.5 2.8-9.2l14.2-21.3a4 4 0 0 1 6.7 0l14.2 21.3c1.8 2.7 2.8 5.9 2.8 9.2 0 10-8 18-18 18Z"
        fill="#ffe0bf"
        stroke="#2f2a1f"
        strokeWidth="4"
      />
      <path
        d="M68 146.5c-5.5 0-10-4.5-10-10 0-1.9.5-3.8 1.6-5.3l8.2-12.5c.8-1.2 2.8-1.2 3.6 0l8.3 12.5c1 1.6 1.5 3.4 1.5 5.3 0 5.5-4.5 10-10 10Z"
        fill="#fff"
        stroke="#1f1a12"
        strokeWidth="3.5"
      />
      <path
        d="M98 42c0-11 9-20 20-20h84c11 0 20 9 20 20"
        stroke="url(#shine)"
        strokeWidth="5"
        strokeLinecap="round"
      />
    </svg>
  );
}

export default function HomePage() {
  const { hero, benefits, howItWorks, testimonials, waitlist, ctaDownload } =
    homeContent;

  return (
    <main className="home-page">
      <ScreenGuide
        title="Bienvenida a Ezivia"
        message="Estás en la página principal de Ezivia. Desplázate para conocer los beneficios, cómo funciona y únete a la lista de espera con los botones destacados."
      />
      <section
        id="inicio"
        className="section hero"
        aria-labelledby="hero-title"
        aria-describedby="hero-description"
      >
        <div className="hero__content">
          {hero.eyebrow ? <p className="section__eyebrow">{hero.eyebrow}</p> : null}
          <h1 id="hero-title">{hero.title}</h1>
          <p id="hero-description" className="section__description">
            {hero.description}
          </p>
          <CTAButtons
            className="hero__actions"
            buttons={[
              hero.primaryCta,
              ...(hero.secondaryCta ? [hero.secondaryCta] : []),
            ]}
          />
          <p className="hero__support">
            ¿Tienes dudas? Visita nuestras {" "}
            <Link href="/faq">Preguntas frecuentes</Link>.
          </p>
        </div>
        <div className="hero__media">
          <Image
            src={hero.image.src}
            alt={hero.image.alt}
            width={460}
            height={420}
            priority
          />
        </div>
      </section>

      <section
        id="accesos-esenciales"
        className="section section--surface action-panel"
        aria-labelledby="actions-title"
        aria-describedby="actions-description"
      >
        <div className="action-panel__layout">
          <div className="action-panel__intro">
            <p className="section__eyebrow">Iconos en trazo grueso</p>
            <h2 id="actions-title">Accesos esenciales a la vista</h2>
            <p id="actions-description" className="section__description">
              Llamada, videollamada, ajustes guiados y SOS se muestran con contornos redondeados y colores
              cálidos para que cada acción importante sea fácil de encontrar.
            </p>
            <p className="action-panel__note">
              Todos los iconos siguen la paleta crema, coral y marrón oscuro de Ezivia y mantienen un grosor de
              trazo pensado para personas mayores.
            </p>
          </div>
          <div className="action-panel__illustration" aria-hidden="true">
            <FriendlyActionsIllustration />
            <p className="action-panel__caption">
              Ilustración amable con formas redondeadas y fondos claros que acompañan el tablero de acciones.
            </p>
          </div>
        </div>

        <div className="action-panel__grid" role="list">
          {essentialActions.map((action) => (
            <article
              key={action.id}
              className="action-panel__card"
              role="listitem"
              style={{
                background: action.accent,
                boxShadow: `0 16px 32px ${action.glow}`,
              }}
            >
              <span className="action-panel__icon" aria-hidden>
                <EssentialIcon id={action.id} />
              </span>
              <div className="action-panel__content">
                <h3>{action.title}</h3>
                <p>{action.description}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section
        id="beneficios"
        className="section section--surface"
        aria-labelledby="benefits-title"
        aria-describedby="benefits-description"
      >
        <div className="section__header">
          <h2 id="benefits-title">{benefits.title}</h2>
          <p id="benefits-description" className="section__description">
            {benefits.description}
          </p>
        </div>
        <div className="card-grid" role="list">
          {benefits.items.map((item) => (
            <article key={item.title} className="card" role="listitem">
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </article>
          ))}
        </div>
      </section>

      <section
        id="como-funciona"
        className="section"
        aria-labelledby="how-title"
        aria-describedby="how-description"
      >
        <div className="section__header">
          <h2 id="how-title">{howItWorks.title}</h2>
          <p id="how-description" className="section__description">
            {howItWorks.description}
          </p>
        </div>
        <ol className="step-list">
          {howItWorks.steps.map((step) => (
            <li key={step.title} className="step-list__item">
              <h3>{step.title}</h3>
              <p>{step.description}</p>
            </li>
          ))}
        </ol>
        {howItWorks.note ? (
          <p className="section__note" role="note">
            {howItWorks.note}
          </p>
        ) : null}
        <p className="section__support">
          Para guías adicionales consulta las {" "}
          <Link href="/faq">Preguntas frecuentes</Link>.
        </p>
      </section>

      <section
        id="testimonios"
        className="section section--surface"
        aria-labelledby="testimonials-title"
        aria-describedby="testimonials-description"
      >
        <div className="section__header">
          <h2 id="testimonials-title">{testimonials.title}</h2>
          <p id="testimonials-description" className="section__description">
            {testimonials.description}
          </p>
        </div>
        <div className="testimonial-grid" role="list">
          {testimonials.items.map((testimonial) => (
            <figure key={testimonial.quote} className="testimonial" role="listitem">
              <blockquote>
                <p>{testimonial.quote}</p>
              </blockquote>
              <figcaption>
                <span className="testimonial__author">{testimonial.author}</span>
                <span className="testimonial__role">{testimonial.role}</span>
              </figcaption>
            </figure>
          ))}
        </div>
      </section>

      <WaitlistForm
        title={waitlist.title}
        description={waitlist.description}
        privacyNote={waitlist.privacyNote}
      />

      <section
        id="descarga"
        className="section cta"
        aria-labelledby="cta-title"
        aria-describedby="cta-description"
      >
        <div className="cta__card">
          <h2 id="cta-title">{ctaDownload.title}</h2>
          <p id="cta-description" className="section__description">
            {ctaDownload.description}
          </p>
          <CTAButtons buttons={ctaDownload.buttons} />
          {ctaDownload.footnote ? (
            <p className="cta__footnote">{ctaDownload.footnote}</p>
          ) : null}
        </div>
      </section>
    </main>
  );
}
