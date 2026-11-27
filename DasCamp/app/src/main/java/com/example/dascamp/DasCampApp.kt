package com.example.dascamp

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.controllers.AdminController
import com.example.dascamp.controllers.CampsiteController
import com.example.dascamp.controllers.LotteryDrawWorker
import com.example.dascamp.controllers.LotteryProposalController
import com.example.dascamp.controllers.NotificationsController
import com.example.dascamp.controllers.ProfileController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.DeviceSession
import com.example.dascamp.screens.LocalCurrentUser
import com.example.dascamp.screens.SignUpScreen
import com.example.dascamp.screens.adminScreens.AdminCampsiteList
import com.example.dascamp.screens.adminScreens.AdminDashboard
import com.example.dascamp.screens.adminScreens.AdminMessageList
import com.example.dascamp.screens.adminScreens.AdminOwnerApplications
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
import com.example.dascamp.screens.ownerScreens.OwnerLotteryApprovals
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
import com.example.dascamp.views.NotificationView
import java.util.concurrent.TimeUnit
import android.Manifest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Root composable of the app: creates the repository and controllers, decides where a
 * user belongs based on their sign-in state, and hosts the navigation graph for all
 * three roles (entrant, owner, admin).
 *
 * **Identity.** The user id is the device's `ANDROID_ID`, read once and passed to
 * [DasCampRepository]. There is no password. The device id identifies the phone, and
 * the person can sign out so another profile on that phone can sign in or sign up.
 *
 * **Sign-in routing.** A single `LaunchedEffect` is the only navigation rule for sign-in
 * state. Once the repository has loaded its first users snapshot:
 * - no user: go to `signUpScreen`, clearing the back stack;
 * - a user while on `signUpScreen`: go to their home dashboard and remove the sign-up
 *   screen from the back stack. The home is the admin dashboard if the user is an admin,
 *   otherwise entrant, then owner, falling back to entrant if no role is set.
 *
 * This is the "effect at the top" that [SignUpScreen] refers to: that screen never
 * navigates itself.
 *
 * **Current user.** The repository's current user is exposed to the whole graph through
 * [LocalCurrentUser], which is what the header's role switcher reads.
 *
 * **Graph.** Entrant, owner, and admin routes are registered below, grouped by role.
 * The start destination is `signUpScreen`, so returning users briefly pass through it
 * (a spinner while users load) before being redirected.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DasCampApp() {
    // Page Navigation
    val navController = rememberNavController()
    val startDestination = "signUpScreen"
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()    // request permission
    ) { isGranted ->
        // isGranted == false: user declined so app won't display notifications
    }

    // The device id never changes. The active profile can, after sign out.
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }
    var activeUserId by remember { mutableStateOf(DeviceSession.activeUserId(context, deviceId)) }
    val signedIn = activeUserId != null
    val repository = remember(activeUserId ?: deviceId) {
        DasCampRepository(activeUserId ?: deviceId)
    }
    val signIn: (String, Boolean) -> Unit = { userId, remember ->
        DeviceSession.signIn(context, userId, remember)
        activeUserId = userId
    }
    val signOut: () -> Unit = {
        DeviceSession.signOut(context)
        activeUserId = null
    }

    // Controllers follow the active profile
    val scheduleController = remember(repository) { ScheduleController(repository) }
    val campsiteController = remember(repository) { CampsiteController(repository) }
    val reservationController = remember(repository) { ReservationController(repository) }
    val reservationView = remember(repository) { ReservationView(repository) }
    val notificationView = remember(repository) { NotificationView(repository) }
    val notificationsController = remember(repository) { NotificationsController(repository) }
    val lotteryProposalController = remember(repository) {
        LotteryProposalController(repository)
    }
    val adminController = remember(repository) { AdminController(repository) }

    // Persist the draw check across app closes and device restarts. The one-time
    // request handles lotteries that are already due; periodic work catches
    // registration periods that end later.
    LaunchedEffect(Unit) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.enqueueUniqueWork(
            LotteryDrawWorker.IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<LotteryDrawWorker>().build()
        )
        workManager.enqueueUniquePeriodicWork(
            LotteryDrawWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<LotteryDrawWorker>(15, TimeUnit.MINUTES).build()
        )
    }

    // The only navigation rule for sign-in state
    LaunchedEffect(repository.usersLoaded, repository.currentUser, signedIn) {
        if (!repository.usersLoaded) return@LaunchedEffect
        val user = repository.currentUser
        val route = navController.currentDestination?.route

        if (signedIn &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!signedIn || user == null) {
            // Signed out, new device, or the profile was deleted
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

    LaunchedEffect(activeUserId) {
        val userId = activeUserId ?: return@LaunchedEffect
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }
    }

    // Provide the local user to the navhost
    CompositionLocalProvider(LocalCurrentUser provides repository.currentUser) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            // First-time download landing page (i.e. devices not in FireBase)
            composable("signUpScreen") {
                SignUpScreen(navController, repository, deviceId, onSignedIn = signIn)
            }

            // ENTRANT NAVIGATION
            composable(EntrantRoutes.DASHBOARD) { EntrantDashboard(navController) }
            composable(EntrantRoutes.CAMPSITE_LIST) {
                EntrantCampsiteList(
                    navController,
                    repository,
                    campsiteController
                )
            }
            composable(EntrantRoutes.NOTIFICATION_LIST) {
                EntrantNotificationList(
                    navController = navController,
                    notificationView = notificationView,
                    notificationsController = notificationsController,
                    onLotterySelected = { repository.openLotteryId = it }
                )
            }
            composable(EntrantRoutes.MESSAGE_LIST) { EntrantMessageList(navController, repository) }
            composable(EntrantRoutes.MESSAGE) { EntrantMessageScreen(navController, repository) }
            composable(EntrantRoutes.RESERVATION_LIST) {
                EntrantReservationList(navController, reservationController, repository)
            }
            composable(EntrantRoutes.PROFILE) {
                EntrantProfileScreen(navController, repository, onSignOut = signOut)
            }

            composable(EntrantRoutes.RESERVATION) { EntrantCampsiteScreen(navController) } // redirects back to campsite screen

            composable(EntrantRoutes.CAMPSITE) {
                EntrantCampsiteScreen(
                    navController,
                    repository,
                    scheduleController,
                    reservationView
                )
            }
            composable(EntrantRoutes.QR) { EntrantQrCodeScreen(navController, repository) }

            // OWNER NAVIGATION
            composable(OwnerRoutes.DASHBOARD) {
                OwnerDashboard(navController, repository)
            }
            composable(OwnerRoutes.APPROVALS) {
                OwnerLotteryApprovals(navController, repository, lotteryProposalController)
            }
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
            composable(OwnerRoutes.QR) { OwnerQrCode(navController, repository) }
            composable(OwnerRoutes.LOTTERIES) { OwnerLotteryList(navController, repository) }
            composable(OwnerRoutes.CREATE) { OwnerCreateLottery(navController, repository) }
            composable(OwnerRoutes.LOTTERY) { OwnerLotteryDetails(navController, repository) }
            composable(OwnerRoutes.EDIT_LOTTERY) { OwnerEditLottery(navController, repository) }
            composable(OwnerRoutes.ALERTS) {
                OwnerNotifications(navController, repository, notificationsController)
            }
            composable(OwnerRoutes.SEND) {
                OwnerSendNotification(navController, notificationsController, repository)
            }
            composable(OwnerRoutes.RESERVATIONS) { OwnerReservations(navController, repository) }
            composable(OwnerRoutes.RESERVATION) { OwnerReservationDetails(navController, repository) }
            composable(OwnerRoutes.PROFILE) { OwnerProfile(navController, repository, onSignOut = signOut) }
            composable(OwnerRoutes.MESSAGE_LIST) { OwnerMessageList(navController, repository) }
            composable(OwnerRoutes.MESSAGE) { OwnerMessages(navController, repository) }

            // ADMIN NAVIGATION
            composable(AdminRoutes.DASHBOARD) { AdminDashboard(navController, onSignOut = signOut) }
            composable(AdminRoutes.PROFILE_LIST) { AdminProfileList(navController, repository) }
            composable(AdminRoutes.PROFILE) { entry ->
                val type = ProfileType.valueOf(           // turn text into enum
                    entry.arguments
                        ?.getString("profileType")  // ENTRANT or OWNER -> check is inside AdminViewProfile
                        ?: "ENTRANT"                      // default to ENTRANT if getString returns null
                )
                val uid = entry.arguments?.getString("uid") ?: ""
                AdminViewProfile(navController, type, repository, uid)     // show the screen
            }
            composable(AdminRoutes.NOTIFICATION_LOG) { NotificationLog(navController) }
            composable(AdminRoutes.REQUEST_LOG) { RequestLog(navController, repository) }
            composable(AdminRoutes.APPROVED_LOG) { ApprovedLog(navController, repository) }
            composable(AdminRoutes.OTHER_LOG) { OtherLog(navController, repository) }
            composable(AdminRoutes.CAMPSITE_LIST) { AdminCampsiteList(navController, repository) }
            composable(AdminRoutes.CAMPSITE) { entry ->
                val campsiteId = entry.arguments?.getString("campsiteId") ?: ""
                AdminViewCampsite(navController, repository, campsiteId, scheduleController)
            }
            composable(AdminRoutes.IMAGE_GALLERY) { ImageGallery(navController) }
            composable(AdminRoutes.MESSAGE_LIST) { AdminMessageList(navController) }
            composable(AdminRoutes.MESSAGE) { entry ->
                val messageId = entry.arguments?.getString("messageId") ?: "default_id"
                AdminViewMessage(navController)
            }
            composable(AdminRoutes.OWNER_APPLICATIONS) { AdminOwnerApplications(navController, repository, adminController) }
        }
    }
}
