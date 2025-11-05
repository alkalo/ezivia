# Procedimientos de Manejo de Datos de Ezivia

Estos procedimientos describen cómo gestionamos el ciclo de vida de los datos personales en Ezivia para cumplir con el GDPR y la LOPDGDD.

## 1. Inventario de datos
- **Datos de perfil:** nombre del usuario, teléfono y relación de contactos prioritarios. Registrados durante la configuración inicial.
- **Preferencias de accesibilidad:** tamaño de letra, contraste, modo simplificado. Almacenados localmente en el dispositivo y sincronizados cifradamente con la nube de Ezivia para restauraciones.
- **Eventos de asistencia remota:** registros de llamadas recientes, ajustes modificados, solicitudes de ayuda. Solo disponibles si el usuario activa la función y se limitan a 90 días.
- **Telemetría anónima:** métricas agregadas de uso y datos técnicos (versión de Android, modelo de dispositivo, errores).

## 2. Retención y supresión
| Tipo de dato | Ubicación | Periodo de retención | Método de supresión |
|--------------|-----------|----------------------|---------------------|
| Datos de perfil | Base de datos cifrada en la UE | Mientras exista la cuenta o hasta 30 días tras baja | Eliminación lógica inmediata y borrado físico trimestral |
| Preferencias de accesibilidad | Dispositivo y copia cifrada | Hasta que el usuario restablezca o desinstale la app | Eliminación al desinstalar y purgado automático cada 24 h en servidores |
| Eventos de asistencia remota | Servidor de soporte en la UE | 90 días | Trabajo automático que anonimiza y destruye registros vencidos |
| Telemetría anónima | Plataforma analítica UE | 12 meses | Agregación irreversible y borrado de crudos a los 30 días |

## 3. Solicitudes de los usuarios
1. **Recepción:** las solicitudes llegan vía correo (privacidad@ezivia.com) o desde el panel de privacidad en la app. Se registra un ticket con fecha y tipo de derecho solicitado.
2. **Verificación:** se solicita confirmación a través del dispositivo registrado o verificación documental si procede.
3. **Evaluación:** el equipo de privacidad determina el alcance y coordina con ingeniería. Plazo máximo de respuesta: 30 días naturales, ampliable 2 meses si es complejo.
4. **Ejecución:**
   - Acceso/portabilidad: exportamos datos en formato JSON y PDF accesible.
   - Rectificación: habilitamos edición directa o asistimos manualmente.
   - Supresión: ejecutamos el flujo de baja y emitimos confirmación.
   - Oposición/limitación: etiquetamos la cuenta para detener tratamientos no necesarios y documentamos la restricción.
5. **Cierre:** notificamos al usuario el resultado y archivamos evidencia durante 3 años.

## 4. Respuesta a incidentes
- Monitoreo 24/7 con alertas automáticas sobre accesos inusuales.
- Comité de crisis (privacidad, seguridad, soporte) convoca en <12 h.
- Notificación a la AEPD y a los afectados en ≤72 h cuando el incidente pueda implicar riesgo para derechos y libertades.
- Documentación detallada del incidente, acciones correctivas y auditoría posterior.

## 5. Evaluaciones de impacto (DPIA)
- Realizamos una DPIA inicial para las funciones de asistencia remota y accesibilidad.
- Revisamos la DPIA anualmente o ante cambios significativos en los flujos de datos.
- Mantenemos registro de decisiones y medidas atenuantes aplicadas.

## 6. Formación y responsabilidades
- El Delegado de Protección de Datos (DPD) supervisa el cumplimiento y asesora a los equipos.
- Formación anual obligatoria para personal de atención al cliente, ingeniería y producto.
- Controles de acceso basados en el principio de mínimo privilegio y revisados trimestralmente.

## 7. Proveedores y contratos
- Todos los encargados de tratamiento firman acuerdos con cláusulas GDPR (art. 28).
- Evaluamos el nivel de seguridad, localización de datos y subencargados antes de contratar.
- Auditoría documental de proveedores críticos cada 12 meses.

## 8. Revisión de procedimientos
- Revisión semestral de este documento por el DPD y el equipo legal.
- Registro de versiones y acciones derivadas en el sistema de cumplimiento de Ezivia.
