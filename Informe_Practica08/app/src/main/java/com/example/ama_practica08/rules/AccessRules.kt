package com.example.ama_practica08.rules

import com.example.ama_practica08.models.*

/**
 * Objeto que contiene reglas de acceso y funciones de orden superior
 * para el control de registros de acceso
 */
object AccessRules {

    /**
     * Función de orden superior que valida un registro de acceso
     * @param registro Registro a validar
     * @param validacion Lambda que define la regla de validación
     * @return Resultado de la validación
     */
    fun validarRegistro(registro: RegistroAcceso, validacion: (RegistroAcceso) -> Boolean): Boolean {
        return validacion(registro)
    }

    /**
     * Función de orden superior que filtra registros según un criterio
     * @param registros Lista de registros
     * @param criterio Lambda que define el criterio
     * @return Lista filtrada de registros
     */
    fun filtrarRegistros(registros: List<RegistroAcceso>, criterio: (RegistroAcceso) -> Boolean): List<RegistroAcceso> {
        return registros.filter(criterio)
    }

    /**
     * Función de orden superior que ejecuta una acción para cada registro que cumple una condición
     * @param registros Lista de registros
     * @param condicion Lambda que define la condición
     * @param accion Lambda que define la acción a ejecutar
     */
    fun procesarRegistros(
        registros: List<RegistroAcceso>,
        condicion: (RegistroAcceso) -> Boolean,
        accion: (RegistroAcceso) -> Unit
    ) {
        registros.filter(condicion).forEach(accion)
    }

    // ==================== LAMBDAS PREDEFINIDAS PARA REGISTROS ====================

    /**
     * Lambda que verifica si un registro es válido
     */
    val esRegistroValido: (RegistroAcceso) -> Boolean = { registro ->
        registro.esAccesoValido()
    }

    /**
     * Lambda que verifica si es un registro de entrada
     */
    val esEntrada: (RegistroAcceso) -> Boolean = { registro ->
        registro.accion == AccionAsistencia.ENTRADA
    }

    /**
     * Lambda que verifica si es un registro de salida
     */
    val esSalida: (RegistroAcceso) -> Boolean = { registro ->
        registro.accion == AccionAsistencia.SALIDA
    }

    /**
     * Lambda que verifica si el usuario está dentro del rango
     */
    val estaDentroDelRango: (RegistroAcceso) -> Boolean = { registro ->
        registro.ubicacion.estaDentroDelRango()
    }

    /**
     * Lambda que verifica si el usuario es administrador
     */
    val esUsuarioAdmin: (RegistroAcceso) -> Boolean = { registro ->
        registro.usuario.esAdmin()
    }

    /**
     * Lambda que obtiene el nombre del usuario del registro
     */
    val obtenerNombreUsuario: (RegistroAcceso) -> String = { registro ->
        registro.usuario.nombre
    }

    /**
     * Lambda que obtiene la hora del registro
     */
    val obtenerHora: (RegistroAcceso) -> String = { registro ->
        registro.obtenerFechaFormateada()
    }

    // ==================== FUNCIONES DE ORDEN SUPERIOR PARA REPORTES ====================

    /**
     * Función de orden superior que genera un reporte personalizado
     * @param registros Lista de registros
     * @param filtro Lambda para filtrar registros
     * @param formato Lambda para formatear cada registro
     * @return String con el reporte formateado
     */
    fun generarReporte(
        registros: List<RegistroAcceso>,
        filtro: (RegistroAcceso) -> Boolean,
        formato: (RegistroAcceso) -> String
    ): String {
        return registros
            .filter(filtro)
            .joinToString("\n") { formato(it) }
    }

    /**
     * Función de orden superior que cuenta registros por criterio
     * @param registros Lista de registros
     * @param criterios Map de nombre de criterio y lambda
     * @return Map con los conteos
     */
    fun contarPorCriterios(
        registros: List<RegistroAcceso>,
        criterios: Map<String, (RegistroAcceso) -> Boolean>
    ): Map<String, Int> {
        return criterios.mapValues { (_, criterio) ->
            registros.count(criterio)
        }
    }

    /**
     * Función de orden superior que agrupa registros
     * @param registros Lista de registros
     * @param clasificador Lambda que define cómo agrupar
     * @return Map con los registros agrupados
     */
    fun <K> agruparRegistros(
        registros: List<RegistroAcceso>,
        clasificador: (RegistroAcceso) -> K
    ): Map<K, List<RegistroAcceso>> {
        return registros.groupBy(clasificador)
    }

    // ==================== LAMBDAS COMPUESTAS PARA REGLAS DE NEGOCIO ====================

    /**
     * Lambda compuesta: Registro válido de entrada
     */
    val entradaValida: (RegistroAcceso) -> Boolean = { registro ->
        esRegistroValido(registro) && esEntrada(registro)
    }

    /**
     * Lambda compuesta: Registro válido de salida
     */
    val salidaValida: (RegistroAcceso) -> Boolean = { registro ->
        esRegistroValido(registro) && esSalida(registro)
    }

    /**
     * Lambda compuesta: Entrada de administrador
     */
    val entradaAdmin: (RegistroAcceso) -> Boolean = { registro ->
        esEntrada(registro) && esUsuarioAdmin(registro)
    }

    /**
     * Lambda que verifica si el registro requiere atención
     * (usuario fuera de rango o deshabilitado)
     */
    val requiereAtencion: (RegistroAcceso) -> Boolean = { registro ->
        !registro.esAccesoValido()
    }

    // ==================== FUNCIONES AVANZADAS ====================

    /**
     * Función de orden superior que aplica diferentes acciones según el tipo de registro
     * @param registro Registro a procesar
     * @param alEntrada Lambda a ejecutar si es entrada
     * @param alSalida Lambda a ejecutar si es salida
     */
    fun procesarSegunAccion(
        registro: RegistroAcceso,
        alEntrada: (RegistroAcceso) -> Unit,
        alSalida: (RegistroAcceso) -> Unit
    ) {
        when (registro.accion) {
            AccionAsistencia.ENTRADA -> alEntrada(registro)
            AccionAsistencia.SALIDA -> alSalida(registro)
        }
    }

    /**
     * Función de orden superior que calcula estadísticas
     * @param registros Lista de registros
     * @param extractor Lambda que extrae el valor a analizar
     * @return Map con estadísticas básicas
     */
    fun calcularEstadisticas(
        registros: List<RegistroAcceso>,
        extractor: (RegistroAcceso) -> Int
    ): Map<String, Int> {
        val valores = registros.map(extractor)
        return mapOf(
            "total" to valores.size,
            "suma" to valores.sum(),
            "promedio" to if (valores.isNotEmpty()) valores.average().toInt() else 0,
            "maximo" to (valores.maxOrNull() ?: 0),
            "minimo" to (valores.minOrNull() ?: 0)
        )
    }

    /**
     * Función que crea una regla personalizada combinando otras reglas
     * @param reglas Lista de lambdas de validación
     * @param operador Operador lógico ("AND" o "OR")
     * @return Lambda que combina todas las reglas
     */
    fun crearReglaPersonalizada(
        vararg reglas: (RegistroAcceso) -> Boolean,
        operador: String = "AND"
    ): (RegistroAcceso) -> Boolean {
        return { registro ->
            when (operador.uppercase()) {
                "AND" -> reglas.all { it(registro) }
                "OR" -> reglas.any { it(registro) }
                else -> false
            }
        }
    }
}