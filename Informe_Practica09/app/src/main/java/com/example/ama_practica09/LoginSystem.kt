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

    // SUSCRIPTOR: Observar SharedFlow<AppEvent> para eventos
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            // Procesar evento en el gateway de notificaciones
            notificationGateway.procesarEvento(event)

            // Además manejar eventos de UI (Snackbar/Toast)
            when (event) {
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

            // Selector de ubicación
            Text(text = "Ubicación:", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
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

            // Botón de registro - HABILITACIÓN REACTIVA
            Button(
                onClick = {
                    // Delegar al ViewModel
                    viewModel.registrarAsistencia(accionSeleccionada)
                },
                enabled = appState.canRegister,  // ← CONTROL REACTIVO DEL BOTÓN
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (appState.canRegister)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray
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

            // Evaluación reactiva - Siempre visible
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.padding(16.dp)) {
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

            // Estadísticas
            val estadisticas = UsuarioRepository.obtenerEstadisticasRegistros()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ESTADÍSTICAS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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

            Spacer(modifier = Modifier.height(16.dp))

            // NUEVO: Botón para ver estadísticas de calificaciones
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

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros
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

            // Lista de registros
            Text(
                text = "REGISTROS DE USUARIOS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val todosLosRegistros = UsuarioRepository.obtenerTodosLosRegistros()
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