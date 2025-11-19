package com.example.ama_practica07.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ama_practica07.models.AccionAsistencia
import com.example.ama_practica07.models.RegistroAcceso
import com.example.ama_practica07.models.Rol
import com.example.ama_practica07.models.Ubicacion
import com.example.ama_practica07.models.Usuario
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repositorio para persistir registros de asistencia localmente
 * Usa SharedPreferences para almacenamiento simple y persistente
 */
class RegistroLocalRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "registros_asistencia",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_REGISTROS = "registros_json"
    }

    /**
     * Guarda todos los registros en SharedPreferences
     */
    fun guardarRegistros(registros: List<RegistroAcceso>) {
        val jsonArray = JSONArray()

        registros.forEach { registro ->
            val jsonObject = JSONObject().apply {
                // Usuario
                put("usuarioId", registro.usuario.id)
                put("usuarioNombre", registro.usuario.nombre)
                put("usuarioCorreo", registro.usuario.correo)
                put("usuarioEdad", registro.usuario.edad)
                put("usuarioRol", registro.usuario.rol.name)
                put("usuarioEnabled", registro.usuario.enabled)

                // Acción
                put("accion", registro.accion.name)

                // Ubicación (sealed class)
                when (val ubicacion = registro.ubicacion) {
                    is Ubicacion.DentroDelRango -> {
                        put("ubicacionTipo", "DENTRO")
                        put("ubicacionDescripcion", ubicacion.descripcion)
                    }
                    is Ubicacion.FueraDelRango -> {
                        put("ubicacionTipo", "FUERA")
                        put("ubicacionRazon", ubicacion.razon)
                    }
                }

                // Timestamp
                put("marcaTiempo", registro.marcaTiempo)
            }
            jsonArray.put(jsonObject)
        }

        prefs.edit().putString(KEY_REGISTROS, jsonArray.toString()).apply()
    }

    /**
     * Carga todos los registros desde SharedPreferences
     */
    fun cargarRegistros(): List<RegistroAcceso> {
        val jsonString = prefs.getString(KEY_REGISTROS, "[]") ?: "[]"
        val registros = mutableListOf<RegistroAcceso>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)

                // Reconstruir Usuario
                val usuario = Usuario(
                    id = json.getInt("usuarioId"),
                    nombre = json.getString("usuarioNombre"),
                    correo = json.getString("usuarioCorreo"),
                    edad = json.getInt("usuarioEdad"),
                    rol = Rol.valueOf(json.getString("usuarioRol")),
                    enabled = json.getBoolean("usuarioEnabled")
                )

                // Reconstruir Acción
                val accion = AccionAsistencia.valueOf(json.getString("accion"))

                // Reconstruir Ubicación (sealed class)
                val ubicacion = when (json.getString("ubicacionTipo")) {
                    "DENTRO" -> Ubicacion.DentroDelRango(
                        descripcion = json.getString("ubicacionDescripcion")
                    )
                    "FUERA" -> Ubicacion.FueraDelRango(
                        razon = json.getString("ubicacionRazon")
                    )
                    else -> Ubicacion.FueraDelRango("Ubicación desconocida")
                }

                // Reconstruir RegistroAcceso
                val registro = RegistroAcceso(
                    usuario = usuario,
                    accion = accion,
                    ubicacion = ubicacion,
                    marcaTiempo = json.getLong("marcaTiempo")
                )

                registros.add(registro)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Si hay error, retornar lista vacía
        }

        return registros
    }

    /**
     * Agrega un nuevo registro a la lista existente y guarda
     */
    fun agregarRegistro(registro: RegistroAcceso) {
        val registrosActuales = cargarRegistros().toMutableList()
        registrosActuales.add(registro)
        guardarRegistros(registrosActuales)
    }

    /**
     * Limpia todos los registros (útil para logout completo)
     */
    fun limpiarRegistros() {
        prefs.edit().remove(KEY_REGISTROS).apply()
    }

    /**
     * Obtiene registros filtrados por usuario
     */
    fun obtenerRegistrosPorUsuario(usuarioId: Int): List<RegistroAcceso> {
        return cargarRegistros().filter { it.usuario.id == usuarioId }
    }
}
