package ar.edu.utn.frba.expendinator

data class Category (
    val id: String,
    val name: String,
    val keywords: List<String> = emptyList(),
    val color: Long
)