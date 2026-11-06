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
import com.example.dascamp.model.Role
import com.example.dascamp.screens.AppScaffold
import com.example.dascamp.screens.RoleMenu

/**
 * Navigation routes for the admin screens.
 *
 * Constants containing `{placeholders}` are route patterns, registered in the NavHost.
 * To navigate to one of them, build the concrete route with [profile], [campsite],
 * or [message] instead of using the constant directly.
 */
object AdminRoutes {
    const val DASHBOARD = "adminDashboard"
    const val CAMPSITE_LIST = "adminCampsiteList"
    const val CAMPSITE = "adminViewCampsite/{campsiteId}"
    const val IMAGE_GALLERY = "adminImageGallery"
    const val MESSAGE_LIST = "adminMessageList"
    const val MESSAGE = "adminViewMessage/{messageId}"
    const val PROFILE_LIST = "adminProfileList"
    const val PROFILE = "adminProfile/{profileType}" // route pattern, registed in NavHost
    const val NOTIFICATION_LOG = "adminNotificationLog"
    const val REQUEST_LOG = "adminRequestLog"
    const val APPROVED_LOG = "adminApprovedLog"
    const val OTHER_LOG = "adminOtherLog"

    /**
     * Builds the route for a profile screen, e.g. `"adminProfile/OWNER"`.
     *
     * @param type the kind of profile to show
     */
    fun profile(type: ProfileType) = "adminProfile/${type.name}"

    /**
     * Builds the route for viewing a single campsite.
     *
     * @param id the campsite id; defaults to `"default_id"`
     */
    fun campsite(id: String = "default_id") = "AdminViewCampsite/$id"

    /**
     * Builds the route for viewing a single message.
     *
     * @param id the message id; defaults to `"default_id"`
     */
    fun message(id: String ="default_id") = "adminViewMessage/$id"
}

/**
 * Common layout for admin screens: the app scaffold with the admin navigation bar
 * and role menu, wrapped around a scrollable, padded column.
 *
 * @param navController used for the back button and the navigation bar
 * @param showBack whether to show a back button that pops the back stack
 * @param horizontalAlignment how the children of the column are aligned horizontally;
 * defaults to [Alignment.Start]
 * @param content the screen's content, laid out inside the column
 */
@Composable
fun AdminPage(
    navController: NavController,
    showBack: Boolean,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    AppScaffold(
        onBackClick = if (showBack) {{ navController.popBackStack() }} else null,
        navBar = { AdminNavBar(navController) },
        roleMenu = { RoleMenu(navController, Role.ADMIN) }
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

/**
 * Bottom navigation bar for admin screens, with buttons for the dashboard,
 * campsites, profiles, notification logs, and the image gallery.
 *
 * @param navController used to navigate to the selected destination
 */
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