package com.example.dascamp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.screens.EntrantCampsiteList
import com.example.dascamp.screens.EntrantCampsiteScreen
import com.example.dascamp.screens.EntrantDashboard
import com.example.dascamp.screens.EntrantMessageList
import com.example.dascamp.screens.EntrantMessageScreen
import com.example.dascamp.screens.EntrantNotificationList
import com.example.dascamp.screens.EntrantProfileScreen
import com.example.dascamp.screens.EntrantQrCodeScreen
import com.example.dascamp.screens.EntrantReservationList
import com.example.dascamp.screens.EntrantRoutes
import com.example.dascamp.screens.SignUpScreen

@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "signUpScreen") {

        // First-time download landing page (i.e. devices not in FireBase)
        composable("signUpScreen") { SignUpScreen(navController) }

        // ENTRANT NAVIGATION
        composable(EntrantRoutes.DASHBOARD) { EntrantDashboard(navController) }
        composable(EntrantRoutes.CAMPSITE_LIST) { EntrantCampsiteList(navController) }
        composable(EntrantRoutes.CAMPSITE) { EntrantCampsiteScreen(navController) }
        composable(EntrantRoutes.NOTIFICATION_LIST) { EntrantNotificationList(navController) }
        composable(EntrantRoutes.MESSAGE_LIST) { EntrantMessageList(navController) }
        composable(EntrantRoutes.MESSAGE) { EntrantMessageScreen(navController) }
        composable(EntrantRoutes.PROFILE) { EntrantProfileScreen(navController) }
        composable(EntrantRoutes.QR) { EntrantQrCodeScreen(navController) }
        composable(EntrantRoutes.RESERVATION_LIST) { EntrantReservationList(navController) }
        composable(EntrantRoutes.RESERVATION) { EntrantCampsiteScreen(navController) } // redirects back to campsite screen

        // OWNER NAVIGATION

        // ADMIN NAVIGATION

    }
}