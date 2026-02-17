package com.example.whiskytastingjournal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.whiskytastingjournal.ui.TastingViewModel
import com.example.whiskytastingjournal.ui.screens.AddTastingScreen
import com.example.whiskytastingjournal.ui.screens.AddWhiskyScreen
import com.example.whiskytastingjournal.ui.screens.EditTastingScreen
import com.example.whiskytastingjournal.ui.screens.EditWhiskyScreen
import com.example.whiskytastingjournal.ui.screens.WhiskyDetailScreen
import com.example.whiskytastingjournal.ui.screens.WhiskyListScreen

@Composable
fun AppNavigation(navController: NavHostController, viewModel: TastingViewModel) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.WhiskyList.route
    ) {
        composable(NavRoutes.WhiskyList.route) {
            WhiskyListScreen(
                viewModel = viewModel,
                onAddWhisky = { navController.navigate(NavRoutes.AddWhisky.route) },
                onWhiskyClick = { id ->
                    navController.navigate(NavRoutes.WhiskyDetail.withId(id))
                }
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
                onEditWhisky = { id ->
                    navController.navigate(NavRoutes.EditWhisky.withId(id))
                },
                onAddTasting = { id ->
                    navController.navigate(NavRoutes.AddTasting.withWhiskyId(id))
                },
                onEditTasting = { tastingId ->
                    navController.navigate(NavRoutes.EditTasting.withId(tastingId))
                },
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
