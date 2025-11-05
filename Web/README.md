# Ezivia Web

Ezivia es un conversor pensado para simplificar teléfonos Android modernos y hacerlos accesibles a personas mayores. Esta aplicación web, desarrollada con Next.js y TypeScript, funciona como el panel de control donde familiares y cuidadores configuran la experiencia.

## Scripts disponibles

En el directorio `Web` puedes ejecutar:

- `npm run dev`: inicia el servidor de desarrollo de Next.js con recarga en caliente.
- `npm run build`: genera la compilación de producción optimizada.
- `npm run lint`: ejecuta las reglas de ESLint definidas para mantener un estilo consistente y accesible.
- `npm run start`: levanta la aplicación compilada en modo producción, detectando automáticamente un puerto libre si el 3000 ya está ocupado.

Cada script utiliza los binarios incluidos en las dependencias del proyecto, por lo que se recomienda instalar primero las dependencias con `npm install`.

## Estructura principal

- `app/`: rutas y componentes basados en el App Router de Next.js.
- `public/`: recursos estáticos como iconos y metadatos.
- `tsconfig.json`: configuración de TypeScript con el alias `@/*` para importaciones organizadas.
- `.eslintrc.json`: reglas adaptadas a la guía de estilo del proyecto.

## Próximos pasos

1. Ejecuta `npm install` para descargar las dependencias.
2. Actualiza los contenidos de la landing según las necesidades de las personas mayores que vayan a usar Ezivia.
3. Integra autenticación y sincronización con los móviles Android a través del backend que el equipo defina.
