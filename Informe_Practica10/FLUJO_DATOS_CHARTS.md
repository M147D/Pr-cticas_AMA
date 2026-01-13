# Flujo de Datos: Sistema de Gráficos Estadísticos

## 📊 Cómo Funcionan los Gráficos

Este documento explica el flujo completo de datos desde que un usuario registra su asistencia hasta que ve los gráficos estadísticos.

---

## 1️⃣ Registro de Asistencia

### Origen de los Datos

**Pantalla:** `UserScreen` (Registro de Asistencia)

Cuando un usuario registra entrada/salida:

```kotlin
// En UserScreen, el usuario hace clic en "Registrar Entrada" o "Registrar Salida"
val nuevoRegistro = RegistroAcceso(
    usuario = usuarioActual,
    accion = AccionAsistencia.ENTRADA, // o SALIDA
    ubicacion = ubicacionActual,
    marcaTiempo = System.currentTimeMillis() // Timestamp actual
)

// Se guarda en SharedPreferences
repository.agregarRegistro(nuevoRegistro)
```

### Datos Guardados

Cada registro incluye:
- **Usuario**: ID, nombre, correo, edad, rol
- **Acción**: ENTRADA o SALIDA
- **Ubicación**: Dentro o fuera del rango
- **Timestamp**: Fecha y hora exacta (milisegundos)

---

## 2️⃣ Almacenamiento Local

### SharedPreferences

**Archivo:** `RegistroLocalRepository.kt`

Los datos se guardan como JSON en SharedPreferences:

```json
[
  {
    "usuarioId": 1,
    "usuarioNombre": "Juan Pérez",
    "accion": "ENTRADA",
    "ubicacionTipo": "DENTRO",
    "marcaTiempo": 1704643200000
  },
  {
    "usuarioId": 1,
    "usuarioNombre": "Juan Pérez",
    "accion": "SALIDA",
    "ubicacionTipo": "DENTRO",
    "marcaTiempo": 1704671200000
  }
]
```

**Ventajas:**
- ✅ Persistencia local sin internet
- ✅ Acceso rápido
- ✅ No requiere configuración de Firebase
- ✅ Privacidad: datos solo en el dispositivo

---

## 3️⃣ Carga de Datos en ChartScreen

### Flujo de Carga

**Archivo:** `ChartScreen.kt`

```kotlin
// 1. Al abrir la pantalla
ChartScreen(userId, onBackClick)

// 2. Se inicializa el repositorio
val repository = RegistroLocalRepository(context)

// 3. Se cargan los registros
val registros = repository.cargarRegistros()

// 4. Se procesan los datos
loadChartDataFromRepository(repository)
```

---

## 4️⃣ Procesamiento de Datos

### Función: `loadChartDataFromRepository()`

**Paso 1: Cargar Registros**
```kotlin
val registros = repository.cargarRegistros()
// Retorna: List<RegistroAcceso>
```

**Paso 2: Agrupar por Fecha**
```kotlin
val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
val attendanceByDay = mutableMapOf<String, Int>()

registros.forEach { registro ->
    val fecha = dateFormat.format(registro.marcaTiempo)
    attendanceByDay[fecha] = attendanceByDay.getOrDefault(fecha, 0) + 1
}

// Resultado: {"07/01": 5, "06/01": 3, "05/01": 7}
```

**Paso 3: Contar Entradas y Salidas**
```kotlin
var totalEntradas = 0
var totalSalidas = 0

registros.forEach { registro ->
    when (registro.accion) {
        AccionAsistencia.ENTRADA -> totalEntradas++
        AccionAsistencia.SALIDA -> totalSalidas++
    }
}
```

**Paso 4: Crear Puntos del Gráfico**
```kotlin
val chartPoints = attendanceByDay.entries
    .sortedBy { entry ->
        // Ordenar cronológicamente por timestamp
        registros.first {
            dateFormat.format(it.marcaTiempo) == entry.key
        }.marcaTiempo
    }
    .takeLast(7) // Últimos 7 días
    .map { ChartDataPoint(it.key, it.value.toFloat()) }

// Resultado: List<ChartDataPoint>
// [("01/01", 5.0), ("02/01", 3.0), ("03/01", 7.0), ...]
```

**Paso 5: Calcular Estadísticas**
```kotlin
val stats = AttendanceStats(
    totalEntradas = totalEntradas,
    totalSalidas = totalSalidas,
    porDia = attendanceByDay,
    promedioDiario = attendanceByDay.values.average().toFloat()
)
```

---

## 5️⃣ Visualización en Gráficos

### Tres Tipos de Gráficos

**1. Gráfico de Barras**
- Muestra asistencias por día
- Altura = número de registros
- Color dinámico según valor

