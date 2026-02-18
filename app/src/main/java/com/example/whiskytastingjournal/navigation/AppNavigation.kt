package com.example.whiskytastingjournal.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.whiskytastingjournal.ui.TastingViewModel
import com.example.whiskytastingjournal.ui.screens.AddTastingScreen
import com.example.whiskytastingjournal.ui.screens.AddWhiskyScreen
import com.example.whiskytastingjournal.ui.screens.EditTastingScreen
import com.example.whiskytastingjournal.ui.screens.EditWhiskyScreen
import com.example.whiskytastingjournal.ui.screens.StatisticsScreen
import com.example.whiskytastingjournal.ui.screens.WhiskyDetailScreen
import com.example.whiskytastingjournal.ui.screens.WhiskyListScreen

@Composable
fun AppNavigation(navController: NavHostController, viewModel: TastingViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBar: @Composable () -> Unit = {
        AppBottomNavBar(
            currentRoute = currentRoute,
            onNavigateToList = {
                navController.navigate(NavRoutes.WhiskyList.route) {
                    popUpTo(NavRoutes.WhiskyList.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToStats = {
                navController.navigate(NavRoutes.Statistics.route) {
                    popUpTo(NavRoutes.WhiskyList.route)
                    launchSingleTop = true
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.WhiskyList.route
    ) {
        composable(NavRoutes.WhiskyList.route) {
            WhiskyListScreen(
                viewModel = viewModel,
                onAddWhisky = { navController.navigate(NavRoutes.AddWhisky.route) },
                onWhiskyClick = { id -> navController.navigate(NavRoutes.WhiskyDetail.withId(id)) },
                bottomBar = bottomBar
            )
        }

        composable(NavRoutes.Statistics.route) {
            StatisticsScreen(
                viewModel = viewModel,
                bottomBar = bottomBar
            )
        }

        composable(NavRoutes.AddWhisky.route) {
            AddWhiskyScreen(
                onSave = { whisky ->
                    viewModel.addWhisky(whisky)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.WhiskyDetail.route,
            arguments = listOf(navArgument("whiskyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val whiskyId = backStackEntry.arguments?.getString("whiskyId") ?: return@composable
            WhiskyDetailScreen(
                whiskyId = whiskyId,
                viewModel = viewModel,
                onEditWhisky = { id -> navController.navigate(NavRoutes.EditWhisky.withId(id)) },
                onAddTasting = { id -> navController.navigate(NavRoutes.AddTasting.withWhiskyId(id)) },
                onEditTasting = { tastingId -> navController.navigate(NavRoutes.EditTasting.withId(tastingId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EditWhisky.route,
            arguments = listOf(navArgument("whiskyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val whiskyId = backStackEntry.arguments?.getString("whiskyId") ?: return@composable
            EditWhiskyScreen(
                whiskyId = whiskyId,
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.AddTasting.route,
            arguments = listOf(navArgument("whiskyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val whiskyId = backStackEntry.arguments?.getString("whiskyId") ?: return@composable
            AddTastingScreen(
                whiskyId = whiskyId,
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EditTasting.route,
            arguments = listOf(navArgument("tastingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tastingId = backStackEntry.arguments?.getString("tastingId") ?: return@composable
            EditTastingScreen(
                tastingId = tastingId,
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun AppBottomNavBar(
    currentRoute: String?,
    onNavigateToList: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == NavRoutes.WhiskyList.route,
            onClick = onNavigateToList,
            icon = { Icon(Icons.Default.Home, contentDescription = "Whiskies") },
            label = { Text("Whiskies") }
        )
        NavigationBarItem(
            selected = currentRoute == NavRoutes.Statistics.route,
            onClick = onNavigateToStats,
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
            label = { Text("Statistics") }
        )
    }
}
