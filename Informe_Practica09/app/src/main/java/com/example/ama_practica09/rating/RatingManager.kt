package com.example.ama_practica09.rating

import android.content.Context
import android.util.Log
import com.example.ama_practica09.models.Rating
import com.example.ama_practica09.models.RatingCategory
import com.example.ama_practica09.models.RatingStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Gestor de calificaciones que maneja la lógica de negocio
 * y persistencia de ratings en Firebase Firestore
 */
class RatingManager(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val ratingsCollection = firestore.collection("ratings")

    companion object {
        private const val TAG = "RatingManager"
    }

    /**
     * Guarda una nueva calificación en Firestore
     */
    suspend fun submitRating(rating: Rating): Result<String> {
        return try {
            // DEBUG: Log de autenticación
            val currentUser = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, "=== DEBUG SUBMIT RATING ===")
            Log.d(TAG, "Usuario autenticado: ${currentUser != null}")
            Log.d(TAG, "UID: ${currentUser?.uid ?: "NULL"}")
            Log.d(TAG, "Email: ${currentUser?.email ?: "NULL"}")
            Log.d(TAG, "Rating usuarioId: ${rating.usuarioId}")
            Log.d(TAG, "==========================")
            // Generar ID único si no existe
            val ratingId = rating.id.ifEmpty { UUID.randomUUID().toString() }
            val ratingWithId = rating.copy(id = ratingId)

            // Guardar en Firestore
            ratingsCollection.document(ratingId)
                .set(ratingWithId)
                .await()

            Log.d(TAG, "Rating guardado exitosamente: $ratingId")
            Result.success(ratingId)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar rating", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza una calificación existente
     */
    suspend fun updateRating(rating: Rating): Result<Unit> {
        return try {
            ratingsCollection.document(rating.id)
                .set(rating)
                .await()

            Log.d(TAG, "Rating actualizado: ${rating.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar rating", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el rating de un usuario específico
     */
    suspend fun getUserRating(usuarioId: String): Result<Rating?> {
        return try {
            // DEBUG: Log de autenticación
            val currentUser = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, "=== DEBUG GET USER RATING ===")
            Log.d(TAG, "Usuario autenticado: ${currentUser != null}")
            Log.d(TAG, "UID actual: ${currentUser?.uid ?: "NULL"}")
            Log.d(TAG, "UID buscado: $usuarioId")
            Log.d(TAG, "==============================")
            val snapshot = ratingsCollection
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val rating = if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(Rating::class.java)
            } else {
                null
            }

            Result.success(rating)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener rating del usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los ratings de un usuario específico (uno por categoría)
     */
    suspend fun getUserRatings(usuarioId: String): Result<List<Rating>> {
        return try {
            Log.d(TAG, "=== DEBUG GET USER RATINGS ===")
            Log.d(TAG, "UID buscado: $usuarioId")

            val snapshot = ratingsCollection
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Rating::class.java)
            }

            Log.d(TAG, "Ratings encontrados: ${ratings.size}")
            Result.success(ratings)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ratings del usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las calificaciones
     */
    suspend fun getAllRatings(): Result<List<Rating>> {
        return try {
            val snapshot = ratingsCollection
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Rating::class.java)
            }

            Result.success(ratings)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ratings", e)
            Result.failure(e)
        }
    }

    /**
     * Calcula estadísticas de las calificaciones
     */
    suspend fun calculateStats(): Result<RatingStats> {
        return try {
            val ratingsResult = getAllRatings()
            if (ratingsResult.isFailure) {
                return Result.failure(ratingsResult.exceptionOrNull()!!)
            }

            val ratings = ratingsResult.getOrNull() ?: emptyList()

            if (ratings.isEmpty()) {
                return Result.success(RatingStats())
            }

            // Calcular promedio general
            val promedioGeneral = ratings.map { it.puntuacion }.average().toFloat()

            // Calcular distribución por estrellas
            val distribucion = mutableMapOf(
                1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0
            )
            ratings.forEach { rating ->
                val stars = rating.puntuacion.toInt().coerceIn(1, 5)
                distribucion[stars] = distribucion[stars]!! + 1
            }

            // Calcular promedio por categoría
            val porCategoria = RatingCategory.values().associateWith { categoria ->
                val categoryRatings = ratings.filter { it.categoria == categoria }
                if (categoryRatings.isEmpty()) 0f
                else categoryRatings.map { it.puntuacion }.average().toFloat()
            }

            val stats = RatingStats(
                promedioGeneral = promedioGeneral,
                totalCalificaciones = ratings.size,
                distribucion = distribucion,
                porCategoria = porCategoria
            )

            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular estadísticas", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina una calificación
     */
    suspend fun deleteRating(ratingId: String): Result<Unit> {
        return try {
            ratingsCollection.document(ratingId)
                .delete()
                .await()

            Log.d(TAG, "Rating eliminado: $ratingId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar rating", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si un usuario ya ha calificado
     */
    suspend fun hasUserRated(usuarioId: String): Boolean {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("usuarioId", usuarioId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar si usuario ha calificado", e)
            false
        }
    }
}
