package com.example.ama_practica07.rules

import com.example.ama_practica07.models.Usuario
import com.example.ama_practica07.models.Rol

/**
 * Objeto que contiene funciones de orden superior y lambdas
 * para validación de usuarios y reglas de negocio
 */
object ValidationRules {

    /**
     * Función de orden superior que aplica una validación a un usuario
     * @param usuario Usuario a validar
     * @param validacion Lambda que define la regla de validación
     * @return Resultado de la validación
     */
    fun validarUsuario(usuario: Usuario, validacion: (Usuario) -> Boolean): Boolean {
        return validacion(usuario)
    }

    /**
     * Función de orden superior que filtra una lista de usuarios según un criterio
     * @param usuarios Lista de usuarios
     * @param criterio Lambda que define el criterio de filtrado
     * @return Lista filtrada de usuarios
     */
    fun filtrarUsuarios(usuarios: List<Usuario>, criterio: (Usuario) -> Boolean): List<Usuario> {
        return usuarios.filter(criterio)
    }

    /**
     * Función de orden superior que transforma una lista de usuarios
     * @param usuarios Lista de usuarios
     * @param transformacion Lambda que define la transformación
     * @return Lista transformada
     */
    fun <R> transformarUsuarios(usuarios: List<Usuario>, transformacion: (Usuario) -> R): List<R> {
        return usuarios.map(transformacion)
    }

    /**
     * Función de orden superior que ejecuta una acción solo si el usuario cumple una condición
     * @param usuario Usuario a evaluar
     * @param condicion Lambda que define la condición
     * @param accion Lambda que define la acción a ejecutar
     */
    fun ejecutarSiCumple(usuario: Usuario, condicion: (Usuario) -> Boolean, accion: (Usuario) -> Unit) {
        if (condicion(usuario)) {
            accion(usuario)
        }
    }

    // ==================== LAMBDAS PREDEFINIDAS ====================

    /**
     * Lambda que verifica si un usuario es mayor de edad
     */
    val esMayorDeEdad: (Usuario) -> Boolean = { usuario ->
        usuario.edad >= 18
    }

    /**
     * Lambda que verifica si un usuario está activo
     */
    val estaActivo: (Usuario) -> Boolean = { usuario ->
        usuario.enabled
    }

    /**
     * Lambda que verifica si un usuario es administrador
     */
    val esAdministrador: (Usuario) -> Boolean = { usuario ->
        usuario.rol == Rol.ADMIN
    }

    /**
     * Lambda que verifica si un usuario puede acceder al sistema
     */
    val puedeAcceder: (Usuario) -> Boolean = { usuario ->
        usuario.enabled && usuario.edad >= 18
    }

    /**
     * Lambda que verifica si un usuario es joven (menor de 25 años)
     */
    val esJoven: (Usuario) -> Boolean = { usuario ->
        usuario.edad < 25
    }

    /**
     * Lambda que obtiene el nombre en mayúsculas
     */
    val nombreEnMayusculas: (Usuario) -> String = { usuario ->
        usuario.nombre.uppercase()
    }

    /**
     * Lambda que obtiene una descripción corta del usuario
     */
    val descripcionCorta: (Usuario) -> String = { usuario ->
        "${usuario.nombre} (${usuario.rol.name})"
    }

    // ==================== FUNCIONES DE ORDEN SUPERIOR AVANZADAS ====================

    /**
     * Función que combina múltiples validaciones con AND
     * @param validaciones Lista de lambdas de validación
     * @return Lambda que aplica todas las validaciones
     */
    fun combinarValidacionesAND(vararg validaciones: (Usuario) -> Boolean): (Usuario) -> Boolean {
        return { usuario ->
            validaciones.all { validacion -> validacion(usuario) }
        }
    }

    /**
     * Función que combina múltiples validaciones con OR
     * @param validaciones Lista de lambdas de validación
     * @return Lambda que aplica cualquiera de las validaciones
     */
    fun combinarValidacionesOR(vararg validaciones: (Usuario) -> Boolean): (Usuario) -> Boolean {
        return { usuario ->
            validaciones.any { validacion -> validacion(usuario) }
        }
    }

    /**
     * Función de orden superior que cuenta cuántos usuarios cumplen un criterio
     * @param usuarios Lista de usuarios
     * @param criterio Lambda que define el criterio
     * @return Cantidad de usuarios que cumplen el criterio
     */
    fun contarPorCriterio(usuarios: List<Usuario>, criterio: (Usuario) -> Boolean): Int {
        return usuarios.count(criterio)
    }

    /**
     * Función de orden superior que agrupa usuarios según un criterio
     * @param usuarios Lista de usuarios
     * @param clasificador Lambda que define cómo clasificar
     * @return Mapa con los usuarios agrupados
     */
    fun <K> agruparUsuarios(usuarios: List<Usuario>, clasificador: (Usuario) -> K): Map<K, List<Usuario>> {
        return usuarios.groupBy(clasificador)
    }

    /**
     * Función de orden superior que ordena usuarios según un criterio
     * @param usuarios Lista de usuarios
     * @param comparador Lambda que define el criterio de ordenamiento
     * @return Lista ordenada de usuarios
     */
    fun <R : Comparable<R>> ordenarUsuarios(usuarios: List<Usuario>, selector: (Usuario) -> R): List<Usuario> {
        return usuarios.sortedBy(selector)
    }

    // ==================== LAMBDAS COMPUESTAS ====================

    /**
     * Lambda compuesta: Usuario activo Y mayor de edad
     */
    val usuarioValido: (Usuario) -> Boolean = { usuario ->
        estaActivo(usuario) && esMayorDeEdad(usuario)
    }

    /**
     * Lambda compuesta: Usuario administrador O mayor de 65 años
     */
    val usuarioPrioritario: (Usuario) -> Boolean = { usuario ->
        esAdministrador(usuario) || usuario.edad >= 65
    }
}