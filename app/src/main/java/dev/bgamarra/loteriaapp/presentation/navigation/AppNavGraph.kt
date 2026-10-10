package dev.bgamarra.loteriaapp.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.bgamarra.loteriaapp.presentation.play.PlayScreen
import dev.bgamarra.loteriaapp.presentation.tracking.TrackingScreen

@Composable
fun AppNavGraph() {

    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "play"
    ) {

        composable("play") {

            PlayScreen(
                navController = navController
            )
        }


        composable("tracking") {

            TrackingScreen(
                navController = navController
            )
        }
    }
}