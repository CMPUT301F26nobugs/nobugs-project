package com.example.dascamp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.Screens.EntrantDashboard

@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "entrantDashboard") {
        composable("entrantDashboard") { EntrantDashboard(navController) }
    }
}