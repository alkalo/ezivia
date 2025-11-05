import type { Preview } from "@storybook/react";
import "../app/globals.css";

const preview: Preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
    a11y: {
      options: {
        checks: {
          "color-contrast": { options: { noScroll: true } },
        },
        restoreFocus: true,
      },
    },
    layout: "centered",
    docs: {
      toc: true,
    },
  },
};

export default preview;
