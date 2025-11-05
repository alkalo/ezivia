import { Link, Outlet, useLocation } from 'react-router-dom';
import TapTopLogo from '../ui/TapTopLogo';

const navItems = [
  { path: '/', label: 'Inicio' },
  { path: '/agenda', label: 'Agenda' }
];

function App() {
  const location = useLocation();

  return (
    <div className="layout">
      <header className="header">
        <TapTopLogo />
        <nav aria-label="Navegación principal">
          <ul className="nav">
            {navItems.map(({ path, label }) => {
              const isActive = location.pathname === path;
              return (
                <li key={path} className={isActive ? 'nav__item nav__item--active' : 'nav__item'}>
                  <Link to={path}>{label}</Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </header>
      <main className="main">
        <Outlet />
      </main>
      <footer className="footer">
        <p>© {new Date().getFullYear()} TapTop. Todos los derechos reservados.</p>
      </footer>
    </div>
  );
}

export default App;
