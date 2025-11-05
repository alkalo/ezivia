import type { Meta, StoryObj } from "@storybook/react";
import { FeatureList } from "../components/ui";

const meta: Meta<typeof FeatureList> = {
  title: "UI/FeatureList",
  component: FeatureList,
  args: {
    title: "Funciones principales",
    description: "Todo lo necesario para simplificar cualquier móvil Android.",
    items: [
      {
        title: "Pantallas simplificadas",
        description: "Iconos grandes y textos claros para una lectura rápida.",
      },
      {
        title: "Asistente de voz",
        description: "Guía paso a paso usando instrucciones habladas y subtituladas.",
      },
      {
        title: "Alertas inteligentes",
        description: "Notificaciones priorizadas para recordar citas y medicación.",
      },
    ],
  },
};

export default meta;

type Story = StoryObj<typeof FeatureList>;

export const Default: Story = {};
