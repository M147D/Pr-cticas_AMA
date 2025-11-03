package com.example.ama_practica04.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Publicador: TimeSource
 *
 * Source que publica la hora actual del sistema.
 *
 * Este source puede operar en dos modos:
 * 1. Modo automático: Actualiza la hora cada minuto usando la hora del sistema
 * 2. Modo manual: Permite configurar manualmente la hora para pruebas
 */
object TimeSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * StateFlow privado mutable que mantiene la hora actual (0-23)
     */
    private val _timeFlow = MutableStateFlow(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))

    /**
     * StateFlow público de solo lectura que expone la hora actual
     *
     * Este Flow emite un nuevo valor cada vez que:
     * - Transcurre un minuto (en modo automático)
     * - Se configura manualmente una hora (en modo manual/pruebas)
     *
     * @return StateFlow<Int> que emite la hora actual en formato 24h (0-23)
     */
    val timeFlow: StateFlow<Int> = _timeFlow.asStateFlow()

    /**
     * Indica si el source está en modo automático (actualización cada minuto)
     */
    private var isAutoMode = false

    /**
     * Inicia el modo automático que actualiza la hora cada minuto
     */
    fun startAutoUpdate() {
        if (isAutoMode) return

        isAutoMode = true
        scope.launch {
            while (isAutoMode) {
                _timeFlow.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                // Esperar hasta el próximo minuto
                delay(60_000L) // 60 segundos
            }
        }
    }

    /**
     * Detiene el modo automático de actualización
     */
    fun stopAutoUpdate() {
        isAutoMode = false
    }

    /**
     * Publica manualmente una hora específica
     * Para pruebas y simulaciones
     *
     * @param hora Hora en formato 24h (0-23)
     * @throws IllegalArgumentException si la hora no está en el rango 0-23
     */
    fun setHora(hora: Int) {
        require(hora in 0..23) { "La hora debe estar entre 0 y 23, recibido: $hora" }
        stopAutoUpdate() // Detener modo automático al configurar manualmente
        _timeFlow.value = hora
    }

    /**
     * Sincroniza con la hora actual del sistema
     * Reinicia después de pruebas manuales
     */
    fun syncWithSystemTime() {
        _timeFlow.value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    /**
     * Obtiene la hora actual de forma síncrona
     *
     * @return Hora actual en formato 24h (0-23)
     */
    fun getCurrentHour(): Int {
        return _timeFlow.value
    }

    /**
     * Verifica si la hora actual está dentro del horario permitido (06:00 - 20:00)
     *
     * @return true si está en horario permitido, false en caso contrario
     */
    fun isHorarioPermitido(): Boolean {
        return _timeFlow.value in 6..20
    }

    /**
     * Obtiene un mensaje descriptivo del horario actual
     *
     * @return String con descripción del horario
     */
    fun getMensajeHorario(): String {
        return when (val hora = _timeFlow.value) {
            in 6..20 -> "Horario válido ($hora:00)"
            in 0..5 -> "Demasiado temprano ($hora:00)"
            else -> "Demasiado tarde ($hora:00)"
        }
    }
}