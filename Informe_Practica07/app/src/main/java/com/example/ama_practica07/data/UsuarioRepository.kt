package com.example.ama_practica07.data

import android.content.Context
import com.example.ama_practica07.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio de usuarios
 * Contiene datos de ejemplo y métodos para acceder a ellos
 *
 * Implementa el patrón publicador-suscriptor para registros de acceso
 * NUEVO: Persiste registros localmente con RegistroLocalRepository
 */
object UsuarioRepository {

    // NUEVO: Repositorio para persistencia local
    private lateinit var registroLocalRepo: RegistroLocalRepository
    private var isInitialized = false

    /**
     * Lista de usuarios de ejemplo
     */
    private val usuarios = mutableListOf(
        Usuario(
            id = 1,
            nombre = "Juan Perez",
            correo = "juan.perez@example.com",
            edad = 25,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = 2,
            nombre = "Maria Garcia",
            correo = "maria.garcia@example.com",
            edad = 30,
            rol = Rol.USER,
            enabled = true
        ),
        Usuario(
            id = 3,
            nombre = "Pedro Rodriguez",
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
            nombre = "Carlos Martinez",
            correo = "carlos.martinez@example.com",
            edad = 35,
            rol = Rol.ADMIN,
            enabled = false
        ),
        Usuario(
            id = 6,
            nombre = "Laura Sanchez",
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

    /**
     * StateFlow privado mutable para publicar registros actualizados
     */
    private val _recordsFlow = MutableStateFlow<List<RegistroAcceso>>(emptyList())

    /**
     * Publicador: recordsFlow
     *
     * StateFlow de solo lectura que publica la lista actualizada de registros de acceso.
     * Cada vez que se agrega, modifica o elimina un registro, este Flow emite
     * una nueva lista actualizada.
     *
     * Los suscriptores pueden observar cambios en tiempo real sin necesidad
     * de consultar manualmente el repositorio.
     *
     * @return StateFlow<List<RegistroAcceso>> que emite la lista actualizada de registros
     */
    val recordsFlow: StateFlow<List<RegistroAcceso>> = _recordsFlow.asStateFlow()

    /**
     * NUEVO: Inicializa el repositorio con persistencia local
     * Debe llamarse al inicio de la aplicación (MainActivity.onCreate)
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        registroLocalRepo = RegistroLocalRepository(context)
        isInitialized = true

        // NUEVO: Cargar registros guardados
        val registrosGuardados = registroLocalRepo.cargarRegistros()

        if (registrosGuardados.isNotEmpty()) {
            // Si hay registros guardados, cargarlos
            registrosAcceso.clear()
            registrosAcceso.addAll(registrosGuardados)
        } else {
            // Si no hay registros guardados, agregar ejemplos
            registrosAcceso.add(
                RegistroAcceso(
                    usuario = usuarios[0],
                    accion = AccionAsistencia.ENTRADA,
                    ubicacion = Ubicacion.DentroDelRango("Campus Principal"),
                    marcaTiempo = System.currentTimeMillis() - 3600000
                )
            )
            registrosAcceso.add(
                RegistroAcceso(
                    usuario = usuarios[1],
                    accion = AccionAsistencia.ENTRADA,
                    ubicacion = Ubicacion.DentroDelRango("Edificio A"),
                    marcaTiempo = System.currentTimeMillis() - 7200000
                )
            )
            registrosAcceso.add(
                RegistroAcceso(
                    usuario = usuarios[3],
                    accion = AccionAsistencia.ENTRADA,
                    ubicacion = Ubicacion.FueraDelRango("Edad insuficiente"),
                    marcaTiempo = System.currentTimeMillis() - 1800000
                )
            )
            registrosAcceso.add(
                RegistroAcceso(
                    usuario = usuarios[4],
                    accion = AccionAsistencia.ENTRADA,
                    ubicacion = Ubicacion.DentroDelRango("Campus Principal"),
                    marcaTiempo = System.currentTimeMillis() - 5400000
                )
            )

            // Guardar registros de ejemplo
            if (isInitialized) {
                registroLocalRepo.guardarRegistros(registrosAcceso)
            }
        }

        // Publicar registros iniciales
        _recordsFlow.value = registrosAcceso.toList()
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
     * Publica la lista actualizada a todos los suscriptores de recordsFlow
     * NUEVO: Persiste el registro localmente
     */
    fun agregarRegistroAcceso(registro: RegistroAcceso) {
        registrosAcceso.add(registro)

        // NUEVO: Guardar en persistencia local
        if (isInitialized) {
            registroLocalRepo.guardarRegistros(registrosAcceso)
        }

        // Publicar actualización a los suscriptores
        _recordsFlow.value = registrosAcceso.toList()
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
     * Busca un usuario por correo electrónico (búsqueda exacta)
     * Útil para validar usuarios que inician sesión con Google
     */
    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        return usuarios.find { it.correo.equals(correo, ignoreCase = true) }
    }

    /**
     * Limpia todos los registros de acceso
     * Publica la lista vacía a todos los suscriptores de recordsFlow
     * NUEVO: Limpia también la persistencia local
     */
    fun limpiarRegistros() {
        registrosAcceso.clear()

        // NUEVO: Limpiar persistencia local
        if (isInitialized) {
            registroLocalRepo.limpiarRegistros()
        }

        // Publicar actualización a los suscriptores
        _recordsFlow.value = emptyList()
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