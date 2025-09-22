package ar.edu.utn.frba.expendinator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        listOf(
            Expense("1", "Supermercado", 12500.0, "Comidas", "2025-09-20"),
            Expense("2", "Cine", 5500.0, "Salidas", "2025-09-19"),
            Expense("3", "Nafta", 18000.0, "Transporte", "2025-09-18")
        )
    )
    val uiState: StateFlow<List<Expense>> = _uiState
}
