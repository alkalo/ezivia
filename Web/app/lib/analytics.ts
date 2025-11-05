export type AnalyticsPayload = Record<string, string | number | boolean | undefined>;

declare global {
  interface Window {
    dataLayer?: Array<Record<string, unknown>>;
    gtag?: (...args: Array<string | Record<string, unknown>>) => void;
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
    console.info("[analytics]", eventName, eventPayload);
  }
};
