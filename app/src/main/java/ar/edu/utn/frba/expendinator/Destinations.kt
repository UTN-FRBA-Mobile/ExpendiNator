package ar.edu.utn.frba.expendinator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Main: Dest("main","Gastos", Icons.Default.Home)
    data object Budget: Dest("budget","Presupuestos", Icons.Default.ShoppingCart)
    data object Categories: Dest("categories","Categorias", Icons.Default.Build)
    data object Metrics: Dest("metrics","Métricas", Icons.Default.DateRange)


    companion object { val drawerItems = listOf(Main, Budget, Categories, Metrics) }
}
