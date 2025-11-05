# Despliegue de Ezivia Web

Esta guía explica cómo desplegar la aplicación web de Ezivia en Vercel o Netlify y cómo coordinar el apuntado de DNS hacia `eazivi.com`.

## Requisitos previos

1. Node.js 18 o superior instalado localmente.
2. Cuenta activa en Vercel y/o Netlify con acceso al repositorio de GitHub que contiene este proyecto.
3. Valores de las variables de entorno necesarias:
   - `WAITLIST_WEBHOOK_URL`: URL HTTPS del servicio (por ejemplo, un webhook o formulario externo) que recibirá los envíos del formulario de lista de espera.

## Configuración general del proyecto

1. Instala dependencias:

   ```bash
   npm install
   ```

2. Prueba el build localmente antes de desplegar:

   ```bash
   npm run lint
   npm run build
   ```

3. Verifica que exista la variable `WAITLIST_WEBHOOK_URL` en el entorno de producción. Si no hay un webhook disponible todavía, puedes omitirla temporalmente; los envíos seguirán funcionando a nivel de interfaz aunque no se notifiquen externamente.

## Despliegue en Vercel

1. Importa el repositorio en Vercel seleccionando el directorio `Web` como root del proyecto.
2. Configura las variables de entorno en el apartado **Settings → Environment Variables**:
   - `WAITLIST_WEBHOOK_URL` (Production) → URL del webhook que recepcionará los contactos.
3. Ajusta los comandos por defecto si fuese necesario:
   - Build Command: `npm run build`
   - Install Command: `npm install`
   - Output Directory: `.vercel/output` (Vercel lo detecta automáticamente para aplicaciones Next.js).
4. Despliega la rama principal (`main`) y espera a que se genere la URL de producción.
5. Activa **Automatic Static Optimization** (es automática para Next.js 14) y, si procede, habilita Vercel Analytics para medir los eventos que se envían a `window.dataLayer` o `gtag`.

## Despliegue en Netlify

1. Crea un nuevo sitio desde Git en Netlify apuntando al repositorio y seleccionando el directorio `Web`.
2. Configura los parámetros de build:
   - Base directory: `Web`
   - Build command: `npm run build`
   - Publish directory: `.next`
3. Añade la variable `WAITLIST_WEBHOOK_URL` en **Site settings → Build & deploy → Environment** para los contextos `production` y `deploy previews` si quieres probar el formulario en ramas.
4. Habilita el adaptador Next.js oficial de Netlify si aún no está activo (Netlify suele detectarlo automáticamente en proyectos creados después de 2023).
5. Realiza un deploy manual inicial o espera a que Netlify ejecute el pipeline tras el primer commit en la rama seleccionada.

## Coordinación de DNS para `eazivi.com`

1. Define la URL primaria de producción como `https://www.eazivi.com` (la canonical ya está configurada en el proyecto).
2. Si usas Vercel:
   - Ve a **Settings → Domains** y añade `eazivi.com` y `www.eazivi.com`.
   - Vercel generará los registros necesarios (CNAME para `www` y `A`/`ALIAS` para el root). Aplica estos registros en el proveedor DNS donde esté alojado el dominio.
3. Si usas Netlify:
   - En **Domain management**, añade los mismos dominios y copia los registros sugeridos (generalmente un CNAME para `www` y un registro `A` o `ALIAS` para el root).
4. Tras propagar los cambios DNS (puede tardar hasta 48 horas), fuerza un redeploy para asegurar que los enlaces canónicos y el sitemap respondan bajo el nuevo dominio.
5. Configura redirecciones 301 de `http` a `https` y de `eazivi.com` a `www.eazivi.com` desde el panel del proveedor elegido para evitar contenido duplicado.

## Verificación post-despliegue

1. Comprueba que `/sitemap.xml` y `/robots.txt` estén accesibles y apunten al dominio correcto.
2. Envía un formulario de lista de espera y verifica que el webhook reciba la carga JSON esperada.
3. Revisa las herramientas de analítica conectadas (Google Tag Manager, Google Analytics, etc.) para asegurarte de que se registran los eventos:
   - `waitlist_form_viewed`
   - `waitlist_form_submitted` con valores `attempt`, `success` o `error` en el campo `interaction`.

Siguiendo estos pasos tendrás un despliegue consistente y medible del sitio web de Ezivia enfocado en ofrecer una experiencia simplificada para dispositivos Android destinados a personas mayores.
