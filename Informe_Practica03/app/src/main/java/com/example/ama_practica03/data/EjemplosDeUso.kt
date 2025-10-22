package com.example.ama_practica03.data

import com.example.ama_practica03.models.*
import com.example.ama_practica03.rules.AccessRules
import com.example.ama_practica03.rules.ValidationRules

/**
 * Objeto que contiene ejemplos de uso de los modelos, rules y repositorio
 * Este archivo sirve como guía de referencia para implementar las funcionalidades
 */
object EjemplosDeUso {

    /**
     * Ejemplo 1: Uso básico de modelos
     */
    fun ejemploModelosBasicos(): String {
        val usuario = Usuario(
            id = 100,
            nombre = "Ejemplo Usuario",
            correo = "ejemplo@test.com",
            edad = 25,
            rol = Rol.USER,
            enabled = true
        )

        val ubicacion = Ubicacion.DentroDelRango("Campus Central")
        val accion = AccionAsistencia.ENTRADA

        val registro = RegistroAcceso(
            usuario = usuario,
            accion = accion,
            ubicacion = ubicacion
        )

        return """
            === EJEMPLO 1: MODELOS BÁSICOS ===

            ${usuario.formatearInfo()}

            Ubicación: ${ubicacion.formatear()}
            Acción: ${accion.formatear()}

            ${registro.formatear()}
        """.trimIndent()
    }

    /**
     * Ejemplo 2: Uso de ValidationRules con lambdas
     */
    fun ejemploValidationRules(): String {
        val usuarios = UsuarioRepository.obtenerTodosLosUsuarios()

        // Filtrar usuarios activos
        val usuariosActivos = ValidationRules.filtrarUsuarios(usuarios, ValidationRules.estaActivo)

        // Filtrar usuarios mayores de edad
        val mayoresDeEdad = ValidationRules.filtrarUsuarios(usuarios, ValidationRules.esMayorDeEdad)

        // Filtrar administradores
        val admins = ValidationRules.filtrarUsuarios(usuarios, ValidationRules.esAdministrador)

        // Combinar validaciones: usuarios activos Y mayores de edad
        val usuariosValidos = ValidationRules.filtrarUsuarios(
            usuarios,
            ValidationRules.combinarValidacionesAND(
                ValidationRules.estaActivo,
                ValidationRules.esMayorDeEdad
            )
        )

        // Transformar usuarios a solo nombres
        val nombres = ValidationRules.transformarUsuarios(usuarios) { it.nombre }

        return """
            === EJEMPLO 2: VALIDATION RULES ===

            Total usuarios: ${usuarios.size}
            Usuarios activos: ${usuariosActivos.size}
            Mayores de edad: ${mayoresDeEdad.size}
            Administradores: ${admins.size}
            Usuarios válidos (activos Y mayores): ${usuariosValidos.size}

            Lista de nombres:
            ${nombres.joinToString("\n") { "- $it" }}
        """.trimIndent()
    }

    /**
     * Ejemplo 3: Uso de AccessRules para registros
     */
    fun ejemploAccessRules(): String {
        val registros = UsuarioRepository.obtenerTodosLosRegistros()

        // Filtrar registros válidos
        val registrosValidos = AccessRules.filtrarRegistros(registros, AccessRules.esRegistroValido)

        // Filtrar solo entradas
        val entradas = AccessRules.filtrarRegistros(registros, AccessRules.esEntrada)

        // Filtrar registros que requieren atención
        val requierenAtencion = AccessRules.filtrarRegistros(registros, AccessRules.requiereAtencion)

        // Generar reporte de entradas válidas
        val reporteEntradas = AccessRules.generarReporte(
            registros,
            AccessRules.entradaValida
        ) { it.resumen() }

        // Contar por criterios
        val conteos = AccessRules.contarPorCriterios(
            registros,
            mapOf(
                "Entradas" to AccessRules.esEntrada,
                "Salidas" to AccessRules.esSalida,
                "Válidos" to AccessRules.esRegistroValido,
                "Dentro del rango" to AccessRules.estaDentroDelRango
            )
        )

        return """
            === EJEMPLO 3: ACCESS RULES ===

            Total registros: ${registros.size}
            Registros válidos: ${registrosValidos.size}
            Entradas: ${entradas.size}
            Requieren atención: ${requierenAtencion.size}

            Conteos por criterio:
            ${conteos.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }}

            Reporte de entradas válidas:
            $reporteEntradas
        """.trimIndent()
    }

