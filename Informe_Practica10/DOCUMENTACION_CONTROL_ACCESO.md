# Documentación del Sistema de Control de Acceso - AMA Practica 08

## Resumen Ejecutivo

Se ha implementado exitosamente un **Sistema de Control de Asistencia EPN** completo con control de acceso robusto basado en roles. El proyecto `Informe_Practica08` ahora incluye toda la funcionalidad de `Proyecto01AMA` más un módulo avanzado de control de acceso.

---

## Estructura del Proyecto

### Archivos Totales Implementados: 73+ archivos

#### 1. Archivos Kotlin (43 archivos)
- **37 archivos** copiados y renombrados de Proyecto01AMA
- **6 archivos nuevos** del módulo `accesscontrol`

#### 2. Recursos
- **Valores XML**: colors.xml, strings.xml, themes.xml
- **Drawables**: 5 archivos (desa1.png, desa2.png, iconos)
- **Mipmaps**: 12 archivos (iconos del launcher en todas las densidades)
- **XML Config**: backup_rules.xml, data_extraction_rules.xml

#### 3. Configuración
- **build.gradle.kts** (app y root) con Jetpack Compose y Firebase
- **google-services.json** configurado para `com.example.ama_practica08`
- **AndroidManifest.xml** con permisos y servicios de Firebase

---

## Módulo AccessControl (NUEVO)

### Ubicación
`app/src/main/java/com/example/ama_practica08/accesscontrol/`

### Componentes

#### 1. **AccessResult.kt**
Sealed class que representa los resultados de validación:
- `Granted`: Acceso concedido
- `Denied(reason, fallbackScreen)`: Acceso denegado con razón y pantalla de fallback
- `RequiresAuth(message)`: Requiere autenticación

#### 2. **ScreenPermissions.kt**
Define niveles de permiso:
- `PUBLIC`: Sin autenticación (Login, Welcome, Register)
- `AUTHENTICATED`: Solo sesión activa (HomeScreen)
- `USER_ROLE`: Solo rol USER (UserScreen)
- `ADMIN_ROLE`: Solo rol ADMIN (AdminScreen)
- `ANY_ROLE`: Cualquier rol autenticado

#### 3. **AccessControlManager.kt**
Gestor central de control de acceso:
- Configura permisos por pantalla
- Valida acceso según SessionState y rol del usuario
- Determina si requiere FLAG_SECURE
- Verifica usuario habilitado

**Configuración de Pantallas:**
```kotlin
"welcome", "login", "register", "menu" → PUBLIC
"home" → AUTHENTICATED
"user" → USER_ROLE (solo Rol.USER)
"admin" → ADMIN_ROLE (solo Rol.ADMIN, FLAG_SECURE=true)
```

#### 4. **ProtectedRoute.kt**
Composable HOC para proteger rutas:
- Valida acceso al renderizar
- Muestra contenido solo si acceso concedido
- Ejecuta callbacks de denegación o autenticación requerida

#### 5. **SecureScreen.kt**
Composable para FLAG_SECURE:
- Aplica FLAG_SECURE automáticamente en pantallas configuradas
- Bloquea screenshots y grabación de pantalla
- Se limpia automáticamente al salir

#### 6. **NavigationManager.kt**
Gestor de navegación con back stack:
- Mantiene stack de pantallas visitadas
- Valida acceso antes de navegar
- Limpia stack al hacer logout
- Previene volver a pantallas protegidas

---

## Pantallas y Permisos

### Pantallas Públicas (sin sesión)
1. **WelcomeScreen**: Pantalla inicial
2. **LoginScreen**: Login tradicional o Google
3. **RegisterScreen**: Crear nueva cuenta
4. **MenuPrincipal**: Información de desarrolladores

### Pantallas Protegidas (requieren sesión activa)
5. **HomeScreen**: Dashboard post-login (AUTHENTICATED)
6. **UserScreen**: Registro de asistencia (solo ROL.USER)
7. **AdminScreen**: Visualización de registros (solo ROL.ADMIN, FLAG_SECURE)

---

## Funcionalidades Implementadas

### ✅ Control de Acceso Basado en Roles
- Validación automática antes de navegar
- Restricción por rol (USER vs ADMIN)
- Verificación de usuario habilitado

### ✅ Validación de Sesión
- Integrado con SessionManager existente
- Verifica sesión activa con Firebase Auth
- Restauración de sesión persistente

### ✅ Diálogos de "Acceso Denegado"
- AlertDialog informativo cuando se deniega acceso
- Mensaje personalizado según razón
- Redirección automática a pantalla permitida

### ✅ Limpieza de Back Stack
- Stack limpiado completamente al hacer logout
- Previene volver a pantallas protegidas
- NavigationManager gestiona el stack de forma segura

### ✅ FLAG_SECURE en AdminScreen
- Bloquea screenshots automáticamente
- Solo activo en AdminScreen
- Se remueve al salir de la pantalla

### ✅ Protección de Navegación
- Todos los callbacks usan `navigateWithValidation()`
- Logout centralizado con `handleLogout()`
- ProtectedRoute envuelve pantallas sensibles

---

## Integración en MainActivity.kt

