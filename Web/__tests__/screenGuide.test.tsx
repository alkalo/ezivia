import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { act } from "react";

import { ScreenGuide } from "../app/components/ScreenGuide";

const speechSynthesisMock = {
  speak: jest.fn(),
  cancel: jest.fn(),
};

declare global {
  // eslint-disable-next-line no-var
  var SpeechSynthesisUtterance: {
    new (text: string): { text: string } & Partial<SpeechSynthesisUtterance>;
  };
}

describe("ScreenGuide", () => {
  beforeEach(() => {
    speechSynthesisMock.speak.mockReset();
    speechSynthesisMock.cancel.mockReset();
    (global as typeof globalThis).speechSynthesis = speechSynthesisMock as unknown as SpeechSynthesis;
    global.SpeechSynthesisUtterance = function (text: string) {
      return {
        text,
        lang: "",
        rate: 1,
        pitch: 1,
        volume: 1,
        onend: null,
        onerror: null,
      };
    } as never;
  });

  it("shows the guide message and opens the contrasted bubble on demand", async () => {
    render(
      <ScreenGuide
        title="Orientación de prueba"
        message="Mensaje de ayuda accesible para la pantalla"
      />,
    );

    expect(
      screen.getByText("Mensaje de ayuda accesible para la pantalla"),
    ).toBeVisible();

    const helpButton = screen.getByRole("button", { name: /abrir ayuda de esta pantalla/i });
    const user = userEvent.setup();
    await act(async () => {
      await user.click(helpButton);
    });

    const dialog = screen.getByRole("dialog", { name: /ayuda contextual/i });
    expect(dialog).toBeVisible();
    expect(within(dialog).getByText(/mensaje de ayuda accesible/i)).toBeVisible();
  });

  it("uses speech synthesis to read the guide when requested", async () => {
    render(
      <ScreenGuide
        title="Orientación de prueba"
        message="Mensaje de ayuda accesible para la pantalla"
      />,
    );

    const playButton = await screen.findByRole("button", { name: /escuchar indicación/i });
    await waitFor(() => expect(playButton).toBeEnabled());

    const user = userEvent.setup();
    await act(async () => {
      await user.click(playButton);
    });

    expect(speechSynthesisMock.speak).toHaveBeenCalledTimes(1);
    const utterance = speechSynthesisMock.speak.mock.calls[0][0] as SpeechSynthesisUtterance;
    expect(utterance.text).toBe("Mensaje de ayuda accesible para la pantalla");
  });
});
