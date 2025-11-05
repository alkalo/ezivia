import { render, screen, within, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Button, Card, FeatureList, CTA, Footer } from "../components/ui";

describe("UI accessibility primitives", () => {
  it("allows keyboard users to focus and activate the primary button", async () => {
    const handleClick = jest.fn();
    render(
      <Button onClick={handleClick}>Descargar Ezivia</Button>,
    );

    const button = screen.getByRole("button", { name: /descargar ezivia/i });
    const user = userEvent.setup();
    await user.tab();
    expect(button).toHaveFocus();
    await user.keyboard("{Enter}");
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it("exposes card content using semantic regions", () => {
    render(
      <Card title="Soporte dedicado" description="Acompañamiento 24/7 para cuidadores.">
        Resolvemos dudas en menos de 5 minutos.
      </Card>,
    );

    const article = screen.getByRole("article", {
      name: /soporte dedicado/i,
    });
    expect(article).toBeInTheDocument();
    expect(
      within(article).getByText(/acompañamiento 24\/7/i),
    ).toBeVisible();
  });

  it("announces feature lists with correct list semantics", () => {
    render(
      <FeatureList
        title="Funciones Ezivia"
        description="Todo lo imprescindible para simplificar tu Android."
        items={[
          { title: "Modo abuela", description: "Pantallas limpias y tipografía grande." },
          { title: "Emergencias", description: "Contacto directo con familiares de confianza." },
        ]}
      />,
    );

    const region = screen.getByRole("region", { name: /funciones ezivia/i });
    const list = within(region).getByRole("list");
    const items = within(list).getAllByRole("listitem");
    expect(items).toHaveLength(2);
  });

  it("can guide focus to the CTA primary action when requested", async () => {
    render(
      <CTA
        heading="Configura Ezivia"
        description="Simplifica el móvil de un familiar en menos de 3 pasos."
        primaryAction={{ label: "Empezar ahora" }}
        secondaryAction={{ label: "Ver tutorial" }}
        autoFocusFirstButton
      />,
    );

    const primary = await screen.findByRole("button", { name: /empezar ahora/i });
    await waitFor(() => expect(primary).toHaveFocus());

    const group = screen.getByRole("group", { name: /acciones principales/i });
    const buttons = within(group).getAllByRole("button");
    expect(buttons).toHaveLength(2);
  });

  it("declares footer information inside the contentinfo landmark", () => {
    render(
      <Footer
        brandName="Ezivia"
        brandDescription="Conversor de móviles Android para personas mayores."
        sections={[
          {
            title: "Guías",
            links: [
              { label: "Preguntas frecuentes", href: "/faq" },
              { label: "Soporte", href: "/soporte" },
            ],
          },
        ]}
        accessibilityStatement={<p>Compatible con lectores de pantalla y modo oscuro.</p>}
      />,
    );

    const contentInfo = screen.getByRole("contentinfo");
    expect(contentInfo).toBeInTheDocument();
    expect(
      within(contentInfo).getByRole("navigation", { name: /enlaces informativos/i }),
    ).toBeInTheDocument();
  });
});
