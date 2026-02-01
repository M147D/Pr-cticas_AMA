@file:OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)

package com.example.ama_practica09.location

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ama_practica09.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Pantalla principal de geolocalización
 *
 * Contiene 3 tabs:
 * 1. Mapa - Muestra el mapa con geovallas
 * 2. Geofences - Estado de las geovallas
 * 3. Historial - Historial de ubicaciones y eventos
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GeolocationScreen(
    usuario: Usuario,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Managers
    val locationManager = remember { LocationManager(context) }
    val geofenceManager = remember { GeofenceManager(context) }
    val locationRepository = remember { LocationRepository(context) }
    val geofenceNotificationManager = remember { GeofenceNotificationManager(context) }

    // Estados
    val locationState by locationManager.locationState.collectAsState()
    val currentLocation by locationManager.currentLocation.collectAsState()
    val geofenceStatus by geofenceManager.geofenceStatus.collectAsState()
    val isMonitoring by geofenceManager.isMonitoring.collectAsState()
    val latestGeofenceEvent by geofenceManager.geofenceEvents.collectAsState(initial = GeofenceEvent.None)

    // Procesar eventos de geovalla para notificaciones
    LaunchedEffect(latestGeofenceEvent) {
        if (latestGeofenceEvent !is GeofenceEvent.None) {
            geofenceNotificationManager.processGeofenceEvent(latestGeofenceEvent)
            Log.d("GeolocationScreen", "Evento de geovalla procesado: $latestGeofenceEvent")
        }
    }

    // Chequeo periódico del Campus (cada 10 minutos)
    LaunchedEffect(locationState) {
        if (locationState is LocationState.Available) {
            val status = (locationState as LocationState.Available).geofenceStatus
            geofenceNotificationManager.periodicCampusCheck(status.isInsideCampus)
        }
    }

    // Tab seleccionado
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mapa", "Geofences", "Historial")

    // Historial
    var locationHistory by remember { mutableStateOf<List<LocationHistoryEntry>>(emptyList()) }
    var geofenceEvents by remember { mutableStateOf<List<GeofenceEventEntry>>(emptyList()) }

    // Registrar GeofenceManager en el BroadcastReceiver
    LaunchedEffect(Unit) {
        GeofenceBroadcastReceiver.setGeofenceManager(geofenceManager)
    }

    // Limpiar al salir
    DisposableEffect(Unit) {
        onDispose {
            locationManager.stopLocationUpdates()
            GeofenceBroadcastReceiver.clearGeofenceManager()
        }
    }

    // Cargar historial
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 2) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: usuario.id.toString()
            scope.launch {
                locationRepository.getLocationHistoryForUser(userId).onSuccess {
                    locationHistory = it
                }
                locationRepository.getGeofenceEventsForUser(userId).onSuccess {
                    geofenceEvents = it
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geolocalización") },
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
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Contenido según tab seleccionado
            LocationPermissionHandler(
                onPermissionsGranted = {
                    // Iniciar actualizaciones de ubicación
                    LaunchedEffect(Unit) {
                        locationManager.startLocationUpdates()
                    }

                    when (selectedTabIndex) {
                        0 -> MapTab(
                            locationState = locationState,
                            currentLocation = currentLocation,
                            geofenceZones = locationManager.getGeofenceZones(),
                            geofenceStatus = geofenceStatus,
                            onRefreshClick = {
                                locationManager.getLastLocation { }
                            }
                        )
                        1 -> GeofencesTab(
                            geofenceManager = geofenceManager,
                            geofenceStatus = geofenceStatus,
                            isMonitoring = isMonitoring,
                            onStartMonitoring = {
                                geofenceManager.registerAllGeofences(
                                    onSuccess = {
                                        Log.d("GeolocationScreen", "Monitoreo iniciado")
                                    },
                                    onError = { e ->
                                        Log.e("GeolocationScreen", "Error al iniciar monitoreo", e)
                                    }
                                )
                            },
                            onStopMonitoring = {
                                geofenceManager.removeAllGeofences()
                            }
                        )
                        2 -> HistoryTab(
                            locationHistory = locationHistory,
                            geofenceEvents = geofenceEvents,
                            onRefresh = {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: usuario.id.toString()
                                scope.launch {
                                    locationRepository.getLocationHistoryForUser(userId).onSuccess {
                                        locationHistory = it
                                    }
                                    locationRepository.getGeofenceEventsForUser(userId).onSuccess {
                                        geofenceEvents = it
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

/**
 * Tab del mapa con geovallas
 */
@Composable
fun MapTab(
    locationState: LocationState,
    currentLocation: GeoPoint?,
    geofenceZones: List<GeofenceZone>,
    geofenceStatus: GeofenceStatus,
    onRefreshClick: () -> Unit
) {
    val cameraPositionState = rememberGeofenceCameraState()

    // Animar cámara cuando cambia la ubicación
    AnimateCameraToLocation(
        cameraPositionState = cameraPositionState,
        targetLocation = currentLocation
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when (locationState) {
            is LocationState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Obteniendo ubicación...")
                    }
                }
            }
            is LocationState.Available -> {
                GeofenceMapView(
                    userLocation = locationState.geoPoint,
                    geofenceZones = geofenceZones,
                    cameraPositionState = cameraPositionState,
                    showUserLocation = true
                )
            }
            is LocationState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${locationState.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is LocationState.Unavailable -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ubicación no disponible: ${locationState.reason}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is LocationState.PermissionRequired -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Se requieren permisos de ubicación")
                }
            }
        }

        // Card de estado de geovalla
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeofenceStatusIndicator(status = geofenceStatus)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = geofenceStatus.getActiveZoneName(),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (geofenceStatus.canRegisterAttendance())
                                "Puede registrar asistencia"
                            else
                                "Fuera de zona de registro",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (geofenceStatus.canRegisterAttendance()) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Puede registrar",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // FAB para refrescar ubicación
        FloatingActionButton(
            onClick = onRefreshClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refrescar ubicación"
            )
        }
    }
}

/**
 * Tab de gestión de geovallas
 */
@Composable
fun GeofencesTab(
    geofenceManager: GeofenceManager,
    geofenceStatus: GeofenceStatus,
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit
) {
    val zones = geofenceManager.getPredefinedZones()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card de estado de monitoreo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMonitoring)
                        Color(0xFFE8F5E9)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Estado del Monitoreo",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isMonitoring) "Activo" else "Inactivo",
                            color = if (isMonitoring) Color(0xFF4CAF50) else Color.Gray
                        )
                    }

                    androidx.compose.material3.Button(
                        onClick = if (isMonitoring) onStopMonitoring else onStartMonitoring
                    ) {
                        Text(if (isMonitoring) "Detener" else "Iniciar")
                    }
                }
            }
        }

        // Título de zonas
        item {
            Text(
                text = "Zonas de Geovalla",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Lista de zonas
        items(zones) { zone ->
            GeofenceZoneCard(
                zone = zone,
                isInside = when (zone) {
                    is GeofenceZone.EdificioSecundario -> geofenceStatus.isInsideEdificioSecundario
                    is GeofenceZone.EdificioPrincipal -> geofenceStatus.isInsideEdificioPrincipal
                    is GeofenceZone.EdificioMecanica -> geofenceStatus.isInsideMecanica
                    is GeofenceZone.Campus -> geofenceStatus.isInsideCampus
                }
            )
        }
    }
}

