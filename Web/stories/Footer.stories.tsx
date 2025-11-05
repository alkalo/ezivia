import type { Meta, StoryObj } from "@storybook/react";
import { Footer } from "../components/ui";

const meta: Meta<typeof Footer> = {
  title: "UI/Footer",
  component: Footer,
  args: {
    brandName: "Ezivia",
    brandDescription: "Conversor de móviles Android pensado para personas mayores.",
    sections: [
      {
        title: "Recursos",
        links: [
          { label: "Preguntas frecuentes", href: "/faq" },
          { label: "Soporte para cuidadores", href: "/cuidadores" },
        ],
      },
      {
        title: "Legal",
        links: [
          { label: "Política de privacidad", href: "/legal/privacidad" },
          { label: "Términos de uso", href: "/legal/terminos" },
        ],
      },
    ],
    accessibilityStatement: (
      <p>
        Nuestro equipo revisa Ezivia con personas mayores para garantizar compatibilidad con lectores
        de pantalla y tamaños de texto dinámicos.
      </p>
    ),
    legal: <small>© {new Date().getFullYear()} Ezivia. Todos los derechos reservados.</small>,
  },
};

export default meta;

type Story = StoryObj<typeof Footer>;

export const Default: Story = {};
