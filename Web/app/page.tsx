import Image from "next/image";
import Link from "next/link";
import content from "../content/home.json";
import { CTAButton, CTAButtons } from "./components/CTAButtons";
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

const homeContent = content as HomeContent;

export default function HomePage() {
  const { hero, benefits, howItWorks, testimonials, waitlist, ctaDownload } =
    homeContent;

  return (
    <main className="home-page">
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
