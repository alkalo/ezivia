import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import WaitlistForm from "../app/components/WaitlistForm";

describe("WaitlistForm accessibility affordances", () => {
  const baseProps = {
    title: "Resérvate Ezivia para tu familiar o paciente",
    description:
      "Déjanos tus datos y te avisaremos en cuanto la versión estable de Ezivia esté lista.",
    privacyNote: "Guardamos tus datos de forma segura y solo los utilizaremos para avisarte."
  };

  it("marks the form busy and announces success messages", async () => {
    let resolveFetch: ((response: unknown) => void) | null = null;
    const mockFetch = jest.fn(() =>
      new Promise((resolve) => {
        resolveFetch = resolve;
      })
    );

    global.fetch = mockFetch as unknown as typeof fetch;

    const user = userEvent.setup();
    render(<WaitlistForm {...baseProps} />);

    await act(async () => {
      await user.type(screen.getByLabelText(/nombre completo/i), "Ana Pérez");
      await user.type(screen.getByLabelText(/correo electrónico/i), "ana@example.com");
    });

    const submitButton = screen.getByRole("button", { name: /lista de espera/i });
    await act(async () => {
      await user.click(submitButton);
    });

    const form = screen.getByRole("form", { name: baseProps.title });
    await waitFor(() => expect(form).toHaveAttribute("aria-busy", "true"));

    await act(async () => {
      resolveFetch?.({
        ok: true,
        json: async () => ({})
      });
    });

    const status = await screen.findByRole("status");
    expect(form).toHaveAttribute("aria-busy", "false");
    expect(status).toHaveAttribute("aria-live", "polite");
    expect(submitButton).toHaveAttribute("aria-describedby", status.getAttribute("id"));
  });

  it("surfaces assertive alerts when submissions fail", async () => {
    const mockFetch = jest.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ error: "Falla de red" })
    });

    global.fetch = mockFetch as unknown as typeof fetch;

    const user = userEvent.setup();
    render(<WaitlistForm {...baseProps} />);

    await act(async () => {
      await user.type(screen.getByLabelText(/nombre completo/i), "Ana Pérez");
      await user.type(screen.getByLabelText(/correo electrónico/i), "ana@example.com");
      await user.click(screen.getByRole("button", { name: /lista de espera/i }));
    });

    const alert = await screen.findByRole("alert");
    expect(alert).toHaveAttribute("aria-live", "assertive");
    expect(alert).toHaveTextContent(/falla de red/i);
  });
});
