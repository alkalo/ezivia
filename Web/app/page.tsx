import Link from "next/link";

const featureList = [
  "Iconos extragrandes y texto claro para una lectura sencilla",
  "Asistente paso a paso para configurar teléfonos Android",
  "Atajos de comunicación directa con familiares y cuidadores",
  "Recordatorios de medicación y citas con alertas sonoras suaves",
  "Modo de emergencia que comparte ubicación y contactos de confianza"
];

export default function HomePage() {
  return (
    <main className="hero">
      <section className="hero__panel" aria-labelledby="hero-title">
        <h1 id="hero-title">Bienvenido a Ezivia</h1>
        <p>
          Ezivia transforma móviles Android modernos en dispositivos amigables
          para personas mayores. Simplificamos la tecnología para que puedan
          mantenerse conectadas con seguridad y confianza.
        </p>
        <Link className="hero__cta" href="mailto:hola@ezivia.com">
          Quiero probar Ezivia
        </Link>
      </section>
      <section className="hero__features" aria-label="Características principales de Ezivia">
        <h2>Diseñada para acompañar</h2>
        <ul>
          {featureList.map((feature) => (
            <li key={feature}>{feature}</li>
          ))}
        </ul>
      </section>
    </main>
  );
}
