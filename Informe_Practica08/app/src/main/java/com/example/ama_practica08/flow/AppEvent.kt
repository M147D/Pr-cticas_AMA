package com.example.ama_practica08.flow

/**
 * Eventos de la aplicación
 *
 * Sealed class que representa todos los eventos que pueden ocurrir
 * en la aplicación y que deben ser comunicados a la UI.
 *
 * Estos eventos son emitidos por el ViewModel a través de un SharedFlow
 * y consumidos por los Composables para mostrar notificaciones,
 * toasts o realizar acciones.
 */
sealed class AppEvent {

    /**
     * Evento: Mostrar notificación
     *
     * Se emite cuando se necesita mostrar una notificación importante al usuario
     *
     * @param title Título de la notificación
     * @param body Cuerpo del mensaje
     * @param type Tipo de notificación (SUCCESS, ERROR, WARNING, INFO)
     */
    data class Notify(
        val title: String,
        val body: String,
        val type: NotificationType = NotificationType.INFO
    ) : AppEvent()

    /**
     * Evento: Mostrar Toast
     *
     * Se emite para mensajes breves y no intrusivos
     *
     * @param message Mensaje a mostrar
     */
    data class ShowToast(val message: String) : AppEvent()

    /**
     * Evento: Mostrar Snackbar
     *
     * Se emite para mensajes que pueden incluir una acción
     *
     * @param message Mensaje a mostrar
     * @param actionLabel Etiqueta del botón de acción (opcional)
     * @param onActionClick Callback cuando se presiona la acción (opcional)
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val onActionClick: (() -> Unit)? = null
    ) : AppEvent()

    /**
     * Evento: Registro exitoso
     *
     * Se emite cuando se registra exitosamente una asistencia
     *
     * @param mensaje Mensaje de confirmación
     */
    data class RegistroExitoso(val mensaje: String) : AppEvent()

    /**
     * Evento: Registro rechazado
     *
     * Se emite cuando se rechaza un intento de registro
     *
     * @param razon Razón del rechazo
     */
    data class RegistroRechazado(val razon: String) : AppEvent()

    /**
     * Evento: Cambio en política canRegister
     *
     * Se emite cada vez que cambia el estado de si el usuario puede registrar
     *
     * @param canRegister true si puede registrar, false si no
     * @param mensaje Mensaje explicativo del cambio
     */
    data class CanRegisterChanged(
        val canRegister: Boolean,
        val mensaje: String
    ) : AppEvent()

    /**
     * Evento: Error del sistema
     *
     * Se emite cuando ocurre un error inesperado
     *
     * @param error Descripción del error
     */
    data class SystemError(val error: String) : AppEvent()
}

/**
 * Tipos de notificación
 *
 * Define los diferentes tipos visuales de notificación
 */
enum class NotificationType {
    SUCCESS,    // Verde - operación exitosa
    ERROR,      // Rojo - error u operación fallida
    WARNING,    // Amarillo - advertencia
    INFO        // Azul - información general
}