package ar.edu.utn.frba.expendinator.models

data class Category (
    var id: String,
    val name: String,
    val keywords: List<String> = emptyList(),
    val color: Long
)