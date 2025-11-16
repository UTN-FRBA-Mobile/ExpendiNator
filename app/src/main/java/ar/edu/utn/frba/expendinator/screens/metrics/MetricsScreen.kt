package ar.edu.utn.frba.expendinator.screens.metrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    expenseVm: ExpenseListViewModel = viewModel(),
    metricsVm: MetricsViewModel = viewModel()
) {
    val expenses by expenseVm.uiState.collectAsState()
    val startDate by metricsVm.startDate.collectAsState()
    val endDate by metricsVm.endDate.collectAsState()

    val categoryData = remember(expenses, startDate, endDate) {
        metricsVm.getCategoryData(expenses)
    }

    val dailyData = remember(expenses, startDate, endDate) {
        metricsVm.getDailyData(expenses)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Filtros
        FiltersCard(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { metricsVm.setStartDate(it) },
            onEndDateChange = { metricsVm.setEndDate(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de torta
        if (categoryData.isNotEmpty()) {
            PieChartCard(categoryData)
        } else {
            EmptyStateCard("No hay datos para mostrar en este período")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de áreas
        if (dailyData.isNotEmpty()) {
            BarChartCard(dailyData)
        } else {
            EmptyStateCard("No hay datos diarios")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersCard(
    startDate: java.time.LocalDate,
    endDate: java.time.LocalDate,
    onStartDateChange: (java.time.LocalDate) -> Unit,
    onEndDateChange: (java.time.LocalDate) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Filtros",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Fecha Inicio
            OutlinedTextField(
                value = startDate.toString(),
                onValueChange = {},
                label = { Text("Fecha Inicio") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha Fin
            OutlinedTextField(
                value = endDate.toString(),
                onValueChange = {},
                label = { Text("Fecha Fin") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Date Picker para fecha inicio
    if (showStartDatePicker) {
        DatePickerModalDialog(
            initialDate = startDate,
            onDateSelected = { newDate ->
                onStartDateChange(newDate)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // Date Picker para fecha fin
    if (showEndDatePicker) {
        DatePickerModalDialog(
            initialDate = endDate,
            onDateSelected = { newDate ->
                onEndDateChange(newDate)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalDialog(
    initialDate: java.time.LocalDate,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val localDate = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(localDate)
                }
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun PieChartCard(data: List<CategoryData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Gastos por Categoría",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SimplePieChart(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Leyenda
            data.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(Color(category.color))
                            }
                        }
                        Text(
                            category.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "$${String.format("%.2f", category.total)} (${(category.percentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SimplePieChart(data: List<CategoryData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f * 0.8f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        data.forEach { category ->
            val sweepAngle = category.percentage * 360f

            drawArc(
                color = Color(category.color),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }

        // Círculo blanco en el centro para efecto donut
        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = center
        )
    }
}

@Composable
fun BarChartCard(data: List<DailyData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Gastos Diarios",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SimpleBarChart(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
fun SimpleBarChart(data: List<DailyData>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOf { it.total }
        val chartHeight = size.height * 0.7f
        val chartWidth = size.width
        val spacing = chartWidth / data.size

        // Crear el path para el área
        val path = androidx.compose.ui.graphics.Path()
        var isFirst = true

        data.forEachIndexed { index, daily ->
            val barHeight = (daily.total / maxValue * chartHeight).toFloat()
            val x = index * spacing + spacing / 2
            val y = size.height - barHeight - 30f

            if (isFirst) {
                path.moveTo(0f, size.height - 30f)
                path.lineTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }
        }

        // Cerrar el path
        path.lineTo(size.width, size.height - 30f)
        path.lineTo(0f, size.height - 30f)
        path.close()

        // Dibujar el área con gradiente
        drawPath(
            path = path,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF6366F1).copy(alpha = 0.6f),
                    Color(0xFF6366F1).copy(alpha = 0.1f)
                )
            )
        )

        // Dibujar la línea del borde
        val linePath = androidx.compose.ui.graphics.Path()
        isFirst = true
        data.forEachIndexed { index, daily ->
            val barHeight = (daily.total / maxValue * chartHeight).toFloat()
            val x = index * spacing + spacing / 2
            val y = size.height - barHeight - 30f

            if (isFirst) {
                linePath.moveTo(x, y)
                isFirst = false
            } else {
                linePath.lineTo(x, y)
            }
        }

        drawPath(
            path = linePath,
            color = Color(0xFF6366F1),
            style = Stroke(width = 3f)
        )

        // Dibujar puntos
        data.forEachIndexed { index, daily ->
            val barHeight = (daily.total / maxValue * chartHeight).toFloat()
            val x = index * spacing + spacing / 2
            val y = size.height - barHeight - 30f

            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFF6366F1),
                radius = 4f,
                center = Offset(x, y)
            )
        }

        // Dibujar fechas (simplificadas)
        data.forEachIndexed { index, daily ->
            val x = index * spacing + spacing / 2
            drawContext.canvas.nativeCanvas.drawText(
                daily.date.substring(5), // MM-DD
                x,
                size.height - 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}