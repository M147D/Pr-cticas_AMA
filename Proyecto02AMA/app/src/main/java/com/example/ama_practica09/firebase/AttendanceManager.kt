package com.example.ama_practica09.firebase

import android.content.Context
import android.util.Log
import com.example.ama_practica09.models.AccionAsistencia
import com.example.ama_practica09.models.RegistroAcceso
import com.example.ama_practica09.models.Rol
import com.example.ama_practica09.models.Ubicacion
import com.example.ama_practica09.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Gestor de asistencias que maneja la lógica de negocio
 * y persistencia de asistencias en Firebase Firestore
 *
 * Similar a RatingManager pero para RegistroAcceso
 */
class AttendanceManager(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val attendanceCollection = firestore.collection("asistencias")

    companion object {
        private const val TAG = "AttendanceManager"
    }

    /**
     * Asegura que el usuario esté autenticado (usando autenticación anónima si es necesario)
     */
    private suspend fun ensureAuthenticated() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.d(TAG, "No hay usuario autenticado, iniciando sesión anónima...")
            FirebaseAuth.getInstance().signInAnonymously().await()
            Log.d(TAG, "Sesión anónima iniciada: ${FirebaseAuth.getInstance().currentUser?.uid}")
        }
    }

    /**
     * Guarda un nuevo registro de asistencia en Firestore
     */
    suspend fun submitAttendance(registro: RegistroAcceso): Result<String> {
        return try {
            // Asegurar que el usuario esté autenticado
            ensureAuthenticated()

            // DEBUG: Log de autenticación
            val currentUser = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, "=== DEBUG SUBMIT ATTENDANCE ===")
            Log.d(TAG, "Usuario autenticado: ${currentUser != null}")
            Log.d(TAG, "UID: ${currentUser?.uid ?: "NULL"}")
            Log.d(TAG, "Usuario registro: ${registro.usuario.nombre}")
            Log.d(TAG, "Acción: ${registro.accion.name}")
            Log.d(TAG, "===============================")

            // Generar ID único
            val registroId = UUID.randomUUID().toString()

            // Convertir a Map para Firestore
            val registroMap = mapOf(
                "id" to registroId,
                "usuarioId" to registro.usuario.id,
                "usuarioNombre" to registro.usuario.nombre,
                "usuarioCorreo" to registro.usuario.correo,
                "usuarioEdad" to registro.usuario.edad,
                "usuarioRol" to registro.usuario.rol.name,
                "usuarioEnabled" to registro.usuario.enabled,
                "accion" to registro.accion.name,
                "ubicacionTipo" to when (registro.ubicacion) {
                    is Ubicacion.DentroDelRango -> "DENTRO"
                    is Ubicacion.FueraDelRango -> "FUERA"
                },
                "ubicacionDescripcion" to when (val ub = registro.ubicacion) {
                    is Ubicacion.DentroDelRango -> ub.descripcion
                    is Ubicacion.FueraDelRango -> ub.razon
                },
                "marcaTiempo" to registro.marcaTiempo,
                "fecha" to com.google.firebase.Timestamp(Date(registro.marcaTiempo)),
                "firebaseUid" to (currentUser?.uid ?: "")
            )

            // Guardar en Firestore
            attendanceCollection.document(registroId)
                .set(registroMap)
                .await()

            Log.d(TAG, "Asistencia guardada exitosamente: $registroId")
            Result.success(registroId)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar asistencia", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las asistencias de un usuario específico
     */
    suspend fun getUserAttendances(usuarioId: Int): Result<List<RegistroAcceso>> {
        return try {
            // Asegurar que el usuario esté autenticado
            ensureAuthenticated()

            Log.d(TAG, "=== DEBUG GET USER ATTENDANCES ===")
            Log.d(TAG, "Usuario ID buscado: $usuarioId")

            val snapshot = attendanceCollection
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)
                .get()
                .await()

            val registros = snapshot.documents.mapNotNull { doc ->
                convertDocumentToRegistro(doc.data)
            }

            Log.d(TAG, "Asistencias encontradas: ${registros.size}")
            Result.success(registros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener asistencias del usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene asistencias de un usuario en un rango de fechas (usando Firebase UID)
     */
    suspend fun getUserAttendancesByDateRangeWithUid(
        firebaseUid: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Result<List<RegistroAcceso>> {
        return try {
            // Asegurar que el usuario esté autenticado
            ensureAuthenticated()

            Log.d(TAG, "=== DEBUG GET ATTENDANCES BY DATE RANGE (Firebase UID) ===")
            Log.d(TAG, "Firebase UID: $firebaseUid")
            Log.d(TAG, "Desde: ${Date(startTimestamp)}")
            Log.d(TAG, "Hasta: ${Date(endTimestamp)}")

            val snapshot = attendanceCollection
                .whereEqualTo("firebaseUid", firebaseUid)
                .whereGreaterThanOrEqualTo("marcaTiempo", startTimestamp)
                .whereLessThanOrEqualTo("marcaTiempo", endTimestamp)
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)
                .get()
                .await()

            val registros = snapshot.documents.mapNotNull { doc ->
                convertDocumentToRegistro(doc.data)
            }

            Log.d(TAG, "Asistencias en rango: ${registros.size}")
            Result.success(registros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener asistencias por rango de fechas", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene asistencias de un usuario en un rango de fechas
     */
    suspend fun getUserAttendancesByDateRange(
        usuarioId: Int,
        startTimestamp: Long,
        endTimestamp: Long
    ): Result<List<RegistroAcceso>> {
        return try {
            // Asegurar que el usuario esté autenticado
            ensureAuthenticated()

            Log.d(TAG, "=== DEBUG GET ATTENDANCES BY DATE RANGE ===")
            Log.d(TAG, "Usuario ID: $usuarioId")
            Log.d(TAG, "Desde: ${Date(startTimestamp)}")
            Log.d(TAG, "Hasta: ${Date(endTimestamp)}")

            val snapshot = attendanceCollection
                .whereEqualTo("usuarioId", usuarioId)
                .whereGreaterThanOrEqualTo("marcaTiempo", startTimestamp)
                .whereLessThanOrEqualTo("marcaTiempo", endTimestamp)
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)
                .get()
                .await()

            val registros = snapshot.documents.mapNotNull { doc ->
                convertDocumentToRegistro(doc.data)
            }

            Log.d(TAG, "Asistencias en rango: ${registros.size}")
            Result.success(registros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener asistencias por rango de fechas", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las asistencias (solo para administradores)
     */
    suspend fun getAllAttendances(): Result<List<RegistroAcceso>> {
        return try {
            val snapshot = attendanceCollection
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)
                .limit(100) // Limitar a 100 registros más recientes
                .get()
                .await()

            val registros = snapshot.documents.mapNotNull { doc ->
                convertDocumentToRegistro(doc.data)
            }

            Result.success(registros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener todas las asistencias", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene asistencias por tipo de acción
     */
    suspend fun getAttendancesByAction(
        usuarioId: Int,
        accion: AccionAsistencia
    ): Result<List<RegistroAcceso>> {
        return try {
            val snapshot = attendanceCollection
                .whereEqualTo("usuarioId", usuarioId)
                .whereEqualTo("accion", accion.name)
                .orderBy("marcaTiempo", Query.Direction.DESCENDING)
                .get()
                .await()

            val registros = snapshot.documents.mapNotNull { doc ->
                convertDocumentToRegistro(doc.data)
            }

            Result.success(registros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener asistencias por acción", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene estadísticas de asistencias
     */
    suspend fun getAttendanceStats(usuarioId: Int): Result<Map<String, Any>> {
        return try {
            val attendancesResult = getUserAttendances(usuarioId)
            if (attendancesResult.isFailure) {
                return Result.failure(attendancesResult.exceptionOrNull()!!)
            }

            val registros = attendancesResult.getOrNull() ?: emptyList()

            val stats = mapOf(
                "total" to registros.size,
                "entradas" to registros.count { it.accion == AccionAsistencia.ENTRADA },
                "salidas" to registros.count { it.accion == AccionAsistencia.SALIDA },
                "dentroDelRango" to registros.count { it.ubicacion.estaDentroDelRango() },
                "fueraDelRango" to registros.count { !it.ubicacion.estaDentroDelRango() },
                "ultimaAsistencia" to (registros.firstOrNull()?.marcaTiempo ?: 0L)
            )

            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estadísticas", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina una asistencia
     */
    suspend fun deleteAttendance(registroId: String): Result<Unit> {
        return try {
            attendanceCollection.document(registroId)
                .delete()
                .await()

            Log.d(TAG, "Asistencia eliminada: $registroId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar asistencia", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza asistencias locales con Firebase
     * Útil para subir registros que se hicieron offline
     */
    suspend fun syncLocalAttendances(registros: List<RegistroAcceso>): Result<Int> {
        return try {
            var syncCount = 0
            registros.forEach { registro ->
                val result = submitAttendance(registro)
                if (result.isSuccess) {
                    syncCount++
                }
            }
            Log.d(TAG, "Sincronizadas $syncCount de ${registros.size} asistencias")
            Result.success(syncCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error al sincronizar asistencias", e)
            Result.failure(e)
        }
    }

    /**
     * Convierte un documento de Firestore a RegistroAcceso
     */
    private fun convertDocumentToRegistro(data: Map<String, Any>?): RegistroAcceso? {
        if (data == null) return null

        return try {
            val usuario = Usuario(
                id = (data["usuarioId"] as? Long)?.toInt() ?: 0,
                nombre = data["usuarioNombre"] as? String ?: "",
                correo = data["usuarioCorreo"] as? String ?: "",
                edad = (data["usuarioEdad"] as? Long)?.toInt() ?: 0,
                rol = Rol.valueOf(data["usuarioRol"] as? String ?: "USER"),
                enabled = data["usuarioEnabled"] as? Boolean ?: true
            )

            val accion = AccionAsistencia.valueOf(data["accion"] as? String ?: "ENTRADA")

            val ubicacion = when (data["ubicacionTipo"] as? String) {
                "DENTRO" -> Ubicacion.DentroDelRango(
                    data["ubicacionDescripcion"] as? String ?: ""
                )
                "FUERA" -> Ubicacion.FueraDelRango(
                    data["ubicacionDescripcion"] as? String ?: ""
                )
                else -> Ubicacion.FueraDelRango("Desconocido")
            }

            val marcaTiempo = data["marcaTiempo"] as? Long ?: System.currentTimeMillis()

            RegistroAcceso(
                usuario = usuario,
                accion = accion,
                ubicacion = ubicacion,
                marcaTiempo = marcaTiempo
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a RegistroAcceso", e)
            null
        }
    }
}
