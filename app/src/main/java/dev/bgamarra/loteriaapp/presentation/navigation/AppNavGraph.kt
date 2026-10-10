package dev.bgamarra.loteriaapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dev.bgamarra.loteriaapp.presentation.auth.LoginScreen
import dev.bgamarra.loteriaapp.presentation.auth.RegisterScreen
import dev.bgamarra.loteriaapp.presentation.play.PlayScreen
import dev.bgamarra.loteriaapp.presentation.tracking.TrackingScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    // Si ya hay sesión iniciada entra directo a la jugada; si no, al login
    val inicio = if (FirebaseAuth.getInstance().currentUser != null) "play" else "login"

    NavHost(
        navController = navController,
        startDestination = inicio
    ) {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("play") {
            PlayScreen(navController = navController)
        }

        composable("tracking") {
            TrackingScreen(navController = navController)
        }
    }
}