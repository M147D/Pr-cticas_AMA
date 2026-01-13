# Guía Completa de Pruebas de Seguridad - Sistema de Control de Acceso

## Prerrequisitos

### 1. Habilitar Logs en Android Studio
1. Abrir **Logcat** en Android Studio (View → Tool Windows → Logcat)
2. Filtrar por tags relevantes:
   - `AccessControlManager`
   - `SecureScreen`
   - `NavigationManager`
   - `SessionManager`

### 2. Usuarios de Prueba Disponibles

Según `UsuarioRepository.kt`, estos son los usuarios disponibles:

#### Usuarios con ROL USER:
- **Juan Pérez** (enabled: true) ✓ Usar este
- **Pedro Martínez** (enabled: true) ✓ Usar este
- **María García** (enabled: false) ✗ Deshabilitado

#### Usuarios con ROL ADMIN:
- **Admin Principal** (enabled: true) ✓ Usar este
- **Carlos Admin** (enabled: false) ✗ Deshabilitado

---

## PRUEBA 1: Usuario USER intentando entrar a pantalla ADMIN

### Objetivo
Verificar que un usuario con rol USER no pueda acceder a AdminScreen.

### Pasos Detallados:

#### 1. Iniciar sesión como USER

**Opción A: Login Tradicional**
1. Abrir la aplicación
2. En WelcomeScreen → Click "Comenzar"
3. En LoginScreen → Escribir: **"Juan Pérez"**
4. Click "INGRESAR"
5. Deberías ver HomeScreen con información del usuario

**Opción B: Google Sign-In (si está configurado)**
1. Abrir la aplicación
2. WelcomeScreen → Click "Comenzar"
3. LoginScreen → Click "Iniciar sesión con Google"
4. Seleccionar cuenta de Google
5. En HomeScreen

#### 2. Verificar que estás como USER

En HomeScreen deberías ver:
- Nombre del usuario
- Botón: "Acceder al Sistema de Asistencia"

#### 3. Intentar acceder a AdminScreen

**Método 1: Navegación Directa (si existe botón)**
- Si hay algún botón que diga "Admin" o "Panel de Administración", haz click

**Método 2: Acceso desde HomeScreen**
- Click en "Acceder al Sistema de Asistencia"
- Deberías ir a **UserScreen** (no a AdminScreen)

**Método 3: Forzar navegación (para desarrollo)**
Para probar directamente el control de acceso, puedes:
1. En el código, agregar temporalmente un botón en HomeScreen:
```kotlin
// En HomeScreen, agregar este botón de prueba:
Button(onClick = { navigateWithValidation("admin") }) {
    Text("PRUEBA: Ir a Admin")
}
```
2. Recompilar y ejecutar
3. Click en el botón de prueba

### Resultados Esperados:

#### ✅ Comportamiento Correcto:
1. **Diálogo de "Acceso Denegado"** aparece con el mensaje:
   ```
   Acceso Denegado
   Acceso denegado: Solo administradores
   [Botón: Entendido]
   ```

2. **Redirección automática** a HomeScreen (pantalla de fallback)

3. **Usuario NO ve AdminScreen** en ningún momento

#### 📊 Logs Esperados en Logcat:
```
D/AccessControlManager: Validando acceso a 'admin' para usuario: Juan Pérez
D/AccessControlManager: Usuario rol: USER, pantalla requiere: ADMIN
D/AccessControlManager: Acceso DENEGADO - Razón: Solo administradores
D/NavigationManager: Acceso denegado a admin: Solo administradores
```

#### ❌ Si Falla la Prueba:
- El usuario ve AdminScreen → **BUG CRÍTICO**
- No aparece diálogo → Verificar integración de AccessControlManager
- La app crashea → Revisar logs de error

---

## PRUEBA 2: Usuario ADMIN intentando entrar a pantalla USER

### Objetivo
Verificar que un usuario ADMIN no pueda acceder a UserScreen (registro de asistencia).

### Pasos Detallados:

#### 1. Iniciar sesión como ADMIN

**Login Tradicional:**
1. Abrir la aplicación (o hacer logout si ya estás logueado)
2. WelcomeScreen → Click "Comenzar"
3. LoginScreen → Escribir: **"Admin Principal"**
4. Click "INGRESAR"
5. Deberías ver HomeScreen

#### 2. Verificar que estás como ADMIN

En HomeScreen deberías ver tu nombre como "Admin Principal"

#### 3. Intentar acceder a UserScreen

**Método 1: Desde HomeScreen**
- Click en "Acceder al Sistema de Asistencia"
- Deberías ir a **AdminScreen** (no a UserScreen)

