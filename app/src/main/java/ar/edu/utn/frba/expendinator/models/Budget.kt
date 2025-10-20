package ar.edu.utn.frba.expendinator.models

data class Budget (
    val id: String,
    val category: Category,
    val limitAmount: Double,
    val period: BudgetPeriod,
    val startDate: String,
    val endDate: String
)

enum class BudgetPeriod { MONTHLY, WEEKLY, YEARLY }