import { Link } from 'react-router-dom';

function Home() {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'https://api.taptop.es';

  return (
    <section className="hero">
      <h1>Bienvenido a la nueva experiencia TapTop</h1>
      <p>
        Descubre contenidos exclusivos, promociones y programación especial pensada para los fans que siguen cada novedad del
        festival.
      </p>
      <p className="hero__api">
        Próximamente conectaremos esta web con nuestro backend en <strong>{apiBaseUrl}</strong> para ofrecerte datos en vivo.
      </p>
      <Link className="cta" to="/agenda">
        Ver agenda
      </Link>
    </section>
  );
}

export default Home;
