import content from "../../content/caregivers.json";
import { CTAButton, CTAButtons } from "../components/CTAButtons";

type CaregiversContent = {
  hero: {
    eyebrow?: string;
    title: string;
    description: string;
    primaryCta: CTAButton;
    secondaryCta?: CTAButton;
  };
  remoteSetup: FeatureSection;
  sosAlerts: FeatureSection;
  privacy: FeatureSection;
  security: FeatureSection;
  cta: {
    title: string;
    description: string;
    buttons: CTAButton[];
    footnote?: string;
  };
};

type FeatureSection = {
  title: string;
  description: string;
  items: string[];
};

const caregiversContent = content as CaregiversContent;

export default function CaregiversPage() {
  const { hero, remoteSetup, sosAlerts, privacy, security, cta } = caregiversContent;

  return (
    <main className="caregivers-page">
      <section
        className="section hero hero--caregivers"
        aria-labelledby="caregivers-hero-title"
        aria-describedby="caregivers-hero-description"
      >
        <div className="hero__content">
          {hero.eyebrow ? <p className="section__eyebrow">{hero.eyebrow}</p> : null}
          <h1 id="caregivers-hero-title">{hero.title}</h1>
          <p id="caregivers-hero-description" className="section__description">
            {hero.description}
          </p>
          <CTAButtons
            className="hero__actions"
            buttons={[hero.primaryCta, ...(hero.secondaryCta ? [hero.secondaryCta] : [])]}
          />
        </div>
      </section>

      <FeatureSectionBlock
        id="configuracion-remota"
        section={remoteSetup}
        background="surface"
      />

      <FeatureSectionBlock id="alertas-sos" section={sosAlerts} />

      <FeatureSectionBlock id="privacidad" section={privacy} background="surface" />

      <FeatureSectionBlock id="seguridad" section={security} />

      <section
        id="cta-cuidadores"
        className="section cta"
        aria-labelledby="caregivers-cta-title"
        aria-describedby="caregivers-cta-description"
      >
        <div className="cta__card">
          <h2 id="caregivers-cta-title">{cta.title}</h2>
          <p id="caregivers-cta-description" className="section__description">
            {cta.description}
          </p>
          <CTAButtons buttons={cta.buttons} />
          {cta.footnote ? <p className="cta__footnote">{cta.footnote}</p> : null}
        </div>
      </section>
    </main>
  );
}

type FeatureSectionBlockProps = {
  id: string;
  section: FeatureSection;
  background?: "surface" | "plain";
};

function FeatureSectionBlock({ id, section, background = "plain" }: FeatureSectionBlockProps) {
  return (
    <section
      id={id}
      className={`section ${background === "surface" ? "section--surface" : ""}`.trim()}
      aria-labelledby={`${id}-title`}
      aria-describedby={`${id}-description`}
    >
      <div className="section__header">
        <h2 id={`${id}-title`}>{section.title}</h2>
        <p id={`${id}-description`} className="section__description">
          {section.description}
        </p>
      </div>
      <ul className="feature-list">
        {section.items.map((item) => (
          <li key={item} className="feature-list__item">
            {item}
          </li>
        ))}
      </ul>
    </section>
  );
}
