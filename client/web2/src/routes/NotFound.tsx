import { Link } from 'react-router-dom';

function NotFound() {
  return (
    <section className="not-found">
      <h1>Ups, esta sección no existe</h1>
      <p>Puede que la URL haya cambiado o que el contenido esté en revisión.</p>
      <Link className="cta" to="/">
        Volver al inicio
      </Link>
    </section>
  );
}

export default NotFound;
