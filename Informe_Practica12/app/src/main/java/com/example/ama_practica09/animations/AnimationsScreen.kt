package com.example.ama_practica09.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica09.models.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla principal de demostración de animaciones
 * Contiene 3 tabs: Componentes, Transiciones, Secuencias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationsScreen(
    usuario: Usuario,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animaciones del Sistema") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row para seleccionar categoría
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Componentes") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Transiciones") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Secuencias") }
                )
            }

            // Contenido basado en tab seleccionado
            when (selectedTab) {
                0 -> ComponentAnimationsTab()
                1 -> TransitionAnimationsTab()
                2 -> SequenceAnimationsTab()
            }
        }
    }
}

// ===============================================
// TAB 1: ANIMACIONES DE COMPONENTES
// ===============================================

@Composable
fun ComponentAnimationsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Animaciones de Componentes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Demostraciones de animaciones sobre botones, tarjetas e iconos usando Jetpack Compose.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sección de botones animados
        ButtonAnimationsSection()

        Spacer(modifier = Modifier.height(8.dp))

        // Sección de cards animadas
        CardAnimationsSection()

        Spacer(modifier = Modifier.height(8.dp))

        // Sección de iconos animados
        IconAnimationsSection()
    }
}

@Composable
fun ButtonAnimationsSection() {
    var isScaleActive by remember { mutableStateOf(false) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var isSuccess by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Botones Animados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Botón con animación de escala
            val scale by animateFloatAsState(
                targetValue = if (isScaleActive) 1.2f else 1.0f,
                animationSpec = tween(300),
                label = "scale"
            )

            Button(
                onClick = { isScaleActive = !isScaleActive },
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Text("Pulsar para Animar (Scale)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón con animación de rotación
            val animatedRotation by animateFloatAsState(
                targetValue = rotationDegrees,
                animationSpec = tween(600),
                label = "rotation"
            )

            Button(
                onClick = { rotationDegrees += 360f },
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationZ = animatedRotation
                    }
            ) {
                Text("Pulsar para Rotar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón con animación de color
            val buttonColor by animateColorAsState(
                targetValue = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                animationSpec = tween(400),
                label = "color"
            )

            Button(
                onClick = { isSuccess = !isSuccess },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(if (isSuccess) "✓ Éxito" else "Cambiar Estado")
            }
        }
    }
}

@Composable
fun CardAnimationsSection() {
    var isExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tarjetas Animadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Card expandible
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(300)),
                onClick = { isExpanded = !isExpanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Tarjeta Expandible",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Este contenido se muestra cuando la tarjeta está expandida. " +
                                    "La animación es suave gracias a animateContentSize().",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Controles de fade in/out
            Button(
                onClick = { isVisible = !isVisible },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isVisible) "Ocultar Tarjeta" else "Mostrar Tarjeta")
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(600)) + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "Tarjeta con animación Fade In/Out",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun IconAnimationsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Iconos Animados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Icono pulsante
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Corazón pulsante",
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            },
                        tint = Color.Red
                    )
                    Text("Pulsante", fontSize = 12.sp)
                }

                // Icono rotante
                val rotatingAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Estrella rotante",
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                rotationZ = rotatingAngle
                            },
                        tint = Color(0xFFFFD700)
                    )
                    Text("Rotante", fontSize = 12.sp)
                }

                // Icono con fade
                val fadeAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "fade"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Icono fade",
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                alpha = fadeAlpha
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Fade", fontSize = 12.sp)
                }
            }
        }
    }
}

// ===============================================
// TAB 2: TRANSICIONES DE PANTALLA
// ===============================================

