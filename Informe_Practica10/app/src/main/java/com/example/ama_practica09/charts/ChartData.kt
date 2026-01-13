package com.example.ama_practica09.charts

import androidx.compose.ui.graphics.Color

/**
 * Modelo de datos para un punto en el gráfico
 */
data class ChartDataPoint(
    val label: String,      // Etiqueta del punto (fecha, categoría, etc.)
    val value: Float,       // Valor numérico
    val color: Color? = null // Color opcional para este punto específico
)

/**
 * Configuración del gráfico
 */
data class ChartConfig(
    val title: String = "Estadísticas",
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val animationDuration: Int = 1000, // Duración de la animación en ms
    val maxValue: Float? = null, // Valor máximo del eje Y (null = auto)
    val minValue: Float = 0f,     // Valor mínimo del eje Y
    val chartType: ChartType = ChartType.BAR
)

/**
 * Tipos de gráficos soportados
 */
enum class ChartType {
    BAR,        // Gráfico de barras
    LINE,       // Gráfico de líneas
    PIE         // Gráfico circular
}

/**
 * Estadísticas de asistencia
 */
data class AttendanceStats(
    val totalEntradas: Int = 0,
    val totalSalidas: Int = 0,
    val porDia: Map<String, Int> = emptyMap(),  // Asistencias por día
    val porMes: Map<String, Int> = emptyMap(),  // Asistencias por mes
    val promedioDiario: Float = 0f,

    // Campos adicionales para ADMIN
    val porUsuario: Map<String, Int> = emptyMap(),  // Asistencias por usuario
    val totalUsuarios: Int = 0,                     // Cantidad de usuarios con asistencias
    val usuarioMasActivo: Pair<String, Int>? = null // (nombre, cantidad) del usuario más activo
)

/**
 * Utilidades para procesar datos de gráficos
 */
object ChartDataProcessor {

    /**
     * Genera colores dinámicos basados en el valor
     */
    fun getColorForValue(
        value: Float,
        maxValue: Float,
        lowColor: Color = Color(0xFF4CAF50),      // Verde
        mediumColor: Color = Color(0xFFFFC107),   // Amarillo
        highColor: Color = Color(0xFFF44336)      // Rojo
    ): Color {
        val percentage = if (maxValue > 0) value / maxValue else 0f

        return when {
            percentage < 0.33f -> lowColor
            percentage < 0.66f -> mediumColor
            else -> highColor
        }
    }

    /**
     * Normaliza los valores para que se ajusten al área del gráfico
     */
    fun normalizeValues(
        values: List<Float>,
        targetMax: Float = 100f
    ): List<Float> {
        val max = values.maxOrNull() ?: 1f
        if (max == 0f) return values

        return values.map { (it / max) * targetMax }
    }

    /**
     * Calcula el valor máximo redondeado para el eje Y
     */
    fun calculateMaxYValue(values: List<Float>): Float {
        val max = values.maxOrNull() ?: 100f

        // Redondear al siguiente múltiplo de 10, 100, etc.
        val magnitude = when {
            max < 10 -> 10f
            max < 100 -> 10f
            max < 1000 -> 100f
            else -> 1000f
        }

        return ((max / magnitude).toInt() + 1) * magnitude
    }

    /**
     * Genera datos de ejemplo para pruebas
     */
    fun generateSampleData(): List<ChartDataPoint> {
        return listOf(
            ChartDataPoint("Lun", 45f),
            ChartDataPoint("Mar", 60f),
            ChartDataPoint("Mié", 38f),
            ChartDataPoint("Jue", 72f),
            ChartDataPoint("Vie", 55f),
            ChartDataPoint("Sáb", 28f),
            ChartDataPoint("Dom", 15f)
        )
    }
}
