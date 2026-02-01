package com.example.ama_practica09.location

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Manejador de permisos de ubicación usando Accompanist Permissions
 *
 * Solicita los permisos necesarios para geolocalización:
 * - ACCESS_FINE_LOCATION: GPS preciso
 * - ACCESS_COARSE_LOCATION: Ubicación aproximada
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionsGranted: @Composable () -> Unit,
    onPermissionsDenied: @Composable (MultiplePermissionsState) -> Unit = { state ->
        DefaultPermissionDeniedContent(state)
    }
) {
    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = locationPermissions)

    when {
        permissionsState.allPermissionsGranted -> {
            onPermissionsGranted()
        }
        else -> {
            onPermissionsDenied(permissionsState)
        }
    }
}

/**
 * Contenido por defecto cuando los permisos son denegados
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DefaultPermissionDeniedContent(
    permissionsState: MultiplePermissionsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Permisos de Ubicación",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Esta función requiere acceso a tu ubicación para:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                PermissionFeatureItem(
                    text = "Mostrar tu posición en el mapa"
                )
                PermissionFeatureItem(
                    text = "Detectar cuando estás en el aula"
                )
                PermissionFeatureItem(
                    text = "Validar tu registro de asistencia"
                )

                Spacer(modifier = Modifier.height(24.dp))

                val textToShow = if (permissionsState.shouldShowRationale) {
                    "Los permisos de ubicación son necesarios para el funcionamiento " +
                    "de la geolocalización. Por favor, concédelos para continuar."
                } else {
                    "Necesitamos acceso a tu ubicación para verificar tu asistencia. " +
                    "Tus datos de ubicación se manejan de forma segura."
                }

                Text(
                    text = textToShow,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Conceder Permisos")
                }
            }
        }
    }
}

/**
 * Item de característica que requiere el permiso
 */
@Composable
private fun PermissionFeatureItem(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

/**
 * Composable que solicita permisos automáticamente al cargar
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermissionsOnLaunch(
    onResult: (Boolean) -> Unit
) {
    val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionsState = rememberMultiplePermissionsState(permissions = locationPermissions)

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        onResult(permissionsState.allPermissionsGranted)
    }
}

/**
 * Verifica si los permisos de ubicación están disponibles
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionsState(): MultiplePermissionsState {
    val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    return rememberMultiplePermissionsState(permissions = locationPermissions)
}
