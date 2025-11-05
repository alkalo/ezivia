"use client";

import { HTMLAttributes, ReactNode, useId } from "react";

export interface CardProps extends HTMLAttributes<HTMLElement> {
  title: string;
  description?: string;
  eyebrow?: string;
  footer?: ReactNode;
}

export function Card({
  title,
  eyebrow,
  description,
  children,
  footer,
  className,
  ...props
}: CardProps) {
  const titleId = useId();
  const descriptionId = useId();

  return (
    <article
      className={["ez-card", className].filter(Boolean).join(" ")}
      aria-labelledby={titleId}
      aria-describedby={description ? descriptionId : undefined}
      {...props}
    >
      <header className="ez-card__header">
        {eyebrow ? <p className="ez-card__eyebrow">{eyebrow}</p> : null}
        <h3 id={titleId} className="ez-card__title">
          {title}
        </h3>
        {description ? (
          <p id={descriptionId} className="ez-card__description">
            {description}
          </p>
        ) : null}
      </header>
      <div className="ez-card__body">{children}</div>
      {footer ? <footer className="ez-card__footer">{footer}</footer> : null}
    </article>
  );
}

export default Card;
