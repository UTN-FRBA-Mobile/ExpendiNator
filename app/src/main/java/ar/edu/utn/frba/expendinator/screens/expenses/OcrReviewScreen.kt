package ar.edu.utn.frba.expendinator.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.expendinator.model.dto.OcrItem

@Composable
fun OcrReviewScreen(
    ocrVm: OcrViewModel = viewModel(),
    onConfirmed: () -> Unit,
) {
    val state by ocrVm.ui.collectAsState()

    LaunchedEffect(Unit) {
        if (state is OcrUiState.Idle) ocrVm.loadMock()
    }

    when (state) {
        is OcrUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is OcrUiState.Error -> {
            val msg = (state as OcrUiState.Error).message
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Error: $msg")
            }
        }
        is OcrUiState.Preview -> {
            val preview = (state as OcrUiState.Preview)
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Ticket: ${preview.data.receiptId}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Fecha: ${preview.data.date}  •  Total: ${preview.data.total}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(preview.editable) { index, item ->
                        OcrItemEditor(
                            item = item,
                            onChange = { preview.editable[index] = it }
                        )
                    }
                }

                Button(
                    onClick = { ocrVm.confirm(onDone = onConfirmed) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Confirmar y guardar")
                }
            }
        }
        OcrUiState.Confirming -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        OcrUiState.Confirmed, OcrUiState.Idle -> Unit
    }
}

@Composable
private fun OcrItemEditor(item: OcrItem, onChange: (OcrItem) -> Unit) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.title,
                onValueChange = { onChange(item.copy(title = it)) },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = item.amount.toString(),
                onValueChange = { txt ->
                    val cleaned = txt.replace(',', '.')
                    val v = cleaned.toDoubleOrNull()
                    onChange(item.copy(amount = v ?: item.amount))
                },
                label = { Text("Monto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = item.category ?: "",
                onValueChange = { onChange(item.copy(category = it.ifBlank { null })) },
                label = { Text("Categoría (texto)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = item.date,
                onValueChange = { onChange(item.copy(date = it)) },
                label = { Text("Fecha (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
