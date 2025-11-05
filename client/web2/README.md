# TapTop Web 2

Aplicación React construida con Vite que se publicará bajo la subcarpeta `/WEB2` del dominio principal de TapTop.

## Scripts disponibles

- `npm run dev`: levanta un servidor de desarrollo.
- `npm run build`: compila la aplicación en modo producción y deja la salida en `dist/`.
- `npm run preview`: sirve el build generado en el puerto `3002` (pensado para emular la configuración del VPS).
- `npm run test`: ejecuta la suite de tests con Vitest.

## Configuración de rutas

El archivo [`vite.config.ts`](./vite.config.ts) define `base: '/WEB2/'` para que todos los assets se resuelvan respecto a la subcarpeta.
Además, el enrutador (`src/router.tsx`) utiliza `basename: '/WEB2'` para alinear las rutas del lado del cliente con la estructura de
publicación.

## Despliegue estático

1. Instala las dependencias con `npm install`.
2. Ejecuta `npm run build` para generar la carpeta `dist`.
3. Copia el contenido de `dist` al alias `/var/www/taptop/web2` (o la ruta equivalente en el servidor).
4. Actualiza Nginx para servir `/WEB2/` mediante un bloque `location` que apunte a esa carpeta y defina `try_files $uri /WEB2/index.html`.
