package com.example.ama_practica09

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica09.data.UsuarioRepository
import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.models.AccionAsistencia
import com.example.ama_practica09.models.RegistroAcceso
import com.example.ama_practica09.models.isEnabled
import com.example.ama_practica09.models.isAdmin
import com.example.ama_practica09.models.displayName
import com.example.ama_practica09.models.formatear
import com.example.ama_practica09.rules.PolicyRules
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.reversed
import kotlin.collections.take
import kotlin.ranges.rangeTo
import kotlin.text.equals
import kotlin.text.isBlank
import kotlin.text.isNotEmpty
import com.example.ama_practica09.animations.RegistrationAnimationController
import com.example.ama_practica09.location.GeofenceNotificationManager
import com.example.ama_practica09.location.LocationManager
import com.example.ama_practica09.location.LocationState
import com.example.ama_practica09.location.GeofenceStatus
import com.example.ama_practica09.flow.LocationSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.rotate

// ==================== PANTALLA DE LOGIN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (Usuario) -> Unit,
    onBack: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}  // NUEVO: Callback para ir a registro
) {
    val usuarios = remember { UsuarioRepository.obtenerTodosLosUsuarios() }

    var nombreUsuario by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("")}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sistema de Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Login",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SISTEMA DE ASISTENCIA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresa tu nombre de usuario",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de texto con autocomplete
        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = {
                nombreUsuario = it
                mensajeError = ""
            },
            label = { Text("Nombre de usuario") },
            placeholder = { Text("Ej: Juan Pérez, María García...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de ingresar
        Button(
            onClick = {
                if (nombreUsuario.isBlank()) {
                    mensajeError = "Por favor ingresa tu nombre de usuario"
                    return@Button
                }

                // Buscar usuario exacto (case-insensitive)
                val usuarioEncontrado = usuarios.filter { it.isEnabled() }.find { usuario ->
                    usuario.nombre.equals(nombreUsuario, ignoreCase = true)
                }

                if (usuarioEncontrado != null) {
                    // NUEVO: Autenticar en Firebase de forma anónima para Firestore
                    val auth = FirebaseAuth.getInstance()
                    auth.signInAnonymously()
                        .addOnSuccessListener {
                            android.util.Log.d("LoginScreen", "Usuario autenticado en Firebase (anónimo)")
                            onLoginSuccess(usuarioEncontrado)
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("LoginScreen", "Error al autenticar en Firebase", e)
                            // Continuar con el login local aunque falle Firebase
                            onLoginSuccess(usuarioEncontrado)
                        }
                } else {
                    mensajeError = "Usuario no encontrado o inactivo. Por favor verifica el nombre."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("INGRESAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divisor
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "O",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Google Sign-In
        OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Icono de Google (simplificado)
                Text(
                    text = "G",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Iniciar sesión con Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Mensaje de error
        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                ),
                border = BorderStroke(2.dp, Color(0xFFF44336))
            ) {
                Text(
                    text = mensajeError,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Botón para crear cuenta
        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "¿No tienes cuenta? Créala aquí",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        }
    }
}

// ==================== PANTALLA DE USUARIO ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    usuario: Usuario,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // ViewModel con estado reactivo
    val viewModel = remember {
        com.example.ama_practica09.viewmodel.RegistroViewModel(initialUsuario = usuario)
    }

    // SUSCRIPTOR: Observar StateFlow<AppState>
    val appState by viewModel.appState.collectAsState()

    // Estados locales de UI
    var accionSeleccionada by remember { mutableStateOf(AccionAsistencia.ENTRADA) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Gateway de notificaciones locales
    val notificationGateway = remember {
        com.example.ama_practica09.flow.NotificationGatewayLocal(context)
    }

    // NUEVO: Gestor de notificaciones de geovallas
    val geofenceNotificationManager = remember {
        GeofenceNotificationManager(context)
    }

    // Controlador de animación para registro
    val animationController = remember { RegistrationAnimationController() }
    val animationState by animationController.animationState
    val scope = rememberCoroutineScope()

    // NUEVO: LocationManager para GPS real
    val locationManager = remember { LocationManager(context) }
    val locationState by locationManager.locationState.collectAsState()
    val currentLocation by locationManager.currentLocation.collectAsState()

    // NUEVO: Modo de ubicación (GPS o Manual)
    var usarGPS by remember { mutableStateOf(true) }

    // NUEVO: Iniciar actualizaciones GPS si tiene permisos
    LaunchedEffect(usarGPS) {
        if (usarGPS && locationManager.hasLocationPermission()) {
            locationManager.startLocationUpdates()
        }
    }

    // NUEVO: Sincronizar ubicación GPS con LocationSource
    LaunchedEffect(locationState) {
        if (usarGPS && locationState is LocationState.Available) {
            val available = locationState as LocationState.Available
            LocationSource.updateFromGPS(available.geoPoint, available.geofenceStatus)
        }
    }

    // Limpiar al salir
    DisposableEffect(Unit) {
        onDispose {
            locationManager.stopLocationUpdates()
        }
    }

    // SUSCRIPTOR: Observar SharedFlow<AppEvent> para eventos
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            // Procesar evento en el gateway de notificaciones
            notificationGateway.procesarEvento(event)

            // Además manejar eventos de UI (Snackbar/Toast) y animaciones
            when (event) {
                is com.example.ama_practica09.flow.AppEvent.RegistroExitoso -> {
                    // Trigger animación de éxito
                    scope.launch {
                        animationController.triggerSuccessSequence()
                    }

                    // NUEVO: Enviar notificación de asistencia registrada
                    val geofenceStatus = com.example.ama_practica09.flow.LocationSource.getCurrentGeofenceStatus()
                    val buildingName = geofenceStatus.getActiveZoneName()
                    val isEntry = accionSeleccionada == AccionAsistencia.ENTRADA

                    // Solo notificar si está en un edificio (no en Campus general)
                    if (geofenceStatus.canRegisterAttendance()) {
                        geofenceNotificationManager.onAttendanceRegistered(
                            buildingName = buildingName,
                            userName = usuario.nombre,
                            isEntry = isEntry
                        )
                    }
                }
                is com.example.ama_practica09.flow.AppEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                }
                is com.example.ama_practica09.flow.AppEvent.ShowToast -> {
                    android.widget.Toast.makeText(
                        context,
                        event.message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // Otros eventos ya se procesan en notificationGateway
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Asistencia") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Volver al inicio")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Información del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bienvenido/a",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = usuario.displayName(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = usuario.correo,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Edad: ${usuario.edad} años", fontSize = 12.sp)
                        Text(
                            text = if (usuario.isEnabled()) "Activo" else "Inactivo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (usuario.isEnabled()) Color(0xFF4CAF50) else Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de registro
            Text(
                text = "REGISTRAR ASISTENCIA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de acción
            Text(text = "Tipo de registro:", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { accionSeleccionada = AccionAsistencia.ENTRADA },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (accionSeleccionada == AccionAsistencia.ENTRADA)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray
                    )
                ) {
                    Text("→ ENTRADA")
                }

                Button(
                    onClick = { accionSeleccionada = AccionAsistencia.SALIDA },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (accionSeleccionada == AccionAsistencia.SALIDA)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray
                    )
                ) {
                    Text("← SALIDA")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de modo de ubicación (GPS / Manual)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Ubicación:", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "GPS", fontSize = 12.sp)
                    Switch(
                        checked = usarGPS,
                        onCheckedChange = { usarGPS = it }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Card de estado GPS (si está activo)
            if (usarGPS) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (locationState) {
                            is LocationState.Available -> {
                                val status = (locationState as LocationState.Available).geofenceStatus
                                when {
                                    status.isInsideMecanica -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                                    status.isInsideEdificioSecundario -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    status.isInsideEdificioPrincipal -> Color(0xFF2196F3).copy(alpha = 0.1f)
                                    status.isInsideCampus -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                    else -> Color(0xFFF44336).copy(alpha = 0.1f)
                                }
                            }
                            is LocationState.Loading -> Color.Gray.copy(alpha = 0.1f)
                            else -> Color(0xFFF44336).copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            tint = when (locationState) {
                                is LocationState.Available -> {
                                    val status = (locationState as LocationState.Available).geofenceStatus
                                    when {
                                        status.isInsideMecanica -> Color(0xFF9C27B0)
                                        status.isInsideEdificioSecundario -> Color(0xFF4CAF50)
                                        status.isInsideEdificioPrincipal -> Color(0xFF2196F3)
                                        status.isInsideCampus -> Color(0xFFFF9800)
                                        else -> Color(0xFFF44336)
                                    }
                                }
                                else -> Color.Gray
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            when (val state = locationState) {
                                is LocationState.Available -> {
                                    val status = state.geofenceStatus
                                    Text(
                                        text = status.getActiveZoneName(),
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            status.isInsideMecanica -> Color(0xFF9C27B0)
                                            status.isInsideEdificioSecundario -> Color(0xFF4CAF50)
                                            status.isInsideEdificioPrincipal -> Color(0xFF2196F3)
                                            status.isInsideCampus -> Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                                    )
                                    Text(
                                        text = if (status.canRegisterAttendance())
                                            "Puede registrar asistencia"
                                        else
                                            "Fuera de zona de registro",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    currentLocation?.let { loc ->
                                        Text(
                                            text = "Precisión: ${loc.accuracy.toInt()}m",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                is LocationState.Loading -> {
                                    Text(text = "Obteniendo ubicación GPS...")
                                }
                                is LocationState.PermissionRequired -> {
                                    Text(
                                        text = "Se requieren permisos de ubicación",
                                        color = Color(0xFFF44336)
                                    )
                                }
                                is LocationState.Error -> {
                                    Text(
                                        text = "Error: ${state.message}",
                                        color = Color(0xFFF44336)
                                    )
                                }
                                is LocationState.Unavailable -> {
                                    Text(
                                        text = "GPS no disponible",
                                        color = Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Modo manual (original)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.actualizarUbicacion(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (appState.ubicacion.estaDentroDelRango())
                                Color(0xFF4CAF50)
                            else
                                Color.Gray
                        )
                    ) {
                        Text("Dentro del rango")
                    }

                    Button(
                        onClick = { viewModel.actualizarUbicacion(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!appState.ubicacion.estaDentroDelRango())
                                Color(0xFFF44336)
                            else
                                Color.Gray
                        )
                    ) {
                        Text("Fuera del rango")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de hora (para pruebas)
            Text(text = "Hora (para pruebas): ${appState.hora}:00", fontWeight = FontWeight.Medium)
            Slider(
                value = appState.hora.toFloat(),
                onValueChange = { viewModel.actualizarHora(it.toInt()) },
                valueRange = 0f..23f,
                steps = 22
            )
            Text(
                text = PolicyRules.obtenerMensajeHorario(appState.hora),
                fontSize = 12.sp,
                color = if (PolicyRules.esHorarioValido(appState.hora))
                    Color(0xFF4CAF50)
                else
                    Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animaciones del botón
            val buttonScale by animateFloatAsState(
                targetValue = when (animationState) {
                    is RegistrationAnimationController.AnimationState.ButtonPulse -> 1.1f
                    else -> 1.0f
                },
                animationSpec = tween(300),
                label = "buttonScale"
            )

            val buttonColor by animateColorAsState(
                targetValue = when (animationState) {
                    is RegistrationAnimationController.AnimationState.ButtonPulse -> Color(0xFF4CAF50)
                    else -> if (appState.canRegister) MaterialTheme.colorScheme.primary else Color.Gray
                },
                animationSpec = tween(300),
                label = "buttonColor"
            )

            // Botón de registro - HABILITACIÓN REACTIVA CON ANIMACIONES
            Button(
                onClick = {
                    // Delegar al ViewModel
                    viewModel.registrarAsistencia(accionSeleccionada)
                    // Reset animación para siguiente uso
                    animationController.reset()
                },
                enabled = appState.canRegister,  // ← CONTROL REACTIVO DEL BOTÓN
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                Text(
                    text = if (appState.canRegister)
                        "REGISTRAR ASISTENCIA"
                    else
                        "REGISTRO NO DISPONIBLE",
                    fontSize = 16.sp
                )
            }

            // Evaluación reactiva - Siempre visible CON ANIMACIONES
            Spacer(modifier = Modifier.height(16.dp))

            // Animaciones del card
            val cardAlpha by animateFloatAsState(targetValue = 1f, label = "cardAlpha")
            val cardScale by animateFloatAsState(
                targetValue = when (animationState) {
                    is RegistrationAnimationController.AnimationState.SuccessIcon -> 1.05f
                    else -> 1.0f
                },
                animationSpec = tween(400),
                label = "cardScale"
            )

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = cardAlpha
                            scaleX = cardScale
                            scaleY = cardScale
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (appState.canRegister)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        if (appState.canRegister) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono animado de éxito
                        AnimatedVisibility(
                            visible = animationState is RegistrationAnimationController.AnimationState.SuccessIcon
                                    || animationState is RegistrationAnimationController.AnimationState.MessageFadeIn
                                    || animationState is RegistrationAnimationController.AnimationState.Complete,
                            enter = scaleIn(animationSpec = tween(300)) + fadeIn()
                        ) {
                            Text(
                                text = "✓",
                                fontSize = 32.sp,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        Column {
                            Text(
                                text = appState.evaluacion.mensaje,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (appState.canRegister) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            if (appState.evaluacion.razon.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = appState.evaluacion.razon,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mis registros - REACTIVO
            Text(
                text = "MIS REGISTROS (${appState.misRegistros.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Usar registros del estado reactivo
            if (appState.misRegistros.isEmpty()) {
                Text(
                    text = "No tienes registros aún",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                appState.misRegistros.reversed().take(5).forEach { registro ->
                    RegistroCard(registro)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

        }
    }
}

// ==================== PANTALLA DE ADMINISTRADOR ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    usuario: Usuario,
    onLogout: () -> Unit,
    onViewRatingStats: () -> Unit = {},
    onViewUserRatings: () -> Unit = {}
) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var vistaSeleccionada by remember { mutableStateOf("Registros") } // Registros o Usuarios

    // Estados para secciones colapsables
    var estadisticasExpandidas by remember { mutableStateOf(true) }
    var accionesExpandidas by remember { mutableStateOf(true) }

    // Animación de rotación para los iconos de flecha
    val rotacionEstadisticas by animateFloatAsState(
        targetValue = if (estadisticasExpandidas) 0f else 180f,
        animationSpec = tween(300),
        label = "rotacionEstadisticas"
    )
    val rotacionAcciones by animateFloatAsState(
        targetValue = if (accionesExpandidas) 0f else 180f,
        animationSpec = tween(300),
        label = "rotacionAcciones"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Volver al inicio")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información del administrador
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Administrador",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = usuario.displayName(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = usuario.correo,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas - Sección Colapsable
            val estadisticas = UsuarioRepository.obtenerEstadisticasRegistros()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Encabezado clickeable para colapsar/expandir
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { estadisticasExpandidas = !estadisticasExpandidas },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESTADÍSTICAS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (estadisticasExpandidas) "Minimizar" else "Expandir",
                            modifier = Modifier.rotate(rotacionEstadisticas)
                        )
                    }

                    // Contenido colapsable con animación
                    AnimatedVisibility(
                        visible = estadisticasExpandidas,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                EstadisticaItem("Total", estadisticas["total"] ?: 0)
                                EstadisticaItem("Válidos", estadisticas["validos"] ?: 0)
                                EstadisticaItem("Inválidos", estadisticas["invalidos"] ?: 0)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                EstadisticaItem("Entradas", estadisticas["entradas"] ?: 0)
                                EstadisticaItem("Salidas", estadisticas["salidas"] ?: 0)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Acciones Rápidas - Sección Colapsable
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Encabezado clickeable para colapsar/expandir
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accionesExpandidas = !accionesExpandidas },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCIONES RÁPIDAS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (accionesExpandidas) "Minimizar" else "Expandir",
                            modifier = Modifier.rotate(rotacionAcciones)
                        )
                    }

                    // Contenido colapsable con animación
                    AnimatedVisibility(
                        visible = accionesExpandidas,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Botón para ver estadísticas de calificaciones
                            Button(
                                onClick = onViewRatingStats,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estadísticas de calificaciones"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver Estadísticas de Calificaciones")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botón para ver calificaciones de usuarios
                            Button(
                                onClick = onViewUserRatings,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Calificaciones de usuarios"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver Calificaciones de Usuarios")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de vista
            Text(
                text = "VISTA",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = vistaSeleccionada == "Registros",
                    onClick = { vistaSeleccionada = "Registros" },
                    label = { Text("Por Registro") }
                )
                FilterChip(
                    selected = vistaSeleccionada == "Usuarios",
                    onClick = { vistaSeleccionada = "Usuarios" },
                    label = { Text("Por Usuario") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros (solo para vista de registros)
            if (vistaSeleccionada == "Registros") {
                Text(
                    text = "FILTRAR REGISTROS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroSeleccionado == "Todos",
                        onClick = { filtroSeleccionado = "Todos" },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = filtroSeleccionado == "Válidos",
                        onClick = { filtroSeleccionado = "Válidos" },
                        label = { Text("Válidos") }
                    )
                    FilterChip(
                        selected = filtroSeleccionado == "Inválidos",
                        onClick = { filtroSeleccionado = "Inválidos" },
                        label = { Text("Inválidos") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Título según vista
            Text(
                text = if (vistaSeleccionada == "Registros") "REGISTROS DE USUARIOS" else "ESTADÍSTICAS POR USUARIO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val todosLosRegistros = UsuarioRepository.obtenerTodosLosRegistros()

            if (vistaSeleccionada == "Registros") {
                // Vista por registros (original)
                val registrosFiltrados = when (filtroSeleccionado) {
                    "Válidos" -> todosLosRegistros.filter { it.esAccesoValido() }
                    "Inválidos" -> todosLosRegistros.filter { !it.esAccesoValido() }
                    else -> todosLosRegistros
                }

                if (registrosFiltrados.isEmpty()) {
                    Text(
                        text = "No hay registros para mostrar",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(registrosFiltrados.reversed()) { registro ->
                            RegistroCard(registro)
                        }
                    }
                }
            } else {
                // Vista por usuario (nueva)
                val estadisticasPorUsuario = calcularEstadisticasPorUsuario(todosLosRegistros)

                if (estadisticasPorUsuario.isEmpty()) {
                    Text(
                        text = "No hay registros para mostrar",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(estadisticasPorUsuario) { stats ->
                            UserStatsCard(stats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticaItem(label: String, valor: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun RegistroCard(registro: RegistroAcceso) {
    val esValido = registro.esAccesoValido()
    val borderColor = if (esValido) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (esValido)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = registro.usuario.displayName(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (esValido) "✓ VÁLIDO" else "✗ INVÁLIDO",
                    fontWeight = FontWeight.Bold,
                    color = borderColor,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = registro.accion.formatear(),
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = registro.ubicacion.formatear(),
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = registro.obtenerFechaFormateada(),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

data class UserStats(
    val usuario: Usuario,
    val totalAsistencias: Int,
    val totalEntradas: Int,
    val totalSalidas: Int,
    val diasConRegistros: Int,
    val promedioDiario: Float
)

fun calcularEstadisticasPorUsuario(registros: List<RegistroAcceso>): List<UserStats> {
    val registrosPorUsuario = registros.groupBy { it.usuario.id }

    return registrosPorUsuario.map { (_, registrosUsuario) ->
        val usuario = registrosUsuario.first().usuario
        val totalEntradas = registrosUsuario.count { it.accion == AccionAsistencia.ENTRADA }
        val totalSalidas = registrosUsuario.count { it.accion == AccionAsistencia.SALIDA }
        val totalAsistencias = totalEntradas + totalSalidas

        val diasUnicos = registrosUsuario
            .map { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.marcaTiempo)) }
            .toSet()
            .size

        val promedioDiario = if (diasUnicos > 0) totalAsistencias.toFloat() / diasUnicos else 0f

        UserStats(
            usuario = usuario,
            totalAsistencias = totalAsistencias,
            totalEntradas = totalEntradas,
            totalSalidas = totalSalidas,
            diasConRegistros = diasUnicos,
            promedioDiario = promedioDiario
        )
    }.sortedByDescending { it.totalAsistencias }
}

@Composable
fun UserStatsCard(stats: UserStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stats.usuario.displayName(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = stats.usuario.correo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "${stats.totalAsistencias}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.totalEntradas}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Entradas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.totalSalidas}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Text(
                        text = "Salidas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.diasConRegistros}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Días",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", stats.promedioDiario),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Promedio/día",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}