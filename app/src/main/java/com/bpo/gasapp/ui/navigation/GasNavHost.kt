package com.bpo.gasapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bpo.gasapp.ui.detail.StationDetailRoute
import com.bpo.gasapp.ui.detail.StationDetailScreen
import com.bpo.gasapp.ui.stations.StationListScreen

object Routes {
    const val LIST = "stations"
}

@Composable
fun GasNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            StationListScreen(
                onStationClick = { id -> navController.navigate(StationDetailRoute.build(id)) }
            )
        }
        composable(
            route = StationDetailRoute.PATTERN,
            arguments = listOf(navArgument(StationDetailRoute.ARG_ID) { type = NavType.StringType })
        ) {
            StationDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
