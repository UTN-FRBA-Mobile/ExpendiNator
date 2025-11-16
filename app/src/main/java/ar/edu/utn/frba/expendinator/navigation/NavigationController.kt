package ar.edu.utn.frba.expendinator.navigation

import androidx.navigation.NavController

object NavigationController {
    var navController: NavController? = null
    var triggerFab: (() -> Unit)? = null  // ← AGREGAR ESTO

    fun navigateToScanner() {
        android.util.Log.d("NavigationController", "navigateToScanner called")
        // Navegar a Main
        navController?.navigate(Dest.Main.route)
        // Trigger el FAB después de un delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            triggerFab?.invoke()
        }, 500)
    }
}