**2. Gráfico de Líneas**
- Tendencia de asistencias
- Conecta puntos cronológicamente
- Suavizado con PathEffect

**3. Gráfico Circular**
- Distribución proporcional
- Cada sector = día
- Área proporcional a registros

---

## 6️⃣ Actualización en Tiempo Real

### Botón "Actualizar Datos"

```kotlin
Button(onClick = { cargarDatos() }) {
    Text("Actualizar Datos")
}

fun cargarDatos() {
    isLoading = true
    val result = loadChartDataFromRepository(repository)
    chartData = result.first
    stats = result.second
    isLoading = false
}
```

**Cuándo actualizar:**
- Al abrir la pantalla (automático)
- Al hacer clic en "Actualizar Datos"
- Después de registrar nueva asistencia

---

## 📈 Ejemplo Completo de Flujo

### Escenario: Usuario registra 3 entradas y 2 salidas en una semana

**Lunes 01/01:**
- 08:00 AM - ENTRADA → Guardado
- 05:00 PM - SALIDA → Guardado

**Martes 02/01:**
- 08:15 AM - ENTRADA → Guardado

**Miércoles 03/01:**
- 08:30 AM - ENTRADA → Guardado
- 05:30 PM - SALIDA → Guardado

### Datos en SharedPreferences

```json
[
  {"accion": "ENTRADA", "marcaTiempo": 1704099600000}, // Lun 08:00
  {"accion": "SALIDA", "marcaTiempo": 1704132000000},  // Lun 17:00
  {"accion": "ENTRADA", "marcaTiempo": 1704186900000}, // Mar 08:15
  {"accion": "ENTRADA", "marcaTiempo": 1704273000000}, // Mié 08:30
  {"accion": "SALIDA", "marcaTiempo": 1704305400000}   // Mié 17:30
]
```

### Procesamiento

```kotlin
// Agrupar por día
"01/01" → 2 registros (ENTRADA + SALIDA)
"02/01" → 1 registro (ENTRADA)
"03/01" → 2 registros (ENTRADA + SALIDA)

// Estadísticas
totalEntradas = 3
totalSalidas = 2
promedioDiario = 1.67
```

### Gráfico Resultante

```
Asistencias por Día

  2  ▓▓▓  ▓▓▓
  1  ▓▓▓  ▓▓▓  ▓▓▓
  0  ─────────────
     01/01 02/01 03/01
```

---

## 🔍 Puntos Clave

### 1. No Requiere Internet
- Todos los datos están en el dispositivo
- SharedPreferences persiste entre sesiones
- Funcionamiento 100% offline

### 2. Tiempo Real
- Los gráficos se actualizan inmediatamente
- No hay sincronización con servidor
- Datos disponibles instantáneamente

### 3. Privacidad
- Los datos nunca salen del dispositivo
- No se envían a Firebase (a menos que se implemente)
- Control total del usuario sobre sus datos

### 4. Últimos 7 Días
- Los gráficos muestran solo los últimos 7 días con datos
- Optimizado para visualización clara
- Evita sobrecarga de información

---

## 🛠️ Cómo Agregar Más Datos

### Para Testing

Si quieres probar con más datos, puedes:

1. **Usar la app normalmente**: Registra entradas/salidas varias veces
2. **Cambiar la fecha del dispositivo**: Registra en diferentes días
3. **Agregar datos programáticamente** (solo para desarrollo):

```kotlin
// En código de testing
val repository = RegistroLocalRepository(context)
repeat(10) { i ->
    val registro = RegistroAcceso(
        usuario = usuarioActual,
        accion = if (i % 2 == 0) AccionAsistencia.ENTRADA else AccionAsistencia.SALIDA,
        ubicacion = Ubicacion.DentroDelRango("Prueba"),
        marcaTiempo = System.currentTimeMillis() - (i * 86400000L) // i días atrás
    )
    repository.agregarRegistro(registro)
}
```

---

## 🎯 Ventajas del Enfoque Actual

✅ **Simple**: No requiere configuración de Firebase
✅ **Rápido**: Acceso instantáneo a datos locales
✅ **Privado**: Datos solo en el dispositivo
✅ **Confiable**: No depende de conexión a internet
✅ **Eficiente**: SharedPreferences optimizado para este uso

---

## 🔄 Posibles Mejoras Futuras

1. **Sincronización Firebase**: Backup en la nube (opcional)
2. **Filtros de fecha**: Seleccionar rango personalizado
3. **Exportar CSV**: Generar reporte descargable
4. **Comparación**: Ver stats de múltiples usuarios (admin)
5. **Notificaciones**: Alertas de patrones inusuales

---

**Última actualización:** 2026-01-07
**Versión:** 1.0
