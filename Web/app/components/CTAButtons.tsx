import Link from "next/link";

export type CTAButton = {
  label: string;
  href: string;
  ariaLabel?: string;
  variant?: "primary" | "secondary";
};

export type CTAButtonsProps = {
  buttons: CTAButton[];
  className?: string;
};

export function CTAButtons({ buttons, className }: CTAButtonsProps) {
  if (!buttons.length) {
    return null;
  }

  return (
    <div className={className ?? "cta__actions"}>
      {buttons.map((button) => (
        <Link
          key={button.label}
          className={`cta-button ${
            button.variant === "secondary" ? "cta-button--secondary" : "cta-button--primary"
          }`}
          href={button.href}
          aria-label={button.ariaLabel ?? button.label}
        >
          {button.label}
        </Link>
      ))}
    </div>
  );
}
