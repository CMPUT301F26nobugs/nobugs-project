package com.example.dascamp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Every owner screen in the storyboard
 * Buttons helps to navigate to these pages
 */
object OwnerRoutes {
    const val DASHBOARD = "ownerDashboard"
    const val CAMPSITES = "ownerCampsitesList"
    const val ADD = "ownerAddCampsite"
    const val CAMPSITE = "ownerCampsite"
    const val EDIT = "ownerEditCampsite"
    const val BOOKED = "ownerBookedDates"
    const val MAP = "ownerMapLocation"
    const val QR = "ownerQrCode"
    const val LOTTERIES = "ownerLotteryList"
    const val CREATE = "ownerCreateLottery"
    const val LOTTERY = "ownerLotteryDetails"
    const val EDIT_LOTTERY = "ownerEditLottery"
    const val ALERTS = "ownerNotifications"
    const val SEND = "ownerSendNotification"
    const val RESERVATIONS = "ownerReservations"
    const val RESERVATION = "ownerReservationDetails"
    const val PROFILE = "ownerProfile"
    const val MESSAGE_LIST = "ownerMessageList"
    const val MESSAGE = "ownerMessages"
}

/** Navbar tabs return to the dashboard first, so Back does not replay every tab */
fun NavController.openOwnerTab(route: String) {
    navigate(route) {
        popUpTo(OwnerRoutes.DASHBOARD) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * NavBar: Home, Campsites, Alerts, Reservations, Profile
 * The same five icons stay on every owner page
 */
@Composable
fun OwnerNavBar(navController: NavController) {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier.navigationBarsPadding().fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OwnerTab(navController, OwnerRoutes.DASHBOARD, Icons.Default.Home, "Home")
            OwnerTab(navController, OwnerRoutes.CAMPSITES, Icons.Default.Place, "Campsites")
            OwnerTab(navController, OwnerRoutes.ALERTS, Icons.Default.Notifications, "Alerts")
            OwnerTab(navController, OwnerRoutes.RESERVATIONS, Icons.Default.DateRange, "Reservations")
            OwnerTab(navController, OwnerRoutes.PROFILE, Icons.Default.Person, "Profile")
        }
    }
}

@Composable
private fun OwnerTab(navController: NavController, route: String, icon: ImageVector, label: String) {
    IconButton(onClick = { navController.openOwnerTab(route) }) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimary)
    }
}

/**
 * Root Tabs have no back arrow.
 * Every other screen uses the arrow to return to the page that opened it.
 */
@Composable
fun OwnerPage(
    navController: NavController,
    showBack: Boolean,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    AppScaffold(
        onBackClick = if (showBack) {{ navController.popBackStack() }} else null,
        navBar = { OwnerNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}



@Composable
fun BlockBox(height: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
    )
}