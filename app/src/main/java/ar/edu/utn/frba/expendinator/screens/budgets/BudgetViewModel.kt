package ar.edu.utn.frba.expendinator.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.expendinator.data.remote.ApiClient
import ar.edu.utn.frba.expendinator.model.dto.BudgetUsageResponse
import ar.edu.utn.frba.expendinator.models.BudgetPeriod
import ar.edu.utn.frba.expendinator.models.Category
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Modelo de UI para mostrar el uso de un presupuesto.
 * Sale del endpoint /budgets/usage.
 */
data class BudgetUsageUi(
    val id: String,
    val category: Category,
    val limitAmount: Double,
    val period: BudgetPeriod,
    val startDate: String,
    val endDate: String,
    val spent: Double
)

class BudgetViewModel : ViewModel() {

    private val client = ApiClient.client
    private val baseUrl = ApiClient.baseUrl

    private val _budgets = MutableStateFlow<List<BudgetUsageUi>>(emptyList())
    val budgets: StateFlow<List<BudgetUsageUi>> = _budgets.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                // Usamos el endpoint de uso de presupuestos
                val resp = client.get("$baseUrl/budgets/usage?active=true")
                val dtos = resp.body<List<BudgetUsageResponse>>()

                _budgets.value = dtos.map { dto ->
                    BudgetUsageUi(
                        id = dto.budget_id.toString(),
                        category = Category(
                            id = dto.category.id.toString(),
                            name = dto.category.name,
                            keywords = emptyList(),
                            color = dto.category.color
                        ),
                        limitAmount = dto.limit_amount,
                        period = when (dto.period) {
                            "WEEKLY" -> BudgetPeriod.WEEKLY
                            "YEARLY" -> BudgetPeriod.YEARLY
                            else -> BudgetPeriod.MONTHLY
                        },
                        startDate = dto.start_date,
                        endDate = dto.end_date,
                        spent = dto.spent
                    )
                }
            } catch (e: Exception) {
                // En demo podemos dejar la lista vacía si falla
                // (si querés, podrías exponer un estado de error)
            }
        }
    }
}
