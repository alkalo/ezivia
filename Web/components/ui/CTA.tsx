"use client";

import { useEffect, useRef, useId, type HTMLAttributes } from "react";
import { Button, type ButtonProps } from "./Button";

export interface CTAAction extends Omit<ButtonProps, "children"> {
  label: string;
  description?: string;
}

export interface CTAProps extends HTMLAttributes<HTMLElement> {
  heading: string;
  description: string;
  primaryAction: CTAAction;
  secondaryAction?: CTAAction;
  footnote?: string;
  autoFocusFirstButton?: boolean;
}

export function CTA({
  heading,
  description,
  primaryAction,
  secondaryAction,
  footnote,
  className,
  autoFocusFirstButton = false,
  ...props
}: CTAProps) {
  const sectionId = useId();
  const descriptionId = useId();
  const footnoteId = useId();
  const primaryDescriptionId = useId();
  const secondaryDescriptionId = useId();
  const primaryButtonRef = useRef<HTMLButtonElement | HTMLAnchorElement | null>(null);

  useEffect(() => {
    if (autoFocusFirstButton && primaryButtonRef.current) {
      primaryButtonRef.current.focus();
    }
  }, [autoFocusFirstButton]);

  const sectionDescribedBy = [
    description ? descriptionId : undefined,
    footnote ? footnoteId : undefined,
  ]
    .filter(Boolean)
    .join(" ");

  const { label: primaryLabel, description: primaryDescription, ...primaryRest } = primaryAction;
  const {
    label: secondaryLabel,
    description: secondaryDescription,
    ...secondaryRest
  } = secondaryAction ?? { label: undefined, description: undefined };

  return (
    <section
      className={["ez-cta", className].filter(Boolean).join(" ")}
      aria-labelledby={sectionId}
      aria-describedby={sectionDescribedBy || undefined}
      {...props}
    >
      <div className="ez-cta__content">
        <h2 id={sectionId} className="ez-cta__heading">
          {heading}
        </h2>
        <p id={descriptionId} className="ez-cta__description">
          {description}
        </p>
        <div className="ez-cta__actions" role="group" aria-label="Acciones principales">
          <Button
            ref={primaryButtonRef}
            aria-describedby={primaryDescription ? primaryDescriptionId : undefined}
            {...primaryRest}
          >
            {primaryLabel}
          </Button>
          {primaryDescription ? (
            <span id={primaryDescriptionId} className="ez-cta__action-description">
              {primaryDescription}
            </span>
          ) : null}
          {secondaryAction ? (
            <Button
              variant="secondary"
              emphasis="medium"
              aria-describedby={
                secondaryDescription ? secondaryDescriptionId : undefined
              }
              {...secondaryRest}
            >
              {secondaryLabel}
            </Button>
          ) : null}
          {secondaryAction && secondaryDescription ? (
            <span id={secondaryDescriptionId} className="ez-cta__action-description">
              {secondaryDescription}
            </span>
          ) : null}
        </div>
        {footnote ? (
          <p id={footnoteId} className="ez-cta__footnote">
            {footnote}
          </p>
        ) : null}
      </div>
    </section>
  );
}

export default CTA;