**Método 2: Forzar navegación (desarrollo)**
Agregar botón temporal en HomeScreen:
```kotlin
Button(onClick = { navigateWithValidation("user") }) {
    Text("PRUEBA: Ir a User")
}
```

### Resultados Esperados:

#### ✅ Comportamiento Correcto:
1. **Diálogo de "Acceso Denegado"** con mensaje:
   ```
   Acceso Denegado
   Esta pantalla es solo para usuarios
   [Botón: Entendido]
   ```

2. **Redirección a HomeScreen**

3. **Admin NO ve UserScreen**

#### 📊 Logs Esperados:
```
D/AccessControlManager: Validando acceso a 'user' para usuario: Admin Principal
D/AccessControlManager: Usuario rol: ADMIN, pantalla requiere: USER_ROLE
D/AccessControlManager: Acceso DENEGADO - Razón: Esta pantalla es solo para usuarios
```

---

## PRUEBA 3: Usuario sin sesión intentando entrar a pantallas protegidas

### Objetivo
Verificar que sin sesión activa no se pueda acceder a pantallas protegidas.

### Pasos Detallados:

#### 1. Asegurarse de NO tener sesión activa

**Opción A: Primera instalación**
- Instalar la app por primera vez
- Debería mostrar WelcomeScreen

**Opción B: Después de logout**
- Si ya estás logueado, hacer logout primero
- En HomeScreen → Click "Cerrar Sesión"
- Deberías volver a WelcomeScreen

**Opción C: Limpiar datos de la app**
- Settings → Apps → AMA Practica 08
- Storage → Clear Data
- Abrir app de nuevo

#### 2. Verificar que estás en WelcomeScreen

Deberías ver:
- Título: "Control de Asistencia EPN"
- Botón: "Comenzar"
- Botón: "Conoce a los Desarrolladores"

#### 3. Intentar acceder a pantallas protegidas

**Método para desarrollo:**
Agregar botones de prueba temporalmente en WelcomeScreen:

```kotlin
// En WelcomeScreen, agregar estos botones de prueba:
Column {
    Button(onClick = { navigateWithValidation("home") }) {
        Text("PRUEBA: Ir a Home")
    }
    Button(onClick = { navigateWithValidation("user") }) {
        Text("PRUEBA: Ir a User")
    }
    Button(onClick = { navigateWithValidation("admin") }) {
        Text("PRUEBA: Ir a Admin")
    }
}
```

#### 4. Probar cada pantalla

**Prueba 3.1: Intentar acceder a HomeScreen**
- Click botón de prueba "Ir a Home"

**Prueba 3.2: Intentar acceder a UserScreen**
- Click botón de prueba "Ir a User"

**Prueba 3.3: Intentar acceder a AdminScreen**
- Click botón de prueba "Ir a Admin"

### Resultados Esperados:

#### ✅ Comportamiento Correcto (para TODAS las pantallas protegidas):

1. **Diálogo de "Acceso Denegado"** aparece:
   ```
   Acceso Denegado
   Debes iniciar sesión para acceder
   [Botón: Entendido]
   ```

2. **Redirección a LoginScreen**

3. **Usuario NO ve ninguna pantalla protegida**

#### 📊 Logs Esperados:
```
D/AccessControlManager: Validando acceso a 'home'
D/SessionManager: SessionState: Inactive
D/AccessControlManager: Requiere autenticación
D/NavigationManager: Requiere autenticación para home
D/NavigationManager: Stack limpiado y navegando a login
```

#### ❌ Si Falla:
- Muestra pantalla protegida sin sesión → **BUG CRÍTICO DE SEGURIDAD**
- No redirige a login → Verificar navegación

---

## PRUEBA 4: Logout y verificación de botón "Atrás"

### Objetivo
Verificar que después de logout:
1. La sesión se cierra correctamente
2. El stack de navegación se limpia
3. No se puede volver a pantallas protegidas con el botón "Atrás"

### Pasos Detallados:

#### PARTE A: Logout desde UserScreen

##### 1. Preparación
1. Iniciar sesión como **Juan Pérez** (USER)
2. Navegar por varias pantallas:
   - WelcomeScreen → LoginScreen → HomeScreen → UserScreen

##### 2. Verificar que estás en UserScreen
- Deberías ver la pantalla de "Registro de Asistencia"
- Botón con icono de "Volver al inicio" en la esquina superior

##### 3. Hacer Logout
**Opción A:**
- Click en el icono de "Volver al inicio" en la TopBar

**Opción B (si cambiaste el callback a handleLogout):**
- El botón debería cerrar sesión directamente

##### 4. Verificar el logout
- Deberías ver **WelcomeScreen**
- NO deberías ver información del usuario

