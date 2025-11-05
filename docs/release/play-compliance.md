# Checklist de Cumplimiento de Google Play para Ezivia

Esta lista ayuda a verificar que la app Ezivia cumple los requisitos de publicación en Google Play, con especial atención a políticas familiares y accesibilidad.

## 1. Ficha de la tienda
- [ ] Título, descripción breve y larga en español e inglés.
- [ ] Capturas de pantalla que muestren la interfaz simplificada para personas mayores.
- [ ] Video promocional opcional con demostración de accesibilidad.
- [ ] Clasificación por edades acorde a IARC (probablemente PEGI 3 o equivalente).
- [ ] Indicar claramente que la app está orientada a personas mayores y cuidadores.

## 2. Políticas de contenido y familias
- [ ] Revisar cumplimiento de la [Política de Familias](https://support.google.com/googleplay/android-developer/answer/9893335) si se habilita modo cuidador con acceso a familiares.
- [ ] Declarar si la app está dirigida a niños. (Ezivia: **No**; seleccionar audiencia primaria adultos mayores).
- [ ] Garantizar que los anuncios, si existen, respetan las políticas de familias (sin publicidad sensible, sin enlaces externos no verificados).
- [ ] Asegurar que las compras dentro de la app están claramente identificadas y cuentan con controles parentales cuando corresponda.
- [ ] Confirmar que no se comparte la ubicación precisa ni identificadores de niños.

## 3. Seguridad de datos
- [ ] Completar la sección de seguridad de datos en Play Console detallando los tratamientos descritos en la Política de Privacidad.
- [ ] Proporcionar URL pública y funcional a la Política de Privacidad (`https://ezivia.com/politica-privacidad`).
- [ ] Indicar los métodos de cifrado y la posibilidad de eliminación de datos.
- [ ] Documentar el flujo de datos de asistencia remota y asegurar que requiere consentimiento explícito.

## 4. Accesibilidad
- [ ] Cumplir con las [Directrices de accesibilidad de Android](https://developer.android.com/guide/topics/ui/accessibility) (contraste, tamaño de fuentes escalables, soporte a TalkBack).
- [ ] Incluir etiquetas de contenido (`contentDescription`) en iconos y botones principales.
- [ ] Probar la app con TalkBack y otras ayudas técnicas (lector de pantalla, controles de voz).
- [ ] Validar que todos los elementos táctiles tienen un área mínima de 48dp.
- [ ] Asegurar rutas alternativas para interacciones que requieren gestos complejos.

## 5. Permisos y API
- [ ] Justificar cada permiso sensible (Contactos, Teléfono, Accesibilidad) en la declaración de permisos.
- [ ] Implementar diálogos in-app explicando el uso de permisos antes de solicitar Android runtime permissions.
- [ ] Verificar el uso conforme a la [Política de Accesibilidad](https://support.google.com/googleplay/android-developer/answer/10565766).
- [ ] Usar únicamente APIs públicas compatibles con las políticas de Google Play.

## 6. Lanzamiento y pruebas
- [ ] Superar pruebas internas, cerradas y abiertas en Play Console antes del lanzamiento.
- [ ] Incluir instrucciones de soporte y contacto dentro de la app y en la ficha de la tienda.
- [ ] Configurar Google Play App Signing y revisión de integridad (Play Integrity API).
- [ ] Preparar plan de respuesta a comentarios y valoraciones de usuarios mayores.

## 7. Cumplimiento legal adicional
- [ ] Confirmar que los términos de uso y política de privacidad están localizados y actualizados.
- [ ] Incluir mecanismos para ejercer derechos de privacidad desde la app.
- [ ] Verificar compatibilidad con dispositivos Android 8.0 (nivel API mínimo) o superior.

Marcar cada elemento como completado antes de enviar la versión a revisión.
