package com.example.ama_practica09

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.ui.theme.AMA_Practica09Theme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import android.util.Log
import androidx.core.content.edit
import com.example.ama_practica09.firebase.FirebaseConfig
import com.example.ama_practica09.firebase.NotificationService
import com.example.ama_practica09.auth.GoogleSignInNavigation
import kotlinx.coroutines.launch
import com.example.ama_practica09.accesscontrol.*
import com.example.ama_practica09.charts.ChartScreen
import com.example.ama_practica09.animations.AnimationsScreen
import com.example.ama_practica09.location.GeolocationScreen

// ============================================
// PRÁCTICA 03: Conceptos de Kotlin con Compose
// ============================================

// 1. VARIABLES CON DIFERENTES TIPOS
// Declaración de variables con al menos 5 tipos diferentes
var enteroInmutable: Int = 42
var numeroDecimal: Double = 3.14159
var textoMutable: String = "Hola Kotlin"
var esVerdadero: Boolean = true
var listaNumeros: List<Int> = listOf(1, 2, 3, 4, 5)

// 2. FUNCIÓN CONVENCIONAL
// Función que calcula el cuadrado de un número
fun calcularCuadrado(numero: Int): Int {
    return numero * numero
}

// 3. CLASE TRADICIONAL CON PROPIEDAD Y METODO
// Clase que representa un contador simple
class Contador {
    var valor: Int = 0  // Propiedad

    // Metodo que incrementa el contador
    fun incrementar() {
        valor++
    }

    fun obtenerValor(): Int {
        return valor
    }
}

// 4. DATA CLASS USUARIO (Para Práctica 02)
// Data class para representar un usuario (versión simple para la práctica)
data class UsuarioPractica02(
    val nombre: String,
    val correo: String,
    val edad: Int
)

// EXTENSION FUNCTIONS para la data class UsuarioPractica02
// Función de extensión que devuelve el nombre con formato
fun UsuarioPractica02.nombreFormateado(): String {
    return "Sr/Sra. ${this.nombre.uppercase()}"
}

