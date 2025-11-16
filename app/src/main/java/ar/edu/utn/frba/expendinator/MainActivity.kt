package ar.edu.utn.frba.expendinator
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import ar.edu.utn.frba.expendinator.navigation.AppNavHost
import ar.edu.utn.frba.expendinator.navigation.NavigationController
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }

        // Verificar después de que todo esté listo
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val openScanner = intent.getBooleanExtra("openScanner", false)
        android.util.Log.d("MainActivity", "handleIntent openScanner = $openScanner")

        if (openScanner) {
            // Esperar a que Compose esté listo
            lifecycleScope.launch {
                kotlinx.coroutines.delay(300) // Esperar 300ms
                NavigationController.navigateToScanner()
            }
        }
    }
}