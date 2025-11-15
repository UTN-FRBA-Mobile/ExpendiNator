package ar.edu.utn.frba.expendinator.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val color: Long? = null,
    val keywords: List<String> = emptyList()
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val color: Long?,
    val keywords: List<String>
)
