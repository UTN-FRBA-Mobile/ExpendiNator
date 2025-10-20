package ar.edu.utn.frba.expendinator.screens.budgets

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import ar.edu.utn.frba.expendinator.models.Budget
import ar.edu.utn.frba.expendinator.models.BudgetPeriod
import ar.edu.utn.frba.expendinator.models.Category
import ar.edu.utn.frba.expendinator.models.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BudgetViewModel : ViewModel() {

    // Para probar con categorias hardcodeadas, no tiene que estar aca:
    private val _categories = mutableStateListOf<Category>(
        Category(
            id = "1",
            name = "Supermercado",
            keywords = listOf("super", "carrefour", "jumbo"),
            color = 0xFF9EC5FEL
        ),
        Category(
            id = "2",
            name = "Transporte",
            keywords = listOf("uber", "colectivo", "subte", "nafta"),
            color = 0xFFFECBA1L
        ),
        Category(
            id = "3",
            name = "Comida afuera",
            keywords = listOf("mc", "burger", "pizza"),
            color = 0xFFA3CFBBL
        ),
        Category(id = "4", name = "Salidas", keywords = listOf("cine"), color = 0xFFF1AEB5L),
    )

    private val _budgets = mutableStateListOf(
        Budget(
            id = "b1",
            category = _categories[0],
            limitAmount = 80000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = "2025-03-01",
            endDate = "2025-03-31"
        ),
        Budget(
            id = "b2",
            category = _categories[1],
            limitAmount = 10000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = "2025-03-01",
            endDate = "2025-03-31"
        ),
        Budget(
            id = "b3",
            category = _categories[2],
            limitAmount = 35000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = "2025-03-01",
            endDate = "2025-03-31"
        ),
        Budget(
            id = "b4",
            category = _categories[3],
            limitAmount = 25000.0,
            period = BudgetPeriod.MONTHLY,
            startDate = "2025-03-01",
            endDate = "2025-03-31"
        )
    )
    val budgets: List<Budget> get() = _budgets





}
