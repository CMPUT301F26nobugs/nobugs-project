package com.example.dascamp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.Screens.EntrantDashboard
import com.example.dascamp.Screens.SignUpScreen

@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "signUpScreen") {
        // First-time download landing page (i.e. devices not in FireBase)
        composable("signUpScreen") { SignUpScreen(navController) }

        // After downloading and signing-up their start destination will be redirected to their appropriate dashboard
        composable("entrantDashboard") { EntrantDashboard(navController) }
    }
}