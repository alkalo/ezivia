- Ajustamos Ajustes restringidos para que solo muestre la opción de salir del modo Ezivia y se abra únicamente manteniendo subir volumen al tocar Ajustes.
# Changelog

## [Unreleased]
- Solicitamos y comprobamos el permiso de contactos antes de iniciar videollamadas de WhatsApp desde inicio o contactos,
  mostrando un aviso claro si se deniega.
- Normalizamos los números de videollamada de WhatsApp al formato E.164 usando el país del dispositivo y un prefijo de código cuando la API no puede formatear, evitando intents con teléfonos locales incompletos.
- Añadimos un botón de videollamada directa que busca el dataId de WhatsApp por número en la agenda, comprobando permisos de contactos y mostrando avisos claros si falta la integración o la app.
- Evitamos caídas al iniciar videollamadas de WhatsApp cuando el país no se puede leer del operador, usando un fallback seguro y pruebas para la región.
- Lanzamos las videollamadas de WhatsApp desde el dataId de la agenda para saltar directo a la pantalla de video sin pasar por el chat.
- Iniciamos las videollamadas de WhatsApp con el esquema nativo de llamada para entrar directamente a la videollamada sin pasar por el chat.
- Corrigimos el enlace de videollamada de WhatsApp para usar wa.me y evitar avisos de enlace no válido.
- Corrigimos el tag de Flow en la tarjeta de contacto favorito para compilar el launcher con ConstraintLayout.
- Ajustamos las paletas móviles para que cualquier texto sobre fondos oscuros use blanco, incluyendo iconos y botones, mejorando el contraste en toda la app.
- Cambiamos la web a una paleta nocturna con textos claros sobre fondos oscuros y tarjetas contrastadas para mejorar la
  legibilidad.
- Simplificamos los ajustes restringidos eliminando la lista de cuidadores, sumando guías por voz y sonidos de confirmación, y reorganizando la tarjeta de lectura y sonido con copias más claras.
- Permitimos que el botón "Cambiar inicio predeterminado" al salir de Ezivia abra los ajustes de inicio cuando ya somos la app por defecto.
- Quitamos el panel del gesto de bloqueo en inicio, compactando el layout y usando confirmaciones para abrir Ajustes o salir sin depender del volumen.
- Reorganizamos la pantalla de inicio del launcher en una sola columna con botones principales apilados, acciones rápidas y favoritos a ancho completo para mejorar la legibilidad.
- Mejoramos la accesibilidad web con anillos de foco visibles, contrastes reforzados en estados críticos y formularios que anuncian sus estados para lectores de pantalla.
- Añadimos una indicación en la guía rápida impresa para recordar que Ajustes se abre manteniendo subir volumen y luego tocando la opción tras un intento fallido.
- Añadimos una sección en la web con iconos de trazo grueso para llamada, videollamada, ajustes y SOS,
  acompañados de una ilustración cálida alineada a la paleta crema/coral.
- Abrimos la lista de contactos favoritos en el launcher a una columna única de tarjetas a ancho completo, con desplazamiento suave, animaciones visibles y estados vacíos o de permiso intactos para listas largas sin recortes.
- Reorganizamos las acciones de contactos favoritos con un Flow que permite distribuir los botones en una o dos filas, evitando saltos de línea en los textos y manteniendo iconos y colores.
- Fix contact wizard extras and favorite editing so the launcher build succeeds by correcting intent usage, starred flag
  handling, and home favorite edit actions.
- Add the missing Body Medium text appearance so the contact wizard hint texts compile under AAPT again.
- Rediseñar los ajustes restringidos para mostrar solo tamaño de letra, volumen de tonos, contactos de emergencia y bloqueo de
  pantalla grande con tarjetas y controles grandes.
- Añadimos guías contextuales con lectura en voz alta y un botón flotante de ayuda contrastado en la web para orientar al
  usuario en cada pantalla.
- Add tactile feedback and XL confirmation messaging across launcher screens, including press scale animations, success/error
  vibrations with tones, global fade transitions, and scale-in list/card motion for new items.
- Add a guided contact wizard with per-step validation, WhatsApp preference, optional voice confirmation, photo preview, and launcher entry points for adding or editing.
- Rediseñar la pantalla de inicio con favoritos en rejilla de dos columnas, botones destacados de llamada y videollamada,
  un SOS flotante y animaciones de entrada con fade y scale.
- Refresh launcher and settings theming with a cream/coral/deep blue palette, Roboto/Inter typography, rounded elevated cards,
  and enlarged touch targets.
- Add a comprehensive Spanish-language accessibility and calling UI style guide covering home, favorites, call flows, animations, ajustes, iconografía y pruebas con personas mayores.
- Cover the launcher lock gesture coordinator with Robolectric tests using mocked views to verify state changes and expiry grace periods.
- Añadir pruebas de Robolectric con Mockito para comprobar el rol de launcher predeterminado y las rutas de solicitud y ajustes.
- Add Robolectric coverage for launcher onboarding preferences to confirm tutorial flags persist across instances.
- Simplificar el flujo seguro del launcher eliminando el PIN, moviendo los ajustes a la barra inferior con acceso mediante volumen, reabriendo la configuración de inicio al salir del modo Ezivia y mejorando la legibilidad de las tarjetas de acciones rápidas.
- Normalize WhatsApp favorite contact numbers before launching video calls so installed users no longer see the install prompt.
- Switch the launcher security gesture to the hardware volume up key, refresh the guidance copy and artwork, and cover the new
  flow with Robolectric tests.
- Update the launcher and settings color palettes to a high-contrast oceanic scheme for clearer, more pleasant readability.
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
- Documentar los controles accesibles de llamada y videollamada con layouts centrados, botones de colores contrastados y video remoto a pantalla grande.
- Añadir un desglose detallado de flujos (inicio, llamada, videollamada, favoritos y ajustes) con notas de accesibilidad, botones grandes y paleta cálida contrastada en la guía de estilo.
- Añadir guion y métricas para sesiones de usabilidad remotas moderadas con tareas de favoritos, videollamadas WhatsApp y SOS, incluyendo pausas y registro de SUS.
- Soportar videollamadas tanto con WhatsApp estándar como Business para evitar falsos avisos de instalación.
- Declarar la visibilidad de los paquetes y esquema de WhatsApp en el manifest del launcher para que Android 11+ detecte correctamente la app instalada y no muestre el aviso de instalación.
- Añadimos configuración de fecha y hora en recordatorios, permiso de notificaciones y alarmas sonoras con reprogramación tras reinicios para no perder avisos.
- Evitar caídas al iniciar videollamadas de WhatsApp sin permiso de contactos usando fallback directo y pruebas de selección de intent.
