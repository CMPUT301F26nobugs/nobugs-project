package com.example.dascamp

import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.controllers.CampsiteController
import com.example.dascamp.controllers.ProfileController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.screens.LocalCurrentUser
import com.example.dascamp.screens.SignUpScreen
import com.example.dascamp.screens.adminScreens.AdminCampsiteList
import com.example.dascamp.screens.adminScreens.AdminDashboard
import com.example.dascamp.screens.adminScreens.AdminMessageList
import com.example.dascamp.screens.adminScreens.AdminProfileList
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.adminScreens.AdminViewCampsite
import com.example.dascamp.screens.adminScreens.AdminViewMessage
import com.example.dascamp.screens.adminScreens.AdminViewProfile
import com.example.dascamp.screens.adminScreens.ApprovedLog
import com.example.dascamp.screens.adminScreens.ImageGallery
import com.example.dascamp.screens.adminScreens.NotificationLog
import com.example.dascamp.screens.adminScreens.OtherLog
import com.example.dascamp.screens.adminScreens.ProfileType
import com.example.dascamp.screens.adminScreens.RequestLog
import com.example.dascamp.screens.entrantScreens.EntrantCampsiteList
import com.example.dascamp.screens.entrantScreens.EntrantCampsiteScreen
import com.example.dascamp.screens.entrantScreens.EntrantDashboard
import com.example.dascamp.screens.entrantScreens.EntrantMessageList
import com.example.dascamp.screens.entrantScreens.EntrantMessageScreen
import com.example.dascamp.screens.entrantScreens.EntrantNotificationList
import com.example.dascamp.screens.entrantScreens.EntrantProfileScreen
import com.example.dascamp.screens.entrantScreens.EntrantQrCodeScreen
import com.example.dascamp.screens.entrantScreens.EntrantReservationList
import com.example.dascamp.screens.entrantScreens.EntrantRoutes
import com.example.dascamp.screens.ownerScreens.OwnerAddCampsite
import com.example.dascamp.screens.ownerScreens.OwnerBookedDates
import com.example.dascamp.screens.ownerScreens.OwnerCampsite
import com.example.dascamp.screens.ownerScreens.OwnerCampsitesList
import com.example.dascamp.screens.ownerScreens.OwnerCreateLottery
import com.example.dascamp.screens.ownerScreens.OwnerDashboard
import com.example.dascamp.screens.ownerScreens.OwnerEditCampsite
import com.example.dascamp.screens.ownerScreens.OwnerEditLottery
import com.example.dascamp.screens.ownerScreens.OwnerLotteryDetails
import com.example.dascamp.screens.ownerScreens.OwnerLotteryList
import com.example.dascamp.screens.ownerScreens.OwnerMapLocation
import com.example.dascamp.screens.ownerScreens.OwnerMessageList
import com.example.dascamp.screens.ownerScreens.OwnerMessages
import com.example.dascamp.screens.ownerScreens.OwnerNotifications
import com.example.dascamp.screens.ownerScreens.OwnerProfile
import com.example.dascamp.screens.ownerScreens.OwnerQrCode
import com.example.dascamp.screens.ownerScreens.OwnerReservationDetails
import com.example.dascamp.screens.ownerScreens.OwnerReservations
import com.example.dascamp.screens.ownerScreens.OwnerRoutes
import com.example.dascamp.screens.ownerScreens.OwnerSendNotification
import com.example.dascamp.views.ReservationView

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DasCampApp() {

    // Page Navigation
    val navController = rememberNavController()
    val startDestination = "signUpScreen"
    val context = LocalContext.current

    // The device id is the user id
    val repository = remember {
        val uid = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        ) ?: ""
        DasCampRepository(uid)
    }

    // Controllers
    val scheduleController = remember{ ScheduleController(repository) }
    val campsiteController = remember { CampsiteController(repository) }
    val profileController = remember { ProfileController(repository) }
    val reservationController = remember { ReservationController(repository) }   // next to the other controllers
    val reservationView = remember{ ReservationView(repository) }

    // The only navigation rule for sign-in state
    LaunchedEffect(repository.usersLoaded, repository.currentUser) {
        if (!repository.usersLoaded) return@LaunchedEffect
        val user = repository.currentUser
        val route = navController.currentDestination?.route
        if (user == null) {
            // New device, or the profile was deleted
            if (route != "signUpScreen") {
                navController.navigate("signUpScreen") { popUpTo(0) }
            }
        } else if (route == "signUpScreen") {
            // Signed up or returning: leave the form
            val home = when {
                user.admin -> AdminRoutes.DASHBOARD
                user.entrant -> EntrantRoutes.DASHBOARD
                user.owner -> OwnerRoutes.DASHBOARD
                else -> EntrantRoutes.DASHBOARD
            }
            navController.navigate(home) { popUpTo("signUpScreen") { inclusive = true } }
        }
    }


    // Provide the local user to the navhost
    CompositionLocalProvider(LocalCurrentUser provides repository.currentUser) {
        NavHost(navController = navController, startDestination = startDestination) {
            // First-time download landing page (i.e. devices not in FireBase)
            composable("signUpScreen") { SignUpScreen(navController, repository) }

            // ENTRANT NAVIGATION
            composable(EntrantRoutes.DASHBOARD) { EntrantDashboard(navController) }
            composable(EntrantRoutes.CAMPSITE_LIST) {
                EntrantCampsiteList(
                    navController,
                    repository,
                    campsiteController
                )
            }
            composable(EntrantRoutes.NOTIFICATION_LIST) { EntrantNotificationList(navController) }
            composable(EntrantRoutes.MESSAGE_LIST) { EntrantMessageList(navController) }
            composable(EntrantRoutes.MESSAGE) { EntrantMessageScreen(navController) }
            composable(EntrantRoutes.RESERVATION_LIST) { EntrantReservationList(navController, reservationController) }
            composable(EntrantRoutes.PROFILE) { EntrantProfileScreen(navController, repository) }

            composable(EntrantRoutes.RESERVATION) { EntrantCampsiteScreen(navController) } // redirects back to campsite screen

            composable(EntrantRoutes.CAMPSITE) {
                EntrantCampsiteScreen(
                    navController,
                    repository,
                    scheduleController,
                    reservationView
                )
            }
            //composable(EntrantRoutes.QR) { EntrantQrCodeScreen(navController) }

            // OWNER NAVIGATION
            composable(OwnerRoutes.DASHBOARD) { OwnerDashboard(navController) }
            composable(OwnerRoutes.CAMPSITES) { OwnerCampsitesList(navController, repository) }
            composable(OwnerRoutes.ADD) { OwnerAddCampsite(navController, repository) }
            composable(OwnerRoutes.CAMPSITE) { OwnerCampsite(navController, repository) }
            composable(OwnerRoutes.EDIT) { OwnerEditCampsite(navController, repository) }
            composable(OwnerRoutes.BOOKED) {
                OwnerBookedDates(
                    navController,
                    repository,
                    scheduleController,
                    reservationView
                )
            }
            composable(OwnerRoutes.MAP) { OwnerMapLocation(navController, repository) }
            composable(OwnerRoutes.QR) { OwnerQrCode(navController) }
            composable(OwnerRoutes.LOTTERIES) { OwnerLotteryList(navController, repository) }
            composable(OwnerRoutes.CREATE) { OwnerCreateLottery(navController, repository) }
            composable(OwnerRoutes.LOTTERY) { OwnerLotteryDetails(navController) }
            composable(OwnerRoutes.EDIT_LOTTERY) { OwnerEditLottery(navController, repository) }
            composable(OwnerRoutes.ALERTS) { OwnerNotifications(navController, repository) }
            composable(OwnerRoutes.SEND) { OwnerSendNotification(navController, repository) }
            composable(OwnerRoutes.RESERVATIONS) { OwnerReservations(navController) }
            composable(OwnerRoutes.RESERVATION) { OwnerReservationDetails(navController) }
            composable(OwnerRoutes.PROFILE) { OwnerProfile(navController, repository) }
            composable(OwnerRoutes.MESSAGE_LIST) { OwnerMessageList(navController, repository) }
            composable(OwnerRoutes.MESSAGE) { OwnerMessages(navController, repository) }

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
            composable(AdminRoutes.IMAGE_GALLERY) { ImageGallery(navController) }
            composable(AdminRoutes.MESSAGE_LIST) { AdminMessageList(navController) }
            composable(AdminRoutes.MESSAGE) { entry ->
                val messageId = entry.arguments?.getString("messageId") ?: "default_id"
                AdminViewMessage(navController)
            }
        }
    }
}