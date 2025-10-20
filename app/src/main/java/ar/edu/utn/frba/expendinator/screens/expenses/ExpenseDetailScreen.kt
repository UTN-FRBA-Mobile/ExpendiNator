package ar.edu.utn.frba.expendinator.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.expendinator.models.Category


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    id: String,
    viewModel: ExpenseListViewModel,
    onBack: () -> Unit,
    onEditingChanged: (Boolean) -> Unit,
    setAppBarHandlers: (
        save: () -> Unit,
        cancel: () -> Unit,
        startEdit: () -> Unit,
        delete: () -> Unit
    ) -> Unit
) {
    val list by viewModel.uiState.collectAsState()
    val expense = remember(id, list) { viewModel.getById(id) }

    if (expense == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var isEditing by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isEditing) { onEditingChanged(isEditing) }

    var title by rememberSaveable { mutableStateOf(expense.title) }
    var amountText by rememberSaveable { mutableStateOf(expense.amount.toString()) }
    var selectedCategory by remember { mutableStateOf<Category?>(expense.category) }


    var showDeleteDialog by remember { mutableStateOf(false) }

    // readOnly evita la opacidad lavada de enabled=false
    val readOnly = !isEditing

    fun save() {
        val amount = amountText.toDoubleOrNull()
        if (title.isBlank() || amount == null || amount < 0) {
            return
        }

        viewModel.update(expense.copy(title = title.trim(), amount = amount, category = selectedCategory))
        isEditing = false
    }

    fun cancelEdit() {
        title = expense.title
        amountText = expense.amount.toString()
        selectedCategory = expense.category
        isEditing = false
    }

    fun startEdit() {
        isEditing = true
    }

    fun askDelete(){
        showDeleteDialog = true
    }

    LaunchedEffect(Unit) {
        setAppBarHandlers(::save, ::cancelEdit, ::startEdit, ::askDelete)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nombre") },
            singleLine = true,
            readOnly = readOnly,
            enabled = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                    .filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                    .replace(',', '.')
            },
            label = { Text("$ Monto") },
            singleLine = true,
            readOnly = readOnly,
            enabled = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (isEditing) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Sin categoría",
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                leadingIcon = {
                    // puntito de color
                    Box(
                        Modifier
                            .size(16.dp)
                            .background(
                                color = selectedCategory?.let { Color(it.color) } ?: Color.LightGray,
                                shape = CircleShape
                            )
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = true // dejamos true para que pueda abrir; controlamos con isEditing arriba
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Opción "Sin categoría"
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(14.dp)
                                .background(Color.LightGray, CircleShape)
                        )
                    },
                    text = { Text("Sin categoría") },
                    onClick = {
                        selectedCategory = null
                        expanded = false
                    },
                    enabled = isEditing
                )

                // Resto de categorías
                viewModel.categories.forEach { cat ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(Color(cat.color), CircleShape)
                            )
                        },
                        text = { Text(cat.name) },
                        onClick = {
                            selectedCategory = cat
                            expanded = false
                        },
                        enabled = isEditing
                    )
                }
            }
        }

        if (isEditing) {
            Button(
                onClick = { save() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar cambios") }
        }
    }


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar gasto") },
            text = { Text("¿Seguro que querés borrar este gasto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(expense.id)
                        showDeleteDialog = false
                    }
                ) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) { Text("Cancelar") }
            }
        )
    }
}
