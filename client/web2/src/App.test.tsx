import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import App from './routes/App';

function renderWithRouter(initialEntries = ['/']) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <Routes>
        <Route path="/" element={<App />}>
          <Route index element={<div>Inicio</div>} />
          <Route path="agenda" element={<div>Agenda</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

describe('App layout', () => {
  it('muestra el logo de TapTop y navegación', () => {
    renderWithRouter();
    expect(screen.getByRole('link', { name: /TapTop/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Inicio/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Agenda/i })).toBeInTheDocument();
  });

  it('resalta la ruta activa', () => {
    renderWithRouter(['/agenda']);
    const activeItem = screen.getByRole('link', { name: /Agenda/i });
    expect(activeItem.parentElement).toHaveClass('nav__item--active');
  });
});
