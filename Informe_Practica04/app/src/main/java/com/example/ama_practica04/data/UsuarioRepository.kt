package com.example.ama_practica03.data

import com.example.ama_practica03.models.*

/**
 * Repositorio de usuarios
 * Contiene datos de ejemplo y métodos para acceder a ellos
 */
object UsuarioRepository {

    /**
     * Lista de usuarios de ejemplo
     */
    private val usuarios = mutableListOf(
        Usuario(
            id = 1,
            nombre = "Juan Pérez",
            correo = "juan.perez@example.com",
            edad = 25,
            rol = Rol.ADMIN,
            enabled = true
        ),
        Usuario(
            id = 2,
            nombre = "María García",
            correo = "maria.garcia@example.com",
            edad = 30,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = 3,
            nombre = "Pedro Rodríguez",
            correo = "pedro.rodriguez@example.com",
            edad = 22,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = 4,
            nombre = "Ana López",
            correo = "ana.lopez@example.com",
            edad = 17,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = 5,
            nombre = "Carlos Martínez",
            correo = "carlos.martinez@example.com",
            edad = 35,
            rol = Rol.ADMIN,
            enabled = false
        ),
        Usuario(
            id = 6,
            nombre = "Laura Sánchez",
            correo = "laura.sanchez@example.com",
            edad = 28,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = 7,
            nombre = "Miguel Pastuña",
            correo = "miguel.pastuna@example.com",
            edad = 24,
            rol = Rol.ADMIN,
            enabled = true
        ),
        Usuario(
            id = 8,
            nombre = "Stalin Garcia",
            correo = "stalin.garcia@example.com",
            edad = 23,
            rol = Rol.ADMIN,
            enabled = true
        )
    )

    /**
     * Lista de registros de acceso de ejemplo
     */
    private val registrosAcceso = mutableListOf<RegistroAcceso>()

    // Inicialización de registros de ejemplo
    init {
        // Agregar algunos registros de ejemplo
        registrosAcceso.add(
            RegistroAcceso(
                usuario = usuarios[0],
                accion = AccionAsistencia.ENTRADA,
                ubicacion = Ubicacion.DentroDelRango("Campus Principal"),
                marcaTiempo = System.currentTimeMillis() - 3600000 // Hace 1 hora
            )
        )
        registrosAcceso.add(
            RegistroAcceso(
                usuario = usuarios[1],
                accion = AccionAsistencia.ENTRADA,
                ubicacion = Ubicacion.DentroDelRango("Edificio A"),
                marcaTiempo = System.currentTimeMillis() - 7200000 // Hace 2 horas
            )
        )
        registrosAcceso.add(
            RegistroAcceso(
                usuario = usuarios[3],
                accion = AccionAsistencia.ENTRADA,
                ubicacion = Ubicacion.FueraDelRango("Edad insuficiente"),
                marcaTiempo = System.currentTimeMillis() - 1800000 // Hace 30 minutos
            )
        )
        registrosAcceso.add(
            RegistroAcceso(
                usuario = usuarios[4],
                accion = AccionAsistencia.ENTRADA,
                ubicacion = Ubicacion.DentroDelRango("Campus Principal"),
                marcaTiempo = System.currentTimeMillis() - 5400000 // Hace 1.5 horas
            )
        )
    }

    // ==================== MÉTODOS DE ACCESO ====================

    /**
     * Obtiene todos los usuarios
     */
    fun obtenerTodosLosUsuarios(): List<Usuario> = usuarios.toList()

    /**
     * Obtiene un usuario por ID
     */
    fun obtenerUsuarioPorId(id: Int): Usuario? = usuarios.find { it.id == id }

    /**
     * Obtiene todos los registros de acceso
     */
    fun obtenerTodosLosRegistros(): List<RegistroAcceso> = registrosAcceso.toList()

    /**
     * Agrega un nuevo usuario
     */
    fun agregarUsuario(usuario: Usuario): Boolean {
        if (usuarios.any { it.id == usuario.id }) {
            return false // Ya existe un usuario con ese ID
        }
        usuarios.add(usuario)
        return true
    }

