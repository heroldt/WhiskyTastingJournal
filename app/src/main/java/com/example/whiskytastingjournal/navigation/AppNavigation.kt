package com.example.whiskytastingjournal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.whiskytastingjournal.ui.TastingViewModel
import com.example.whiskytastingjournal.ui.screens.AddTastingScreen
import com.example.whiskytastingjournal.ui.screens.EditTastingScreen
import com.example.whiskytastingjournal.ui.screens.TastingDetailScreen
import com.example.whiskytastingjournal.ui.screens.TastingListScreen

@Composable
fun AppNavigation(navController: NavHostController, viewModel: TastingViewModel) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.TastingList.route
    ) {
        composable(NavRoutes.TastingList.route) {
            TastingListScreen(
                viewModel = viewModel,
                onAddTasting = { navController.navigate(NavRoutes.AddTasting.route) },
                onTastingClick = { id ->
                    navController.navigate(NavRoutes.TastingDetail.withId(id))
                }
            )
        }

        composable(NavRoutes.AddTasting.route) {
            AddTastingScreen(
                onSave = { entry ->
                    viewModel.addTasting(entry)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.TastingDetail.route,
            arguments = listOf(navArgument("tastingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tastingId = backStackEntry.arguments?.getString("tastingId") ?: return@composable
            TastingDetailScreen(
                tastingId = tastingId,
                viewModel = viewModel,
                onEdit = { id ->
                    navController.navigate(NavRoutes.EditTasting.withId(id))
                },
                onBack = { navController.popBackStack() }
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
                onSaved = {
                    // Pop back to detail screen
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