### Managers Creados
```kotlin
val sessionManager = remember { SessionManager(context) }
val accessControlManager = remember { AccessControlManager(sessionManager) }
val navigationManager = remember { NavigationManager(accessControlManager) }
```

### Funciones Implementadas

#### navigateWithValidation()
```kotlin
fun navigateWithValidation(targetScreen: String) {
    val result = accessControlManager.validateAccess(targetScreen)
    when (result) {
        is AccessResult.Granted -> pantalla = targetScreen
        is AccessResult.Denied -> {
            mensajeAccesoDenegado = result.reason
            mostrarDialogoAccesoDenegado = true
            pantalla = result.fallbackScreen
        }
        is AccessResult.RequiresAuth -> {
            mensajeAccesoDenegado = result.message
            mostrarDialogoAccesoDenegado = true
            pantalla = "login"
        }
    }
}
```

#### handleLogout()
```kotlin
fun handleLogout() {
    scope.launch {
        sessionManager.signOut()
        navigationManager.clearAndNavigateTo("welcome") { pantalla = it }
    }
}
```

### AlertDialog para Acceso Denegado
```kotlin
if (mostrarDialogoAccesoDenegado) {
    AlertDialog(
        onDismissRequest = { mostrarDialogoAccesoDenegado = false },
        title = { Text("Acceso Denegado") },
        text = { Text(mensajeAccesoDenegado) },
        confirmButton = {
            Button(onClick = { mostrarDialogoAccesoDenegado = false }) {
                Text("Entendido")
            }
        }
    )
}
```

### Envolturas de Seguridad

#### SecureScreen (toda la navegación)
```kotlin
SecureScreen(
    screenName = pantalla,
    accessControlManager = accessControlManager
) {
    when (pantalla) {
        // ... todas las pantallas
    }
}
```

#### ProtectedRoute (pantallas protegidas)
```kotlin
"admin" -> {
    ProtectedRoute(
        targetScreen = "admin",
        accessControlManager = accessControlManager,
        onAccessDenied = { reason, fallback ->
            mensajeAccesoDenegado = reason
            mostrarDialogoAccesoDenegado = true
            pantalla = fallback
        },
        onRequiresAuth = { navigateWithValidation("login") }
    ) {
        usuarioActual?.let { usuario ->
            AdminScreen(usuario = usuario, onLogout = { navigateWithValidation("home") })
        }
    }
}
```

---

## Flujo de Validación de Acceso

### 1. Usuario Intenta Acceder a Pantalla
```
Usuario hace clic en botón
    ↓
navigateWithValidation("admin")
    ↓
accessControlManager.validateAccess("admin")
```

### 2. Validación de Acceso
```
SessionState.Active?
    ↓
¿Usuario habilitado?
    ↓
¿Rol coincide con pantalla?
    ↓
AccessResult.Granted/Denied/RequiresAuth
```

### 3. Resultado
```
Granted → Navegar a pantalla
Denied → Mostrar diálogo + Redirigir a fallback
RequiresAuth → Mostrar diálogo + Redirigir a Login
```

---

## Escenarios de Prueba

### Test 1: Usuario USER intenta acceder a AdminScreen
**Esperado:**
- Diálogo: "Acceso denegado: Solo administradores"
- Redirige a HomeScreen
- No puede ver datos de administrador

### Test 2: Usuario sin sesión intenta acceder a UserScreen
**Esperado:**
- Diálogo: "Debes iniciar sesión para acceder"
- Redirige a LoginScreen
- Debe iniciar sesión primero

### Test 3: Logout desde UserScreen
**Esperado:**
- SessionManager.signOut() ejecutado
- NavigationManager limpia stack
- Redirige a WelcomeScreen
- Back button NO permite volver a UserScreen

### Test 4: Logout desde AdminScreen
**Esperado:**
- SessionManager.signOut() ejecutado
- FLAG_SECURE removido automáticamente
- Stack limpiado
- Redirige a WelcomeScreen
- Screenshots ahora permitidos en otras pantallas

### Test 5: FLAG_SECURE en AdminScreen
**Esperado:**
- Al entrar: `FLAG_SECURE activado para admin - Screenshots bloqueados`
- Intentar screenshot → bloqueado (pantalla negra o error)
- Al salir: `FLAG_SECURE desactivado para admin - Screenshots permitidos`
- Screenshots funcionan normalmente en otras pantallas

### Test 6: Restauración de Sesión
**Esperado:**
- Login como USER
- Cerrar app (NO logout)
- Reabrir app
- SessionManager restaura sesión
- Muestra HomeScreen automáticamente

### Test 7: Usuario Deshabilitado
**Esperado:**
- Usuario con `enabled = false`
- Intenta navegar a pantalla protegida
- Diálogo: "Tu cuenta está deshabilitada"
- Redirige a LoginScreen

---

## Compilación y Ejecución

### Requisitos
- Android Studio Arctic Fox o superior
- JDK 11
- Gradle 8.0+
- Android SDK API 24-36

### Compilar desde Android Studio
1. Abrir proyecto `Informe_Practica08` en Android Studio
2. Sincronizar Gradle (Sync Project with Gradle Files)
3. Build → Make Project
4. Run → Run 'app'