@Composable
fun TransitionAnimationsTab() {
    var currentDemo by remember { mutableStateOf("fade") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Transiciones de Pantalla",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Demostraciones de transiciones Fade, Slide y Scale entre contenidos.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de tipo de transición
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentDemo == "fade",
                onClick = { currentDemo = "fade" },
                label = { Text("Fade") }
            )
            FilterChip(
                selected = currentDemo == "slide",
                onClick = { currentDemo = "slide" },
                label = { Text("Slide") }
            )
            FilterChip(
                selected = currentDemo == "scale",
                onClick = { currentDemo = "scale" },
                label = { Text("Scale") }
            )
        }

        // Demo con transiciones
        AnimatedContent(
            targetState = currentDemo,
            transitionSpec = {
                when (targetState) {
                    "fade" -> fadeIn(tween(600)) togetherWith fadeOut(tween(600))
                    "slide" -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    "scale" -> (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                    else -> fadeIn() togetherWith fadeOut()
                }
            },
            label = "demoTransition"
        ) { demo ->
            DemoCard(demo)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Estas transiciones se aplican automáticamente al navegar entre pantallas.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun DemoCard(demoType: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (demoType) {
                "fade" -> MaterialTheme.colorScheme.primaryContainer
                "slide" -> MaterialTheme.colorScheme.secondaryContainer
                "scale" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (demoType) {
                        "fade" -> "Fade Transition"
                        "slide" -> "Slide Transition"
                        "scale" -> "Scale Transition"
                        else -> ""
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (demoType) {
                        "fade" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "slide" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "scale" -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> Color.Black
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (demoType) {
                        "fade" -> "Desvanecimiento suave"
                        "slide" -> "Deslizamiento horizontal"
                        "scale" -> "Escalado con desvanecimiento"
                        else -> ""
                    },
                    color = Color.Gray
                )
            }
        }
    }
}

// ===============================================
// TAB 3: SECUENCIAS ANIMADAS
// ===============================================

@Composable
fun SequenceAnimationsTab() {
    var isPlaying by remember { mutableStateOf(false) }
    val animationController = remember { RegistrationAnimationController() }
    val animationState by animationController.animationState
    val scope = rememberCoroutineScope()

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animationController.triggerSuccessSequence()
            delay(2000)
            animationController.reset()
            isPlaying = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Secuencias Animadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Esta es la secuencia que se ejecuta cuando registras una asistencia exitosamente:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Timeline de la secuencia
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Timeline de Animación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                SequenceStep(
                    number = 1,
                    title = "Pulso del Botón",
                    description = "El botón aumenta de tamaño momentáneamente",
                    isActive = animationState is RegistrationAnimationController.AnimationState.ButtonPulse
                )

                SequenceStep(
                    number = 2,
                    title = "Icono de Éxito",
                    description = "Aparece un icono de confirmación (✓)",
                    isActive = animationState is RegistrationAnimationController.AnimationState.SuccessIcon
                )

                SequenceStep(
                    number = 3,
                    title = "Mensaje de Confirmación",
                    description = "Se muestra el mensaje con fade in",
                    isActive = animationState is RegistrationAnimationController.AnimationState.MessageFadeIn
                )

                SequenceStep(
                    number = 4,
                    title = "Completado",
                    description = "La animación finaliza",
                    isActive = animationState is RegistrationAnimationController.AnimationState.Complete
                )
            }
        }

        // Botón para reproducir
        Button(
            onClick = { isPlaying = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPlaying
        ) {
            Text(if (isPlaying) "Reproduciendo..." else "Reproducir Secuencia")
        }

        // Demo visual
        AnimatedVisibility(
            visible = isPlaying,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (animationState) {
                        is RegistrationAnimationController.AnimationState.ButtonPulse -> {
                            Text(
                                "Botón pulsando...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is RegistrationAnimationController.AnimationState.SuccessIcon -> {
                            Text("✓", fontSize = 64.sp, color = Color(0xFF4CAF50))
                        }
                        is RegistrationAnimationController.AnimationState.MessageFadeIn -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✓", fontSize = 48.sp, color = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Asistencia registrada", fontSize = 18.sp)
                            }
                        }
                        is RegistrationAnimationController.AnimationState.Complete -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✓", fontSize = 48.sp, color = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Completado",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "💡 Información",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Esta secuencia se ejecuta automáticamente cuando registras tu asistencia " +
                            "desde la pantalla de Registro de Asistencia. Cada paso dura entre 300-800ms " +
                            "para crear una experiencia fluida y agradable.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SequenceStep(
    number: Int,
    title: String,
    description: String,
    isActive: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "stepBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