// Función de extensión que determina si el usuario es mayor de edad
fun UsuarioPractica02.esMayorDeEdad(): Boolean {
    return this.edad >= 18
}

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Launcher para solicitar permisos de notificación (API moderna)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permiso de notificaciones concedido")
        } else {
            Log.w(TAG, "Permiso de notificaciones denegado")
            Toast.makeText(
                this,
                "Se requiere permiso de notificaciones para recibir mensajes",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Comentado temporalmente para evitar conflictos con Google Sign-In
        // enableEdgeToEdge()

        // NUEVO: Inicializar UsuarioRepository con persistencia local
        com.example.ama_practica09.data.UsuarioRepository.initialize(this)

        // Inicializar Firebase
        initializeFirebase()

        // Solicitar permisos de notificación (Android 13+)
        requestNotificationPermission()

        setContent {
            AMA_Practica09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    /**
     * Inicializa Firebase y obtiene el token de FCM
     * FCM es opcional - la app funcionará correctamente aunque falle
     */
    private fun initializeFirebase() {
        try {
            Log.d(TAG, "Inicializando Firebase...")

            // Inicializar Firebase
            FirebaseConfig.initialize(this)

            // Crear servicio de notificaciones
            val notificationService = NotificationService(this)

            // Obtener el token de FCM (opcional - no bloquea la app)
            FirebaseConfig.getToken { token ->
                if (token != null) {
                    Log.d(TAG, "Token de FCM obtenido exitosamente")
                    Log.d(TAG, "Token completo para pruebas: $token")

                    // Mostrar notificación con el token (solo en modo debug)
                    if (FirebaseConfig.debugMode) {
                        try {
                            notificationService.showNotification(
                                title = "Firebase Inicializado",
                                message = "Token FCM registrado correctamente",
                                channelId = NotificationService.CHANNEL_ID_DEFAULT,
                                notificationId = 9999
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "No se pudo mostrar notificación de debug: ${e.message}")
                        }
                    }

                    // Guardar el token en SharedPreferences
                    saveTokenLocally(token)

                    // Suscribirse al tópico de todos los usuarios
                    FirebaseConfig.subscribeToTopic(
                        topic = FirebaseConfig.Topics.ALL_USERS,
                        onSuccess = {
                            Log.d(TAG, "Suscrito exitosamente al tópico: ${FirebaseConfig.Topics.ALL_USERS}")
                        },
                        onError = { error ->
                            Log.w(TAG, "No se pudo suscribir al tópico: ${error.message}")
                        }
                    )

                } else {
                    // FCM no disponible - la app continuará funcionando normalmente
                    Log.w(TAG, "No se pudo obtener el token de FCM. Esto puede deberse a:")
                    Log.w(TAG, "- Google Play Services no disponible o desactualizado")
                    Log.w(TAG, "- Sin conexión a Internet")
                    Log.w(TAG, "- Configuración de Firebase incompleta")
                    Log.w(TAG, "La aplicación continuará funcionando sin notificaciones push")
                }
            }

        } catch (e: Exception) {
            // Error al inicializar Firebase - la app continuará funcionando
            Log.e(TAG, "Error al inicializar Firebase: ${e.message}", e)
            Log.w(TAG, "La aplicación continuará funcionando sin Firebase Cloud Messaging")
        }
    }

    /**
     * Guarda el token localmente en SharedPreferences usando KTX
     */
    private fun saveTokenLocally(token: String) {
        getSharedPreferences("FCM_PREFS", MODE_PRIVATE).edit {
            putString("FCM_TOKEN", token)
        }
        Log.d(TAG, "Token guardado localmente")
    }

    /**
     * Solicita permisos de notificación en Android 13+ usando API moderna
     */
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Para versiones anteriores a Android 13, los permisos se conceden automáticamente
            Log.d(TAG, "Permiso de notificaciones no requerido para esta versión de Android")
        }
    }
}

