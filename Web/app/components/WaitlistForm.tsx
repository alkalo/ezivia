"use client";

import { FormEvent, useEffect, useState } from "react";
import { trackEvent } from "../lib/analytics";

type WaitlistFormState = "idle" | "loading" | "success" | "error";

type Props = {
  title: string;
  description: string;
  privacyNote: string;
};

const initialFormValues = {
  name: "",
  email: "",
  phone: "",
  message: ""
};

export const WaitlistForm = ({ title, description, privacyNote }: Props) => {
  const [formValues, setFormValues] = useState(initialFormValues);
  const [status, setStatus] = useState<WaitlistFormState>("idle");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    trackEvent("waitlist_form_viewed");
  }, []);

  const handleChange = (
    field: keyof typeof initialFormValues,
    value: string
  ) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const resetForm = () => {
    setFormValues(initialFormValues);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setStatus("loading");
    setErrorMessage(null);
    const submittedEmail = formValues.email.trim().toLowerCase();
    const payload = {
      ...formValues,
      name: formValues.name.trim(),
      email: submittedEmail,
      phone: formValues.phone.trim(),
      message: formValues.message.trim()
    };

    trackEvent("waitlist_form_submitted", {
      interaction: "attempt",
      email: submittedEmail
    });

    try {
      const response = await fetch("/api/waitlist", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const { error } = await response.json();
        throw new Error(error ?? "No se pudo enviar el formulario");
      }

      resetForm();
      setStatus("success");
      trackEvent("waitlist_form_submitted", {
        interaction: "success",
        email: submittedEmail
      });
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "Hemos tenido un problema. Inténtalo de nuevo en unos minutos.";
      setErrorMessage(message);
      setStatus("error");
      trackEvent("waitlist_form_submitted", {
        interaction: "error",
        email: submittedEmail
      });
    }
  };

  return (
    <section
      id="contacto"
      className="section waitlist"
      aria-labelledby="waitlist-title"
      aria-describedby="waitlist-description"
    >
      <div className="section__header">
        <h2 id="waitlist-title">{title}</h2>
        <p id="waitlist-description" className="section__description">
          {description}
        </p>
      </div>
      <form className="waitlist__form" onSubmit={handleSubmit}>
        <div className="waitlist__grid">
          <label className="waitlist__field">
            <span>Nombre completo</span>
            <input
              type="text"
              name="name"
              autoComplete="name"
              required
              value={formValues.name}
              onChange={(event) => handleChange("name", event.target.value)}
            />
          </label>
          <label className="waitlist__field">
            <span>Correo electrónico</span>
            <input
              type="email"
              name="email"
              autoComplete="email"
              required
              value={formValues.email}
              onChange={(event) => handleChange("email", event.target.value)}
            />
          </label>
          <label className="waitlist__field">
            <span>Teléfono de contacto (opcional)</span>
            <input
              type="tel"
              name="phone"
              autoComplete="tel"
              value={formValues.phone}
              onChange={(event) => handleChange("phone", event.target.value)}
            />
          </label>
          <label className="waitlist__field waitlist__field--full">
            <span>¿En qué podemos ayudarte?</span>
            <textarea
              name="message"
              rows={4}
              value={formValues.message}
              onChange={(event) => handleChange("message", event.target.value)}
            />
          </label>
        </div>
        <p className="waitlist__note">{privacyNote}</p>
        <div className="waitlist__actions">
          <button
            type="submit"
            className="cta-button cta-button--primary"
            disabled={status === "loading"}
          >
            {status === "loading" ? "Enviando…" : "Unirme a la lista de espera"}
          </button>
          {status === "success" ? (
            <p role="status" className="waitlist__status waitlist__status--success">
              ¡Gracias! Te contactaremos muy pronto.
            </p>
          ) : null}
          {status === "error" && errorMessage ? (
            <p role="alert" className="waitlist__status waitlist__status--error">
              {errorMessage}
            </p>
          ) : null}
        </div>
      </form>
    </section>
  );
};

export default WaitlistForm;
