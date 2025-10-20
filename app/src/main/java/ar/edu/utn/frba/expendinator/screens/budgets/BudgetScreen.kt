package ar.edu.utn.frba.expendinator.screens.budgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.expendinator.models.Budget
import ar.edu.utn.frba.expendinator.models.Expense
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    budgetVm: BudgetViewModel,
    expensesVm: ExpenseListViewModel,
    onNew: () -> Unit = {},
    onBudgetClicked: (Budget) -> Unit = {},
) {

    // Calculamos a partir de los gastos
    val expenses = expensesVm.uiState.collectAsState().value

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 64.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(budgetVm.budgets, key = { b: Budget -> b.id }) { b ->

            val spent = remember(b, expenses) {
                // TODO: Chequear rango de fechas
                expenses.filter { it.category?.id == b.category.id }
                    .sumOf { it.amount }
            }

            val percentage = (spent / b.limitAmount).coerceIn(0.0, 1.0)
            val over = spent > b.limitAmount

            Card(onClick = { onBudgetClicked(b) }) {
                Column(Modifier.fillMaxWidth().padding(12.dp)){
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically){
                            Box(
                                Modifier.size(10.dp).background(Color(b.category.color),
                                    CircleShape
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(b.category.name, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            "${"%.0f".format(spent)} / ${"%.0f".format(b.limitAmount)} $",
                            color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = { percentage.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = if (over) MaterialTheme.colorScheme.error else Color(b.category.color),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        brush = SolidColor(MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable { onNew() }
                    .padding(vertical = 20.dp, horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

    }
}