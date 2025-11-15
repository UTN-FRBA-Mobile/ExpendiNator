package ar.edu.utn.frba.expendinator.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class BudgetUsageCategoryDto(
    val id: Int,
    val name: String,
    val color: Long
)

@Serializable
data class BudgetUsageResponse(
    val budget_id: Int,
    val limit_amount: Double,
    val period: String,              // "MONTHLY" | "WEEKLY" | "YEARLY"
    val start_date: String,
    val end_date: String,
    val effective_start_date: String? = null,
    val effective_end_date: String? = null,
    val spent: Double,
    val percent_used: Double,
    val remaining: Double,
    val over: Boolean,
    val category: BudgetUsageCategoryDto
)