    /**
     * Ejemplo 4: Uso del UsuarioRepository
     */
    fun ejemploRepositorio(): String {
        val repo = UsuarioRepository

        // Obtener estadísticas
        val estadisticas = repo.obtenerEstadisticas()
        val estadisticasRegistros = repo.obtenerEstadisticasRegistros()

        // Obtener diferentes tipos de usuarios
        val activos = repo.obtenerUsuariosActivos()
        val admins = repo.obtenerAdministradores()

        // Buscar usuarios
        val busqueda = repo.buscarUsuariosPorNombre("Garcia")

        return """
            === EJEMPLO 4: REPOSITORY ===

            Estadísticas de Usuarios:
            ${estadisticas.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }}

            Estadísticas de Registros:
            ${estadisticasRegistros.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }}

            Usuarios activos: ${activos.size}
            Administradores: ${admins.size}

            Búsqueda "Garcia": ${busqueda.size} resultados
            ${busqueda.joinToString("\n") { "- ${it.nombre}" }}
        """.trimIndent()
    }

    /**
     * Ejemplo 5: Funciones de orden superior personalizadas
     */
    fun ejemploFuncionesOrdenSuperior(): String {
        val usuarios = UsuarioRepository.obtenerTodosLosUsuarios()

        // Contar usuarios jóvenes
        val jovenes = ValidationRules.contarPorCriterio(usuarios, ValidationRules.esJoven)

        // Agrupar usuarios por rol
        val porRol = ValidationRules.agruparUsuarios(usuarios) { it.rol }

        // Ordenar usuarios por edad
        val ordenadosPorEdad = ValidationRules.ordenarUsuarios(usuarios) { it.edad }

        // Crear una regla personalizada: usuario válido O administrador
        val reglaPersonalizada = ValidationRules.combinarValidacionesOR(
            ValidationRules.usuarioValido,
            ValidationRules.esAdministrador
        )
        val usuariosConRegla = usuarios.filter(reglaPersonalizada)

        return """
            === EJEMPLO 5: FUNCIONES DE ORDEN SUPERIOR ===

            Usuarios jóvenes (< 25 años): $jovenes

            Agrupación por rol:
            ${porRol.entries.joinToString("\n") { "- ${it.key}: ${it.value.size} usuarios" }}

            Usuarios ordenados por edad:
            ${ordenadosPorEdad.joinToString("\n") { "- ${it.nombre}: ${it.edad} años" }}

            Con regla personalizada (válido O admin): ${usuariosConRegla.size}
        """.trimIndent()
    }

    /**
     * Ejemplo 6: Crear y registrar nuevo acceso
     */
    fun ejemploCrearRegistro(): String {
        val usuario = UsuarioRepository.obtenerUsuarioPorId(1)

        if (usuario != null) {
            // Crear nuevo registro de salida
            val nuevoRegistro = RegistroAcceso(
                usuario = usuario,
                accion = AccionAsistencia.SALIDA,
                ubicacion = Ubicacion.DentroDelRango("Salida Principal"),
                marcaTiempo = System.currentTimeMillis()
            )

            // Validar antes de agregar
            val esValido = AccessRules.validarRegistro(nuevoRegistro, AccessRules.esRegistroValido)

            if (esValido) {
                UsuarioRepository.agregarRegistroAcceso(nuevoRegistro)
                return """
                    === EJEMPLO 6: CREAR REGISTRO ===

                    ✓ Registro creado exitosamente

                    ${nuevoRegistro.formatear()}
                """.trimIndent()
            } else {
                return """
                    === EJEMPLO 6: CREAR REGISTRO ===

                    ✗ Registro inválido
                    Razón: Usuario no puede acceder o ubicación incorrecta
                """.trimIndent()
            }
        }

        return "Error: Usuario no encontrado"
    }

    /**
     * Ejemplo 7: Procesar registros con diferentes acciones
     */
    fun ejemploProcesarRegistros(): String {
        val registros = UsuarioRepository.obtenerTodosLosRegistros()
        val resultado = StringBuilder()

        resultado.appendLine("=== EJEMPLO 7: PROCESAR REGISTROS ===")
        resultado.appendLine()

        // Procesar cada registro según su acción
        registros.forEach { registro ->
            AccessRules.procesarSegunAccion(
                registro,
                alEntrada = { r ->
                    resultado.appendLine("→ ENTRADA: ${r.usuario.nombre} a las ${r.obtenerFechaFormateada()}")
                },
                alSalida = { r ->
                    resultado.appendLine("← SALIDA: ${r.usuario.nombre} a las ${r.obtenerFechaFormateada()}")
                }
            )
        }

        resultado.appendLine()
        resultado.appendLine("Procesamiento completado: ${registros.size} registros")

        return resultado.toString()
    }

    /**
     * Genera todos los ejemplos
     */
    fun generarTodosLosEjemplos(): String {
        return """
            ${ejemploModelosBasicos()}

            ═══════════════════════════════════════════

            ${ejemploValidationRules()}

            ═══════════════════════════════════════════

            ${ejemploAccessRules()}

            ═══════════════════════════════════════════

            ${ejemploRepositorio()}

            ═══════════════════════════════════════════

            ${ejemploFuncionesOrdenSuperior()}

            ═══════════════════════════════════════════

            ${ejemploCrearRegistro()}

            ═══════════════════════════════════════════

            ${ejemploProcesarRegistros()}
        """.trimIndent()
    }
}
