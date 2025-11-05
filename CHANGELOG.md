# Changelog

## [Unreleased]
- Make Ezivia declare and behave as the default HOME launcher with a guided setup flow, boot handling, and tests covering launcher helper fallbacks.
- Fix favourite contacts synchronisation flow emissions and caregiver preference parsing so Android builds compile cleanly again.
- Ensure the Web production start script checks IPv4 and IPv6 bindings so it can reliably fall back to an open port when 3000 is already in use.
- Remove legacy `MainActivity` from the launcher module after eliminating its layout binding.
- Replace the Web app's `next.config.ts` with an equivalent `next.config.js` for compatibility with tooling expecting JavaScript configs.
- Inline the Web project's TypeScript defaults to remove the missing `next/core-web-vitals` preset dependency.
- Resolve CTA button prop typing to keep Next.js builds passing under strict union checks.
- Replace analytics fallback logging with an in-browser debug event buffer to satisfy linting.
- Update the launcher module to Material Components 1.12 and adjust theme backgrounds so assemble tasks succeed without resource errors.
- Migrate the launcher theme and typography styles to Material 3 so TextAppearance lookups compile under AAPT.
- Align the restricted settings screen with a dedicated Material 3 theme to keep colors and typography consistent.
