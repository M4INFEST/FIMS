package com.example.fims.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fims.FimsApplication
import com.example.fims.data.InventoryRepository
import com.example.fims.viewmodel.AddEditItemViewModel
import com.example.fims.viewmodel.AddEditItemViewModelFactory
import com.example.fims.viewmodel.AlertsAndHistoryViewModel
import com.example.fims.viewmodel.AlertsAndHistoryViewModelFactory
import com.example.fims.viewmodel.DashboardViewModel
import com.example.fims.viewmodel.DashboardViewModelFactory
import com.example.fims.viewmodel.ItemListViewModel
import com.example.fims.viewmodel.ItemListViewModelFactory
import com.example.fims.viewmodel.TransactionsViewModel
import com.example.fims.viewmodel.TransactionsViewModelFactory

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Dashboard : Screen("dashboard")
    object ItemList : Screen("item_list")
    object AddEditItem : Screen("add_edit_item")
    object StockIn : Screen("stock_in")
    object StockOut : Screen("stock_out")
    object Wastage : Screen("wastage")
    object Alerts : Screen("alerts")
    object History : Screen("history")
}

@Composable
fun FimsNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = (context.applicationContext as FimsApplication).database
    val repository = InventoryRepository(database.fimsDao(), database)

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    } 
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(repository)
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToItems = { navController.navigate(Screen.ItemList.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToStockIn = { navController.navigate(Screen.StockIn.route) },
                onNavigateToStockOut = { navController.navigate(Screen.StockOut.route) },
                onNavigateToWastage = { navController.navigate(Screen.Wastage.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }
        
        composable(Screen.ItemList.route) {
            val itemListViewModel: ItemListViewModel = viewModel(
                factory = ItemListViewModelFactory(repository)
            )
            ItemListScreen(
                viewModel = itemListViewModel,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddItem = { navController.navigate(Screen.AddEditItem.route) }
            )
        }
        
        composable(Screen.AddEditItem.route) {
            val addEditItemViewModel: AddEditItemViewModel = viewModel(
                factory = AddEditItemViewModelFactory(repository)
            )
            AddEditItemScreen(
                viewModel = addEditItemViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(Screen.StockIn.route) {
            val transactionsViewModel: TransactionsViewModel = viewModel(
                factory = TransactionsViewModelFactory(repository)
            )
            StockInScreen(viewModel = transactionsViewModel, onNavigateBack = { navController.navigateUp() })
        }
        
        composable(Screen.StockOut.route) {
            val transactionsViewModel: TransactionsViewModel = viewModel(
                factory = TransactionsViewModelFactory(repository)
            )
            StockOutWastageScreen(viewModel = transactionsViewModel, isWastage = false, onNavigateBack = { navController.navigateUp() })
        }
        
        composable(Screen.Wastage.route) {
            val transactionsViewModel: TransactionsViewModel = viewModel(
                factory = TransactionsViewModelFactory(repository)
            )
            StockOutWastageScreen(viewModel = transactionsViewModel, isWastage = true, onNavigateBack = { navController.navigateUp() })
        }
        
        composable(Screen.Alerts.route) {
            val alertsHistoryViewModel: AlertsAndHistoryViewModel = viewModel(
                factory = AlertsAndHistoryViewModelFactory(repository)
            )
            AlertsScreen(viewModel = alertsHistoryViewModel, onNavigateBack = { navController.navigateUp() })
        }
        
        composable(Screen.History.route) {
            val alertsHistoryViewModel: AlertsAndHistoryViewModel = viewModel(
                factory = AlertsAndHistoryViewModelFactory(repository)
            )
            HistoryScreen(viewModel = alertsHistoryViewModel, onNavigateBack = { navController.navigateUp() })
        }
    }
}