/**
 * Navegación principal de la aplicación
 * Maneja el flujo entre menú, login, pantalla de usuario y pantalla de administrador
 * Incluye control de sesión con SessionManager
 */
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var pantalla by remember { mutableStateOf("welcome") }  // MODIFICADO: Inicia en welcome
    var mostrarGoogleSignIn by remember { mutableStateOf(false) }

    // Crear instancia del auth manager para Google Sign-In
    val authManager = remember { com.example.ama_practica09.auth.GoogleAuthManager(context) }
    val authState by authManager.authState.collectAsState()

    // NUEVO: Crear instancia del SessionManager para control de sesión
    val sessionManager = remember { com.example.ama_practica09.session.SessionManager(context) }
    val sessionState by sessionManager.sessionState.collectAsState()

    // NUEVO: Crear managers de control de acceso
    val accessControlManager = remember { AccessControlManager(sessionManager) }
    val navigationManager = remember { NavigationManager(accessControlManager) }

    // NUEVO: Estados para diálogo de acceso denegado
    var mostrarDialogoAccesoDenegado by remember { mutableStateOf(false) }
    var mensajeAccesoDenegado by remember { mutableStateOf("") }

    // NUEVO: Función para navegar con validación de acceso
    fun navigateWithValidation(targetScreen: String) {
        val result = accessControlManager.validateAccess(targetScreen)
        when (result) {
            is AccessResult.Granted -> pantalla = targetScreen
            is AccessResult.Denied -> {
                mensajeAccesoDenegado = result.reason
                mostrarDialogoAccesoDenegado = true
                pantalla = result.fallbackScreen
            }
            is AccessResult.RequiresAuth -> {
                mensajeAccesoDenegado = result.message
                mostrarDialogoAccesoDenegado = true
                pantalla = "login"
            }
        }
    }

    // NUEVO: Función para manejar logout
    fun handleLogout() {
        scope.launch {
            sessionManager.signOut()
            navigationManager.clearAndNavigateTo("welcome") { pantalla = it }
        }
    }

    // NUEVO: Verificar sesión activa al iniciar la aplicación
    LaunchedEffect(Unit) {
        sessionManager.checkActiveSession()
    }

    // NUEVO: Manejar cambios en el estado de sesión
    LaunchedEffect(sessionState) {
        when (val state = sessionState) {
            is com.example.ama_practica09.session.SessionState.Active -> {
                // Sesión activa restaurada - redirigir a HomeScreen
                usuarioActual = state.usuario
                pantalla = "home"  // MODIFICADO: redirige a home en lugar de user/admin
                Log.d("AppNavigation", "Sesión activa restaurada para: ${state.usuario.nombre}")
            }
            is com.example.ama_practica09.session.SessionState.Inactive -> {
                // Sin sesión activa - mostrar welcome
                if (pantalla == "user" || pantalla == "admin" || pantalla == "home") {
                    pantalla = "welcome"  // MODIFICADO: vuelve a welcome
                }
            }
            is com.example.ama_practica09.session.SessionState.Error -> {
                Log.e("AppNavigation", "Error en sesión: ${state.message}")
                pantalla = "menu"
            }
            is com.example.ama_practica09.session.SessionState.Loading -> {
                // Estado de carga - esperar
            }
        }
    }

    // Launcher para manejar el resultado del Google Sign-In
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch {
                authManager.handleSignInResult(result.data)
            }
        } else {
            scope.launch {
                authManager.handleSignInResult(null)
            }
        }
    }

    // Manejar autenticación exitosa con Google
    LaunchedEffect(authState) {
        if (authState is com.example.ama_practica09.auth.AuthState.Authenticated) {
            val googleUser = (authState as com.example.ama_practica09.auth.AuthState.Authenticated).user
            // Convertir GoogleUserInfo a Usuario para el sistema de asistencia
            usuarioActual = googleUser.toUsuario()
            mostrarGoogleSignIn = false

            // MODIFICADO: Redirigir a HomeScreen en lugar de directamente a user/admin
            pantalla = "home"

            // NUEVO: Iniciar sesión con el SessionManager
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
                scope.launch {
                    sessionManager.startSession(firebaseUser)
                }
            }
        }
    }

    // NUEVO: Diálogo para mostrar acceso denegado
    if (mostrarDialogoAccesoDenegado) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAccesoDenegado = false },
            title = { Text("Acceso Denegado") },
            text = { Text(mensajeAccesoDenegado) },
            confirmButton = {
                Button(onClick = { mostrarDialogoAccesoDenegado = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Mostrar pantalla de Google Sign-In si está activado
    if (mostrarGoogleSignIn) {
        com.example.ama_practica09.auth.LoginScreen(
            authState = authState,
            onSignInClick = {
                authManager.signIn(signInLauncher)
            },
            onBack = {
                mostrarGoogleSignIn = false
                scope.launch {
                    authManager.signOut()
                }
            }
        )
    } else {
        // NUEVO: Envolver todas las pantallas con SecureScreen para monitoreo
        SecureScreen(
            screenName = pantalla,
            accessControlManager = accessControlManager
        ) {
            when (pantalla) {
            // NUEVO: Pantalla de Bienvenida (1/4 actividades)
            "welcome" -> {
                com.example.ama_practica09.auth.WelcomeScreen(
                    onGetStarted = {
                        navigateWithValidation("login")
                    },
                    onDevelopersClick = {
                        navigateWithValidation("menu")
                    }
                )
            }
            "menu" -> {
                MenuPrincipal(
                    onPractica02Click = { navigateWithValidation("practica02") },
                    onSistemaAsistenciaClick = { navigateWithValidation("login") },
                    onGoogleSignInClick = { navigateWithValidation("googleSignIn") },
                    onBack = { navigateWithValidation("welcome") }
                )
            }
            // MODIFICADO: Pantalla de Login (2/4 actividades)
            "login" -> {
                LoginScreen(
                    onLoginSuccess = { usuario ->
                        usuarioActual = usuario

                        // NUEVO: Guardar sesión con el SessionManager para login tradicional
                        scope.launch {
                            sessionManager.saveTraditionalLogin(
                                userId = usuario.id.toString(),
                                email = usuario.correo,
                                displayName = usuario.nombre
                            )
                        }

                        // Navegar directamente a home sin validación (acabamos de autenticar)
                        pantalla = "home"
                    },
                    onBack = { navigateWithValidation("welcome") },
                    onGoogleSignInClick = {
                        mostrarGoogleSignIn = true
                    },
                    onRegisterClick = {
                        navigateWithValidation("register")
                    }
                )
            }
            // NUEVO: Pantalla de Registro (3/4 actividades)
            "register" -> {
                com.example.ama_practica09.auth.RegisterScreen(
                    onRegisterSuccess = { usuario ->
                        usuarioActual = usuario

                        // NUEVO: Guardar sesión con el SessionManager para registro tradicional
                        scope.launch {
                            sessionManager.saveTraditionalLogin(
                                userId = usuario.id.toString(),
                                email = usuario.correo,
                                displayName = usuario.nombre
                            )
                        }

                        // Navegar directamente a home sin validación (acabamos de crear la cuenta)
                        pantalla = "home"
                    },
                    onBack = { navigateWithValidation("login") },
                    onLoginClick = { navigateWithValidation("login") }
                )
            }
            "user" -> {
                ProtectedRoute(
                    targetScreen = "user",
                    accessControlManager = accessControlManager,
                    onAccessDenied = { reason, fallback ->
                        mensajeAccesoDenegado = reason
                        mostrarDialogoAccesoDenegado = true
                        pantalla = fallback
                    },
                    onRequiresAuth = { navigateWithValidation("login") }
                ) {
                    usuarioActual?.let { usuario ->
                        UserScreen(
                            usuario = usuario,
                            onLogout = {
                                // Volver a HomeScreen, no hacer logout completo
                                pantalla = "home"
                            }
                        )
                    }
                }
            }
            "admin" -> {
                ProtectedRoute(
                    targetScreen = "admin",
                    accessControlManager = accessControlManager,
                    onAccessDenied = { reason, fallback ->
                        mensajeAccesoDenegado = reason
                        mostrarDialogoAccesoDenegado = true
                        pantalla = fallback
                    },
                    onRequiresAuth = { navigateWithValidation("login") }
                ) {
                    usuarioActual?.let { usuario ->
                        AdminScreen(
                            usuario = usuario,
                            onLogout = {
                                // Volver a HomeScreen, no hacer logout completo
                                pantalla = "home"
                            },
                            onViewRatingStats = {
                                // NUEVO: Navegar a estadísticas de calificaciones
                                navigateWithValidation("ratingStats")
                            },
                            onViewUserRatings = {
                                // NUEVO: Navegar a calificaciones de usuarios
                                navigateWithValidation("userRatings")
                            }
                        )
                    }
                }
            }
            "practica02" -> {
                MainScreenPractica02(
                    onBack = { navigateWithValidation("menu") }
                )
            }
            "googleSignIn" -> {
                GoogleSignInNavigation(
                    onBack = { navigateWithValidation("menu") }
                )
            }
            "home" -> {
                // MODIFICADO: HomeScreen sin ProtectedRoute - validación solo al hacer click en botones
                usuarioActual?.let { usuario ->
                    val googleUserInfo = com.example.ama_practica09.auth.GoogleUserInfo(
                        uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        email = usuario.correo,
                        displayName = usuario.nombre,
                        photoUrl = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
                    )
                    com.example.ama_practica09.auth.HomeScreen(
                        user = googleUserInfo,
                        onSignOutClick = {
                            handleLogout()
                        },
                        onAccessSystemClick = {
                            // Botón "Registro de Asistencia" - siempre navega a UserScreen
                            navigateWithValidation("user")
                        },
                        onAccessDashboardClick = {
                            // Botón "Dashboard de Asistencias" - siempre navega a AdminScreen
                            // Si es USER, mostrará el mensaje de acceso denegado
                            navigateWithValidation("admin")
                        },
                        onRateExperienceClick = {
                            // Botón "Calificar Experiencia" - navega a RatingScreen
                            navigateWithValidation("rating")
                        },
                        onViewStatsClick = {
                            // Botón "Ver Estadísticas" - navega a ChartScreen
                            navigateWithValidation("charts")
                        },
                        onViewAnimationsClick = {
                            // NUEVO: Botón "Ver Animaciones" - navega a AnimationsScreen
                            navigateWithValidation("animations")
                        },
                        onGeolocationClick = {
                            // NUEVO: Botón "Geolocalización" - navega a GeolocationScreen
                            navigateWithValidation("geolocation")
                        }
                    )
                }
            }
            "rating" -> {
                // NUEVO: Pantalla de calificación de experiencia
                usuarioActual?.let { usuario ->
                    com.example.ama_practica09.rating.RatingScreen(
                        usuario = usuario,
                        onBack = { navigateWithValidation("home") },
                        onRatingSubmitted = {
                            // Opcional: mostrar mensaje de éxito
                        }
                    )
                }
            }
            "ratingStats" -> {
                // NUEVO: Pantalla de estadísticas de calificaciones (solo admin)
                com.example.ama_practica09.rating.RatingStatsScreen(
                    onBack = { navigateWithValidation("admin") }
                )
            }
            "userRatings" -> {
                // NUEVO: Pantalla de calificaciones de usuarios (solo admin)
                com.example.ama_practica09.rating.UserRatingsListScreen(
                    onBack = { navigateWithValidation("admin") }
                )
            }
            "charts" -> {
                // Pantalla de estadísticas con gráficos
                usuarioActual?.let { usuario ->
                    ChartScreen(
                        usuario = usuario,
                        onBackClick = { navigateWithValidation("home") }
                    )
                } ?: run {
                    Text("Error: Usuario no encontrado")
                }
            }
            "animations" -> {
                // NUEVO: Pantalla de animaciones
                ProtectedRoute(
                    targetScreen = "animations",
                    accessControlManager = accessControlManager,
                    onAccessDenied = { reason, fallback ->
                        mensajeAccesoDenegado = reason
                        mostrarDialogoAccesoDenegado = true
                        pantalla = fallback
                    },
                    onRequiresAuth = { navigateWithValidation("login") }
                ) {
                    usuarioActual?.let { usuario ->
                        AnimationsScreen(
                            usuario = usuario,
                            onBackClick = { navigateWithValidation("home") }
                        )
                    } ?: run {
                        Text("Error: Usuario no encontrado")
                    }
                }
            }
            "geolocation" -> {
                // NUEVO: Pantalla de geolocalización
                ProtectedRoute(
                    targetScreen = "geolocation",
                    accessControlManager = accessControlManager,
                    onAccessDenied = { reason, fallback ->
                        mensajeAccesoDenegado = reason
                        mostrarDialogoAccesoDenegado = true
                        pantalla = fallback
                    },
                    onRequiresAuth = { navigateWithValidation("login") }
                ) {
                    usuarioActual?.let { usuario ->
                        GeolocationScreen(
                            usuario = usuario,
                            onBackClick = { navigateWithValidation("home") }
                        )
                    } ?: run {
                        Text("Error: Usuario no encontrado")
                    }
                }
            }
        }
        } // Cierre de SecureScreen
    }
}

/**
 * Menú principal de la aplicación
 * Permite seleccionar entre Práctica 02, Sistema de Asistencia y Google Sign-In
 */
@Composable
fun MenuPrincipal(
    onPractica02Click: () -> Unit,
    onSistemaAsistenciaClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header con información de desarrolladores
        Text(
            text = "APLICACIONES MÓVILES AVANZADAS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Práctica 06",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text="Desarollador 1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Image(
                    painter = painterResource(id = R.drawable.desa1),
                    contentDescription = "Miguel Pastuña",
                    modifier = Modifier.size(80.dp)
                )
                Text("Miguel Pastuña", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text="Desarollador 2", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Image(
                    painter = painterResource(id = R.drawable.desa2),
                    contentDescription = "Stalin Garcia",
                    modifier = Modifier.size(80.dp)
                )
                Text("Stalin Garcia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Botón Práctica 02
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Button(
                onClick = onPractica02Click,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "PRÁCTICA 02",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Conceptos de Kotlin",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Sistema de Asistencia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Button(
                onClick = onSistemaAsistenciaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "SISTEMA DE ASISTENCIA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reglas y Políticas",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Google Sign-In (Práctica 06)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Button(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "GOOGLE SIGN-IN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Práctica 06 - Autenticación",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para volver
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Volver",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona una opción para continuar",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * Pantalla original con las prácticas de Kotlin
 * Puede ser accedida desde el menú principal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenPractica02(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Práctica 02 - Conceptos Kotlin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Volver al menú principal"
                        )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DevelopersHeader()

            // Sección 1: Calculador de Edad
            CalculadorEdadSection(context)

            // Sección 2: Función Calcular Cuadrado
            CalcularCuadradoSection(context)

            // Sección 3: Clase Contador
            ContadorSection()

            // Sección 4: Data Class Usuario + Extensions
            UsuarioSection(context)

            // Sección 5: Variables
            VariablesSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DevelopersHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Columna Izquierda - Stalin Garcia
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "STALIN GARCIA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.desa2),
                    contentDescription = "Stalin Garcia",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Desarrollador 1",
                    fontSize = 12.sp
                )
            }

            // Columna Derecha - Miguel Pastuña
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MIGUEL PASTUÑA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.desa1),
                    contentDescription = "Miguel Pastuña",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Desarrollador 2",
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==================== SECCIÓN 1: CALCULADOR DE EDAD ====================
@Composable
fun CalculadorEdadSection(context: android.content.Context) {
    var fechaNacimiento by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    Column {
        Text(
            text = "1. CALCULADOR DE EDAD",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it },
            label = { Text("Fecha de nacimiento (dd/MM/yyyy)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (fechaNacimiento.isEmpty()) {
                    Toast.makeText(context, "Por favor ingrese la fecha de nacimiento", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.isLenient = false
                    val fechaNac = sdf.parse(fechaNacimiento)

                    if (fechaNac == null) {
                        Toast.makeText(context, "Fecha inválida. Use el formato dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val calNacimiento = Calendar.getInstance()
                    calNacimiento.time = fechaNac
                    val calHoy = Calendar.getInstance()
                    var edad = calHoy.get(Calendar.YEAR) - calNacimiento.get(Calendar.YEAR)

                    if (calHoy.get(Calendar.DAY_OF_YEAR) < calNacimiento.get(Calendar.DAY_OF_YEAR)) {
                        edad--
                    }

                    if (edad < 0) {
                        Toast.makeText(context, "La fecha de nacimiento no puede ser futura", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    resultado = "Tu edad es: $edad años"

                } catch (_: Exception) {
                    Toast.makeText(context, "Fecha inválida. Use el formato dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular Edad")
        }

        if (resultado.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultado,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD))
                    .padding(8.dp),
                fontSize = 14.sp
            )
        }
    }
}

// ==================== SECCIÓN 2: FUNCIÓN CALCULAR CUADRADO ====================
@Composable
fun CalcularCuadradoSection(context: android.content.Context) {
    var numeroInput by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    Column {
        Text(
            text = "2. FUNCIÓN: CALCULAR CUADRADO",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = numeroInput,
            onValueChange = { numeroInput = it },
            label = { Text("Ingrese un número") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (numeroInput.isEmpty()) {
                    Toast.makeText(context, "Por favor ingrese un número", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                try {
                    val numero = numeroInput.toInt()
                    val cuadrado = calcularCuadrado(numero)

                    resultado = """
                        ENTRADA: $numero
                        FUNCIÓN: calcularCuadrado($numero)
                        SALIDA: $cuadrado

                        Explicación: $numero × $numero = $cuadrado
                    """.trimIndent()
                } catch (_: NumberFormatException) {
                    Toast.makeText(context, "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular Cuadrado")
        }

        if (resultado.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultado,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9))
                    .padding(8.dp),
                fontSize = 14.sp
            )
        }
    }
}

// ==================== SECCIÓN 3: CLASE CONTADOR ====================
@Composable
fun ContadorSection() {
    val contador = remember { Contador() }
    var valorContador by remember { mutableIntStateOf(0) }

    Column {
        Text(
            text = "3. CLASE CONTADOR",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Valor del contador: $valorContador",
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF9C4))
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    contador.incrementar()
                    valorContador = contador.obtenerValor()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Incrementar")
            }

            Button(
                onClick = {
                    contador.valor = 0
                    valorContador = contador.obtenerValor()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Resetear")
            }
        }
    }
}

// ==================== SECCIÓN 4: DATA CLASS USUARIO + EXTENSIONS ====================
@Composable
fun UsuarioSection(context: android.content.Context) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    Column {
        Text(
            text = "4. DATA CLASS USUARIO + EXTENSIONS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = edad,
            onValueChange = { edad = it },
            label = { Text("Edad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (nombre.isEmpty() || correo.isEmpty() || edad.isEmpty()) {
                    Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                try {
                    val edadInt = edad.toInt()

                    // Crear instancia de data class UsuarioPractica02
                    val usuario = UsuarioPractica02(nombre, correo, edadInt)

                    // Usar las extension functions
                    val nombreFormato = usuario.nombreFormateado()
                    val mayoriaEdad = usuario.esMayorDeEdad()
                    val estadoEdad = if (mayoriaEdad) "SÍ es mayor de edad" else "NO es mayor de edad"

                    resultado = """
                        DATA CLASS CREADA:
                        $usuario

                        EXTENSION FUNCTIONS:

                        1 nombreFormateado():
                           → "$nombreFormato"

                        2 esMayorDeEdad():
                           → $mayoriaEdad
                           → $estadoEdad (edad >= 18)
                    """.trimIndent()

                } catch (_: NumberFormatException) {
                    Toast.makeText(context, "La edad debe ser un número válido", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Usuario y Ver Extensions")
        }

        if (resultado.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultado,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3E5F5))
                    .padding(12.dp),
                fontSize = 14.sp
            )
        }
    }
}

// ==================== SECCIÓN 5: MOSTRAR VARIABLES ====================
@Composable
fun VariablesSection() {
    var mostrarVariables by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "5. VARIABLES (5 tipos diferentes)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { mostrarVariables = !mostrarVariables },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mostrar Variables Declaradas")
        }

        if (mostrarVariables) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = """
                    VARIABLES DECLARADAS (5 tipos):

                    1. Int (Entero):
                       val enteroInmutable: Int = $enteroInmutable

                    2. Double (Decimal):
                       var numeroDecimal: Double = $numeroDecimal

                    3. String (Texto):
                       var textoMutable: String = "$textoMutable"

                    4. Boolean (Booleano):
                       var esVerdadero: Boolean = $esVerdadero

                    5. List<Int> (Lista):
                       var listaNumeros: List<Int> = $listaNumeros
                """.trimIndent(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEFF1))
                    .padding(12.dp),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}