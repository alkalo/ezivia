# Ezivia Style Guide

Ezivia simplifies Android smartphones so older adults can stay connected without the cognitive load of modern UIs. This guide defines the core palette, typography, spacing, iconography, and accessibility criteria to ensure every screen feels calm, legible, and dependable.

## Color Palette

| Token | Role | Hex | Usage |
|-------|------|-----|-------|
| `primary-emerald` | Primary brand | `#1C7C54` | Key actions, focused controls |
| `primary-emerald-dark` | Primary pressed | `#12573B` | Pressed buttons, active states |
| `primary-emerald-light` | Primary tint | `#E5F4EE` | Backgrounds, cards |
| `neutral-ink` | Body text | `#1E1E1E` | Headings, body copy |
| `neutral-cloud` | Surface | `#F8FAFB` | App background |
| `neutral-mist` | Divider | `#D7DEE4` | Separators, outlines |
| `accent-sunrise` | Positive | `#FFC857` | Highlights, confirmation |
| `accent-berry` | Alert | `#C72C41` | Critical alerts |
| `status-focus` | Focus ring | `#1D4ED8` | Focus outlines |

*Contrast:* Primary on neutral surfaces meets WCAG AA (ratio ≥ 4.5:1). Accent colors are reserved for status messaging to reduce cognitive load.

## Typography

| Style | Typeface | Size | Line Height | Usage |
|-------|----------|------|-------------|-------|
| Display | Google Sans Display | 28 pt | 36 pt | Welcome screens, hero copy |
| Heading | Google Sans | 24 pt | 32 pt | Section headers |
| Title | Google Sans | 22 pt | 30 pt | Card titles, dialog headings |
| Body Large | Google Sans | 20 pt | 28 pt | Primary instructions |
| Body Base | Google Sans | 18 pt | 26 pt | Supporting content |
| Label | Google Sans | 18 pt | 24 pt | Button labels, navigation |

Use sentence case with generous line spacing. Maintain left alignment to support readability for older adults.

## Spacing & Iconography Scales

**Spacing scale (px):** 4, 8, 12, 16, 24, 32, 40, 56. Apply the 24 px increment for primary padding around tappable clusters. Never go below 12 px between interactive elements.

**Icon sizes (px):** 24, 32, 40, 48. Use 40 px icons in primary navigation, 32 px for inline actions, and reserve 48 px icons for onboarding illustrations.

## Design Tokens

### CSS Custom Properties

```css
:root {
  /* Colors */
  --color-primary-emerald: #1C7C54;
  --color-primary-emerald-dark: #12573B;
  --color-primary-emerald-light: #E5F4EE;
  --color-neutral-ink: #1E1E1E;
  --color-neutral-cloud: #F8FAFB;
  --color-neutral-mist: #D7DEE4;
  --color-accent-sunrise: #FFC857;
  --color-accent-berry: #C72C41;
  --color-status-focus: #1D4ED8;

  /* Typography (px equivalents for Android) */
  --font-size-display: 28px;
  --font-size-heading: 24px;
  --font-size-title: 22px;
  --font-size-body-large: 20px;
  --font-size-body-base: 18px;
  --font-size-label: 18px;

  /* Focus states */
  --focus-ring-color: var(--color-status-focus);
  --focus-ring-width: 3px;
  --focus-ring-offset: 2px;

  /* Spacing */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 24px;
  --space-6: 32px;
  --space-7: 40px;
  --space-8: 56px;

  /* Icon sizes */
  --icon-sm: 24px;
  --icon-md: 32px;
  --icon-lg: 40px;
  --icon-xl: 48px;
}
```

### JSON Tokens

```json
{
  "colors": {
    "primary": {
      "emerald": "#1C7C54",
      "emerald-dark": "#12573B",
      "emerald-light": "#E5F4EE"
    },
    "neutral": {
      "ink": "#1E1E1E",
      "cloud": "#F8FAFB",
      "mist": "#D7DEE4"
    },
    "accent": {
      "sunrise": "#FFC857",
      "berry": "#C72C41"
    },
    "status": {
      "focus": "#1D4ED8"
    }
  },
  "fontSizes": {
    "display": "28px",
    "heading": "24px",
    "title": "22px",
    "bodyLarge": "20px",
    "bodyBase": "18px",
    "label": "18px"
  },
  "focus": {
    "ringColor": "#1D4ED8",
    "ringWidth": "3px",
    "ringOffset": "2px"
  },
  "spacing": [4, 8, 12, 16, 24, 32, 40, 56],
  "icons": [24, 32, 40, 48]
}
```

## Accessibility Checklist

- Ensure text and interactive elements achieve WCAG 2.1 AA contrast ratios (4.5:1 for text, 3:1 for large text and UI components).
- Minimum button/touch target: 48 × 48 px with at least 12 px spacing between interactive elements.
- Provide visible focus indicators using `--focus-ring-color`, extending beyond the component by the focus offset.
- Support full keyboard navigation: logical focus order, trap focus within dialogs, and make all interactions available via Enter/Space.
- Offer descriptive labels for icons and screen reader text for critical actions.
- Avoid relying solely on color for meaning; pair color with text or iconography.

By aligning UI decisions with these tokens and guardrails, Ezivia delivers an intuitive Android experience tailored to the needs of older adults.
