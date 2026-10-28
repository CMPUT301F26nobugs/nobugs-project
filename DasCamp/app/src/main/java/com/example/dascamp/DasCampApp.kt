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
import com.example.dascamp.screens.OwnerAddCampsite
import com.example.dascamp.screens.OwnerBookedDates
import com.example.dascamp.screens.OwnerCampsite
import com.example.dascamp.screens.OwnerCampsitesList
import com.example.dascamp.screens.OwnerCreateLottery
import com.example.dascamp.screens.OwnerDashboard
import com.example.dascamp.screens.OwnerEditCampsite
import com.example.dascamp.screens.OwnerEditLottery
import com.example.dascamp.screens.OwnerLotteryDetails
import com.example.dascamp.screens.OwnerLotteryList
import com.example.dascamp.screens.OwnerMapLocation
import com.example.dascamp.screens.OwnerMessageList
import com.example.dascamp.screens.OwnerMessages
import com.example.dascamp.screens.OwnerNotifications
import com.example.dascamp.screens.OwnerProfile
import com.example.dascamp.screens.OwnerQrCode
import com.example.dascamp.screens.OwnerReservationDetails
import com.example.dascamp.screens.OwnerReservations
import com.example.dascamp.screens.OwnerRoutes
import com.example.dascamp.screens.OwnerSendNotification

import com.example.dascamp.screens.adminScreens.AdminDashboard
import com.example.dascamp.screens.adminScreens.AdminProfileList
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.adminScreens.AdminViewProfile
import com.example.dascamp.screens.adminScreens.ApprovedLog
import com.example.dascamp.screens.adminScreens.NotificationLog
import com.example.dascamp.screens.adminScreens.OtherLog
import com.example.dascamp.screens.adminScreens.ProfileType
import com.example.dascamp.screens.adminScreens.RequestLog

@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = OwnerRoutes.DASHBOARD) {

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
        composable(OwnerRoutes.DASHBOARD) { OwnerDashboard(navController) }
        composable(OwnerRoutes.CAMPSITES) { OwnerCampsitesList(navController) }
        composable(OwnerRoutes.ADD) { OwnerAddCampsite(navController) }
        composable(OwnerRoutes.CAMPSITE) { OwnerCampsite(navController) }
        composable(OwnerRoutes.EDIT) { OwnerEditCampsite(navController) }
        composable(OwnerRoutes.BOOKED) { OwnerBookedDates(navController) }
        composable(OwnerRoutes.MAP) { OwnerMapLocation(navController) }
        composable(OwnerRoutes.QR) { OwnerQrCode(navController) }
        composable(OwnerRoutes.LOTTERIES) { OwnerLotteryList(navController) }
        composable(OwnerRoutes.CREATE) { OwnerCreateLottery(navController) }
        composable(OwnerRoutes.LOTTERY) { OwnerLotteryDetails(navController) }
        composable(OwnerRoutes.EDIT_LOTTERY) { OwnerEditLottery(navController) }
        composable(OwnerRoutes.ALERTS) { OwnerNotifications(navController) }
        composable(OwnerRoutes.SEND) { OwnerSendNotification(navController) }
        composable(OwnerRoutes.RESERVATIONS) { OwnerReservations(navController) }
        composable(OwnerRoutes.RESERVATION) { OwnerReservationDetails(navController) }
        composable(OwnerRoutes.PROFILE) { OwnerProfile(navController) }
        composable(OwnerRoutes.MESSAGE_LIST) { OwnerMessageList(navController) }
        composable(OwnerRoutes.MESSAGE) { OwnerMessages(navController) }

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
    }
}