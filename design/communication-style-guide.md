# Guía Integral de Estilo y Prototipo para Llamadas Accesibles

## Fundamentos visuales
- **Tipografía:** Sans-serif grande (Roboto/Inter), títulos 26–32 sp, acciones 22–24 sp, textos secundarios 18–20 sp; interlineado 1.5x. Pictogramas de trazo grueso y etiquetas breves para fácil lectura.
- **Paleta de alto contraste:** Fondos claros cálidos (crema  #FFF4E6) con acentos sólidos (coral #FF7043 para acciones, azul profundo #1F4F7A para énfasis) y textos en gris antracita/azul noche. Contraste mínimo 4.5:1 en todo el texto y 7:1 en estados críticos.
- **Espaciado y jerarquía:** Márgenes y paddings de 16–24 px alrededor de tarjetas; separación vertical de 12–16 px entre elementos. Sombras suaves (blur 16, opacidad 12–16%) para dar jerarquía sin distracciones.
- **Objetivos táctiles:** Mínimo 48 px en cualquier dimensión; botones principales de 64–72 px de alto y esquinas redondeadas 14–18 px.
- **Lenguaje visual:** Diseño limpio, con colores cálidos y botones redondeados de gran tamaño; pictogramas claros alineados a la paleta definida.

## Pantallas clave (ejemplos)
- **Inicio/Home:** Tarjetas de favoritos en grid (2 columnas) con fotos grandes y nombre; botones de Llamada, Videollamada WhatsApp y SOS a primer plano; barra inferior simple (Inicio, Ajustes).
- **Favoritos:** Lista de contactos en tarjetas XL con foto y nombre; accesos rápidos a llamar, videollamar y editar.
- **Llamada entrante/saliente:** Foto grande del contacto, nombre y número; controles centrados y separados con alto contraste.

## Prototipo de inicio (acceso inmediato)
- **Layout:** Grid de 4–6 favoritos en tarjetas grandes (foto circular 72–88 px, nombre en bold). Botón principal de llamada (coral) y secundario de videollamada WhatsApp (azul) bajo el encabezado; botón SOS fijo en esquina inferior derecha con tono ámbar/rojo.
- **Barra inferior simplificada:** Inicio (activo), Ajustes (secundario) con iconos grandes y texto.
- **Transiciones:** Fade + scale-in (150–180 ms) al mostrar tarjetas y botones. Comentario: layout tipo grid con fotos grandes de contactos para reconocimiento rápido.

## Flujo de alta/edición de favoritos
- **Pantallas:** Añadir/Editar contacto con campos mínimos (Nombre, Teléfono, toggle "Usar WhatsApp"). Paso a paso con textos XL y ayuda contextual.
- **Accesibilidad:** Íconos claros en cada campo, validación en tiempo real con mensajes grandes (verde/rojo) y confirmación opcional por voz.
- **Confirmación:** Resumen final con foto opcional y botón grande de guardar.

## Interfaz de llamada y videollamada
- **Controles mínimos:** Colgar (rojo), Altavoz (gris/azul), Volumen +/- (azul), Silenciar (gris). Videollamada añade Cambiar cámara y muestra video remoto en tamaño grande.
- **Distribución:** Controles centrados y separados con fondos sólidos de alto contraste; textos e iconos grandes.
- **Jerarquía visual:** Botón de colgar dominante en rojo coral, acciones de audio (altavoz y volumen) en azul profundo, silenciar en gris neutral. Separación mínima de 16–20 px entre botones para reducir toques accidentales.
- **Video remoto:** En videollamada el feed remoto ocupa la zona superior o completa de la pantalla, con miniatura propia flotante; controles se mantienen en una franja inferior accesible.

## Animaciones y microinteracciones
- **Botones:** Scale-up ligero (1.04x) <200 ms con easing suave.
- **Feedback:** Confirmaciones con vibración háptica leve y sonidos claros. Estados de error/success con vibración diferenciada y mensajes XL.
- **Transiciones globales:** Discretas para no distraer; fade entre pantallas y scale-in en tarjetas nuevas.

## Pantalla de ajustes simplificada
- **Opciones clave:** Tamaño de letra, Volumen de tonos, Contactos de emergencia, Bloqueo de pantalla grande.
- **Controles:** Toggles grandes, sliders gruesos con etiquetas; secciones en tarjetas claras con títulos grandes.

## Sistema de asistencia
- Mensajes guía en lenguaje claro; opción de lectura en voz alta al entrar en cada pantalla.
- Icono fijo "?" que reproduce instrucción contextual. Globos de ayuda grandes, alto contraste.

## Iconografía e ilustraciones
- Set de iconos de trazo grueso para llamadas, video, ajustes, SOS; coherentes con la paleta cálida/contrastada.
- Ilustraciones sencillas y amables, formas redondeadas y fondos claros con acentos cálidos.

## Accesibilidad táctil y lectura (WCAG AA+)
- Contraste > 4.5:1; fuentes mínimas 16–18 pt; objetivos táctiles 48 px; foco visible.
- Textos alternativos para imágenes; soporte VoiceOver/TalkBack; mantener consistencia para favorecer memoria muscular.

## Plan de pruebas con personas mayores
- **Tareas:** Llamar a favorito, iniciar videollamada WhatsApp, agregar favorito, usar SOS.
- **Métricas:** Tiempo de tarea, errores, satisfacción (SUS) y notas de claridad visual/tamaño de botones.
- **Sesiones:** Moderadas y remotas, lapsos cortos con descansos, usando dispositivos de pantalla grande.

## Prototipo navegable
- Entregar prototipo de alta fidelidad (Figma u otra) con flujos: inicio, llamada, videollamada, gestionar favoritos y ajustes. Incluir etiquetas de accesibilidad y notas de diseño. Fondos claros con acentos cálidos y botones grandes.
