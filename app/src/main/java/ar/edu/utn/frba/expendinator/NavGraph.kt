package ar.edu.utn.frba.expendinator

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val vm: ExpenseListViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            ExpenseListScreen(
                viewModel = vm,
                onExpenseClicked = {
                    expense -> navController.navigate("detail/${expense.id}")
                }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType})
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            ExpenseDetailScreen(
                id = id,
                viewModel = vm,
                onBack = {navController.popBackStack()}
            )
        }

    }
}