/**
 * Card para una zona de geovalla
 */
@Composable
fun GeofenceZoneCard(
    zone: GeofenceZone,
    isInside: Boolean
) {
    val zoneColor = when (zone) {
        is GeofenceZone.EdificioSecundario -> Color(0xFF4CAF50)
        is GeofenceZone.EdificioPrincipal -> Color(0xFF2196F3)
        is GeofenceZone.EdificioMecanica -> Color(0xFF9C27B0)
        is GeofenceZone.Campus -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInside) zoneColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(zoneColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = zoneColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Radio: ${zone.radiusMeters.toInt()}m",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (zone.allowsAttendance)
                        "Permite registro de asistencia"
                    else
                        "Solo monitoreo",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isInside) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Dentro",
                    tint = zoneColor
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fuera",
                    tint = Color.Gray
                )
            }
        }
    }
}

/**
 * Tab de historial
 */
@Composable
fun HistoryTab(
    locationHistory: List<LocationHistoryEntry>,
    geofenceEvents: List<GeofenceEventEntry>,
    onRefresh: () -> Unit
) {
    var showLocations by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selector de tipo de historial
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.FilterChip(
                selected = showLocations,
                onClick = { showLocations = true },
                label = { Text("Ubicaciones") }
            )
            androidx.compose.material3.FilterChip(
                selected = !showLocations,
                onClick = { showLocations = false },
                label = { Text("Eventos") }
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
            }
        }

        if (showLocations) {
            if (locationHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay historial de ubicaciones",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(locationHistory) { entry ->
                        LocationHistoryCard(entry = entry)
                    }
                }
            }
        } else {
            if (geofenceEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos de geovalla",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(geofenceEvents) { event ->
                        GeofenceEventCard(event = event)
                    }
                }
            }
        }
    }
}

/**
 * Card para entrada de historial de ubicación
 */
@Composable
fun LocationHistoryCard(entry: LocationHistoryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.zoneName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lat: ${String.format("%.6f", entry.latitude)}, Lon: ${String.format("%.6f", entry.longitude)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(entry.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (entry.isInsideEdificioSecundario) {
                    StatusChip(text = "FIEE", color = Color(0xFF4CAF50))
                } else if (entry.isInsideEdificioPrincipal) {
                    StatusChip(text = "Fac. Ciencias", color = Color(0xFF2196F3))
                } else if (entry.isInsideMecanica) {
                    StatusChip(text = "Fac. Mecánica", color = Color(0xFF9C27B0))
                } else if (entry.isInsideCampus) {
                    StatusChip(text = "Campus", color = Color(0xFFFF9800))
                } else {
                    StatusChip(text = "Fuera", color = Color.Gray)
                }
            }
        }
    }
}

/**
 * Card para evento de geovalla
 */
@Composable
fun GeofenceEventCard(event: GeofenceEventEntry) {
    val eventColor = when (event.eventType) {
        "ENTER" -> Color(0xFF4CAF50)
        "EXIT" -> Color(0xFFF44336)
        "DWELL" -> Color(0xFF2196F3)
        else -> Color.Gray
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(eventColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (event.eventType) {
                        "ENTER" -> "E"
                        "EXIT" -> "S"
                        "DWELL" -> "P"
                        else -> "?"
                    },
                    fontWeight = FontWeight.Bold,
                    color = eventColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${event.eventType} - ${event.zoneName}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDate(event.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Chip de estado
 */
@Composable
fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Formatea fecha
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
