package com.example.dascamp.screens.adminScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.screens.AppScaffold

// ADMIN ROUTES
object AdminRoutes {
    const val DASHBOARD = "adminDashboard"
    const val CAMPSITE_LIST = "adminCampsiteList"
    const val CAMPSITE = "adminViewCampsite/{campsiteId}"
    const val IMAGE_GALLERY = "adminImageGallery"
    const val MESSAGE_LIST = "adminMessageList"
    const val MESSAGE = "adminMessageScreen"
    const val PROFILE_LIST = "adminProfileList"
    const val PROFILE = "adminProfile/{profileType}" // route pattern, registed in NavHost
    const val NOTIFICATION_LOG = "adminNotificationLog"
    const val REQUEST_LOG = "adminRequestLog"
    const val APPROVED_LOG = "adminApprovedLog"
    const val OTHER_LOG = "adminOtherLog"

    // Build route for a profile, ie. "adminProfile/OWNER"
    fun profile(type: ProfileType) = "adminProfile/${type.name}"
    fun campsite(id: String = "default_id") = "AdminViewCampsite/$id"
}

@Composable
fun AdminPage(
    navController: NavController,
    showBack: Boolean,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    AppScaffold(
        onBackClick = if (showBack) {{ navController.popBackStack() }} else null,
        navBar = { AdminNavBar(navController) }
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

// ADMIN NAVBAR
@Composable
fun AdminNavBar(navController: NavController) {
    // Parent for navbar
    Surface(color = MaterialTheme.colorScheme.primary) {

        // Parent for navbar contents
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Home Button
            IconButton(
                onClick = { navController.navigate(AdminRoutes.DASHBOARD) }
            ) { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onPrimary) }

            // Campsites Button
            IconButton(
                onClick = { navController.navigate(AdminRoutes.CAMPSITE_LIST) }
            ) { Icon(imageVector = Icons.Default.Place, contentDescription = "Campsites", tint = MaterialTheme.colorScheme.onPrimary) }

            // Profiles Button
            IconButton(
                onClick = { navController.navigate(AdminRoutes.PROFILE_LIST) }
            ) { Icon(imageVector = Icons.Default.Person, contentDescription = "Profiles", tint = MaterialTheme.colorScheme.onPrimary) }

            // Notification Logs Button
            IconButton(
                onClick = { navController.navigate(AdminRoutes.NOTIFICATION_LOG) }
            ) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notification Logs", tint = MaterialTheme.colorScheme.onPrimary) }

            // Image Gallery Button
            IconButton(
                onClick = { navController.navigate(AdminRoutes.IMAGE_GALLERY) }
            ) { Icon(imageVector = Icons.Default.Image, contentDescription = "Image Gallery", tint = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}