export type AnalyticsPayload = Record<string, string | number | boolean | undefined>;

declare global {
  interface Window {
    dataLayer?: Array<Record<string, unknown>>;
    gtag?: (...args: Array<string | Record<string, unknown>>) => void;
    __EZIVIA_DEBUG_ANALYTICS__?: Array<{
      event: string;
      payload?: AnalyticsPayload;
    }>;
  }
}

export const trackEvent = (eventName: string, payload?: AnalyticsPayload) => {
  if (typeof window === "undefined") {
    return;
  }

  const eventPayload = { ...payload };

  if (Array.isArray(window.dataLayer)) {
    window.dataLayer.push({ event: eventName, ...eventPayload });
    return;
  }

  if (typeof window.gtag === "function") {
    window.gtag("event", eventName, eventPayload ?? {});
    return;
  }

  if (process.env.NODE_ENV !== "production") {
    if (!Array.isArray(window.__EZIVIA_DEBUG_ANALYTICS__)) {
      window.__EZIVIA_DEBUG_ANALYTICS__ = [];
    }

    window.__EZIVIA_DEBUG_ANALYTICS__.push({
      event: eventName,
      payload: eventPayload,
    });
  }
};
