package ar.edu.utn.frba.expendinator.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseResponse(
    val id: Int,
    val title: String,
    val amount: Double,
    val date: String,
    val category_id: Int? = null,
    val category_name: String? = null,
    val category_color: Long? = null
)

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val amount: Double,
    val date: String,
    val category_id: Int? = null
)

@Serializable
data class UpdateExpenseRequest(
    val title: String,
    val amount: Double,
    val date: String,
    val category_id: Int? = null
)
