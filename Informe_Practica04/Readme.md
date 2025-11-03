# Práctica 04

**Nombre:** SistemaAsistencia_Num04

## Descripción

Este proyecto tiene como objetivo comprender e implementar el patrón publicador-suscriptor aplicando Kotlin Flow y gestión de notificaciones locales para controlar
la habilitación e inhabilitación de controles en la interfaz UI de manera completamente reactiva.

## Conclusiones y Recomendaciones

### Conclusiones:

* La implementación del patrón Publicador-Suscriptor mediante Kotlin Flow (StateFlow y SharedFlow) permitió crear un sistema completamente reactivo donde los
cambios en las fuentes de datos (ubicación, hora, autenticación) se propagan automáticamente a través de toda la aplicación sin necesidad de polling o actualización
manual de la UI.
* Se demostró que la separación entre estado (StateFlow) y eventos (SharedFlow) es fundamental para una arquitectura robusta: StateFlow mantiene el estado
actual de la aplicación con un valor siempre disponible, mientras que SharedFlow maneja eventos puntuales como notificaciones o acciones del usuario que
ocurren una sola vez.
* El uso de operadores como combine() para fusionar múltiples flows en PolicyEngine permitió crear una lógica de validación reactiva que re-evalúa
automáticamente las políticas de registro cada vez que cambia cualquiera de sus dependencias (usuario, ubicación u hora), manteniendo sincronizada la
habilitación de botones con las reglas de negocio.
* La arquitectura de Sources como singletons (objects) centralizó la gestión del estado global de la aplicación, evitando duplicación de datos y garantizando
una única fuente de verdad (single source of truth) para cada tipo de información, lo que simplificó el debugging y mantenimiento del código.

### Recomendaciones:

*  Se recomienda mantener la distinción clara entre StateFlow para estados persistentes (usuario actual, ubicación, registros) y SharedFlow para eventos
efímeros (notificaciones, toasts, navegación), evitando usar StateFlow para eventos que no deben tener un "valor actual" persistente.
*  Para proyectos que escalen en complejidad, se sugiere implementar una capa de casos de uso (Use Cases) entre los Sources y el ViewModel, encapsulando
la lógica de combinación de flows y permitiendo reutilizar estas operaciones en diferentes ViewModels.
*  Se recomienda implementar un sistema de caché y persistencia (Room Database con Flow, DataStore) para que los StateFlows sobrevivan a la destrucción
del proceso, especialmente para datos críticos como el usuario autenticado o registros de asistencia.
* Para proyectos con múltiples pantallas, se sugiere utilizar un EventBus global basado en SharedFlow para eventos que deben ser observados desde
cualquier parte de la app (ej: sesión expirada, cambios de configuración), evitando pasar callbacks entre múltiples niveles de composición.

### Capturas
Visualización de registros de asistencia inhabilitación por fuera de horario.
![Alt text](https://github.com/M147D/Pr-cticas_AMA/blob/main/Informe_Practica04/capturas/Proyecto%2004%20(1).png "a title")  

Visualización de registros de asistencia inhabilitación por estar fuera del rango.
![Alt text](https://github.com/M147D/Pr-cticas_AMA/blob/main/Informe_Practica04/capturas/Proyecto%2004%20(2).png "a title")  

Validación de registro de asistencia cuando se cumplen los paramétros.
![Alt text](https://github.com/M147D/Pr-cticas_AMA/blob/main/Informe_Practica04/capturas/Proyecto%2004%20(3).png "a title")  
