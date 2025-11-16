package ar.edu.utn.frba.expendinator.screens.metrics

import androidx.lifecycle.ViewModel
import ar.edu.utn.frba.expendinator.models.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

data class CategoryData(
    val name: String,
    val total: Double,
    val color: Long,
    val percentage: Float
)

data class DailyData(
    val date: String,
    val total: Double
)

class MetricsViewModel : ViewModel() {

    private val _startDate = MutableStateFlow(LocalDate.now().minusMonths(1))
    val startDate: StateFlow<LocalDate> = _startDate

    private val _endDate = MutableStateFlow(LocalDate.now())
    val endDate: StateFlow<LocalDate> = _endDate

    fun setStartDate(date: LocalDate) {
        _startDate.value = date
    }

    fun setEndDate(date: LocalDate) {
        _endDate.value = date
    }

    /**
     * Agrupa gastos por categoría para el gráfico de torta
     */
    fun getCategoryData(expenses: List<Expense>): List<CategoryData> {
        val filtered = filterExpensesByDate(expenses)
        val total = filtered.sumOf { it.amount }

        if (total == 0.0) return emptyList()

        return filtered
            .groupBy { it.category?.name ?: "Sin categoría" }
            .map { (name, exps) ->
                val categoryTotal = exps.sumOf { it.amount }
                val color = exps.firstOrNull()?.category?.color ?: 0xFF9E9E9EL
                CategoryData(
                    name = name,
                    total = categoryTotal,
                    color = color,
                    percentage = (categoryTotal / total).toFloat()
                )
            }
            .sortedByDescending { it.total }
    }

    /**
     * Agrupa gastos por día para el gráfico de áreas
     */
    fun getDailyData(expenses: List<Expense>): List<DailyData> {
        val filtered = filterExpensesByDate(expenses)

        return filtered
            .groupBy { it.date.take(10) } // YYYY-MM-DD
            .map { (date, exps) ->
                DailyData(
                    date = date,
                    total = exps.sumOf { it.amount }
                )
            }
            .sortedBy { it.date }
    }

    private fun filterExpensesByDate(expenses: List<Expense>): List<Expense> {
        val start = _startDate.value.toString()
        val end = _endDate.value.toString()

        return expenses.filter { expense ->
            val expenseDate = expense.date.take(10)
            expenseDate >= start && expenseDate <= end
        }
    }
}