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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseListViewModel : ViewModel() {

    private val client = ApiClient.client
    private val baseUrl = ApiClient.baseUrl

    private val _uiState = MutableStateFlow<List<Expense>>(emptyList())
    val uiState: StateFlow<List<Expense>> = _uiState

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: List<Category> get() = _categories.value
    val categoriesFlow: StateFlow<List<Category>> = _categories.asStateFlow()

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

                val dateOnly = expense.date.take(10)

                android.util.Log.d("ExpenseUpdate", "Actualizando gasto ${expense.id}")
                android.util.Log.d("ExpenseUpdate", "Category ID enviado: $categoryId")
                android.util.Log.d("ExpenseUpdate", "Category seleccionada: ${expense.category?.name}")

                val req = UpdateExpenseRequest(
                    title = expense.title,
                    amount = expense.amount,
                    date = dateOnly,
                    category_id = categoryId
                )

                val resp = client.put("$baseUrl/expenses/${expense.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(req)
                }

                if (resp.status.value in 200..299) {
                    android.util.Log.d("ExpenseUpdate", "Gasto actualizado exitosamente")
                    _uiState.value = _uiState.value.map {
                        if (it.id == expense.id) expense else it
                    }
                } else {
                    android.util.Log.e("ExpenseUpdate", "Error: ${resp.status.value}")
                }
            } catch (e: Exception) {
                android.util.Log.e("ExpenseUpdate", "Excepción al actualizar", e)
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
    private fun ExpenseResponse.toModel(categories: List<Category>): Expense {
        val catIdStr = category_id?.toString()
        val existing = categories.firstOrNull { it.id == catIdStr }

        // ⬇️ AGREGAR LOGS
        android.util.Log.d("ExpenseMapping", "=== Mapeando gasto: $title ===")
        android.util.Log.d("ExpenseMapping", "category_id recibido: $category_id")
        android.util.Log.d("ExpenseMapping", "catIdStr: $catIdStr")
        android.util.Log.d("ExpenseMapping", "Categoría encontrada: ${existing?.name}")
        android.util.Log.d("ExpenseMapping", "Total categorías disponibles: ${categories.size}")
        // ⬆️

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

    /**
     * Calcula el total gastado hoy
     */
    fun getTotalToday(): Double {
        val today = java.time.LocalDate.now()
        android.util.Log.d("WidgetDebug", "Hoy es: $today")

        return uiState.value
            .filter { expense ->
                try {
                    // Extraer solo la fecha (YYYY-MM-DD) del timestamp
                    val expenseDate = expense.date.take(10) // "2025-11-16T03:00..." -> "2025-11-16"
                    val parsed = java.time.LocalDate.parse(expenseDate)
                    val isToday = parsed == today

                    android.util.Log.d("WidgetDebug", "Gasto: ${expense.title}, fecha: '$expenseDate', parsed: $parsed, isToday: $isToday")
                    isToday
                } catch (e: Exception) {
                    android.util.Log.e("WidgetDebug", "Error parsing date: ${expense.date}", e)
                    false
                }
            }
            .sumOf { it.amount }
            .also { android.util.Log.d("WidgetDebug", "Total hoy: $it") }
    }

    /**
     * Calcula el total gastado en los últimos 7 días
     */
    fun getTotalWeek(): Double {
        val today = java.time.LocalDate.now()
        val weekAgo = today.minusDays(7)

        return uiState.value
            .filter {
                try {
                    // Extraer solo la fecha (YYYY-MM-DD) del timestamp
                    val expenseDate = it.date.take(10)
                    val parsed = java.time.LocalDate.parse(expenseDate)
                    parsed.isAfter(weekAgo.minusDays(1)) &&
                            parsed.isBefore(today.plusDays(1))
                } catch (e: Exception) {
                    false
                }
            }
            .sumOf { it.amount }
    }

    /**
     * Obtiene los últimos N gastos formateados
     */
    fun getLastExpenses(count: Int = 3): List<String> {
        val sorted = uiState.value
            .sortedByDescending { expense ->
                // Ordenar por ID (más alto = más reciente)
                expense.id.toIntOrNull() ?: 0
            }

        android.util.Log.d("LastExpenses", "=== Últimos gastos (ordenados por ID) ===")
        sorted.take(count).forEach { expense ->
            android.util.Log.d("LastExpenses", "ID: ${expense.id}, Título: ${expense.title}")
        }

        return sorted
            .take(count)
            .map { "${it.title} - $${String.format("%.2f", it.amount)}" }
    }

    /**
     * Guarda los totales en SharedPreferences para que el widget los lea
     */
    fun saveWidgetData(context: android.content.Context) {
        val prefs = context.getSharedPreferences("widget_data", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("total_today", String.format("%.2f", getTotalToday()))
            putString("total_week", String.format("%.2f", getTotalWeek()))

            val expenses = getLastExpenses(3)
            putString("expense_1", expenses.getOrNull(0) ?: "")
            putString("expense_2", expenses.getOrNull(1) ?: "")
            putString("expense_3", expenses.getOrNull(2) ?: "")

            apply()
        }
        android.util.Log.d("WidgetData", "Datos guardados, actualizando widget...")
        updateWidget(context)
    }

    private fun updateWidget(context: android.content.Context) {
        val intent = android.content.Intent(context, ar.edu.utn.frba.ExpendinatorApp.widget.ExpendinatorWidget::class.java)
        intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = android.appwidget.AppWidgetManager.getInstance(context)
            .getAppWidgetIds(android.content.ComponentName(context, ar.edu.utn.frba.ExpendinatorApp.widget.ExpendinatorWidget::class.java))
        intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}


