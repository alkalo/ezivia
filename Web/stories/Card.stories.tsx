import type { Meta, StoryObj } from "@storybook/react";
import { Card } from "../components/ui";

const meta: Meta<typeof Card> = {
  title: "UI/Card",
  component: Card,
  args: {
    title: "Asistencia personalizada",
    description:
      "Nuestra app guía a las personas mayores paso a paso para que se sientan seguras al usar su móvil.",
    children: "Contenido adicional disponible para complementar la tarjeta.",
  },
};

export default meta;

type Story = StoryObj<typeof Card>;

export const Default: Story = {};

export const WithEyebrow: Story = {
  args: {
    eyebrow: "Pensado para mayores",
  },
};

export const WithFooter: Story = {
  args: {
    footer: <small>Disponible solo para Android.</small>,
  },
};