##### 5. Probar botón "Atrás" del dispositivo
1. Presionar botón "Atrás" del dispositivo Android (o back gesture)
2. Repetir varias veces

#### Resultados Esperados (PARTE A):

##### ✅ Comportamiento Correcto:

1. **Cierre de sesión exitoso:**
   - SessionManager.signOut() ejecutado
   - Datos de sesión eliminados de DataStore

2. **Navegación a WelcomeScreen:**
   - Usuario ve WelcomeScreen
   - No hay información de sesión visible

3. **Stack limpiado:**
   - Botón "Atrás" NO vuelve a UserScreen
   - Botón "Atrás" NO vuelve a HomeScreen
   - Botón "Atrás" NO vuelve a LoginScreen
   - Botón "Atrás" puede cerrar la app (comportamiento normal)

##### 📊 Logs Esperados:
```
D/SessionManager: Cerrando sesión...
D/SessionManager: Sesión cerrada exitosamente
D/NavigationManager: Limpiando stack de navegación (tamaño anterior: 4)
D/NavigationManager: Stack limpiado y navegando a welcome
```

##### ❌ Si Falla:
- Botón "Atrás" vuelve a UserScreen → **BUG CRÍTICO**
- Botón "Atrás" vuelve a HomeScreen → **BUG CRÍTICO**
- Sesión sigue activa → Verificar SessionManager.signOut()

---

#### PARTE B: Logout desde AdminScreen (con FLAG_SECURE)

##### 1. Preparación
1. Hacer logout si estás logueado
2. Iniciar sesión como **Admin Principal**
3. Navegar: WelcomeScreen → LoginScreen → HomeScreen → AdminScreen

##### 2. Verificar FLAG_SECURE en AdminScreen
**Antes de logout:**
1. Estando en AdminScreen, intentar hacer screenshot:
   - Android: Presionar Power + Volume Down
   - Emulador: Click en icono de cámara

2. Verificar logs:
```
D/SecureScreen: FLAG_SECURE activado para admin - Screenshots bloqueados
```

3. El screenshot debería:
   - **Fallar** (pantalla negra o mensaje de error)
   - O mostrar mensaje: "No se pueden capturar screenshots en pantallas protegidas"

##### 3. Hacer Logout desde AdminScreen
- Click en "Volver al inicio" o botón de logout
- Deberías ver WelcomeScreen

##### 4. Verificar FLAG_SECURE removido
**Después de logout:**
1. Estando en WelcomeScreen, intentar hacer screenshot
2. El screenshot debería **funcionar correctamente**

3. Verificar logs:
```
D/SecureScreen: FLAG_SECURE desactivado para admin - Screenshots permitidos
```

##### 5. Probar botón "Atrás"
1. Presionar botón "Atrás" del dispositivo
2. Repetir varias veces

#### Resultados Esperados (PARTE B):

##### ✅ Comportamiento Correcto:

1. **FLAG_SECURE activado en AdminScreen:**
   - Screenshots bloqueados
   - Log confirma activación

2. **Logout exitoso:**
   - Redirige a WelcomeScreen
   - SessionManager.signOut() ejecutado

3. **FLAG_SECURE removido:**
   - Screenshots funcionan en otras pantallas
   - Log confirma desactivación

4. **Stack limpiado:**
   - Botón "Atrás" NO vuelve a AdminScreen
   - Botón "Atrás" NO vuelve a pantallas protegidas

##### 📊 Logs Esperados:
```
D/SecureScreen: FLAG_SECURE activado para admin - Screenshots bloqueados
D/SessionManager: Cerrando sesión...
D/SecureScreen: FLAG_SECURE desactivado para admin - Screenshots permitidos
D/NavigationManager: Limpiando stack de navegación
D/NavigationManager: Stack limpiado y navegando a welcome
```

---

## PRUEBA 5: Usuario Deshabilitado (BONUS)

### Objetivo
Verificar que usuarios con `enabled = false` no puedan acceder.

### Pasos:
1. Intentar login como **"María García"** (enabled: false)
2. O como **"Carlos Admin"** (enabled: false)

### Resultados Esperados:
- LoginScreen: "Usuario no encontrado o inactivo"
- O si logra entrar, al navegar:
  ```
  Acceso Denegado
  Tu cuenta está deshabilitada
  ```

---

## Herramientas de Monitoreo

### 1. Logcat con Filtros

#### Filtro por Tag:
```
tag:AccessControlManager OR tag:SecureScreen OR tag:NavigationManager OR tag:SessionManager
```

#### Filtro por Nivel:
```
level:debug
```

