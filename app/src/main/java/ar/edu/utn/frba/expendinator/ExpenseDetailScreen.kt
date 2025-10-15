package ar.edu.utn.frba.expendinator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    id: String,
    viewModel: ExpenseListViewModel,
    onBack: () -> Unit
) {
    val list by viewModel.uiState.collectAsState()
    val expense = remember(id, list) { viewModel.getById(id) }

    if (expense == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var isEditing by rememberSaveable { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf(expense.title) }
    var amountText by rememberSaveable { mutableStateOf(expense.amount.toString()) }
    var category by rememberSaveable { mutableStateOf(expense.category) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // readOnly evita la opacidad lavada de enabled=false
    val readOnly = !isEditing

    fun save() {
        val amount = amountText.toDoubleOrNull()
        if (title.isBlank() || amount == null || amount < 0) {
            return
        }

        viewModel.update(expense.copy(title = title.trim(), amount = amount, category = category))
        isEditing = false
    }

    fun cancelEdit() {
        title = expense.title
        amountText = expense.amount.toString()
        category = expense.category
        isEditing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar" else "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { save() }) {
                            Icon(Icons.Filled.Check, contentDescription = "Guardar")
                        }
                        IconButton(onClick = { cancelEdit() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancelar")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
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
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = true,
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    viewModel.categories.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                category = opt
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
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar gasto") },
            text = { Text("¿Seguro que querés borrar este gasto?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.delete(expense.id)
                        showDeleteDialog = false
                    }
                ) { Text("Borrar") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteDialog = false }
                ) { Text("Cancelar") }
            }
        )
    }
}
