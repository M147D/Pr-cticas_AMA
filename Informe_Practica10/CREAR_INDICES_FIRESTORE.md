# Crear Índices Compuestos en Firestore

## ❌ Problema

Las queries de la pantalla de estadísticas requieren índices compuestos que no existen:

```
FAILED_PRECONDITION: The query requires an index
```

## ✅ Solución

### Método 1: Usar los Enlaces Automáticos (MÁS RÁPIDO)

Firebase proporciona enlaces directos para crear los índices. Clic en estos enlaces:

**1. Índice para Asistencias (firebaseUid + marcaTiempo):**

https://console.firebase.google.com/v1/r/project/sitemaasistencia-num05/firestore/indexes?create_composite=Clpwcm9qZWN0cy9zaXRlbWFhc2lzdGVuY2lhLW51bTA1L2RhdGFiYXNlcy8oZGVmYXVsdCkvY29sbGVjdGlvbkdyb3Vwcy9hc2lzdGVuY2lhcy9pbmRleGVzL18QARoRCgxmaXJlYmFzZVVpZBABGg8KC21hcmNhVGllbXBvEAIaDAoIX19uYW1lX18QAg

**2. Índice para Ratings (usuarioId + fecha):**

https://console.firebase.google.com/v1/r/project/sitemaasistencia-num05/firestore/indexes?create_composite=ClZwcm9qZWN0cy9zaXRlbWFhc2lzdGVuY2lhLW51bTA1L2RhdGFiYXNlcy8oZGVmYXVsdCkvY29sbGVjdGlvbkdyb3Vwcy9yYXRpbmdzL2luZGV4ZXMvXxABGg0KCXVzdWFyaW9JZBABGgkKBWZlY2hhEAIaDAoIX19uYW1lX18QAg

**Pasos:**
1. Haz clic en cada enlace
2. Se abrirá Firebase Console con el índice pre-configurado
3. Clic en **"Create Index"** (Crear índice)
4. Espera 2-5 minutos mientras se crea el índice
5. Verás un indicador de progreso
6. Cuando esté listo, aparecerá con estado "Enabled" (Habilitado)

---

### Método 2: Crear Manualmente desde Firebase Console

Si los enlaces no funcionan, crea los índices manualmente:

#### 1. Ir a Firebase Console

1. Ve a https://console.firebase.google.com/
2. Selecciona tu proyecto: **sitemaasistencia-num05**
3. En el menú lateral: **Firestore Database**
4. Pestaña: **Indexes** (Índices)
5. Clic en **"Create Index"** (Crear índice)

#### 2. Índice para Asistencias

**Configuración:**
- Collection ID: `asistencias`
- Fields to index:
  - Campo 1: `firebaseUid` | Order: **Ascending**
  - Campo 2: `marcaTiempo` | Order: **Descending**

Clic en **Create**.

#### 3. Índice para Ratings

**Configuración:**
- Collection ID: `ratings`
- Fields to index:
  - Campo 1: `usuarioId` | Order: **Ascending**
  - Campo 2: `fecha` | Order: **Descending**

Clic en **Create**.

---

## 🔍 Verificar que los Índices están Creados

1. Ve a Firebase Console → Firestore Database → Indexes
2. Deberías ver:

```
Collection: asistencias
Fields: firebaseUid (ASC), marcaTiempo (DESC)
Status: Enabled ✓

Collection: ratings
Fields: usuarioId (ASC), fecha (DESC)
Status: Enabled ✓
```

---

## ⏱️ Tiempo de Creación

- Los índices toman **2-5 minutos** en crearse
- Mientras se crean, el estado será "Building" (Construyendo)
- Una vez listos, el estado cambiará a "Enabled" (Habilitado)

---

## 🧪 Probar después de Crear los Índices

1. Espera a que ambos índices estén en estado "Enabled"
2. **Cierra la app completamente** (Force Stop)
3. **Limpia la caché**: Settings → Apps → Asistencia → Clear Cache
4. Vuelve a abrir la app
5. Ve a la pantalla de estadísticas
6. Ahora debería cargar los datos correctamente sin errores

---

## 📝 Notas Importantes

### Sobre el cambio de `usuarioId` a `firebaseUid`

**Problema identificado:**
```kotlin
// En AuthState.kt línea 44
id = uid.hashCode(), // ⚠️ Genera IDs negativos como -1608591253
```

**Solución implementada:**
- Las asistencias ahora se consultan usando `firebaseUid` (String de Firebase Auth)
- Los ratings se siguen consultando con `usuarioId` (pero se necesita índice)
- Esto evita problemas con hashCodes negativos

### ¿Por qué se necesitan estos índices?

Firestore requiere índices compuestos cuando una query usa:
1. **Múltiples campos** en filtros (whereEqualTo + whereGreaterThan)
2. **Ordenamiento** (orderBy)

Nuestras queries hacen ambas cosas:
```kotlin
.whereEqualTo("firebaseUid", ...)
.whereGreaterThanOrEqualTo("marcaTiempo", ...)
.whereLessThanOrEqualTo("marcaTiempo", ...)
.orderBy("marcaTiempo", DESCENDING)
```

Por eso requieren índice compuesto.

---

## ✅ Checklist Final

- [ ] Índice de asistencias creado (firebaseUid + marcaTiempo)
- [ ] Índice de ratings creado (usuarioId + fecha)
- [ ] Ambos índices en estado "Enabled"
- [ ] App cerrada completamente (Force Stop)
- [ ] Caché limpiada
- [ ] App reabierta
- [ ] Pantalla de estadísticas funciona sin errores

---

**Fecha:** 2026-01-08
**Proyecto:** sitemaasistencia-num05
**Versión de la app:** Informe_Practica10

**Archivos modificados:**
- `AttendanceManager.kt` - Agregada función `getUserAttendancesByDateRangeWithUid()`
- `ChartScreen.kt` - Actualizada para usar Firebase UID en lugar de usuario.id