### 2. Comandos ADB Útiles

#### Ver logs en tiempo real:
```bash
adb logcat | grep -E "AccessControl|SecureScreen|Navigation|SessionManager"
```

#### Limpiar logs:
```bash
adb logcat -c
```

#### Forzar cerrar app:
```bash
adb shell am force-stop com.example.ama_practica06
```

#### Limpiar datos de la app:
```bash
adb shell pm clear com.example.ama_practica06
```

---

## Checklist de Pruebas

Usa este checklist para asegurarte de completar todas las pruebas:

### ☐ PRUEBA 1: USER → ADMIN
- [ ] Login como USER exitoso
- [ ] Intento de acceso a AdminScreen
- [ ] Diálogo "Acceso denegado: Solo administradores"
- [ ] Redirección a HomeScreen
- [ ] Logs correctos en Logcat

### ☐ PRUEBA 2: ADMIN → USER
- [ ] Login como ADMIN exitoso
- [ ] Intento de acceso a UserScreen
- [ ] Diálogo "Esta pantalla es solo para usuarios"
- [ ] Redirección a HomeScreen
- [ ] Logs correctos en Logcat

### ☐ PRUEBA 3: Sin Sesión → Pantallas Protegidas
- [ ] Sin sesión activa verificado
- [ ] Intento acceso a HomeScreen
- [ ] Intento acceso a UserScreen
- [ ] Intento acceso a AdminScreen
- [ ] Diálogo "Debes iniciar sesión" en todos
- [ ] Redirección a LoginScreen
- [ ] Logs correctos

### ☐ PRUEBA 4A: Logout desde UserScreen
- [ ] Login como USER
- [ ] Navegación completa (Welcome→Login→Home→User)
- [ ] Logout ejecutado
- [ ] Redirección a WelcomeScreen
- [ ] Botón "Atrás" NO vuelve a UserScreen
- [ ] Botón "Atrás" NO vuelve a HomeScreen
- [ ] Stack limpiado en logs

### ☐ PRUEBA 4B: Logout desde AdminScreen + FLAG_SECURE
- [ ] Login como ADMIN
- [ ] Navegación a AdminScreen
- [ ] Screenshot bloqueado en AdminScreen
- [ ] Log "FLAG_SECURE activado"
- [ ] Logout ejecutado
- [ ] Screenshot funciona en WelcomeScreen
- [ ] Log "FLAG_SECURE desactivado"
- [ ] Botón "Atrás" NO vuelve a AdminScreen
- [ ] Stack limpiado

### ☐ PRUEBA 5: Usuario Deshabilitado
- [ ] Intento login como María García
- [ ] Mensaje "Usuario no encontrado o inactivo"
- [ ] No puede acceder al sistema

---

## Documentación de Resultados

### Formato de Reporte

Para cada prueba, documenta:

```markdown
## PRUEBA X: [Nombre]
**Fecha:** [DD/MM/YYYY]
**Tester:** [Nombre]
**Dispositivo:** [Emulador/Físico - Modelo]
**Android Version:** [Ej: API 30]

### Resultado: ✅ PASS / ❌ FAIL

### Pasos Ejecutados:
1. [Paso 1]
2. [Paso 2]
...

### Resultados Obtenidos:
- [Comportamiento observado]

### Screenshots:
[Capturas de pantalla si aplica]

### Logs Relevantes:
```
[Logs de Logcat]
```

### Observaciones:
[Cualquier nota adicional]
```

---

## Problemas Comunes y Soluciones

### Problema 1: No aparece diálogo de "Acceso Denegado"
**Causa:** AccessControlManager no está integrado correctamente
**Solución:** Verificar que navigateWithValidation() se esté usando

### Problema 2: Botón "Atrás" vuelve a pantallas protegidas
**Causa:** Stack no se limpió correctamente
**Solución:** Verificar que handleLogout() llame a navigationManager.clearAndNavigateTo()

### Problema 3: FLAG_SECURE no funciona
**Causa:** Puede ser un emulador sin soporte
**Solución:** Probar en dispositivo físico

### Problema 4: Sesión persiste después de logout
**Causa:** SessionManager.signOut() no se ejecuta
**Solución:** Verificar implementación de handleLogout()

---

## Conclusión

Al completar todas estas pruebas exitosamente, habrás verificado que:

✅ El control de acceso basado en roles funciona correctamente
✅ Las pantallas protegidas no son accesibles sin sesión
✅ El logout limpia correctamente la sesión y el stack
✅ FLAG_SECURE protege la información sensible
✅ La navegación es segura y no permite bypass de seguridad

**¡Tu sistema de control de acceso está funcionando correctamente!**
