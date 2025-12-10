package com.example.ama_practica08.accesscontrol

import android.util.Log

/**
 * Gestor de navegación con control de back stack
 *
 * Responsabilidades:
 * - Mantener un stack de navegación de pantallas visitadas
 * - Validar acceso antes de navegar
 * - Limpiar el stack al hacer logout para prevenir volver a pantallas protegidas
 * - Manejar navegación hacia atrás con validación
 *
 * @param accessControlManager Gestor de control de acceso para validar navegaciones
 */
class NavigationManager(
    private val accessControlManager: AccessControlManager
) {
    companion object {
        private const val TAG = "NavigationManager"
    }

    // Stack de navegación (lista mutable de pantallas visitadas)
    private val navigationStack = mutableListOf<String>()

    /**
     * Navega a una nueva pantalla con validación de acceso
     *
     * @param targetScreen Pantalla a la que navegar
     * @param onNavigate Callback para ejecutar la navegación (actualizar estado)
     * @param onAccessDenied Callback cuando el acceso es denegado
     */
    fun navigateTo(
        targetScreen: String,
        onNavigate: (String) -> Unit,
        onAccessDenied: (String, String) -> Unit
    ) {
        val result = accessControlManager.validateAccess(targetScreen)

        when (result) {
            is AccessResult.Granted -> {
                // Acceso concedido - agregar al stack y navegar
                navigationStack.add(targetScreen)
                onNavigate(targetScreen)
                Log.d(TAG, "Navegando a $targetScreen. Stack: ${navigationStack.takeLast(3)}")
            }

            is AccessResult.Denied -> {
                // Acceso denegado - ejecutar callback
                Log.w(TAG, "Acceso denegado a $targetScreen: ${result.reason}")
                onAccessDenied(result.reason, result.fallbackScreen)
            }

            is AccessResult.RequiresAuth -> {
                // Requiere autenticación - limpiar stack y navegar a login
                Log.d(TAG, "Requiere autenticación para $targetScreen")
                clearStack()
                navigationStack.add("login")
                onNavigate("login")
            }
        }
    }

    /**
     * Navega hacia atrás de forma segura
     * Valida que el usuario tenga acceso a la pantalla anterior
     *
     * @param currentScreen Pantalla actual
     * @param onNavigate Callback para ejecutar la navegación
     * @return true si se pudo navegar atrás, false si no hay pantalla anterior
     */
    fun navigateBack(
        currentScreen: String,
        onNavigate: (String) -> Unit
    ): Boolean {
        if (navigationStack.size <= 1) {
            Log.d(TAG, "No se puede navegar atrás - stack vacío o una sola pantalla")
            return false // No se puede retroceder más
        }

        // Remover pantalla actual del stack
        navigationStack.removeLastOrNull()
        val previousScreen = navigationStack.lastOrNull() ?: "welcome"

        // Validar acceso a la pantalla anterior
        val result = accessControlManager.validateAccess(previousScreen)

        when (result) {
            is AccessResult.Granted -> {
                // Tiene acceso - navegar
                onNavigate(previousScreen)
                Log.d(TAG, "Navegando atrás a $previousScreen")
                return true
            }

            else -> {
                // No tiene acceso - limpiar y volver a welcome
                Log.w(TAG, "No tiene acceso a pantalla anterior $previousScreen - volviendo a welcome")
                clearStack()
                navigationStack.add("welcome")
                onNavigate("welcome")
                return true
            }
        }
    }

    /**
     * Limpia completamente el stack de navegación
     * Se usa al hacer logout para prevenir volver a pantallas protegidas
     */
    fun clearStack() {
        Log.d(TAG, "Limpiando stack de navegación (tamaño anterior: ${navigationStack.size})")
        navigationStack.clear()
    }

    /**
     * Limpia el stack y navega a una pantalla específica
     * Usado principalmente para logout
     *
     * @param targetScreen Pantalla a la que navegar (ej: "welcome")
     * @param onNavigate Callback para ejecutar la navegación
     */
    fun clearAndNavigateTo(
        targetScreen: String,
        onNavigate: (String) -> Unit
    ) {
        clearStack()
        navigationStack.add(targetScreen)
        onNavigate(targetScreen)
        Log.d(TAG, "Stack limpiado y navegando a $targetScreen")
    }

    /**
     * Obtiene el stack actual de navegación (para debugging)
     * @return Lista inmutable con las pantallas en el stack
     */
    fun getStack(): List<String> = navigationStack.toList()

    /**
     * Obtiene el tamaño actual del stack
     * @return Número de pantallas en el stack
     */
    fun getStackSize(): Int = navigationStack.size
}
