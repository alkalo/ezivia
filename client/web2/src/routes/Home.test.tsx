import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Home from './Home';

describe('Home route', () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllEnvs();
  });

  it('muestra la URL del backend configurada', () => {
    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );
    expect(screen.getByText(/Próximamente conectaremos esta web/)).toBeInTheDocument();

    const apiUrl = 'https://api-pruebas.taptop.es';
    vi.stubEnv('VITE_API_BASE_URL', apiUrl);

    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );

    expect(screen.getAllByText(apiUrl)[0]).toBeInTheDocument();
  });
});
