package com.example.ama_practica09.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica09.data.UsuarioRepository
import com.example.ama_practica09.models.Rol
import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.session.SessionManager
import com.example.ama_practica09.session.SessionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * Utilidades para disparar animaciones basadas en cambios de datos
 * Monitorea DataStore, SharedPreferences y Firebase Firestore
 */

/**
 * Composable genérico que observa cambios en un Flow y ejecuta un callback
 */
@Composable
fun <T> AnimateOnDataChange(
    dataFlow: Flow<T>,
    initialValue: T? = null,
    onDataChanged: (T) -> Unit
) {
    val data by dataFlow.collectAsState(initial = initialValue)

    LaunchedEffect(data) {
        data?.let { onDataChanged(it) }
    }
}

/**
 * Monitorea cambios en la sesión y ejecuta callbacks
 */
@Composable
fun AnimateOnSessionChange(
    sessionManager: SessionManager,
    onSessionActive: () -> Unit = {},
    onSessionInactive: () -> Unit = {},
    onSessionExpired: () -> Unit = {}
) {
    val sessionState by sessionManager.sessionState.collectAsState()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.Active -> onSessionActive()
            is SessionState.Inactive -> onSessionInactive()
            is SessionState.Error -> onSessionExpired()
            else -> {}
        }
    }
}

/**
 * Composable que muestra una notificación animada cuando se detecta un nuevo registro
 * en el repositorio de asistencias
 */
@Composable
fun FirestoreNewRegistrationNotification(
    usuario: Usuario,
    modifier: Modifier = Modifier
) {
    val registros by UsuarioRepository.recordsFlow.collectAsState()
    var lastCount by remember { mutableIntStateOf(0) }
    var showNotification by remember { mutableStateOf(false) }

    // Detectar nuevos registros
    LaunchedEffect(registros.size) {
        if (registros.size > lastCount && lastCount > 0) {
            // Nuevo registro detectado
            showNotification = true
            delay(2500) // Mostrar por 2.5 segundos
            showNotification = false
        }
        lastCount = registros.size
    }

    // Notificación animada
    AnimatedVisibility(
        visible = showNotification,
        enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
        exit = scaleOut(animationSpec = tween(300)) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "¡Nueva asistencia registrada!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Composable que muestra contadores animados cuando cambian los datos
 */
@Composable
fun AnimatedDataCounter(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    var previousCount by remember { mutableIntStateOf(count) }
    var shouldAnimate by remember { mutableStateOf(false) }

    // Detectar cambios en el contador
    LaunchedEffect(count) {
        if (count != previousCount) {
            shouldAnimate = true
            delay(300)
            shouldAnimate = false
            previousCount = count
        }
    }

    // Animación de escala cuando cambia
    val scale by animateFloatAsState(
        targetValue = if (shouldAnimate) 1.2f else 1.0f,
        animationSpec = tween(300),
        label = "counterScale"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (shouldAnimate)
                Color(0xFF4CAF50).copy(alpha = 0.2f)
            else
                Color.Transparent
        )
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "$label: $count",
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
    }
}

/**
 * Monitor de cambios en el estado del usuario
 */
@Composable
fun AnimateOnUserStateChange(
    usuario: Usuario,
    onEnabledChanged: (Boolean) -> Unit = {},
    onRoleChanged: (String) -> Unit = {}
) {
    var previousEnabled by remember { mutableStateOf(usuario.enabled) }
    var previousRole by remember { mutableStateOf(usuario.rol) }

    LaunchedEffect(usuario.enabled) {
        if (usuario.enabled != previousEnabled) {
            onEnabledChanged(usuario.enabled)
            previousEnabled = usuario.enabled
        }
    }

    LaunchedEffect(usuario.rol) {
        if (usuario.rol != previousRole) {
            onRoleChanged(usuario.rol.name)
            previousRole = usuario.rol
        }
    }
}
