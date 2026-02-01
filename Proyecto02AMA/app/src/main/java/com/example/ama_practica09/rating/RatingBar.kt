package com.example.ama_practica09.rating

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Componente personalizado de barra de calificación con estrellas
 *
 * @param rating Calificación actual (0-5)
 * @param onRatingChanged Callback cuando cambia la calificación
 * @param maxStars Número máximo de estrellas (por defecto 5)
 * @param enabled Si el componente es interactivo
 * @param starSize Tamaño de las estrellas
 * @param showLabel Si mostrar el texto con el valor numérico
 */
@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    enabled: Boolean = true,
    starSize: Dp = 40.dp,
    showLabel: Boolean = true,
    starColor: Color = Color(0xFFFFD700) // Color dorado
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxStars) {
                Star(
                    filled = i <= rating.toInt(),
                    size = starSize,
                    color = starColor,
                    onClick = {
                        if (enabled) {
                            onRatingChanged(i.toFloat())
                        }
                    }
                )
                if (i < maxStars) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${rating.toInt()} de $maxStars estrellas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Componente individual de estrella
 */
@Composable
private fun Star(
    filled: Boolean,
    size: Dp,
    color: Color,
    onClick: () -> Unit
) {
    Icon(
        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = if (filled) "Estrella llena" else "Estrella vacía",
        tint = if (filled) color else Color.Gray.copy(alpha = 0.3f),
        modifier = Modifier
            .size(size)
            .clickable { onClick() }
    )
}

/**
 * Vista compacta de rating (solo para visualización)
 */
@Composable
fun CompactRatingView(
    rating: Float,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 20.dp,
    showValue: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            Icon(
                imageVector = if (i <= rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i <= rating.toInt()) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(starSize)
            )
        }
        if (showValue) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
