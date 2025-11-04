# Integración con Intents de WhatsApp

Ezivia depende de los intents públicos de WhatsApp para ofrecer accesos directos
a conversaciones y videollamadas simplificadas para personas mayores. A
continuación se recopilan los intents disponibles y las limitaciones detectadas
durante la investigación interna (enero 2025).

## Intents conocidos

| Acción | Esquema | Notas |
| --- | --- | --- |
| Abrir chat | `Intent.ACTION_VIEW` + `Uri.parse("https://wa.me/<numero>")` | Crea o abre una conversación con el número en formato internacional. |
| Compartir texto | `Intent.ACTION_SEND` con `setPackage("com.whatsapp")` | Soporta texto plano e imágenes con permiso de `content://`. |
| Llamada de voz | `Intent.ACTION_VIEW` + `Uri.parse("whatsapp://call?phone=<numero>")` | Requiere que el número tenga WhatsApp activo y el contacto esté registrado. |
| Videollamada (no documentado) | `Intent.ACTION_VIEW` + `Uri.parse("whatsapp://call?phone=<numero>&call_type=video")` | Utiliza un parámetro interno observado en versiones recientes de WhatsApp. |

## Limitaciones y riesgos

1. **API no oficial**. Meta no publica documentación estable para intents de llamadas.
   Cambios en versiones futuras pueden romper la integración sin previo aviso.
2. **Formato del número**. Todos los intents requieren números en formato E.164
   (por ejemplo, `+34123456789`). Números locales o con espacios fallan silenciosamente.
3. **Dependencia de contactos**. WhatsApp solo permite iniciar llamadas cuando el
   contacto está guardado en la agenda y asociado a una cuenta activa.
4. **Requisitos de instalación**. Si la app `com.whatsapp` no está instalada o está
   deshabilitada, los intents fallan con `ActivityNotFoundException`. Es necesario
   implementar un flujo de fallback hacia Google Play o web.
5. **Permisos restringidos**. No existen permisos adicionales que garanticen la
   ejecución de la llamada; incluso con el intent correcto, WhatsApp puede mostrar
   advertencias o bloquear la acción si la cuenta no tiene acceso a videollamadas.
6. **Experiencia fragmentada**. En tablets o dispositivos sin la versión completa
   de WhatsApp (p. ej., WhatsApp Business o versiones beta) los parámetros pueden
   variar y la videollamada podría iniciarse como llamada de voz.
7. **Políticas de privacidad**. La automatización de llamadas puede requerir
   confirmaciones explícitas del usuario para cumplir con las políticas de Meta.
8. **Compatibilidad con múltiples usuarios**. En teléfonos con múltiples perfiles,
   el intent puede abrir la cuenta por defecto y no la de la persona mayor, lo que
   provoca confusión y potenciales fallos.

## Recomendaciones

- Validar la instalación de WhatsApp antes de mostrar accesos rápidos.
- Pedir confirmación explícita al usuario para cumplir las políticas de privacidad
  y evitar llamadas accidentales.
- Registrar métricas de fallo (sin datos personales) para detectar cambios en la
  API de WhatsApp de forma temprana.
- Mantener documentación actualizada con cada release significativo de WhatsApp.
