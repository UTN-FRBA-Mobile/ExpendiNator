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
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import ar.edu.utn.frba.expendinator.screens.budgets.BudgetScreen
import ar.edu.utn.frba.expendinator.screens.budgets.BudgetViewModel
import ar.edu.utn.frba.expendinator.screens.categories.CategoryCreateScreen
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseDetailScreen
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListScreen
import ar.edu.utn.frba.expendinator.screens.expenses.ExpenseListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val vm: ExpenseListViewModel = viewModel()
    val budgetVm: BudgetViewModel = viewModel()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Expendinator", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                Dest.drawerItems.forEach { dest ->
                    NavigationDrawerItem(
                        label = { Text(dest.label) },
                        selected = currentRoute == dest.route,
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            // Evitá duplicar
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
                TopAppBar(
                    title = { Text(currentLabel) },
                    navigationIcon = {
                        if (isDetail) {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        }
                    },
                    actions = topBarActions.value
                )
            },
            floatingActionButton = {
                if (currentRoute == Dest.Main.route) {
                    FloatingActionButton(onClick = { fabAction() }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = nav,
                startDestination = Dest.Main.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Pantalla Principal
                composable(Dest.Main.route) {
                    ExpenseListScreen(
                        viewModel = vm,
                        onExpenseClicked = { expense ->
                            nav.navigate("detail/${expense.id}")
                        },
                        setFabAction = { action -> fabAction = action }

                    )
                }

                // Pantalla de Detalle
                composable(
                    route = "detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: return@composable

                    var onSave     by remember { mutableStateOf<() -> Unit>({}) }
                    var onCancel   by remember { mutableStateOf<() -> Unit>({}) }
                    var onStartEdit by remember { mutableStateOf<() -> Unit>({}) }
                    var onDelete   by remember { mutableStateOf<() -> Unit>({}) }

                    // Acciones para la AppBar
                    LaunchedEffect( id, detailIsEditing) {
                        topBarActions.value = {
                            if (detailIsEditing) {
                                IconButton(onClick = { onSave() })   { Icon(Icons.Default.Check,  contentDescription = "Guardar") }
                                IconButton(onClick = { onCancel() }) { Icon(Icons.Default.Close,  contentDescription = "Cancelar") }
                            } else {
                                IconButton(onClick = { onStartEdit() }) { Icon(Icons.Default.Edit,   contentDescription = "Editar") }
                                IconButton(onClick = { onDelete() })    { Icon(Icons.Default.Delete, contentDescription = "Borrar") }
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
                            onSave = save; onCancel = cancel; onStartEdit = startEdit; onDelete =
                            delete
                        }
                    )
                }

                // Pantalla Principal
                composable(Dest.Categories.route) {
                    CategoryCreateScreen(
                        viewModel = vm,
                        onSaved = { nav.popBackStack() }
                    )
                }

                // Pantalla Presupuestos
                composable(Dest.Budget.route) {
                    BudgetScreen(
                        budgetVm = budgetVm,
                        expensesVm = vm,
                        onNew = { nav.navigate("budget/new") },
                    )
                }

                composable("budget/new") { PlaceholderScreen("Nuevo presupuesto") }

                composable(Dest.Metrics.route)    { PlaceholderScreen("Metricas") }
            }
        }
    }
}


// Pantalla solo para mostrar algo
@Composable
private fun PlaceholderScreen(text: String) {
    Surface(modifier = Modifier) {
        Text(text = text, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(24.dp))
    }
}
