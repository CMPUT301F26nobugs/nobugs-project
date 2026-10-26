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

@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = OwnerRoutes.DASHBOARD) {

        // First-time download landing page (i.e. devices not in FireBase)
        composable("signUpScreen") { SignUpScreen(navController) }

        // ENTRANT NAVIGATION
        composable("entrantDashboard") { EntrantDashboard(navController) }
        composable("campsiteList") { CampsiteList(navController) }
        composable("notificationList") { NotificationList(navController) }
        composable("messageList") { MessageList(navController) }
        composable("profileScreen") { ProfileScreen(navController) }
        composable("qrCodeScreen") { QrCodeScreen(navController) }

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
    }
}