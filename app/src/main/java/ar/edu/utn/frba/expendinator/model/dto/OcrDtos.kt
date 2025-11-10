package ar.edu.utn.frba.expendinator.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class OcrItem(
    var title: String,
    var amount: Double,
    var category: String? = null,
    var date: String
)

@Serializable
data class OcrPreview(
    val receiptId: String,
    val currency: String,
    val date: String,
    val items: List<OcrItem>,
    val total: Double
    // byCategory existe en backend pero para editar no es necesario
)

@Serializable
data class OcrConfirmRequest(
    val items: List<OcrItem>
)

@Serializable
data class OcrConfirmResponse<T>(
    val created: T? = null,
    val error: String? = null
)
