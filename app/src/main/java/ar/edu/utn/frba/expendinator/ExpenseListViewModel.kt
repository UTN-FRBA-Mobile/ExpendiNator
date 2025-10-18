package ar.edu.utn.frba.expendinator

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseListViewModel : ViewModel() {

    private val _categories = mutableStateListOf<Category>(
        Category(id = "1", name = "Supermercado", keywords = listOf("super", "carrefour", "jumbo"), color = 0xFF9EC5FEL),
        Category(id = "2", name = "Transporte",  keywords = listOf("uber", "colectivo", "subte", "nafta"), color = 0xFFFECBA1L),
        Category(id = "3", name = "Comida afuera", keywords = listOf("mc", "burger", "pizza"), color = 0xFFA3CFBBL),
        Category(id = "4", name = "Salidas", keywords = listOf("cine"), color = 0xFFF1AEB5L),
    )
    private val _uiState = MutableStateFlow(
        listOf(
            Expense("1", "Supermercado", 12500.0, _categories[0], "2025-09-20"),
            Expense("2", "Cine", 5500.0, _categories[3], "2025-09-19"),
            Expense("3", "Nafta", 18000.0, _categories[1], "2025-09-18"),
            Expense("4", "Cena", 12000.0, _categories[2], "2025-09-18"),
        )
    )

    val uiState: StateFlow<List<Expense>> = _uiState


    val categories: List<Category> get() = _categories

    fun getById(id: String): Expense? = _uiState.value.firstOrNull {it.id == id}

    fun update(expense: Expense){
        _uiState.value = _uiState.value.map { if (it.id == expense.id) expense else it }
    }

    fun delete(id: String){
        _uiState.value = _uiState.value.filterNot { it.id == id }
    }
}
