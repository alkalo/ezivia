"use client";

import {
  forwardRef,
  type ReactNode,
  type Ref,
  type ButtonHTMLAttributes,
  type AnchorHTMLAttributes,
} from "react";

type ButtonElement = HTMLButtonElement | HTMLAnchorElement;

export type ButtonVariant = "primary" | "secondary" | "ghost";

interface SharedButtonProps {
  variant?: ButtonVariant;
  emphasis?: "high" | "medium" | "low";
  className?: string;
  children?: ReactNode;
}

type ButtonAsButton = SharedButtonProps & ButtonHTMLAttributes<HTMLButtonElement> & {
  href?: undefined;
};

type ButtonAsLink = SharedButtonProps & AnchorHTMLAttributes<HTMLAnchorElement> & {
  href: string;
};

export type ButtonProps = ButtonAsButton | ButtonAsLink;

const baseStyles = "ez-button";
const variantStyles: Record<ButtonVariant, string> = {
  primary: "ez-button--primary",
  secondary: "ez-button--secondary",
  ghost: "ez-button--ghost",
};

const emphasisStyles: Record<NonNullable<ButtonProps["emphasis"]>, string> = {
  high: "ez-button--high",
  medium: "ez-button--medium",
  low: "ez-button--low",
};

function composeClassName(
  variant: ButtonVariant,
  emphasis: NonNullable<ButtonProps["emphasis"]>,
  className?: string,
) {
  return [baseStyles, variantStyles[variant], emphasisStyles[emphasis], className]
    .filter(Boolean)
    .join(" ");
}

export const Button = forwardRef<ButtonElement, ButtonProps>(
  (
    {
      variant = "primary",
      emphasis = "high",
      className,
      children,
      href,
      ...props
    },
    ref,
  ) => {
    const composedClassName = composeClassName(variant, emphasis, className);

    if (href) {
      return (
        <a
          ref={ref as Ref<HTMLAnchorElement>}
          className={composedClassName}
          href={href}
          role="button"
          {...(props as AnchorHTMLAttributes<HTMLAnchorElement>)}
        >
          {children}
        </a>
      );
    }

    const buttonProps = props as ButtonHTMLAttributes<HTMLButtonElement>;
    return (
      <button
        ref={ref as Ref<HTMLButtonElement>}
        type={buttonProps.type ?? "button"}
        className={composedClassName}
        {...buttonProps}
      >
        {children}
      </button>
    );
  },
);

Button.displayName = "Button";

export default Button;
