# Despliegue de TapTop Web 2 bajo `/WEB2`

Este documento resume los pasos para compilar, publicar y mantener la web secundaria en el VPS sin afectar a la aplicación
principal del dominio `taptop.es`.

## 1. Framework y build

La aplicación ubicada en `client/web2` utiliza **Vite** con React. Su configuración principal está en [`vite.config.ts`](../../client/web2/vite.config.ts),
donde se establece `base: '/WEB2/'` para que los assets generados se sirvan desde la subcarpeta. El enrutador del lado del cliente
(`client/web2/src/router.tsx`) usa `basename: '/WEB2'`, garantizando que la navegación interna funcione tanto en modo SPA como al
recargar rutas profundas.

## 2. Variables de entorno

- Copia `client/web2/.env.production.example` a `.env.production` y ajusta los valores antes de ejecutar el build en el servidor.
- Exporta las variables necesarias en la shell o configúralas en el gestor de procesos si llegara a ejecutarse `npm run preview`.

## 3. Build y salida estática

```
cd /var/www/taptop/client/web2
npm ci
npm run build
```

El comando anterior generará `client/web2/dist`. Copia esa carpeta (o sincronízala con `rsync`) a la ubicación pública del servidor,
por ejemplo `/var/www/taptop/web2`.

## 4. Configuración de Nginx

Edita el bloque `server` asociado a `taptop.es` y añade:

```
location /WEB2/ {
    alias /var/www/taptop/web2/;
    try_files $uri $uri/ /WEB2/index.html;
}
```

- El uso de `alias` evita interferir con la app principal.
- `try_files` asegura que las rutas internas del router regresen a `index.html`.
- Revisa que los headers de caché aplicados a la carpeta principal se hereden o, si lo prefieres, declara reglas específicas para `*.js`, `*.css`, etc.

Recarga Nginx con `sudo systemctl reload nginx`.

## 5. Verificaciones

1. Ejecuta `npm run preview -- --outDir dist` si quieres validar localmente el build bajo `http://localhost:3002/WEB2/`.
2. En el VPS, revisa que `curl -I http://127.0.0.1:3002/WEB2/` (o el puerto que uses para pruebas) devuelva estado `200`.
3. Desde un navegador, entra a `https://taptop.es/WEB2/` y prueba rutas como `https://taptop.es/WEB2/agenda` para confirmar que no se
   producen 404.
4. Comprueba que los assets cargan desde `https://taptop.es/WEB2/assets/...`.

## 6. Supervisión

Al ser un despliegue estático, no se requiere un proceso Node permanente. Si en el futuro se decide servir la app mediante
`npm run preview`, conviene gestionarlo con PM2 o systemd, habilitando arranque automático y rotación de logs (`journalctl -u taptop-web2`).
