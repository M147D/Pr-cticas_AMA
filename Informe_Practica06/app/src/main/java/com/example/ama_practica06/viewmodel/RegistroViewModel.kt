package com.example.ama_practica06.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ama_practica06.data.UsuarioRepository
import com.example.ama_practica06.flow.AppEvent
import com.example.ama_practica06.flow.AuthSource
import com.example.ama_practica06.flow.LocationSource
import com.example.ama_practica06.flow.PolicyEngine
import com.example.ama_practica06.flow.TimeSource
import com.example.ama_practica06.models.AccionAsistencia
import com.example.ama_practica06.models.RegistroAcceso
import com.example.ama_practica06.models.Ubicacion
import com.example.ama_practica06.models.Usuario
import com.example.ama_practica06.rules.PolicyRules
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * RegistroViewModel
 *
 * ViewModel principal para la pantalla de registro de asistencia.
 * Implementa el patrón publicador-suscriptor completo:
 *
 * SUSCRIPCIONES:
 * - AuthSource.userFlow
 * - LocationSource.zoneFlow
 * - TimeSource.timeFlow
 * - UsuarioRepository.recordsFlow
 * - PolicyEngine.canRegister
 *
 * PUBLICACIONES:
 * - appState: StateFlow<AppState> (estado consolidado)
 * - events: SharedFlow<AppEvent> (eventos puntuales)
 */
class RegistroViewModel(
    initialUsuario: Usuario
) : ViewModel() {

    // ==================== SHARED FLOW DE EVENTOS ====================

    /**
     * SharedFlow privado mutable para emitir eventos
     */
    private val _events = MutableSharedFlow<AppEvent>()

    /**
     * Publicador: SharedFlow de eventos de la aplicación
     *
     * Emite eventos puntuales que deben ser consumidos por la UI:
     * - Notificaciones
     * - Toasts
     * - Snackbars
     * - Confirmaciones de registro
     *
     * A diferencia de StateFlow, SharedFlow no retiene estado,
     * ideal para eventos que ocurren en un momento específico.
     *
     * @return SharedFlow<AppEvent> que emite eventos de la aplicación
     */
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    // ==================== INICIALIZACIÓN ====================

    init {
        // Publicar usuario inicial en AuthSource
        AuthSource.login(initialUsuario)

        // Configurar hora para pruebas (en producción usar startAutoUpdate())
        TimeSource.syncWithSystemTime()
    }

    // ==================== STATE FLOW CONSOLIDADO ====================

    /**
     * Publicador: StateFlow del estado consolidado de la aplicación
     *
     * Este Flow combina TODOS los sources y expone un único AppState
     * que contiene todo lo que la UI necesita saber.
     *
     * Se actualiza automáticamente cuando cambia cualquiera de:
     * - Usuario (AuthSource)
     * - Ubicación (LocationSource)
     * - Hora (TimeSource)
     * - Registros (UsuarioRepository)
     * - Política canRegister (PolicyEngine)
     *
     * @return StateFlow<AppState> con el estado completo de la aplicación
     */
    val appState: StateFlow<AppState> = combine(
        AuthSource.userFlow,
        LocationSource.zoneFlow,
        TimeSource.timeFlow,
        UsuarioRepository.recordsFlow,
        PolicyEngine.canRegister,
        PolicyEngine.evaluacionDetallada
    ) { flows: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        AppState(
            usuario = flows[0] as Usuario?,
            ubicacion = flows[1] as Ubicacion,
            hora = flows[2] as Int,
            registros = flows[3] as List<RegistroAcceso>,
            canRegister = flows[4] as Boolean,
            evaluacion = flows[5] as PolicyRules.ResultadoEvaluacion,
            isLoading = false,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppState(
            usuario = initialUsuario,
            hora = TimeSource.getCurrentHour()
        )
    )

    // ==================== ACCIONES ====================

    /**
     * Actualiza la ubicación del usuario
     *
     * Publica el cambio a LocationSource, lo que dispara:
     * 1. PolicyEngine re-evalúa canRegister
     * 2. appState se actualiza
     * 3. Si canRegister cambia, se emite evento de notificación
     *
     * @param dentroDelRango true si está dentro del rango, false si está fuera
     */
    fun actualizarUbicacion(dentroDelRango: Boolean) {
        if (dentroDelRango) {
            LocationSource.setDentroDelRango("Campus Principal")
        } else {
            LocationSource.setFueraDelRango("Fuera del área permitida")
        }
    }

    /**
     * Actualiza la hora (para pruebas)
     *
     * Publica el cambio a TimeSource, lo que dispara re-evaluación de políticas
     *
     * @param nuevaHora Hora en formato 24h (0-23)
     */
    fun actualizarHora(nuevaHora: Int) {
        TimeSource.setHora(nuevaHora)
    }

    /**
     * Registra una asistencia (entrada o salida)
     *
     * Evalúa la política actual y si está permitido:
     * 1. Crea el registro
     * 2. Lo agrega al repositorio (que publica a recordsFlow)
     * 3. Emite evento de éxito
     *
     * Si no está permitido:
     * 1. Emite evento de rechazo con la razón
     *
     * @param accion ENTRADA o SALIDA
     */
    fun registrarAsistencia(accion: AccionAsistencia) {
        viewModelScope.launch {
            val usuario = AuthSource.getCurrentUser()
            if (usuario == null) {
                _events.emit(
                    AppEvent.RegistroRechazado("No hay usuario autenticado")
                )
                return@launch
            }

            val ubicacion = LocationSource.getCurrentZone()
            val hora = TimeSource.getCurrentHour()

            // Evaluar si puede registrar
            val evaluacion = PolicyEngine.evaluarNow()

            if (evaluacion.permitido) {
                // Crear registro
                val registro = RegistroAcceso(
                    usuario = usuario,
                    accion = accion,
                    ubicacion = ubicacion,
                    marcaTiempo = System.currentTimeMillis()
                )

                // Agregar al repositorio (automáticamente publica a recordsFlow)
                UsuarioRepository.agregarRegistroAcceso(registro)

                // Emitir evento de éxito
                _events.emit(
                    AppEvent.RegistroExitoso(
                        "Registro de ${accion.name} exitoso"
                    )
                )

                _events.emit(
                    AppEvent.ShowSnackbar(
                        message = "✓ Asistencia registrada correctamente"
                    )
                )
            } else {
                // Emitir evento de rechazo
                _events.emit(
                    AppEvent.RegistroRechazado(evaluacion.razon)
                )

                _events.emit(
                    AppEvent.Notify(
                        title = "Registro no permitido",
                        body = evaluacion.mensaje,
                        type = com.example.ama_practica06.flow.NotificationType.ERROR
                    )
                )
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual
     *
     * Publica logout en AuthSource, lo que limpia el usuario
     * y actualiza toda la UI reactivamente
     */
    fun logout() {
        AuthSource.logout()
        viewModelScope.launch {
            _events.emit(
                AppEvent.ShowToast("Sesión cerrada")
            )
        }
    }

    // ==================== CLEANUP ====================

    override fun onCleared() {
        super.onCleared()
        // Detener actualizaciones automáticas si estaban activas
        TimeSource.stopAutoUpdate()
    }
}