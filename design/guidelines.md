# Guías de Diseño para Ezivia

Ezivia es una aplicación Android que simplifica la experiencia de smartphones modernos para personas mayores. Estos principios orientan la definición de la interfaz y deben aplicarse en todos los flujos principales.

## Principios Fundamentales

- **Legibilidad prioritaria:** Utilizar tipografía XL (al menos 20 pt en equivalentes Android SP) para elementos de texto principales como títulos, acciones primarias y mensajes críticos.
- **Alto contraste:** Mantener una relación de contraste mínima de 7:1 entre texto e información esencial y sus fondos. Priorizar combinaciones de colores cálidos con fondos neutros oscuros o claros, siempre revisando con herramientas de comprobación de contraste.
- **Iconografía clara:** Emparejar cada acción con iconos fácilmente reconocibles y simplificados.
- **Lenguaje sencillo:** Mensajes y etiquetas con vocabulario cotidiano y frases cortas.
- **Jerarquía visual consistente:** Distribuir el contenido con mucho espacio en blanco, botones grandes y zonas de interacción amplias (mínimo 56x56 dp).

## Uso de Color

- Paleta base cálida inspirada en tonos suaves (por ejemplo, naranja, crema y azul profundo para acciones).
- Evitar colores saturados que puedan generar fatiga visual.
- Destacar acciones primarias con botones sólidos y bordes redondeados.

## Tipografía

- Fuente recomendada: Roboto o equivalente sans-serif, con pesos Bold para títulos y Regular para texto auxiliar.
- Tamaños mínimos:
  - Títulos principales: 24–28 sp (tipografía XL).
  - Botones y acciones: 22–24 sp.
  - Texto secundario: 18–20 sp.
- Mantener un interlineado de al menos 1.5x para mejorar la lectura.

## Componentes Clave

- **Botones principales:** Botones de altura generosa (mínimo 64 dp) y esquinas redondeadas.
- **Tarjetas de información:** Fondos claros, bordes suaves, icono grande en la esquina izquierda y texto corto a la derecha.
- **Navegación:** Barra inferior con iconos grandes y etiquetas descriptivas; evitar más de 4 destinos principales.

## Accesibilidad y Uso

- Incluir retroalimentación háptica y auditiva para confirmar acciones.
- Proporcionar modos "alto contraste" y "texto extra grande" configurables desde ajustes.
- Garantizar que todas las interacciones críticas sean alcanzables con una sola mano.

## Revisión Continua

- Validar cada nuevo flujo mediante pruebas con usuarios mayores.
- Documentar hallazgos en `/docs/research/usability.md` y ajustar estos lineamientos en consecuencia.