    /**
     * Agrega un nuevo registro de acceso
     */
    fun agregarRegistroAcceso(registro: RegistroAcceso) {
        registrosAcceso.add(registro)
    }

    /**
     * Actualiza un usuario existente
     */
    fun actualizarUsuario(usuario: Usuario): Boolean {
        val index = usuarios.indexOfFirst { it.id == usuario.id }
        if (index != -1) {
            usuarios[index] = usuario
            return true
        }
        return false
    }

    /**
     * Elimina un usuario por ID
     */
    fun eliminarUsuario(id: Int): Boolean {
        return usuarios.removeIf { it.id == id }
    }

    /**
     * Obtiene usuarios activos
     */
    fun obtenerUsuariosActivos(): List<Usuario> {
        return usuarios.filter { it.enabled }
    }

    /**
     * Obtiene usuarios administradores
     */
    fun obtenerAdministradores(): List<Usuario> {
        return usuarios.filter { it.rol == Rol.ADMIN }
    }

    /**
     * Obtiene usuarios por rol
     */
    fun obtenerUsuariosPorRol(rol: Rol): List<Usuario> {
        return usuarios.filter { it.rol == rol }
    }

    /**
     * Obtiene registros de un usuario específico
     */
    fun obtenerRegistrosPorUsuario(usuarioId: Int): List<RegistroAcceso> {
        return registrosAcceso.filter { it.usuario.id == usuarioId }
    }

    /**
     * Obtiene registros válidos
     */
    fun obtenerRegistrosValidos(): List<RegistroAcceso> {
        return registrosAcceso.filter { it.esAccesoValido() }
    }

    /**
     * Obtiene registros inválidos
     */
    fun obtenerRegistrosInvalidos(): List<RegistroAcceso> {
        return registrosAcceso.filter { !it.esAccesoValido() }
    }

    /**
     * Obtiene registros por tipo de acción
     */
    fun obtenerRegistrosPorAccion(accion: AccionAsistencia): List<RegistroAcceso> {
        return registrosAcceso.filter { it.accion == accion }
    }

    /**
     * Cuenta usuarios por criterio personalizado
     */
    fun contarUsuarios(criterio: (Usuario) -> Boolean): Int {
        return usuarios.count(criterio)
    }

    /**
     * Busca usuarios por nombre (búsqueda parcial)
     */
    fun buscarUsuariosPorNombre(nombre: String): List<Usuario> {
        return usuarios.filter { it.nombre.contains(nombre, ignoreCase = true) }
    }

    /**
     * Limpia todos los registros de acceso
     */
    fun limpiarRegistros() {
        registrosAcceso.clear()
    }

    /**
     * Obtiene estadísticas de usuarios
     */
    fun obtenerEstadisticas(): Map<String, Int> {
        return mapOf(
            "total" to usuarios.size,
            "activos" to usuarios.count { it.enabled },
            "inactivos" to usuarios.count { !it.enabled },
            "admins" to usuarios.count { it.rol == Rol.ADMIN },
            "users" to usuarios.count { it.rol == Rol.USER },
            "mayoresDeEdad" to usuarios.count { it.edad >= 18 },
            "menoresDeEdad" to usuarios.count { it.edad < 18 }
        )
    }

    /**
     * Obtiene estadísticas de registros
     */
    fun obtenerEstadisticasRegistros(): Map<String, Int> {
        return mapOf(
            "total" to registrosAcceso.size,
            "validos" to registrosAcceso.count { it.esAccesoValido() },
            "invalidos" to registrosAcceso.count { !it.esAccesoValido() },
            "entradas" to registrosAcceso.count { it.accion == AccionAsistencia.ENTRADA },
            "salidas" to registrosAcceso.count { it.accion == AccionAsistencia.SALIDA },
            "dentroDelRango" to registrosAcceso.count { it.ubicacion.estaDentroDelRango() },
            "fueraDelRango" to registrosAcceso.count { !it.ubicacion.estaDentroDelRango() }
        )
    }
}
