package ar.edu.utn.frba.expendinator.models

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val category: Category?,
    val date: String // por ahora string simple
)
