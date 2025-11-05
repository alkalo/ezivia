import type { Meta, StoryObj } from "@storybook/react";
import { CTA } from "../components/ui";

const meta: Meta<typeof CTA> = {
  title: "UI/CTA",
  component: CTA,
  args: {
    heading: "Convierte cualquier móvil en Ezivia",
    description:
      "Descarga la app y transforma teléfonos Android en experiencias sencillas para tus seres queridos.",
    primaryAction: {
      label: "Descargar para Android",
      href: "https://example.com/android",
    },
    secondaryAction: {
      label: "Solicitar demostración",
    },
    footnote: "Disponible gratis con opciones premium para cuidadores.",
  },
};

export default meta;

type Story = StoryObj<typeof CTA>;

export const Default: Story = {};

export const Autofocus: Story = {
  args: {
    autoFocusFirstButton: true,
  },
  parameters: {
    docs: {
      description: {
        story:
          "Esta variante enfoca automáticamente el botón principal para demostrar el control del foco.",
      },
    },
  },
};
