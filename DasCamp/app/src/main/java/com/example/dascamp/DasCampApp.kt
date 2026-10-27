package com.example.dascamp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.screens.CampsiteList
import com.example.dascamp.screens.EntrantDashboard
import com.example.dascamp.screens.MessageList
import com.example.dascamp.screens.NotificationList
import com.example.dascamp.screens.ProfileScreen
import com.example.dascamp.screens.QrCodeScreen
import com.example.dascamp.screens.ReservationList
import com.example.dascamp.screens.SignUpScreen

@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "signUpScreen") {

        // First-time download landing page (i.e. devices not in FireBase)
        composable("signUpScreen") { SignUpScreen(navController) }

        // ENTRANT NAVIGATION
        composable("entrantDashboard") { EntrantDashboard(navController) }
        composable("campsiteList") { CampsiteList(navController) }
        composable("notificationList") { NotificationList(navController) }
        composable("messageList") { MessageList(navController) }
        composable("profileScreen") { ProfileScreen(navController) }
        composable("qrCodeScreen") { QrCodeScreen(navController) }
        composable("reservationList") { ReservationList(navController) }

        // OWNER NAVIGATION

        // ADMIN NAVIGATION
    }
}