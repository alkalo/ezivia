# Paquete Play Store - Ezivia

Este directorio contiene la documentación para preparar la ficha de Play Store. Debido a las limitaciones del entorno (sin acceso a Android Studio, claves de firma ni consola de Google Play), no fue posible generar APK/AAB firmados ni subirlos a una beta cerrada. A continuación se detallan los elementos preparados y los pasos pendientes para completarlo manualmente.

## Resumen del estado
- [ ] Generación de arte promocional (icono 512x512, banner 1024x500).
- [ ] Capturas de pantalla de la app.
- [ ] Video de demostración opcional.
- [ ] APK/AAB firmado listo para subir.
- [ ] Alta en consola de Google Play y despliegue a beta cerrada.

## Recursos de copia (texto)
- **Nombre de la aplicación:** Ezivia – Teléfono simplificado para mayores.
- **Breve descripción (80 caracteres):** Convierte cualquier Android en un teléfono fácil para adultos mayores.
- **Descripción completa:**
  > Ezivia transforma un smartphone moderno en una experiencia amigable para personas mayores. Ofrece acceso rápido a contactos favoritos, botones grandes y protección con PIN para mantener configuraciones seguras. Pensado para familias que quieren mantenerse conectadas sin complicaciones.

- **Características clave:**
  - Agenda visual con contactos frecuentes.
  - Botones grandes para llamar o enviar mensajes.
  - Protección con PIN para evitar cambios accidentales.
  - Integración con ajustes simplificados.

## Pasos recomendados para completar el lanzamiento
1. **Generar build de release** usando `./gradlew bundleRelease` o `./gradlew assembleRelease` desde Android Studio con un keystore válido.
2. **Firmar el artefacto** con la clave oficial de Ezivia siguiendo la guía de Play App Signing.
3. **Capturar assets gráficos** en un dispositivo real para garantizar fidelidad (mínimo 3 capturas en formato 1080x1920 o superior).
4. **Subir metadatos** (textos anteriores) y assets a la consola de Google Play en la pestaña "Crecimiento" > "Configuración de la tienda".
5. **Crear prueba interna o cerrada**, invitando a los testers mediante sus correos electrónicos.
6. **Completar checklist de lanzamiento** (clasificación de contenido, privacidad de datos y políticas).

## Notas
- Se recomienda coordinar con el equipo de diseño para obtener los archivos fuente (SVG/PSD) del icono y banner.
- Mantener un registro de la clave de subida en un gestor seguro.
- Validar que el paquete utilice el `applicationId` `com.ezivia.launcher` antes de subirlo.
