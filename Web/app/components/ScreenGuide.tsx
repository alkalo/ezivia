"use client";

import { useEffect, useId, useState } from "react";

export type ScreenGuideProps = {
  message: string;
  title?: string;
};

const GUIDE_LANGUAGE = "es-ES";

export function ScreenGuide({ message, title = "Guía rápida" }: ScreenGuideProps) {
  const bubbleId = useId();
  const [isBubbleOpen, setIsBubbleOpen] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [canSpeak, setCanSpeak] = useState(false);

  useEffect(() => {
    const hasSpeechSupport =
      typeof window !== "undefined" &&
      "speechSynthesis" in window &&
      typeof window.SpeechSynthesisUtterance !== "undefined";
    setCanSpeak(hasSpeechSupport);

    return () => {
      if (hasSpeechSupport) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  const stopSpeaking = () => {
    if (!canSpeak) return;

    window.speechSynthesis.cancel();
    setIsSpeaking(false);
  };

  const speakMessage = () => {
    if (!canSpeak) return;

    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(message);
    utterance.lang = GUIDE_LANGUAGE;
    utterance.rate = 0.95;
    utterance.pitch = 1;
    utterance.volume = 1;
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);
    setIsSpeaking(true);
    window.speechSynthesis.speak(utterance);
  };

  const toggleHelp = () => {
    const nextState = !isBubbleOpen;
    setIsBubbleOpen(nextState);
    if (nextState) {
      speakMessage();
    } else {
      stopSpeaking();
    }
  };

  const playButtonLabel = isSpeaking
    ? "Reproduciendo la guía contextual"
    : canSpeak
      ? "Escuchar indicación"
      : "Lectura en voz alta no disponible";

  return (
    <div className="screen-guide" aria-live="polite">
      <div className="screen-guide__badge" aria-hidden>
        🧭
      </div>
      <div className="screen-guide__content">
        <p className="screen-guide__eyebrow">Orientación en voz y texto</p>
        <p className="screen-guide__title">{title}</p>
        <p className="screen-guide__message">{message}</p>
        <div className="screen-guide__actions">
          <button
            type="button"
            className="screen-guide__button"
            onClick={speakMessage}
            disabled={!canSpeak || isSpeaking}
            aria-label={playButtonLabel}
          >
            {isSpeaking ? "Reproduciendo…" : "Escuchar indicación"}
          </button>
          <button
            type="button"
            className="screen-guide__button screen-guide__button--ghost"
            onClick={stopSpeaking}
            disabled={!isSpeaking}
          >
            Detener audio
          </button>
          <p className="screen-guide__hint">
            Pulsa el botón con el signo de pregunta para volver a ver y escuchar
            esta guía cuando lo necesites.
          </p>
        </div>
      </div>

      <button
        type="button"
        className="screen-guide__fab"
        aria-label="Abrir ayuda de esta pantalla"
        aria-expanded={isBubbleOpen}
        onClick={toggleHelp}
      >
        ?
      </button>

      {isBubbleOpen ? (
        <div
          className="screen-guide__bubble"
          role="dialog"
          aria-modal="false"
          aria-label="Ayuda contextual"
          aria-describedby={`${bubbleId}-text`}
        >
          <p id={`${bubbleId}-text`} className="screen-guide__bubble-text">
            {message}
          </p>
          <div className="screen-guide__bubble-actions">
            <button
              type="button"
              className="screen-guide__button screen-guide__button--inline"
              onClick={speakMessage}
              disabled={!canSpeak}
            >
              Reproducir la guía
            </button>
            <button
              type="button"
              className="screen-guide__button screen-guide__button--ghost screen-guide__button--inline"
              onClick={toggleHelp}
            >
              Cerrar
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

export default ScreenGuide;
