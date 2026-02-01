package com.example.ama_practica09.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componentes de Google Maps Compose
 *
 * Composables aislados para evitar recomposiciones innecesarias
 */

/**
 * Mapa de Google con geovallas
 *
 * @param userLocation Ubicación actual del usuario
 * @param geofenceZones Zonas de geovallas a mostrar
 * @param cameraPositionState Estado de la cámara (debe ser recordado externamente)
 * @param onMapClick Callback cuando se hace click en el mapa
 */
@Composable
fun GeofenceMapView(
    userLocation: GeoPoint?,
    geofenceZones: List<GeofenceZone>,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
    onMapClick: (LatLng) -> Unit = {},
    showUserLocation: Boolean = true,
    mapType: MapType = MapType.NORMAL
) {
    val mapProperties = remember(showUserLocation, mapType) {
        MapProperties(
            isMyLocationEnabled = showUserLocation,
            mapType = mapType
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = showUserLocation,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapClick = onMapClick
    ) {
        // Dibujar círculos de geovallas
        geofenceZones.forEach { zone ->
            GeofenceCircle(zone = zone)
        }

        // Marcador de la ubicación del usuario (si no se usa myLocationEnabled)
        if (userLocation != null && !showUserLocation) {
            UserLocationMarker(location = userLocation)
        }

        // Marcador del centro principal (Campus y Facultad de Ciencias)
        CenterMarker(center = GeoPoint.EPN_CENTER, title = "Facultad de Ciencias", snippet = "Campus EPN - Facultad de Ciencias")

        // Marcador de la FIEE
        CenterMarker(center = GeoPoint.EDIFICIO_SECUNDARIO_CENTER, title = "FIEE", snippet = "Facultad de Ingeniería Eléctrica y Electrónica")

        // Marcador de Facultad de Mecánica
        CenterMarker(center = GeoPoint.FACULTAD_MECANICA_CENTER, title = "Fac. Mecánica", snippet = "Facultad de Ingeniería Mecánica")
    }
}

/**
 * Círculo que representa una geovalla
 */
@Composable
fun GeofenceCircle(zone: GeofenceZone) {
    val (fillColor, strokeColor) = when (zone) {
        is GeofenceZone.EdificioSecundario -> Pair(
            Color(0x2200FF00), // Verde semi-transparente
            Color(0xFF00FF00)  // Verde
        )
        is GeofenceZone.EdificioPrincipal -> Pair(
            Color(0x220000FF), // Azul semi-transparente
            Color(0xFF0000FF)  // Azul
        )
        is GeofenceZone.EdificioMecanica -> Pair(
            Color(0x229C27B0), // Púrpura semi-transparente
            Color(0xFF9C27B0)  // Púrpura
        )
        is GeofenceZone.Campus -> Pair(
            Color(0x22FF9800), // Naranja semi-transparente
            Color(0xFFFF9800)  // Naranja
        )
    }

    Circle(
        center = LatLng(zone.center.latitude, zone.center.longitude),
        radius = zone.radiusMeters.toDouble(),
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeWidth = 3f,
        tag = zone.id
    )
}

/**
 * Marcador para la ubicación del usuario
 */
@Composable
fun UserLocationMarker(location: GeoPoint) {
    Marker(
        state = MarkerState(
            position = LatLng(location.latitude, location.longitude)
        ),
        title = "Tu ubicación",
        snippet = "Precisión: ${location.accuracy.toInt()}m"
    )
}

/**
 * Marcador del centro (EPN)
 */
@Composable
fun CenterMarker(
    center: GeoPoint,
    title: String = "EPN - Centro",
    snippet: String = "Escuela Politécnica Nacional"
) {
    Marker(
        state = MarkerState(
            position = LatLng(center.latitude, center.longitude)
        ),
        title = title,
        snippet = snippet
    )
}

/**
 * Polyline para mostrar el historial de ubicaciones
 */
@Composable
fun LocationHistoryPolyline(
    locations: List<GeoPoint>,
    color: Color = Color(0xFF2196F3),
    width: Float = 5f
) {
    if (locations.size < 2) return

    val points = locations.map { LatLng(it.latitude, it.longitude) }

    Polyline(
        points = points,
        color = color,
        width = width
    )
}

/**
 * Crea y recuerda el estado de la cámara
 * Sobrevive a recomposiciones y rotaciones
 */
@Composable
fun rememberGeofenceCameraState(
    initialPosition: GeoPoint = GeoPoint.EPN_CENTER,
    initialZoom: Float = 17f
): CameraPositionState {
    return rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(initialPosition.latitude, initialPosition.longitude),
            initialZoom
        )
    }
}

/**
 * Mueve la cámara a una ubicación específica con animación
 */
@Composable
fun AnimateCameraToLocation(
    cameraPositionState: CameraPositionState,
    targetLocation: GeoPoint?,
    zoom: Float = 17f
) {
    LaunchedEffect(targetLocation) {
        targetLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    zoom
                ),
                durationMs = 1000
            )
        }
    }
}

/**
 * Indicador visual del estado de geovalla
 */
@Composable
fun GeofenceStatusIndicator(
    status: GeofenceStatus,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        status.isInsideMecanica -> Color(0xFF9C27B0) // Púrpura
        status.isInsideEdificioSecundario -> Color(0xFF4CAF50) // Verde
        status.isInsideEdificioPrincipal -> Color(0xFF2196F3) // Azul
        status.isInsideCampus -> Color(0xFFFF9800) // Naranja
        else -> Color(0xFFF44336) // Rojo
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = indicatorColor,
                shape = CircleShape
            )
    )
}

/**
 * Marcadores para múltiples puntos de asistencia
 */
@Composable
fun AttendancePointsMarkers(
    attendancePoints: List<AttendancePoint>
) {
    attendancePoints.forEach { point ->
        Marker(
            state = MarkerState(
                position = LatLng(point.geoPoint.latitude, point.geoPoint.longitude)
            ),
            title = "Registro de asistencia",
            snippet = "${point.zoneName} - ${formatTimestamp(point.timestamp)}"
        )
    }
}

/**
 * Formatea timestamp a string legible
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
