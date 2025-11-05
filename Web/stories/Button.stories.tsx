import type { Meta, StoryObj } from "@storybook/react";
import { Button } from "../components/ui";

const meta: Meta<typeof Button> = {
  title: "UI/Button",
  component: Button,
  args: {
    children: "Descargar Ezivia",
  },
  parameters: {
    a11y: {
      element: "button",
    },
  },
};

export default meta;

type Story = StoryObj<typeof Button>;

export const Primary: Story = {
  args: {
    variant: "primary",
    emphasis: "high",
  },
};

export const Secondary: Story = {
  args: {
    variant: "secondary",
    emphasis: "medium",
    children: "Solicitar ayuda",
  },
};

export const Ghost: Story = {
  args: {
    variant: "ghost",
    emphasis: "low",
    children: "Conocer Ezivia",
  },
};
