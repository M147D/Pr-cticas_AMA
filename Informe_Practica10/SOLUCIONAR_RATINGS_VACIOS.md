# Solución: Ratings con usuarioId Vacío

## ❌ Problema Detectado

Los ratings existentes en Firestore tienen `usuarioId: ""` (vacío), por eso la pantalla de estadísticas no los encuentra.

**Ejemplo del documento problemático:**
```
usuarioId: ""  // ⚠️ VACÍO
usuarioNombre: "Stalin Garcia"
puntuacion: 5
categoria: "USABILIDAD"
```

## ✅ Solución Implementada

He modificado `RatingManager.kt` para que automáticamente llene el `usuarioId` con el Firebase UID cuando esté vacío:

```kotlin
// En submitRating() y updateRating()
val firebaseUid = currentUser?.uid ?: ""
val finalUsuarioId = if (rating.usuarioId.isEmpty()) firebaseUid else rating.usuarioId
```

**Esto arregla:**
- ✅ Nuevos ratings se guardarán con usuarioId correcto
- ✅ Ratings actualizados tendrán el usuarioId correcto

## 🔧 Cómo Arreglar los Ratings Existentes

### Opción 1: Actualizar Ratings desde la App (RECOMENDADO)

1. **Abre la app**
2. **Ve a la pantalla de Ratings** (calificar sistema)
3. **Cambia cualquier estrella** (aunque sea la misma calificación)
4. **Clic en "Enviar Calificaciones"**

Esto ejecutará `updateRating()` que automáticamente agregará el `usuarioId` correcto.

### Opción 2: Eliminar y Crear Nuevos Ratings

1. **Ve a Firebase Console** → Firestore Database
2. **Colección "ratings"**
3. **Elimina los documentos** con `usuarioId: ""`
4. **Vuelve a la app** y califica de nuevo

---

## 🧪 Verificar que Funciona

### Antes de la Solución (ERROR):
```
// Log de ChartScreen
RatingManager: UID buscado: -1608591253
Error: FAILED_PRECONDITION: The query requires an index
```

### Después de la Solución (FUNCIONANDO):
```
// Log de ChartScreen
RatingManager: Usuario autenticado: true
RatingManager: UID: jfV5XhKHX6dKXFuEw1FGE8mwz3D3
RatingManager: Rating usuarioId final: jfV5XhKHX6dKXFuEw1FGE8mwz3D3
RatingManager: Ratings encontrados: 5
```

### Verificar en Firebase Console:

Ve a Firestore Database → ratings → Abre un documento:

**Antes (INCORRECTO):**
```
usuarioId: ""  // ❌ Vacío
```

**Después (CORRECTO):**
```
usuarioId: "jfV5XhKHX6dKXFuEw1FGE8mwz3D3"  // ✅ Firebase UID
```

---

## 📊 Probar la Pantalla de Estadísticas

Una vez que hayas actualizado los ratings:

1. **Force Stop** de la app
2. **Clear Cache**
3. **Abre la app**
4. **Ve a Estadísticas**
5. **Clic en tab "Ratings"**
6. Deberías ver:
   - ✅ Promedio general
   - ✅ Total de calificaciones
   - ✅ Distribución por estrellas
   - ✅ Gráfico de barras/líneas/circular

---

## 🔍 Debug: Verificar Logs

Si sigues teniendo problemas, verifica los logs:

```bash
# Filtrar logs de RatingManager
adb logcat -s RatingManager

# Deberías ver:
RatingManager: Usuario autenticado: true
RatingManager: UID: [tu-firebase-uid]
RatingManager: Rating usuarioId final: [tu-firebase-uid]
RatingManager: Rating guardado exitosamente
```

---

## 📝 Notas Importantes

### ¿Por qué estaba vacío el usuarioId?

En `RatingScreen.kt` línea 55:
```kotlin
val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
```

Si `currentUser?.uid` era null (por alguna razón de timing), se asignaba un string vacío "".

### ¿Cómo se solucionó?

Ahora `RatingManager` verifica automáticamente y llena el `usuarioId` justo antes de guardar:
```kotlin
// Si rating.usuarioId está vacío, usa el Firebase UID actual
val finalUsuarioId = if (rating.usuarioId.isEmpty()) firebaseUid else rating.usuarioId
```

Esto garantiza que SIEMPRE haya un `usuarioId` válido, incluso si RatingScreen no lo proporcionó.

---

## ✅ Checklist

- [ ] Modificaciones de RatingManager compiladas
- [ ] App instalada con los cambios
- [ ] Ratings actualizados desde la app (o eliminados y recreados)
- [ ] Verificado en Firebase Console que usuarioId ya no está vacío
- [ ] Pantalla de Estadísticas → Tab "Ratings" funciona
- [ ] Se ven los gráficos correctamente

---

**Fecha:** 2026-01-08
**Archivos modificados:**
- `rating/RatingManager.kt` - submitRating() y updateRating() ahora llenan usuarioId automáticamente