### Compilar desde Terminal (Windows)
```bash
cd Informe_Practica08
gradlew.bat clean
gradlew.bat assembleDebug
```

### Compilar desde Terminal (Linux/Mac)
```bash
cd Informe_Practica08
./gradlew clean
./gradlew assembleDebug
```

### APK de Salida
```
Informe_Practica08/app/build/outputs/apk/debug/app-debug.apk
```

---

## Arquitectura del Sistema

### Capas

#### 1. Capa de Presentación (UI)
- Jetpack Compose
- Screens: Welcome, Login, Register, Home, User, Admin
- Diálogos y navegación

#### 2. Capa de Control de Acceso (accesscontrol)
- AccessControlManager
- ProtectedRoute
- SecureScreen
- NavigationManager

#### 3. Capa de Sesión (session)
- SessionManager
- SessionRepository
- SessionState (Active, Inactive, Loading, Error)

#### 4. Capa de Autenticación (auth)
- GoogleAuthManager
- Firebase Authentication
- Login tradicional

#### 5. Capa de Datos (data)
- UsuarioRepository
- RegistroLocalRepository
- DataStore para persistencia

#### 6. Capa de Firebase (firebase)
- FirebaseConfig
- MyFirebaseMessagingService
- NotificationService

#### 7. Capa de Modelos (models)
- Usuario (con Rol: USER/ADMIN)
- RegistroAcceso
- AccionAsistencia
- Ubicacion

#### 8. Capa de Reglas (rules)
- PolicyRules (validación de asistencia)
- AccessRules (reglas de acceso)
- ValidationRules

---

## Dependencias Principales

```kotlin
// Jetpack Compose
implementation("androidx.activity.compose:1.x.x")
implementation("androidx.compose.material3:1.x.x")

// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")

// Google Sign-In
implementation("com.google.android.gms:play-services-auth:20.7.0")

// Coil (imágenes)
implementation("io.coil-kt:coil-compose:2.5.0")

// DataStore (persistencia)
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

---

## Características de Seguridad

### 1. Validación de Roles
- Verificación estricta de rol antes de acceder a pantallas
- Usuario USER no puede acceder a AdminScreen
- Usuario ADMIN no puede registrar asistencia (solo USER)

### 2. FLAG_SECURE
- Bloquea screenshots en AdminScreen
- Protege información sensible de administrador
- Se remueve automáticamente al salir

### 3. Limpieza de Back Stack
- Stack completamente limpiado al logout
- Previene volver a pantallas protegidas con back button
- Navegación segura con validación constante

### 4. Sesión Persistente
- SessionManager con DataStore
- Verificación de sesión al iniciar app
- Soporte para login tradicional y Firebase

### 5. Validación de Usuario Habilitado
- Usuarios deshabilitados no pueden acceder
- Verificación en cada navegación
- Mensaje informativo al usuario

---

## Logs del Sistema

### AccessControlManager
```
D/AccessControlManager: Validando acceso a 'admin' para usuario ID: 123
D/AccessControlManager: Usuario rol: USER, pantalla requiere: ADMIN
D/AccessControlManager: Acceso DENEGADO - Razón: Solo administradores
```

### SecureScreen
```
D/SecureScreen: FLAG_SECURE activado para admin - Screenshots bloqueados
D/SecureScreen: FLAG_SECURE desactivado para admin - Screenshots permitidos
```

### NavigationManager
```
D/NavigationManager: Navegando a admin. Stack: [welcome, login, home, admin]
D/NavigationManager: Limpiando stack de navegación (tamaño anterior: 4)
D/NavigationManager: Stack limpiado y navegando a welcome
```

### SessionManager
```
D/SessionManager: Verificando sesión activa...
D/SessionManager: Sesión activa restaurada para: Juan Pérez
D/SessionManager: Cerrando sesión...
D/SessionManager: Sesión cerrada exitosamente
```

---

## Próximos Pasos

### Compilar y Probar
1. Abrir proyecto en Android Studio
2. Sincronizar Gradle
3. Ejecutar en emulador o dispositivo
4. Probar todos los escenarios de prueba

### Usuarios de Prueba
Según `UsuarioRepository.kt`:

**Usuarios normales (USER):**
- Juan Pérez (enabled: true)
- María García (enabled: false)
- Pedro Martínez (enabled: true)

**Administradores (ADMIN):**
- Admin Principal (enabled: true)
- Carlos Admin (enabled: false)

---

## Contacto y Soporte

**Desarrolladores:**
- Desarrollador 1: Miguel Pastuña
- Desarrollador 2: Stalin Garcia

**Práctica:** AMA Practica 08
**Institución:** Escuela Politécnica Nacional (EPN)
**Materia:** Aplicaciones Móviles Avanzadas

---

## Licencia

Este proyecto es parte de las prácticas académicas de la materia Aplicaciones Móviles Avanzadas de la Escuela Politécnica Nacional.

---

**Última actualización:** 10 de diciembre de 2025
**Versión:** 1.0
**Estado:** ✅ COMPLETADO Y LISTO PARA PRUEBAS
