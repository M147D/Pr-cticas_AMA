package com.example.ama_practica09.rating

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ama_practica09.models.Rating
import com.example.ama_practica09.models.RatingCategory
import com.example.ama_practica09.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

/**
 * Pantalla completa para calificar el sistema
 * Permite calificar las 5 categorías simultáneamente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    usuario: Usuario,
    onBack: () -> Unit,
    onRatingSubmitted: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ratingManager = remember { RatingManager(context) }

    // Estados - Ahora usamos un mapa para las 5 categorías
    var ratings by remember {
        mutableStateOf(
            RatingCategory.values().associateWith { 0f }.toMutableMap()
        )
    }
    var comentario by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasRated by remember { mutableStateOf(false) }
    var existingRatings by remember { mutableStateOf<Map<RatingCategory, Rating>>(emptyMap()) }

    // Obtener el UID de Firebase Auth (necesario para las reglas de Firestore)
    val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Verificar si el usuario ya ha calificado (cargar todas las categorías)
    LaunchedEffect(Unit) {
        scope.launch {
            val result = ratingManager.getUserRatings(firebaseUid)
            if (result.isSuccess) {
                val userRatings = result.getOrNull() ?: emptyList()
                if (userRatings.isNotEmpty()) {
                    hasRated = true

                    // Mapear ratings existentes por categoría
                    val ratingsMap = userRatings.associateBy { it.categoria }
                    existingRatings = ratingsMap

                    // Cargar calificaciones existentes
                    RatingCategory.values().forEach { category ->
                        ratingsMap[category]?.let { rating ->
                            ratings[category] = rating.puntuacion
                            // Usar el comentario del primer rating encontrado
                            if (comentario.isEmpty()) {
                                comentario = rating.comentario
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (hasRated) "Actualizar Calificaciones" else "Calificar Sistema",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título descriptivo
                Text(
                    text = if (hasRated)
                        "Ya has calificado el sistema. Puedes actualizar tus calificaciones."
                    else
                        "Califica cada aspecto del sistema",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mostrar las 5 categorías con sus respectivos rating bars
                RatingCategory.values().forEach { category ->
                    CategoryRatingCard(
                        category = category,
                        rating = ratings[category] ?: 0f,
                        onRatingChanged = { newRating ->
                            ratings = ratings.toMutableMap().apply {
                                put(category, newRating)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Campo de comentario general
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Comentarios generales (opcional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Cuéntanos más sobre tu experiencia...") },
                            maxLines = 5
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de envío
                Button(
                    onClick = {
                        // Validar que al menos una categoría tenga calificación
                        val hasAtLeastOneRating = ratings.values.any { it > 0 }

                        if (hasAtLeastOneRating) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    // Crear/actualizar una calificación por cada categoría que tenga rating
                                    val results = mutableListOf<Result<*>>()

                                    ratings.forEach { (category, rating) ->
                                        if (rating > 0) {
                                            val existingRating = existingRatings[category]

                                            val newRating = Rating(
                                                id = existingRating?.id ?: "",
                                                usuarioId = firebaseUid,
                                                usuarioNombre = usuario.nombre,
                                                puntuacion = rating,
                                                comentario = comentario,
                                                fecha = Date(),
                                                categoria = category
                                            )

                                            val result = if (existingRating != null) {
                                                ratingManager.updateRating(newRating)
                                            } else {
                                                ratingManager.submitRating(newRating)
                                            }

                                            results.add(result)
                                        }
                                    }

                                    isLoading = false

                                    // Verificar si todas las operaciones fueron exitosas
                                    if (results.all { it.isSuccess }) {
                                        showSuccessDialog = true
                                    } else {
                                        errorMessage = "Error al enviar algunas calificaciones"
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Error al enviar las calificaciones: ${e.message}"
                                }
                            }
                        } else {
                            errorMessage = "Por favor califica al menos una categoría"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasRated) "Actualizar Calificaciones" else "Enviar Calificaciones",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Mensaje de error
                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Diálogo de éxito
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Gracias!") },
                text = {
                    Text(
                        if (hasRated)
                            "Tus calificaciones han sido actualizadas exitosamente."
                        else
                            "Tus calificaciones han sido enviadas exitosamente. Apreciamos tu feedback."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onRatingSubmitted()
                            onBack()
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

/**
 * Card para mostrar una categoría con su rating bar
 */
@Composable
fun CategoryRatingCard(
    category: RatingCategory,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rating > 0)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            RatingBar(
                rating = rating,
                onRatingChanged = onRatingChanged,
                starSize = 40.dp,
                showLabel = true
            )
        }
    }
}
