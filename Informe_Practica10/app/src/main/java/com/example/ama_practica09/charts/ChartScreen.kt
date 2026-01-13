package com.example.ama_practica09.charts

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica09.data.RegistroLocalRepository
import com.example.ama_practica09.firebase.AttendanceManager
import com.example.ama_practica09.models.AccionAsistencia
import com.example.ama_practica09.models.Rating
import com.example.ama_practica09.models.RatingCategory
import com.example.ama_practica09.models.RatingStats
import com.example.ama_practica09.models.RegistroAcceso
import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.rating.RatingManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla principal de estadísticas y gráficos
 */
/**
 * Rango de fechas para filtrado
 */
enum class DateRange(val days: Int, val label: String) {
    SEVEN_DAYS(7, "Últimos 7 días"),
    FIFTEEN_DAYS(15, "Últimos 15 días"),
    THIRTY_DAYS(30, "Últimos 30 días"),
    ALL_TIME(365, "Todo el tiempo")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    usuario: Usuario,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { RegistroLocalRepository(context) }
    val attendanceManager = remember { AttendanceManager(context) }

    // Estados principales de navegación
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0=Asistencias, 1=Ratings
    var selectedChartType by remember { mutableIntStateOf(0) } // 0=Barras, 1=Líneas, 2=Circular
    var selectedDateRange by remember { mutableStateOf(DateRange.SEVEN_DAYS) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para Asistencias
    var attendanceChartData by remember { mutableStateOf<List<ChartDataPoint>>(emptyList()) }
    var attendanceStats by remember { mutableStateOf(AttendanceStats()) }
    var attendanceDataSource by remember { mutableStateOf("Local") }

    // Estados para Ratings
    var ratingChartData by remember { mutableStateOf<List<ChartDataPoint>>(emptyList()) }
    var ratingStats by remember { mutableStateOf(RatingStats()) }
    var ratingViewMode by remember { mutableIntStateOf(0) } // 0=Distribución, 1=Por Categoría

    // Labels de tabs
    val mainTabs = listOf("Asistencias", "Ratings")
    val chartTabs = listOf("Barras", "Líneas", "Circular")

    // Función para cargar datos desde Firebase o local
    fun cargarDatos() {
        isLoading = true
        scope.launch {
            when (selectedMainTab) {
                0 -> {
                    // ASISTENCIAS
                    try {
                        val firebaseResult = loadAttendanceData(attendanceManager, usuario, selectedDateRange)
                        if (firebaseResult.first.isNotEmpty()) {
                            attendanceChartData = firebaseResult.first
                            attendanceStats = firebaseResult.second
                            attendanceDataSource = "Firebase"
                        } else {
                            val localResult = loadChartDataFromRepository(repository, selectedDateRange)
                            attendanceChartData = localResult.first
                            attendanceStats = localResult.second
                            attendanceDataSource = "Local"
                        }
                    } catch (e: Exception) {
                        val localResult = loadChartDataFromRepository(repository, selectedDateRange)
                        attendanceChartData = localResult.first
                        attendanceStats = localResult.second
                        attendanceDataSource = "Local (sin conexión)"
                    }
                }
                1 -> {
                    // RATINGS
                    try {
                        val ratingManager = RatingManager(context)
                        val result = loadRatingData(ratingManager, usuario)
                        ratingChartData = result.first
                        ratingStats = result.second
                    } catch (e: Exception) {
                        Log.e("ChartScreen", "Error al cargar ratings", e)
                        ratingChartData = emptyList()
                        ratingStats = RatingStats()
                    }
                }
            }
            isLoading = false
        }
    }

    // Cargar datos al iniciar o cuando cambia el tab principal o el filtro de fecha
    LaunchedEffect(selectedMainTab, selectedDateRange) {
        cargarDatos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Estadísticas de Asistencia",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // NIVEL 1: Tabs principales (Asistencias | Ratings)
                TabRow(selectedTabIndex = selectedMainTab, modifier = Modifier.fillMaxWidth()) {
                    mainTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedMainTab == index,
                            onClick = { selectedMainTab = index },
                            text = { Text(text = title, fontWeight = if (selectedMainTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Indicador de fuente (solo Asistencias)
                if (selectedMainTab == 0) {
                    Text(
                        text = "Fuente: $attendanceDataSource",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.End
                    )
                }

                // Selector de fecha (solo Asistencias)
                if (selectedMainTab == 0) {
                    DateRangeSelector(selectedRange = selectedDateRange, onRangeSelected = { selectedDateRange = it })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Selector de vista (solo Ratings)
                if (selectedMainTab == 1) {
                    RatingViewSelector(selectedView = ratingViewMode, onViewSelected = { ratingViewMode = it })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tarjetas de resumen
                if (selectedMainTab == 0) {
                    if (usuario.esAdmin()) {
                        AdminAttendanceStatsCards(stats = attendanceStats)
                    } else {
                        StatsCards(stats = attendanceStats)
                    }
                } else {
                    RatingStatsCards(stats = ratingStats)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // NIVEL 2: Tabs de gráfico (Barras | Líneas | Circular)
                TabRow(selectedTabIndex = selectedChartType, modifier = Modifier.fillMaxWidth()) {
                    chartTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedChartType == index,
                            onClick = { selectedChartType = index },
                            text = { Text(text = title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Gráfico
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val currentChartData = when (selectedMainTab) {
                            0 -> attendanceChartData
                            1 -> if (ratingViewMode == 0) processRatingStatsToChartData(ratingStats)
                                 else processRatingStatsByCategoryToChartData(ratingStats)
                            else -> emptyList()
                        }

                        val chartTitle = when (selectedMainTab) {
                            0 -> when (selectedChartType) {
                                0 -> "Asistencias por Día"
                                1 -> "Tendencia de Asistencias"
                                2 -> "Distribución de Asistencias"
                                else -> "Estadísticas"
                            }
                            1 -> when (selectedChartType) {
                                0 -> if (ratingViewMode == 0) "Distribución por Estrellas" else "Promedio por Categoría"
                                1 -> if (ratingViewMode == 0) "Tendencia de Calificaciones" else "Comparativa de Categorías"
                                2 -> if (ratingViewMode == 0) "Proporción de Estrellas" else "Distribución por Categoría"
                                else -> "Estadísticas"
                            }
                            else -> "Estadísticas"
                        }

                        when (selectedChartType) {
                            0 -> BarChart(data = currentChartData, config = ChartConfig(title = chartTitle, chartType = ChartType.BAR))
                            1 -> LineChart(data = currentChartData, config = ChartConfig(title = chartTitle, chartType = ChartType.LINE))
                            2 -> PieChart(data = currentChartData, config = ChartConfig(title = chartTitle, chartType = ChartType.PIE))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón actualizar
                Button(
                    onClick = { cargarDatos() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Actualizar Datos", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Tarjetas con resumen de estadísticas
 */
@Composable
fun StatsCards(stats: AttendanceStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Entradas",
            value = stats.totalEntradas.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Salidas",
            value = stats.totalSalidas.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Promedio",
            value = String.format("%.1f", stats.promedioDiario),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Tarjeta individual de estadística
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tarjetas de estadísticas de ratings
 */
@Composable
fun RatingStatsCards(stats: RatingStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Promedio",
                value = String.format("%.1f ★", stats.promedioGeneral),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total",
                value = stats.totalCalificaciones.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Distribución",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                stats.distribucion.entries.sortedByDescending { it.key }.forEach { (stars, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$stars ★", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

/**
 * Selector de vista para ratings (Distribución / Por Categoría)
 */
@Composable
fun RatingViewSelector(
    selectedView: Int,
    onViewSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val views = listOf("Distribución", "Por Categoría")
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            views.forEachIndexed { index, view ->
                FilterChip(
                    selected = selectedView == index,
                    onClick = { onViewSelected(index) },
                    label = { Text(text = view, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Tarjetas de estadísticas de asistencias para ADMIN
 */
@Composable
fun AdminAttendanceStatsCards(stats: AttendanceStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(title = "Entradas", value = stats.totalEntradas.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "Salidas", value = stats.totalSalidas.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "Promedio", value = String.format("%.1f", stats.promedioDiario), modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(title = "Usuarios", value = stats.totalUsuarios.toString(), modifier = Modifier.weight(1f))
            stats.usuarioMasActivo?.let { (nombre, cantidad) ->
                Card(
                    modifier = Modifier.weight(2f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Más activo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(nombre.take(15), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1)
                        Text("$cantidad asist.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}

/**
 * Selector de rango de fechas
 */
@Composable
fun DateRangeSelector(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Rango de fechas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRange.values().forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeSelected(range) },
                        label = {
                            Text(
                                text = range.label,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Carga datos de asistencias desde Firebase con diferenciación USER/ADMIN
 */
private suspend fun loadAttendanceData(
    attendanceManager: AttendanceManager,
    usuario: Usuario,
    dateRange: DateRange
): Pair<List<ChartDataPoint>, AttendanceStats> {
    val endTimestamp = System.currentTimeMillis()
    val startTimestamp = endTimestamp - (dateRange.days * 24 * 60 * 60 * 1000L)

    val result = if (usuario.esAdmin()) {
        Log.d("ChartScreen", "ADMIN: Cargando todas las asistencias")
        attendanceManager.getAllAttendances()
    } else {
        // Obtener Firebase UID del usuario autenticado
        val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        if (firebaseUid != null) {
            Log.d("ChartScreen", "USER: Cargando asistencias con Firebase UID=$firebaseUid")
            attendanceManager.getUserAttendancesByDateRangeWithUid(
                firebaseUid = firebaseUid,
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp
            )
        } else {
            Log.w("ChartScreen", "USER: Firebase UID es null, intentando con usuarioId=${usuario.id}")
            attendanceManager.getUserAttendancesByDateRange(
                usuarioId = usuario.id,
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp
            )
        }
    }

    val registros = result.getOrNull() ?: emptyList()

    // Si es ADMIN, filtrar manualmente por fecha
    val registrosFiltrados = if (usuario.esAdmin()) {
        registros.filter { it.marcaTiempo in startTimestamp..endTimestamp }
    } else {
        registros
    }

    return processRegistrosToChartData(registrosFiltrados)
}

/**
 * Carga datos desde RegistroLocalRepository con rango de fechas
 */
private fun loadChartDataFromRepository(
    repository: RegistroLocalRepository,
    dateRange: DateRange
): Pair<List<ChartDataPoint>, AttendanceStats> {
    // Cargar todos los registros
    val todosLosRegistros = repository.cargarRegistros()

    // Filtrar por rango de fechas
    val endTimestamp = System.currentTimeMillis()
    val startTimestamp = endTimestamp - (dateRange.days * 24 * 60 * 60 * 1000L)

    val registros = todosLosRegistros.filter { registro ->
        registro.marcaTiempo >= startTimestamp && registro.marcaTiempo <= endTimestamp
    }

    return processRegistrosToChartData(registros)
}

/**
 * Procesa lista de RegistroAcceso a datos de gráfico
 */
private fun processRegistrosToChartData(
    registros: List<RegistroAcceso>
): Pair<List<ChartDataPoint>, AttendanceStats> {
    if (registros.isEmpty()) {
        // Si no hay datos, retornar datos de ejemplo
        val sampleData = ChartDataProcessor.generateSampleData()
        val sampleStats = AttendanceStats(
            totalEntradas = 0,
            totalSalidas = 0,
            promedioDiario = 0f
        )
        return Pair(sampleData, sampleStats)
    }

    // Agrupar registros por día
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val attendanceByDay = mutableMapOf<String, Int>()
    val attendanceByUser = mutableMapOf<String, Int>()

    // Contar entradas y salidas
    var totalEntradas = 0
    var totalSalidas = 0

    registros.forEach { registro ->
        // Formatear fecha (dd/MM)
        val fecha = dateFormat.format(registro.marcaTiempo)
        attendanceByDay[fecha] = attendanceByDay.getOrDefault(fecha, 0) + 1

        // Contar por tipo de acción
        when (registro.accion) {
            AccionAsistencia.ENTRADA -> totalEntradas++
            AccionAsistencia.SALIDA -> totalSalidas++
        }

        // Contar por usuario (para estadísticas ADMIN)
        val userName = registro.usuario.nombre
        attendanceByUser[userName] = attendanceByUser.getOrDefault(userName, 0) + 1
    }

    // Ordenar y obtener todos los días con datos (no limitado a 7)
    val chartPoints = attendanceByDay.entries
        .sortedBy { entry ->
            // Ordenar por timestamp real para tener orden cronológico
            registros.first {
                dateFormat.format(it.marcaTiempo) == entry.key
            }.marcaTiempo
        }
        .map { ChartDataPoint(it.key, it.value.toFloat()) }

    // Calcular usuario más activo
    val usuarioMasActivo = attendanceByUser.entries.maxByOrNull { it.value }?.let { it.key to it.value }

    // Calcular estadísticas
    val stats = AttendanceStats(
        totalEntradas = totalEntradas,
        totalSalidas = totalSalidas,
        porDia = attendanceByDay,
        promedioDiario = if (attendanceByDay.isNotEmpty()) {
            attendanceByDay.values.average().toFloat()
        } else 0f,
        porUsuario = attendanceByUser,
        totalUsuarios = attendanceByUser.size,
        usuarioMasActivo = usuarioMasActivo
    )

    return Pair(chartPoints, stats)
}

/**
 * Carga datos de ratings desde Firebase con diferenciación USER/ADMIN
 */
private suspend fun loadRatingData(
    ratingManager: RatingManager,
    usuario: Usuario
): Pair<List<ChartDataPoint>, RatingStats> {
    val result = if (usuario.esAdmin()) {
        ratingManager.calculateStats()
    } else {
        val userRatingsResult = ratingManager.getUserRatings(usuario.id.toString())
        val ratings = userRatingsResult.getOrNull() ?: emptyList()
        calculateUserRatingStats(ratings)
    }

    val stats = result.getOrNull() ?: RatingStats()
    val chartData = processRatingStatsToChartData(stats)
    return Pair(chartData, stats)
}

/**
 * Convierte RatingStats a datos de gráfico (distribución por estrellas)
 */
private fun processRatingStatsToChartData(stats: RatingStats): List<ChartDataPoint> {
    return stats.distribucion.entries
        .sortedBy { it.key }
        .map { ChartDataPoint("${it.key} ★", it.value.toFloat()) }
}

/**
 * Convierte RatingStats a datos de gráfico (promedio por categoría)
 */
private fun processRatingStatsByCategoryToChartData(stats: RatingStats): List<ChartDataPoint> {
    return stats.porCategoria.entries
        .filter { it.value > 0 }
        .map { ChartDataPoint(it.key.displayName, it.value) }
}

/**
 * Calcula estadísticas de ratings para un usuario específico
 */
private fun calculateUserRatingStats(ratings: List<Rating>): Result<RatingStats> {
    if (ratings.isEmpty()) return Result.success(RatingStats())

    val promedio = ratings.map { it.puntuacion }.average().toFloat()
    val distribucion = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
    ratings.forEach { rating ->
        val stars = rating.puntuacion.toInt().coerceIn(1, 5)
        distribucion[stars] = distribucion[stars]!! + 1
    }

    val porCategoria = RatingCategory.values().associateWith { categoria ->
        val categoryRatings = ratings.filter { it.categoria == categoria }
        if (categoryRatings.isEmpty()) 0f
        else categoryRatings.map { it.puntuacion }.average().toFloat()
    }

    return Result.success(
        RatingStats(
            promedioGeneral = promedio,
            totalCalificaciones = ratings.size,
            distribucion = distribucion,
            porCategoria = porCategoria
        )
    )
}
