package com.example.ama_practica03

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica03.data.UsuarioRepository
import com.example.ama_practica03.models.Usuario
import com.example.ama_practica03.models.AccionAsistencia
import com.example.ama_practica03.models.Ubicacion
import com.example.ama_practica03.models.RegistroAcceso
import com.example.ama_practica03.models.isEnabled
import com.example.ama_practica03.models.isAdmin
import com.example.ama_practica03.models.displayName
import com.example.ama_practica03.models.formatear
import com.example.ama_practica03.rules.PolicyRules
import java.text.SimpleDateFormat
import java.util.*

// ==================== PANTALLA DE LOGIN ====================

@Composable
fun LoginScreen(onLoginSuccess: (Usuario) -> Unit) {
    val usuarios = remember { UsuarioRepository.obtenerTodosLosUsuarios() }
    val usuariosActivos = usuarios.filter { it.isEnabled() }

    var nombreUsuario by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var mostrarSugerencias by remember { mutableStateOf(false) }

    // Filtrar usuarios basado en lo que el usuario escribe (búsqueda case-insensitive)
    val usuariosFiltrados = remember(nombreUsuario) {
        if (nombreUsuario.isBlank()) {
            usuariosActivos
        } else {
            usuariosActivos.filter { usuario ->
                usuario.nombre.contains(nombreUsuario, ignoreCase = true) ||
                usuario.correo.contains(nombreUsuario, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                mostrarSugerencias = it.isNotBlank()
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
                val usuarioEncontrado = usuariosActivos.find { usuario ->
                    usuario.nombre.equals(nombreUsuario, ignoreCase = true)
                }

                if (usuarioEncontrado != null) {
                    onLoginSuccess(usuarioEncontrado)
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

        // Mensaje de error
        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF44336))
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

        // Sugerencias de autocompletado
        if (mostrarSugerencias && usuariosFiltrados.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sugerencias:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            /*usuariosFiltrados.take(5).forEach { usuario ->
                SugerenciaCard(
                    usuario = usuario,
                    onClick = {
                        nombreUsuario = usuario.nombre
                        mostrarSugerencias = false
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }*/

            if (usuariosFiltrados.size > 5) {
                Text(
                    text = "... y ${usuariosFiltrados.size - 5} más",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Ayuda de usuarios disponibles
        if (nombreUsuario.isBlank() && usuariosActivos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Usuarios disponibles:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            usuariosActivos.take(6).forEach { usuario ->
                Text(
                    text = "• ${usuario.nombre} ${if (usuario.isAdmin()) "(Admin)" else ""}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (usuariosActivos.size > 6) {
                Text(
                    text = "... y ${usuariosActivos.size - 6} más",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun UsuarioCard(usuario: Usuario, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (usuario.isAdmin())
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.displayName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = usuario.correo,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (usuario.isAdmin()) "Administrador" else "Usuario",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (usuario.isAdmin())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            }

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==================== PANTALLA DE USUARIO ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(usuario: Usuario, onLogout: () -> Unit) {
    var accionSeleccionada by remember { mutableStateOf(AccionAsistencia.ENTRADA) }
    var ubicacionDentro by remember { mutableStateOf(true) }
    var horaActual by remember { mutableStateOf(PolicyRules.obtenerHoraActual()) }
    var mensajeResultado by remember { mutableStateOf("") }
    var colorResultado by remember { mutableStateOf(Color.Gray) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Asistencia") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
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
                    onClick = { ubicacionDentro = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ubicacionDentro)
                            Color(0xFF4CAF50)
                        else
                            Color.Gray
                    )
                ) {
                    Text("Dentro del rango")
                }

                Button(
                    onClick = { ubicacionDentro = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!ubicacionDentro)
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
            Text(text = "Hora (para pruebas): $horaActual:00", fontWeight = FontWeight.Medium)
            Slider(
                value = horaActual.toFloat(),
                onValueChange = { horaActual = it.toInt() },
                valueRange = 0f..23f,
                steps = 22
            )
            Text(
                text = PolicyRules.obtenerMensajeHorario(horaActual),
                fontSize = 12.sp,
                color = if (PolicyRules.esHorarioValido(horaActual))
                    Color(0xFF4CAF50)
                else
                    Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro
            Button(
                onClick = {
                    val ubicacion = if (ubicacionDentro)
                        Ubicacion.DentroDelRango("Campus Principal")
                    else
                        Ubicacion.FueraDelRango("Fuera del área permitida")

                    val context = PolicyRules.RegistroContext(
                        usuario = usuario,
                        ubicacion = ubicacion,
                        hora = horaActual
                    )

                    val resultado = PolicyRules.evaluarRegistro(context)
                    mensajeResultado = "${resultado.mensaje}\n${resultado.razon}"
                    colorResultado = if (resultado.permitido) Color(0xFF4CAF50) else Color(0xFFF44336)

                    if (resultado.permitido) {
                        // Registrar la asistencia
                        val registro = RegistroAcceso(
                            usuario = usuario,
                            accion = accionSeleccionada,
                            ubicacion = ubicacion,
                            marcaTiempo = System.currentTimeMillis()
                        )
                        UsuarioRepository.agregarRegistroAcceso(registro)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("REGISTRAR ASISTENCIA", fontSize = 16.sp)
            }

            // Resultado
            if (mensajeResultado.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResultado.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, colorResultado)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = mensajeResultado,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResultado
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mis registros
            Text(
                text = "MIS REGISTROS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val misRegistros = UsuarioRepository.obtenerRegistrosPorUsuario(usuario.id)
            if (misRegistros.isEmpty()) {
                Text(
                    text = "No tienes registros aún",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                misRegistros.reversed().take(5).forEach { registro ->
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
fun AdminScreen(usuario: Usuario, onLogout: () -> Unit) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
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
            .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
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
