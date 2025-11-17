package ar.edu.utn.frba.expendinator.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.expendinator.model.dto.OcrItem
import ar.edu.utn.frba.expendinator.models.Category
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun OcrReviewScreen(
    ocrVm: OcrViewModel = viewModel(),
    expensesVm: ExpenseListViewModel,
    onConfirmed: () -> Unit,
) {
    val state by ocrVm.ui.collectAsState()
    val categories by expensesVm.categoriesFlow.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryDialogError by remember { mutableStateOf<String?>(null) }
    var creatingCategory by remember { mutableStateOf(false) }
    var pendingCategoryIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { ocrVm.loadMock() }
    LaunchedEffect(categories.isEmpty()) {
        if (categories.isEmpty()) {
            expensesVm.refreshAll()
        }
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
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Ticket: ${preview.data.receiptId}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Fecha: ${preview.data.date}  •  Total: ${preview.data.total}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                itemsIndexed(preview.editable, key = { index, item -> "$index-${item.title}-${item.date}" }) { index, item ->
                    OcrItemEditor(
                        item = item,
                        categories = categories,
                        onChange = { preview.editable[index] = it },
                        onAddCategoryRequested = {
                            pendingCategoryIndex = index
                            categoryDialogError = null
                            showAddCategoryDialog = true
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { ocrVm.confirm(onDone = onConfirmed) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Confirmar y guardar", textAlign = TextAlign.Center)
                    }
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

    if (showAddCategoryDialog) {
        val preview = state as? OcrUiState.Preview
        AddCategoryDialog(
            isLoading = creatingCategory,
            error = categoryDialogError,
            onDismiss = {
                if (!creatingCategory) {
                    showAddCategoryDialog = false
                    categoryDialogError = null
                    pendingCategoryIndex = null
                }
            },
            onConfirm = { name, keywords ->
                val idx = pendingCategoryIndex ?: return@AddCategoryDialog
                if (name.isBlank()) {
                    categoryDialogError = "El nombre es requerido"
                    return@AddCategoryDialog
                }
                scope.launch {
                    creatingCategory = true
                    val success = expensesVm.createCategory(
                        name.trim(),
                        randomCategoryColor(),
                        keywords
                    )
                    creatingCategory = false
                    if (success) {
                        preview?.editable?.let { list ->
                            val created = expensesVm.categoriesFlow.value.lastOrNull { it.name == name.trim() }
                            list[idx] = list[idx].copy(
                                categoryName = name.trim(),
                                categoryId = created?.id?.toIntOrNull()
                            )
                        }
                        showAddCategoryDialog = false
                        categoryDialogError = null
                        pendingCategoryIndex = null
                    } else {
                        categoryDialogError = "No se pudo crear la categoría"
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrItemEditor(
    item: OcrItem,
    categories: List<Category>,
    onChange: (OcrItem) -> Unit,
    onAddCategoryRequested: () -> Unit
) {
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
            var expanded by remember { mutableStateOf(false) }
            val categoryLabel = item.categoryName ?: "Sin categoría"
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = categoryLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    leadingIcon = {
                        val color = item.categoryId?.let { id ->
                            categories.firstOrNull { it.id == id.toString() }?.color
                        }
                        CategoryColorDot(color)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin categoría") },
                        onClick = {
                            expanded = false
                            onChange(item.copy(categoryId = null, categoryName = null))
                        }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            leadingIcon = { CategoryColorDot(category.color) },
                            text = { Text(category.name) },
                            onClick = {
                                expanded = false
                                onChange(item.copy(categoryId = category.id.toIntOrNull(), categoryName = category.name))
                            }
                        )
                    }
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Agregar categoría") },
                        onClick = {
                            expanded = false
                            onAddCategoryRequested()
                        }
                    )
                }
            }
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

@Composable
private fun CategoryColorDot(color: Long?) {
    val actualColor = color?.let { Color(it) } ?: Color.LightGray
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(actualColor, shape = CircleShape)
    )
}

@Composable
private fun AddCategoryDialog(
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var keywordsText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keywordsText,
                    onValueChange = { keywordsText = it },
                    label = { Text("Keywords (separadas por coma)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!error.isNullOrBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val keywords = keywordsText.split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                onConfirm(name, keywords)
            }, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }
    )
}

private fun randomCategoryColor(): Long {
    val r = Random.nextInt(90, 220)
    val g = Random.nextInt(90, 220)
    val b = Random.nextInt(90, 220)
    return 0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}
