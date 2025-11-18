# Práctica 05

**Nombre:** SistemaAsistencia_Num05

## Descripción

Este proyecto tiene como objetivo comprender e implementar la consola de firebase para controlar el despliegue de notificaciones y alertas que son adminsitradas}
y generadas por la consola de firebase.

## Conclusiones y Recomendaciones

### Conclusiones:

* La implementación de Firebase Cloud Messaging (FCM) mediante un servicio personalizado (MyFirebaseMessagingService) demostró ser efectiva para recibir y
procesar notificaciones push en tiempo real, permitiendo diferenciar entre mensajes de tipo "notificación" manejados automáticamente por el sistema cuando
la app está en background y mensajes de tipo "data" procesados siempre por el servicio sin importar el estado de la aplicación.
* El patrón arquitectural de separación de responsabilidades mediante clases especializadas (FirebaseConfig para configuración, MessagingHandler para
procesamiento de mensajes, NotificationService para gestión de notificaciones y NotificationBuilder para construcción) facilitó el mantenimiento del
código y permitió una clara trazabilidad del flujo de datos desde la recepción del mensaje FCM hasta la visualización de la notificación al usuario.
* La integración de StateFlow en MessagingHandler para mantener el historial de mensajes y el token FCM actual demostró ser una solución efectiva para
implementar un sistema reactivo que permite a otros componentes de la aplicación suscribirse a cambios en el estado de la mensajería sin acoplamiento
directo con el servicio de Firebase.

### Recomendaciones:

* Es fundamental migrar a las APIs modernas de Android para la solicitud de permisos, reemplazando el método deprecado requestPermissions() por
ActivityResultLauncher con registerForActivityResult(), ya que este último ofrece un flujo más robusto y mantenible que evita problemas de ciclo de vida,
mejora el manejo de resultados mediante callbacks tipados y se alinea con las mejores prácticas actuales de Android, garantizando compatibilidad futura
y reduciendo advertencias de deprecación que pueden causar problemas en futuras versiones del SDK.
* Se recomienda implementar un sistema de persistencia local (SharedPreferences o Room Database) para almacenar el historial de notificaciones recibidas
y permitir al usuario consultar mensajes anteriores incluso sin conexión a internet, especialmente importante para notificaciones de asistencia que
pueden servir como registro de evidencia de marcaciones.

### Capturas
<p>Visualización de la generación de alertas</p>
<img src="https://github.com/M147D/Pr-cticas_AMA/blob/main/Informe_Practica05/capturas/Proyecto%2005%20(1).png" alt="Alt text" title="a title" width="300" height="auto"/>

<p>Visualización de notificaciones generadas</p>
<img src="https://github.com/M147D/Pr-cticas_AMA/blob/main/Informe_Practica05/capturas/Proyecto%2005%20(2).png" alt="Alt text" title="a title" width="300" height="auto"/>
<img src="https://github.com/M147D/Pr-cticas_AMA/blob/main/Informe_Practica05/capturas/Proyecto%2005%20(3).png" alt="Alt text" title="a title" width="300" height="auto"/>
