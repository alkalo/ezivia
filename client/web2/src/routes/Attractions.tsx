const attractions = [
  {
    name: 'Rooftop Sessions',
    description: 'Música en vivo al atardecer con vistas panorámicas de la ciudad.',
    schedule: 'Viernes y sábados a partir de las 19:00'
  },
  {
    name: 'Game Lounge',
    description: 'Zona retro con las máquinas arcade favoritas de la comunidad TapTop.',
    schedule: 'Disponible todos los días de 12:00 a 02:00'
    // Diseño del Game Lounge: iluminación neón, mobiliario modular y estaciones temáticas que cambian semanalmente para
    // reforzar la narrativa del club sin desentonar con la estética principal.
  },
  {
    name: 'Mixology Lab',
    description: 'Cocktails de autor inspirados en los DJs invitados del mes.',
    schedule: 'Sesiones especiales los jueves a las 21:00'
  }
];

function Attractions() {
  return (
    <section aria-labelledby="agenda-heading">
      <h1 id="agenda-heading">Agenda destacada</h1>
      <p className="agenda__intro">
        Esta es la programación curada para la web secundaria del club. Actualizamos la información semanalmente para que no te
        pierdas ninguna experiencia.
      </p>
      <ul className="agenda__list">
        {attractions.map((attraction) => (
          <li key={attraction.name} className="agenda__item">
            <h2>{attraction.name}</h2>
            <p>{attraction.description}</p>
            <p className="agenda__schedule">{attraction.schedule}</p>
          </li>
        ))}
      </ul>
    </section>
  );
}

export default Attractions;
