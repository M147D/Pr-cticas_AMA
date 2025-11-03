package com.example.ama_practica05.flow

import com.example.ama_practica05.models.Ubicacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Publicador: LocationSource
 *
 * Source que publica la ubicación actual del usuario.
 *
 */
object LocationSource {

    /**
     * StateFlow privado mutable que mantiene la ubicación actual
     */
    private val _zoneFlow = MutableStateFlow<Ubicacion>(
        Ubicacion.DentroDelRango("Campus Principal")
    )

    /**
     * StateFlow público de solo lectura que expone la ubicación actual
     *
     * Este Flow emite un nuevo valor cada vez que:
     * - El usuario se mueve a una nueva ubicación
     * - Se detecta entrada/salida del rango permitido
     * - Se simula un cambio de ubicación para pruebas
     *
     * @return StateFlow<Ubicacion> que emite la ubicación actual
     */
    val zoneFlow: StateFlow<Ubicacion> = _zoneFlow.asStateFlow()

    /**
     * Publica que el usuario está dentro del rango permitido
     *
     * @param descripcion Descripción de la ubicación (ej: "Campus Principal")
     */
    fun setDentroDelRango(descripcion: String = "Campus Principal") {
        _zoneFlow.value = Ubicacion.DentroDelRango(descripcion)
    }

    /**
     * Publica que el usuario está fuera del rango permitido
     *
     * @param descripcion Descripción de la ubicación (ej: "Fuera del área")
     */
    fun setFueraDelRango(descripcion: String = "Fuera del área permitida") {
        _zoneFlow.value = Ubicacion.FueraDelRango(descripcion)
    }

    /**
     * Publica una ubicación directamente
     *
     * @param ubicacion Nueva ubicación a publicar
     */
    fun setUbicacion(ubicacion: Ubicacion) {
        _zoneFlow.value = ubicacion
    }

    /**
     * Obtiene la ubicación actual de forma síncrona
     *
     * @return Ubicacion actual
     */
    fun getCurrentZone(): Ubicacion {
        return _zoneFlow.value
    }

    /**
     * Verifica si el usuario está dentro del rango permitido
     *
     * @return true si está dentro del rango, false en caso contrario
     */
    fun isDentroDelRango(): Boolean {
        return _zoneFlow.value.estaDentroDelRango()
    }
}