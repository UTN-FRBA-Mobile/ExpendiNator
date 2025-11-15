package ar.edu.utn.frba.expendinator.screens.categories

import androidx.collection.longListOf
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListViewModel
import ar.edu.utn.frba.expendinator.utils.showErrorToast
import ar.edu.utn.frba.expendinator.utils.showSuccessToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreateScreen(
    viewModel: ExpenseListViewModel,
    onSaved: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    val keywords = remember { mutableStateListOf("") }
    var selectedColor by rememberSaveable { mutableLongStateOf(0xFF9EC5FEL) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun removeKeyword(index: Int) {
        if (keywords.size > 1) {
            keywords.removeAt(index)
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Color", style = MaterialTheme.typography.labelLarge)
        val colorOptions = longListOf(
            0xFF9EC5FEL, 0xFFFECBA1L, 0xFFA3CFBBL,
            0xFFF1AEB5L, 0xFFE2C6FEL, 0xFFFFD966L
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colorOptions.forEach { color ->
                val selected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(if (selected) 40.dp else 32.dp)
                        .background(Color(color), CircleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected)
                                MaterialTheme.colorScheme.onSurface
                            else
                                Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = color }
                )
            }
        }

        Text("Palabras clave", style = MaterialTheme.typography.labelLarge)

        keywords.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { new -> keywords[index] = new },
                placeholder = { Text("Ej: super, cine...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { removeKeyword(index) },
                        enabled = keywords.size > 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar palabra clave"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (keywords[index].isNotBlank()) keywords.add("")
                    }
                )
            )
        }

        TextButton(onClick = { keywords.add("") }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Agregar palabra clave")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    showErrorToast(context, "El nombre es requerido.")
                    return@Button
                }

                val cleanedKeywords = keywords
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                scope.launch {
                    val ok = viewModel.createCategory(
                        name = name.trim(),
                        color = selectedColor,
                        keywords = cleanedKeywords
                    )
                    if (ok) {
                        showSuccessToast(
                            context,
                            "Categoría guardada correctamente."
                        )

                        name = ""
                        keywords.clear()
                        keywords.add("")
                        selectedColor = 0xFF9EC5FEL

                        onSaved()
                    } else {
                        showErrorToast(
                            context,
                            "No se pudo guardar la categoría."
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
