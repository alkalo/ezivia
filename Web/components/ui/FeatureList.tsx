"use client";

import { HTMLAttributes, useId, type ReactNode } from "react";

export interface FeatureItem {
  title: string;
  description: string;
  icon?: ReactNode;
}

export interface FeatureListProps extends HTMLAttributes<HTMLElement> {
  title: string;
  description?: string;
  items: FeatureItem[];
}

export function FeatureList({
  title,
  description,
  items,
  className,
  ...props
}: FeatureListProps) {
  const titleId = useId();
  const descriptionId = useId();

  return (
    <section
      className={["ez-feature-list", className].filter(Boolean).join(" ")}
      aria-labelledby={titleId}
      aria-describedby={description ? descriptionId : undefined}
      {...props}
    >
      <div className="ez-feature-list__intro">
        <h2 id={titleId} className="ez-feature-list__title">
          {title}
        </h2>
        {description ? (
          <p id={descriptionId} className="ez-feature-list__description">
            {description}
          </p>
        ) : null}
      </div>
      <ul className="ez-feature-list__items" role="list">
        {items.map((item) => (
          <li key={item.title} className="ez-feature-list__item" role="listitem">
            {item.icon ? <span className="ez-feature-list__icon">{item.icon}</span> : null}
            <div className="ez-feature-list__content">
              <h3 className="ez-feature-list__item-title">{item.title}</h3>
              <p className="ez-feature-list__item-description">{item.description}</p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}

export default FeatureList;
