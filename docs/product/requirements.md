# Requisitos MVP – Ezivia

## Objetivo del Producto
Ezivia es un adaptador de teléfonos Android modernos para personas mayores. Simplifica la experiencia de comunicación y seguridad permitiendo a cuidadores configurar y monitorear funciones clave.

## Alcance del MVP
- Interfaz de inicio con accesos directos a funciones principales.
- Integración con llamadas telefónicas nativas, WhatsApp, SMS y una función SOS.
- Sistema de recordatorios de medicación y citas sincronizable con cuidadores.

## Personas Clave
1. **Persona mayor usuaria**: Necesita acceso rápido, interfaz clara, retroalimentación auditiva/visual.
2. **Cuidador/a familiar o profesional**: Configura el dispositivo, supervisa recordatorios y recibe alertas.

## Requisitos Funcionales

### Flujo de Llamadas Telefónicas
1. Botón principal "Llamar" en pantalla de inicio con lista de contactos favoritos (máximo 6).
2. Acceso a teclado numérico simplificado (botones grandes, contraste alto).
3. Historial de llamadas agrupado por contacto con etiquetas visuales (perdida, recibida, realizada).
4. Integración con Android Dialer mediante intents; soporte de confirmación por voz ("llamando a Ana").

### Flujo de Videollamadas WhatsApp
1. Botón dedicado "Videollamar" que abre lista de contactos favoritos de WhatsApp.
2. Confirmación previa con foto y nombre del contacto.
3. Integración mediante `Intent` de llamada de video de WhatsApp; fallback para instalar/actualizar app si no está disponible.
4. Recordatorio emergente cuando hay videollamadas entrantes: pantalla completa con botones "Responder" y "Rechazar" grandes.

### Flujo de SMS
1. Botón "Mensajes" con acceso a plantillas rápidas ("Estoy bien", "¿Puedes llamarme?", "Necesito ayuda").
2. Entrada de texto asistida con dictado por voz y tamaño de fuente configurable.
3. Confirmación visual de mensaje enviado y opción de escuchar el contenido enviado.
4. Sincronización opcional con cuidador mediante copia automática a contacto configurado.

### Flujo SOS
1. Botón SOS fijo en todas las pantallas (barra inferior) con protección contra activación accidental (mantener 2 segundos).
2. Al activarse, realiza llamada automática al contacto prioritario y envía SMS con ubicación actual.
3. Reproduce alarma audible y vibración hasta confirmación de recepción.
4. Registro de eventos SOS accesible para cuidadores.

### Flujo de Recordatorios
1. Configuración de recordatorios (medicación, citas) desde modo cuidador con repetición diaria/semanal.
2. Notificación multimodal: pantalla completa con texto grande, mensaje de voz y vibración prolongada.
3. Botones "Tomado" / "Recordar más tarde"; el estado se sincroniza con cuidador.
4. Historial de adherencia visible por día/semana.

## Requisitos No Funcionales
- **Accesibilidad**: WCAG nivel AA donde aplique; soporte de fuentes 18-28 pt, alto contraste.
- **Compatibilidad**: Android 9+ con WhatsApp instalado.
- **Seguridad y Privacidad**: Cumplimiento de GDPR; almacenamiento cifrado para datos personales; controles de acceso para cuidadores.
- **Desempeño**: Acceso a funciones principales en menos de 2 toques; tiempo de apertura inferior a 2 segundos en dispositivos de gama media.

## Reglas de Negocio
- El modo cuidador requiere autenticación (PIN o biometría).
- SOS solo configurable para contactos verificados.
- Los contactos favoritos no pueden exceder 10 para mantener simplicidad.

## Métricas de Éxito
- >=80% de usuarios mayores completan una llamada sin asistencia en pruebas de usabilidad.
- Tasa de respuesta a recordatorios >90% en el primer mes.
- Reducción de incidencias de soporte relacionadas con navegación en un 50% respecto al dispositivo original.

## Riesgos y Suposiciones
- Dependencia de WhatsApp: cambios en API podrían requerir ajustes.
- Requiere permisos de accesibilidad y superposición; se debe guiar al usuario en la configuración inicial.
- Supone que el cuidador tiene acceso remoto o presencial para configurar.

## Roadmap Inicial
1. Prototipo interactivo + pruebas de usabilidad (Mes 1).
2. Desarrollo de launcher simplificado, flujos de llamadas/SOS (Mes 2).
3. Integración con WhatsApp y recordatorios sincronizados (Mes 3).
4. Beta cerrada con cuidadores y mejoras de accesibilidad (Mes 4).
