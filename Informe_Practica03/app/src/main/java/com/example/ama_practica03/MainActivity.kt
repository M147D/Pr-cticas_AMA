package com.example.ama_practica03

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import com.example.ama_practica03.ui.theme.AMA_Practica03Theme
import com.example.ama_practica03.models.Usuario
import com.example.ama_practica03.models.isAdmin
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ============================================
// PRÁCTICA 03: Conceptos de Kotlin con Compose
// ============================================

// 1. VARIABLES CON DIFERENTES TIPOS
// Declaración de variables con al menos 5 tipos diferentes
val enteroInmutable: Int = 42
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

// 5. SEALED CLASS (para ejemplo comparativo)
// Sealed class que representa el estado de una operación
sealed class EstadoOperacion {
    data class Exito(val mensaje: String) : EstadoOperacion()
    data class Error(val error: String) : EstadoOperacion()
    object Cargando : EstadoOperacion()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AMA_Practica03Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Navegación principal de la aplicación
 * Maneja el flujo entre menú, login, pantalla de usuario y pantalla de administrador
 */
@Composable
fun AppNavigation() {
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var pantalla by remember { mutableStateOf("menu") }

    when (pantalla) {
        "menu" -> {
            MenuPrincipal(
                onPractica02Click = { pantalla = "practica02" },
                onSistemaAsistenciaClick = { pantalla = "login" }
            )
        }
        "login" -> {
            LoginScreen(
                onLoginSuccess = { usuario ->
                    usuarioActual = usuario
                    pantalla = if (usuario.isAdmin()) "admin" else "user"
                }
            )
        }
        "user" -> {
            usuarioActual?.let { usuario ->
                UserScreen(
                    usuario = usuario,
                    onLogout = {
                        usuarioActual = null
                        pantalla = "menu"
                    }
                )
            }
        }
        "admin" -> {
            usuarioActual?.let { usuario ->
                AdminScreen(
                    usuario = usuario,
                    onLogout = {
                        usuarioActual = null
                        pantalla = "menu"
                    }
                )
            }
        }
        "practica02" -> {
            MainScreenPractica02(
                onBack = { pantalla = "menu" }
            )
        }
    }
}

/**
 * Menú principal de la aplicación
 * Permite seleccionar entre Práctica 02 y Sistema de Asistencia
 */
@Composable
fun MenuPrincipal(
    onPractica02Click: () -> Unit,
    onSistemaAsistenciaClick: () -> Unit
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
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Práctica 03",
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
                Image(
                    painter = painterResource(id = R.drawable.captura_de_pantalla_2025_10_01_065607),
                    contentDescription = "Stalin Garcia",
                    modifier = Modifier.size(80.dp)
                )
                Text("Stalin Garcia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.captura_de_pantalla_2025_10_01_065424),
                    contentDescription = "Miguel Pastuña",
                    modifier = Modifier.size(80.dp)
                )
                Text("Miguel Pastuña", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

        Spacer(modifier = Modifier.height(32.dp))

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
                            imageVector = Icons.Default.ExitToApp,
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
                    painter = painterResource(id = R.drawable.captura_de_pantalla_2025_10_01_065607),
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
                    painter = painterResource(id = R.drawable.captura_de_pantalla_2025_10_01_065424),
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

                } catch (e: Exception) {
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
                } catch (e: NumberFormatException) {
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
    var valorContador by remember { mutableStateOf(0) }

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

                } catch (e: NumberFormatException) {
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
