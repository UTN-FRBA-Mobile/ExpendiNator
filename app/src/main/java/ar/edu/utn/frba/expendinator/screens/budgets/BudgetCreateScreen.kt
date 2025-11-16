package ar.edu.utn.frba.expendinator.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.expendinator.models.BudgetPeriod
import ar.edu.utn.frba.expendinator.models.Category
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListViewModel
import ar.edu.utn.frba.expendinator.utils.showErrorToast
import ar.edu.utn.frba.expendinator.utils.showSuccessToast
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCreateScreen(
    budgetVm: BudgetViewModel,
    expensesVm: ExpenseListViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val categories by expensesVm.categoriesFlow.collectAsState()

    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var selectedPeriod by rememberSaveable { mutableStateOf(BudgetPeriod.MONTHLY.name) }

    val today = remember { LocalDate.now() }
    var startDate by rememberSaveable { mutableStateOf(today.toString()) }
    var endDate by rememberSaveable { mutableStateOf(today.plusMonths(1).toString()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(categories) {
        if (categories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = categories.first().id
        }
    }

    LaunchedEffect(categories.isEmpty()) {
        if (categories.isEmpty()) {
            expensesVm.refreshAll()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nuevo presupuesto",
            style = MaterialTheme.typography.headlineSmall
        )

        CategoryPicker(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            enabled = categories.isNotEmpty(),
            onCategorySelected = { selectedCategoryId = it }
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                    .filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                    .replace(',', '.')
            },
            label = { Text("Monto límite ($)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Periodo", style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BudgetPeriod.values().forEach { period ->
                val label = when (period) {
                    BudgetPeriod.MONTHLY -> "Mensual"
                    BudgetPeriod.WEEKLY -> "Semanal"
                    BudgetPeriod.YEARLY -> "Anual"
                }
                FilterChip(
                    selected = selectedPeriod == period.name,
                    onClick = { selectedPeriod = period.name },
                    label = { Text(label) }
                )
            }
        }

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Fecha inicio (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("Fecha fin (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val categoryId = selectedCategoryId?.toIntOrNull()
                val amount = amountText.toDoubleOrNull()
                val period = BudgetPeriod.valueOf(selectedPeriod)

                if (categoryId == null) {
                    showErrorToast(context, "Elegí una categoría")
                    return@Button
                }

                if (amount == null || amount <= 0) {
                    showErrorToast(context, "Ingresá un monto válido")
                    return@Button
                }

                if (!isValidDate(startDate) || !isValidDate(endDate)) {
                    showErrorToast(context, "Las fechas deben tener formato YYYY-MM-DD")
                    return@Button
                }

                if (startDate > endDate) {
                    showErrorToast(context, "La fecha inicio debe ser anterior a la fin")
                    return@Button
                }

                scope.launch {
                    val ok = budgetVm.createBudget(
                        categoryId = categoryId,
                        limitAmount = amount,
                        period = period,
                        startDate = startDate,
                        endDate = endDate
                    )
                    if (ok) {
                        showSuccessToast(context, "Presupuesto creado")
                        onSaved()
                    } else {
                        showErrorToast(context, "No se pudo crear el presupuesto")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = categories.isNotEmpty()
        ) {
            Text("Guardar")
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditScreen(
    budgetVm: BudgetViewModel,
    expensesVm: ExpenseListViewModel,
    budgetId: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val categories by expensesVm.categoriesFlow.collectAsState()
    val budgets by budgetVm.budgets.collectAsState()
    val existing = budgets.firstOrNull { it.id == budgetId }

    var selectedCategoryId by rememberSaveable { mutableStateOf(existing?.category?.id) }
    var amountText by rememberSaveable { mutableStateOf(existing?.limitAmount?.toString() ?: "") }
    var selectedPeriod by rememberSaveable { mutableStateOf(existing?.period?.name ?: BudgetPeriod.MONTHLY.name) }
    var startDate by rememberSaveable { mutableStateOf(existing?.startDate ?: "") }
    var endDate by rememberSaveable { mutableStateOf(existing?.endDate ?: "") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(categories) {
        if (existing != null && selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.firstOrNull { it.id == existing.category.id }?.id
                ?: categories.first().id
        }
    }

    LaunchedEffect(existing) {
        existing?.let {
            selectedCategoryId = it.category.id
            amountText = it.limitAmount.toString()
            selectedPeriod = it.period.name
            startDate = it.startDate
            endDate = it.endDate
        }
    }

    LaunchedEffect(categories.isEmpty()) {
        if (categories.isEmpty()) {
            expensesVm.refreshAll()
        }
    }

    LaunchedEffect(Unit) { if (existing == null) budgetVm.refresh() }

    if (existing == null) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cargando presupuesto...", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onCancel) { Text("Volver") }
        }
        return
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Editar presupuesto",
            style = MaterialTheme.typography.headlineSmall
        )

        CategoryPicker(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            enabled = categories.isNotEmpty(),
            onCategorySelected = { selectedCategoryId = it }
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                    .filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                    .replace(',', '.')
            },
            label = { Text("Monto límite ($)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Periodo", style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BudgetPeriod.values().forEach { period ->
                val label = when (period) {
                    BudgetPeriod.MONTHLY -> "Mensual"
                    BudgetPeriod.WEEKLY -> "Semanal"
                    BudgetPeriod.YEARLY -> "Anual"
                }
                FilterChip(
                    selected = selectedPeriod == period.name,
                    onClick = { selectedPeriod = period.name },
                    label = { Text(label) }
                )
            }
        }

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Fecha inicio (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("Fecha fin (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val categoryId = selectedCategoryId?.toIntOrNull()
                val amount = amountText.toDoubleOrNull()
                val period = BudgetPeriod.valueOf(selectedPeriod)

                if (categoryId == null) {
                    showErrorToast(context, "Elegí una categoría")
                    return@Button
                }

                if (amount == null || amount <= 0) {
                    showErrorToast(context, "Ingresá un monto válido")
                    return@Button
                }

                if (!isValidDate(startDate) || !isValidDate(endDate)) {
                    showErrorToast(context, "Las fechas deben tener formato YYYY-MM-DD")
                    return@Button
                }

                if (startDate > endDate) {
                    showErrorToast(context, "La fecha inicio debe ser anterior a la fin")
                    return@Button
                }

                scope.launch {
                    val ok = budgetVm.updateBudget(
                        budgetId = budgetId.toInt(),
                        categoryId = categoryId,
                        limitAmount = amount,
                        period = period,
                        startDate = startDate,
                        endDate = endDate
                    )
                    if (ok) {
                        showSuccessToast(context, "Presupuesto actualizado")
                        onSaved()
                    } else {
                        showErrorToast(context, "No se pudo actualizar el presupuesto")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = categories.isNotEmpty()
        ) {
            Text("Guardar cambios")
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: String?,
    enabled: Boolean,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: if (enabled) "" else "Sin categorías disponibles",
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = {
                val color = selected?.let { Color(it.color) } ?: Color.LightGray
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp)
                        .padding(2.dp)
                        .background(color, CircleShape)
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .width(14.dp)
                                .background(Color(category.color), CircleShape)
                        )
                    },
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun isValidDate(value: String): Boolean {
    return try {
        LocalDate.parse(value)
        true
    } catch (_: DateTimeParseException) {
        false
    }
}
