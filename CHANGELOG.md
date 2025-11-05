# Changelog

## [Unreleased]
- Switch the launcher security gesture to the hardware volume up key, refresh the guidance copy and artwork, and cover the new
  flow with Robolectric tests.
- Fix the contacts favorites management fallback chain so Kotlin infers the correct Unit type and launcher builds succeed.
- Resolve the Contacts favorites launcher intent fallback to avoid Kotlin callable reference ambiguity during builds.
- Replace the removed `ContactsContract.Intents.UI` favorite intent with a resilient fallback action so the launcher builds again.
- Refresh the Ezivia launcher visual theme with un nuevo esquema cálido coral/dorado y un panel interactivo para el gesto de seguridad.
- Añadir el gesto de mantener presionado el botón lateral virtual para autorizar la salida y ajustes, junto con un tutorial guiado la primera vez.
- Rebuild the Ezivia launcher home to surface large quick actions for calls, WhatsApp video calls, messages, photos, reminders, and SOS assistance, including refreshed favourite contact cards.
- Add dedicated Contacts, Reminders, and SOS screens with reminder creation, completion toggles, and caregiver notifications to keep the experience useful beyond settings and exit.
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
- Fix reminders layouts by using the correct text color attribute, configuring the time picker in code, and adding the missing PIN hint string so Android resource linking succeeds again.
- Restore the protection PIN copy, require explicit launcher R imports, and double-check Material 3 usage so the launcher assembles cleanly with the updated theme stack.
- Replace legacy coroutine UI dispatcher usage with Dispatchers.Main in the launcher module so assemble tasks compile without unresolved references.
