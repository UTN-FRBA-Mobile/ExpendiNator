package ar.edu.utn.frba.expendinator.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.edu.utn.frba.expendinator.screens.auth.AuthViewModel
import ar.edu.utn.frba.expendinator.screens.auth.LoginScreen
import ar.edu.utn.frba.expendinator.screens.auth.RegisterScreen
import ar.edu.utn.frba.expendinator.screens.budgets.BudgetScreen
import ar.edu.utn.frba.expendinator.screens.budgets.BudgetViewModel
import ar.edu.utn.frba.expendinator.screens.categories.CategoryCreateScreen
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseDetailScreen
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListScreen
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListViewModel
import ar.edu.utn.frba.expendinator.screens.expenses.OcrReviewScreen
import ar.edu.utn.frba.expendinator.screens.expenses.OcrViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // <- evita el warning/error de API experimental
@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val vm: ExpenseListViewModel = viewModel()
    val budgetVm: BudgetViewModel = viewModel()
    val authVm: AuthViewModel = viewModel()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentRoute by nav.currentBackStackEntryAsState()
        .let { state -> derivedStateOf { state.value?.destination?.route } }

    val isDetail = currentRoute?.startsWith("detail/") == true || currentRoute == "detail/{id}"
    val currentLabel = when {
        isDetail -> "Detalle"
        else -> Dest.drawerItems.firstOrNull { it.route == currentRoute }?.label ?: "Expendinator"
    }

    val topBarActions = remember { mutableStateOf<@Composable RowScope.() -> Unit>({}) }
    var detailIsEditing by rememberSaveable { mutableStateOf(false) }

    var fabAction by remember { mutableStateOf<() -> Unit>({}) }

    val ocrVm: OcrViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Expendinator",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                Dest.drawerItems.forEach { dest ->
                    NavigationDrawerItem(
                        label = { Text(dest.label!!) },
                        selected = currentRoute == dest.route,
                        icon = { Icon(dest.icon!!, contentDescription = dest.label) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != dest.route) {
                                nav.navigate(dest.route) {
                                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != Dest.Login.route && currentRoute != Dest.Register.route) {
                    TopAppBar(
                        title = { Text(currentLabel) },
                        navigationIcon = {
                            if (isDetail) {
                                IconButton(onClick = { nav.popBackStack() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Menú")
                                }
                            }
                        },
                        actions = topBarActions.value
                    )
                }
            },
            floatingActionButton = {
                if (currentRoute == Dest.Main.route) {
                    FloatingActionButton(onClick = { fabAction() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = nav,
                startDestination = Dest.startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {

                composable(Dest.Login.route) {
                    LoginScreen(
                        viewModel = authVm,
                        onSuccess = {
                            nav.navigate(Dest.Main.route) {
                                popUpTo(Dest.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { nav.navigate(Dest.Register.route) }
                    )
                }

                composable(Dest.Register.route) {
                    RegisterScreen(
                        viewModel = authVm,
                        onSuccess = {
                            nav.navigate(Dest.Main.route) {
                                popUpTo(Dest.Register.route) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { nav.navigate(Dest.Login.route) }
                    )
                }

                // Home
                composable(Dest.Main.route) {
                    ExpenseListScreen(
                        viewModel = vm,
                        onExpenseClicked = { expense -> nav.navigate("detail/${expense.id}") },
                        setFabAction = { action -> fabAction = action },
                        onAddClicked = { nav.navigate(Dest.OcrReview.route) }
                    )
                }

                // Detalle
                composable(
                    route = "detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: return@composable

                    var onSave by remember { mutableStateOf<() -> Unit>({}) }
                    var onCancel by remember { mutableStateOf<() -> Unit>({}) }
                    var onStartEdit by remember { mutableStateOf<() -> Unit>({}) }
                    var onDelete by remember { mutableStateOf<() -> Unit>({}) }

                    LaunchedEffect(id, detailIsEditing) {
                        topBarActions.value = {
                            if (detailIsEditing) {
                                IconButton(onClick = { onSave() }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Guardar")
                                }
                                IconButton(onClick = { onCancel() }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Cancelar")
                                }
                            } else {
                                IconButton(onClick = { onStartEdit() }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { onDelete() }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Borrar")
                                }
                            }
                        }
                    }
                    DisposableEffect(Unit) { onDispose { topBarActions.value = {} } }

                    ExpenseDetailScreen(
                        id = id,
                        viewModel = vm,
                        onBack = { nav.popBackStack() },
                        onEditingChanged = { detailIsEditing = it },
                        setAppBarHandlers = { save, cancel, startEdit, delete ->
                            onSave = save
                            onCancel = cancel
                            onStartEdit = startEdit
                            onDelete = delete
                        }
                    )
                }

                // Categorías
                composable(Dest.Categories.route) {
                    CategoryCreateScreen(
                        viewModel = vm,
                        onSaved = { nav.popBackStack() }
                    )
                }

                // Presupuestos
                composable(Dest.Budget.route) {
                    BudgetScreen(
                        budgetVm = budgetVm,
                        expensesVm = vm,
                        onNew = { nav.navigate("budget/new") },
                    )
                }

                composable("budget/new") { PlaceholderScreen("Nuevo presupuesto") }
                composable(Dest.Metrics.route) { PlaceholderScreen("Metricas") }

                composable(Dest.OcrReview.route) {
                    OcrReviewScreen(
                        ocrVm,
                        onConfirmed = {
                            // Volvemos al home y podrías refrescar gastos
                            nav.navigate(Dest.Main.route) {
                                popUpTo(Dest.Main.route) { inclusive = true }
                            }
                        }
                    )
                }

            }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Surface(modifier = Modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(24.dp)
        )
    }
}
