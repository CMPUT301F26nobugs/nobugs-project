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


import com.example.dascamp.screens.adminScreens.AdminDashboard
import com.example.dascamp.screens.adminScreens.AdminProfileList
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.adminScreens.AdminViewProfile
import com.example.dascamp.screens.adminScreens.ApprovedLog
import com.example.dascamp.screens.adminScreens.NotificationLog
import com.example.dascamp.screens.adminScreens.OtherLog
import com.example.dascamp.screens.adminScreens.ProfileType
import com.example.dascamp.screens.adminScreens.RequestLog
import com.example.dascamp.screens.adminScreens.AdminCampsiteList
import com.example.dascamp.screens.adminScreens.AdminViewCampsite

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
        composable(EntrantRoutes.NOTIFICATION) { EntrantCampsiteScreen(navController) } // redirects back to campsite (screen buttons change depending on notification)
        composable(EntrantRoutes.MESSAGE_LIST) { EntrantMessageList(navController) }
        composable(EntrantRoutes.MESSAGE) { EntrantMessageScreen(navController) }
        composable(EntrantRoutes.PROFILE) { EntrantProfileScreen(navController) }
        composable(EntrantRoutes.QR) { EntrantQrCodeScreen(navController) }
        composable(EntrantRoutes.RESERVATION_LIST) { EntrantReservationList(navController) }
        composable(EntrantRoutes.RESERVATION) { EntrantCampsiteScreen(navController) } // redirects back to campsite screen

        // OWNER NAVIGATION

        // ADMIN NAVIGATION
        composable(AdminRoutes.DASHBOARD) { AdminDashboard(navController) }
        composable(AdminRoutes.PROFILE_LIST) { AdminProfileList(navController) }
        composable(AdminRoutes.PROFILE) { entry ->
            val type = ProfileType.valueOf(           // turn text into enum
                entry.arguments
                    ?.getString("profileType")  // ENTRANT or OWNER -> check is inside AdminViewProfile
                    ?: "ENTRANT"                      // default to ENTRANT if getString returns null
            )
            AdminViewProfile(navController, type)     // show the screen
        }
        composable(AdminRoutes.NOTIFICATION_LOG) { NotificationLog(navController) }
        composable(AdminRoutes.REQUEST_LOG) { RequestLog(navController) }
        composable(AdminRoutes.APPROVED_LOG) { ApprovedLog(navController) }
        composable(AdminRoutes.OTHER_LOG) { OtherLog(navController) }
        composable(AdminRoutes.CAMPSITE_LIST) { AdminCampsiteList(navController) }
        composable(AdminRoutes.CAMPSITE) { entry ->
            val campsiteId = entry.arguments?.getString("campsiteId") ?: "default_id"
            AdminViewCampsite(navController)
        }
    }
}