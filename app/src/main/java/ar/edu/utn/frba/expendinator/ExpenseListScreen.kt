package ar.edu.utn.frba.expendinator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel,
    onAddClicked: () -> Unit = {},
    onExpenseClicked: (Expense) -> Unit = {}
) {
    val expenses by viewModel.uiState.collectAsState()

    // ---- Cámara: estado + launchers ----
    var photo by remember { mutableStateOf<Bitmap?>(null) }

    // Abre la cámara y devuelve un Bitmap "preview"
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        photo = bitmap
        // TODO: convertir a ByteArray y mandar al backend para OCR
    }

    // Pide permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch()
        // TODO: mostrar Snackbar si no se otorgó permiso
    }

    // Context disponible en composición
    val context = LocalContext.current


    val onFabClick = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            cameraLauncher.launch()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        onAddClicked() // opcional para tracking/navegación futura
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gastos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Text("+")
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin gastos todavía")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses, key = { it.id }) { e ->
                        ExpenseItem(e, onClick = { onExpenseClicked(e) })
                    }
                }
            }

            // Preview chiquita de la última foto capturada (opcional)
            photo?.let {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier.size(96.dp)
                    )
                    Text("Última captura lista para OCR")
                }
            }
        }
    }
}

@Composable
private fun ExpenseItem(e: Expense, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    e.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${e.category} • ${e.date}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "$ ${"%,.2f".format(e.amount)}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseItemPreview() {
    ExpenseItem(
        e = Expense("0", "Ejemplo", 12345.67, "Categoría", "2025-09-21"),
        onClick = {}
    )
}
