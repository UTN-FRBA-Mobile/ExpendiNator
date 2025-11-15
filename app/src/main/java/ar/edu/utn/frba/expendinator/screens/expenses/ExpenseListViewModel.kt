package ar.edu.utn.frba.expendinator.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.expendinator.data.remote.ApiClient
import ar.edu.utn.frba.expendinator.model.dto.CategoryResponse
import ar.edu.utn.frba.expendinator.model.dto.CreateCategoryRequest
import ar.edu.utn.frba.expendinator.model.dto.ExpenseResponse
import ar.edu.utn.frba.expendinator.model.dto.UpdateExpenseRequest
import ar.edu.utn.frba.expendinator.models.Category
import ar.edu.utn.frba.expendinator.models.Expense
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseListViewModel : ViewModel() {

    private val client = ApiClient.client
    private val baseUrl = ApiClient.baseUrl

    private val _uiState = MutableStateFlow<List<Expense>>(emptyList())
    val uiState: StateFlow<List<Expense>> = _uiState

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: List<Category> get() = _categories.value

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            loadCategories()
            loadExpenses()
        }
    }

    private suspend fun loadCategories() {
        try {
            val resp = client.get("$baseUrl/categories")
            val dtos = resp.body<List<CategoryResponse>>()
            _categories.value = dtos.map { dto ->
                Category(
                    id = dto.id.toString(),
                    name = dto.name,
                    keywords = dto.keywords,
                    color = dto.color ?: 0xFF9EC5FEL // color default si viene null
                )
            }
        } catch (e: Exception) {
            // En demo: si falla dejamos la lista como está
        }
    }

    private suspend fun loadExpenses() {
        try {
            val resp = client.get("$baseUrl/expenses")
            val dtos = resp.body<List<ExpenseResponse>>()
            _uiState.value = dtos.map { it.toModel(_categories.value) }
        } catch (e: Exception) {
            // En demo: si falla dejamos el estado anterior
        }
    }

    fun getById(id: String): Expense? =
        _uiState.value.firstOrNull { it.id == id }

    fun update(expense: Expense) {
        viewModelScope.launch {
            try {
                val categoryId = expense.category?.id?.toIntOrNull()
                val req = UpdateExpenseRequest(
                    title = expense.title,
                    amount = expense.amount,
                    date = expense.date,
                    category_id = categoryId
                )

                val resp = client.put("$baseUrl/expenses/${expense.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(req)
                }

                if (resp.status.value in 200..299) {
                    _uiState.value = _uiState.value.map {
                        if (it.id == expense.id) expense else it
                    }
                }
            } catch (e: Exception) {
                // Podrías exponer un estado de error si lo necesitás
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                val resp = client.delete("$baseUrl/expenses/$id")
                if (resp.status.value in 200..299) {
                    _uiState.value = _uiState.value.filterNot { it.id == id }
                }
            } catch (e: Exception) {
                // Manejo de error opcional
            }
        }
    }

    /**
     * Se usa desde CategoryCreateScreen.
     * Devuelve true si la categoría se creó bien en el backend.
     */
    suspend fun createCategory(
        name: String,
        color: Long,
        keywords: List<String>
    ): Boolean {
        return try {
            val req = CreateCategoryRequest(
                name = name,
                color = color,
                keywords = keywords
            )
            val resp = client.post("$baseUrl/categories") {
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            if (resp.status.value !in 200..299) return false

            val dto = resp.body<CategoryResponse>()
            val newCat = Category(
                id = dto.id.toString(),
                name = dto.name,
                keywords = dto.keywords,
                color = dto.color ?: color
            )
            _categories.value = _categories.value + newCat
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Helper para mapear del DTO del backend al modelo de UI.
 */
private fun ExpenseResponse.toModel(categories: List<Category>): Expense {
    val catIdStr = category_id?.toString()
    val existing = categories.firstOrNull { it.id == catIdStr }

    val cat = when {
        existing != null -> existing
        category_id != null && category_name != null && category_color != null ->
            Category(
                id = category_id.toString(),
                name = category_name,
                keywords = emptyList(),
                color = category_color
            )
        else -> null
    }

    return Expense(
        id = id.toString(),
        title = title,
        amount = amount,
        category = cat,
        date = date
    )
}
