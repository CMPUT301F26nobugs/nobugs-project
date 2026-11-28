package com.example.dascamp.screens.adminScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.screens.EmptyValue
import com.example.dascamp.screens.LocalCurrentUser
import com.example.dascamp.ui.theme.DasCampTheme
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.example.dascamp.controllers.AdminController
import com.example.dascamp.controllers.MessageController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Policy
import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Notification
import com.example.dascamp.model.NotificationType
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.OwnerMessage
import com.example.dascamp.model.User
import com.example.dascamp.model.formatPrice
import com.example.dascamp.screens.CampsiteMap
import com.example.dascamp.screens.StoredImage
import com.example.dascamp.screens.entrantScreens.CampsiteFact
import com.example.dascamp.screens.entrantScreens.CampsiteSection
import com.example.dascamp.screens.entrantScreens.EntrantCalendarGrid
import com.example.dascamp.screens.entrantScreens.EntrantPage
import com.example.dascamp.screens.entrantScreens.LegendDot
import com.example.dascamp.screens.entrantScreens.readableCoordinates
import com.example.dascamp.screens.ownerScreens.OwnerPage
import com.example.dascamp.screens.ownerScreens.OwnerRoutes
import com.example.dascamp.views.MessageView
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.orEmpty
import kotlin.text.orEmpty

/**
 * The kinds of profile an admin can browse.
 *
 * @property label the plural name shown on filter chips
 */
enum class ProfileType(val label: String) {
    ENTRANT("Entrants"),
    OWNER("Owners"),
    ADMIN("Admins")
}

/**
 * Admin home screen, with shortcuts to the campsite list, the profile list,
 * and the notification logs.
 *
 * @param navController used to navigate to the selected section
 */
@Composable
fun AdminDashboard(navController: NavController, onSignOut: () -> Unit = {}) {
    val firstName = LocalCurrentUser.current?.firstName
        ?.takeIf { it.isNotBlank() } ?: "User"

    AdminPage(
        navController = navController,
        showBack = false,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title container
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome Back, $firstName", style = MaterialTheme.typography.titleLarge)
        }

        // Campsite button & profiles button container
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Campsite list button
            BigButton(
                onClickAction = { navController.navigate(AdminRoutes.CAMPSITE_LIST) },
                icon = Icons.Default.LocationOn,
                iconDesc = "campsites",
                label = "View\nCampsites"
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Profile list button
            BigButton(
                onClickAction = { navController.navigate(AdminRoutes.PROFILE_LIST) },
                icon = Icons.Default.Person,
                iconDesc = "profiles",
                label = "View\nProfiles"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(AdminRoutes.NOTIFICATION_LOG) }
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notification logs")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(AdminRoutes.OWNER_APPLICATIONS) }
            ) {
                Icon(Icons.Default.HowToReg, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Owner applications")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign out")
            }
        }
    }
}

@Preview
@Composable
fun AdminDashboardPreview() {
    DasCampTheme {
        AdminDashboard(rememberNavController())
    }
}

/**
 * Screen listing user profiles, with a search field and entrant/owner filter chips.
 *
 * The search text and selected filter survive configuration changes. Selecting
 * the chosen chip again clears the filter (shows all). The list itself is still
 * a placeholder card that navigates to a profile.
 *
 * @param navController used to navigate to a profile
 */
@Composable
fun AdminProfileList(navController: NavController, campStore: CampStore) {
    var query by rememberSaveable { mutableStateOf("") }                        // profile search
    var selectedType by rememberSaveable { mutableStateOf<ProfileType?>(null) } // null = show all

    //filter by entrant or owner toggles
    val filteredUsers = campStore.users.filter {u ->
        val matchedTypeFilter = if(selectedType == ProfileType.ADMIN){  //filter chip selected is admin
            u.admin  //only show admins
        }else if(u.admin){
            false  //do not display admins in general list
        }else if(selectedType == ProfileType.ENTRANT){
            u.entrant && !u.owner  //entrant role only, no owners
        }else if(selectedType == ProfileType.OWNER){
            u.owner
        }else{
            u.entrant || u.owner  //no filter appiled, only show entrants and owners, no admins
        }

        val matchedSearchResults = query.isBlank() ||
                u.displayName().contains(query, ignoreCase = true) ||
                u.email.contains(query, ignoreCase = true)

        matchedTypeFilter && matchedSearchResults  //what the filter returns
    }

    AdminPage(
        navController = navController,
        showBack = false,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Profiles", style = MaterialTheme.typography.titleLarge) }

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Profiles") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        // Filter -> 2 options (entrants or owners) only
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileType.values().forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = {
                        if (selectedType == type) {  // click selected chip again deselect
                            selectedType = null
                        } else {
                            selectedType = type
                        }
                    },
                    label = { Text(type.label) }
                )
            }
        }
        if(filteredUsers.isEmpty()){
            Text("No profiles found")
        }
        filteredUsers.forEach{user ->
            Card(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        val type = if(user.admin){  //determine the user type of the clicked profile
                            ProfileType.ADMIN
                        }else if(user.owner){
                            ProfileType.OWNER
                        }else{
                            ProfileType.ENTRANT
                        }
                        navController.navigate(AdminRoutes.profile(type, user.id))
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ){
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(user.displayName(), style = MaterialTheme.typography.titleMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/**
 * Detail screen for a single entrant or owner profile.
 *
 * Both types can have their profile picture deleted. Entrants can additionally be
 * deleted, while owners offer a link to their messages with entrants and a
 * "Revoke Campsite Owner Role" action. Destructive actions ask for confirmation first.
 * The profile data and the delete actions are still placeholders.
 *
 * @param navController used for back navigation and for following actions
 * @param profileType whether this is an entrant or an owner profile
 */
@Composable
fun AdminViewProfile(navController: NavController, profileType: ProfileType, campStore: CampStore, uid: String) {

    val user = campStore.userById(uid)  //get the specific user by the uid
    val adminController = remember(campStore) {AdminController(campStore)}

    AdminPage(
        navController = navController,
        showBack = true,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        //display what role the use has in their profile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if(profileType == ProfileType.ADMIN){
                    "Admin"
                }else if(profileType == ProfileType.OWNER){
                    "Entrant/Owner"
                }else{
                    "Entrant"
                },
                style = MaterialTheme.typography.titleLarge
            )
        }

        if(user == null){
            Text("Profile not found")
        } else{

            // Profile picture (PLACEHOLDER)
            Card(
                modifier = Modifier.size(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Icon(Icons.Default.Person, "Profile picture", Modifier.fillMaxSize()) }

            // Profile info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Name", style = MaterialTheme.typography.labelLarge)
                Text(user.displayName())
                Text("Email", style = MaterialTheme.typography.labelLarge)
                Text(user.email)
                Text("Phone", style = MaterialTheme.typography.labelLarge)
                Text(user.phoneNumber.ifBlank{"N/A"})
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Both profile types: delete profile picture
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = rememberConfirmClick(
                    message = "Remove this profile picture? This can't be undone.",
                    onConfirm =  {navController.navigate(AdminRoutes.PROFILE_LIST
                    ) }
                )  /*todo: implement delete*/
            ) { Text("Delete Profile Picture") }

            if (profileType == ProfileType.OWNER || profileType == ProfileType.ADMIN) {
                // Owner roles only -> view messages with entrants (same message list as from a campsite)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(AdminRoutes.MESSAGE_LIST) }
                ) { Text("View Messages with Entrants") }
            }
            if(profileType == ProfileType.OWNER){
                // Owner only -> remove campsite owner role
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = destructiveButtonColors(),
                    onClick = rememberConfirmClick(
                        message = "Remove owner role? This can't be undone.",
                        onConfirm = {
                            adminController.revokeOwnership(user)  //remove ownership from owned campsites
                            campStore.updateUser(user.copy(owner = false))
                        }
                    )
                ) {
                    Text("Revoke Campsite Owner Role")
                }
            }
            //delete profile option only for entrants and owners, cannot delete admins
            if(profileType != ProfileType.ADMIN) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = destructiveButtonColors(),
                    onClick = rememberConfirmClick(
                        message = "Remove this profile? This can't be undone.",
                        onConfirm = {
                            campStore.deleteUser(user)
                            navController.popBackStack()  //for owner profile, if came from campsite page to back to camp list, if come from profile list, go back to profile list
                        }
                    )
                ) {
                    Text("Delete Profile")
                }
            }
        }
    }
}

/** Button colors for destructive actions (error container with matching content color). */
@Composable
private fun destructiveButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.error,
    contentColor = MaterialTheme.colorScheme.onError
)

/**
 * Notification log hub, with buttons leading to the campsite request log,
 * the approved request log, and the "other" log.
 *
 * @param navController used to navigate to the selected log
 */
@Composable
fun NotificationLog(navController: NavController) {
    AdminPage(
        navController = navController,
        showBack = false,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Notification Logs", style = MaterialTheme.typography.titleLarge) }

        // User requests for campsites
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(AdminRoutes.REQUEST_LOG) }
        ) { Text("Campsite Requests") }

        // Approved requests
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(AdminRoutes.APPROVED_LOG) }
        ) { Text("Approved Requests") }

        // Other
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(AdminRoutes.OTHER_LOG) }
        ) { Text("Other") }
    }
}

@Preview
@Composable
fun NotificationLogPreview() {
    DasCampTheme {
        NotificationLog(rememberNavController())
    }
}

/**
 * Lists users whose owner application is pending, with approve and deny actions.
 *
 * @param navController used for back navigation
 * @param store source of all users
 * @param adminController performs the approve and deny actions
 */
@Composable
fun AdminOwnerApplications(
    navController: NavController,
    store: CampStore? = null,
    adminController: AdminController? = null
) {
    val pending = store?.users.orEmpty()
        .filter { it.ownerApplication == OwnerApplicationStatus.PENDING }

    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Owner Applications", style = MaterialTheme.typography.titleLarge) }

        if (pending.isEmpty()) Text("No pending applications")

        pending.forEach { user ->
            OwnerApplicationCard(
                user = user,
                onApprove = { adminController?.approveOwner(user) },
                onDeny = { adminController?.rejectOwner(user) }
            )
        }
    }
}

@Composable
private fun OwnerApplicationCard(user: User, onApprove: () -> Unit, onDeny: () -> Unit) {
    // rememberConfirmClick must be called inside a composable, so each card makes its own
    val confirmDeny = rememberConfirmClick(
        message = "Deny ${user.firstName}'s application?",
        confirmText = "Deny",
        onConfirm = onDeny
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(user.displayName(), style = MaterialTheme.typography.titleMedium)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove) { Text("Approve") }
                Button(onClick = confirmDeny, colors = destructiveButtonColors()) { Text("Deny") }
            }
        }
    }
}

/**
 * Log of campsite requests made by users.
 *
 * @param navController used for navigation
 */
@Composable
fun RequestLog(
    navController: NavController,
    store: CampStore? = null
) {
    LogScreen(navController, "Campsite Requests", store) { it.type == NotificationType.WIN }
}

/**
 * Log of approved requests.
 *
 * @param navController used for navigation
 */
@Composable
fun ApprovedLog(
    navController: NavController,
    store: CampStore? = null
) {
    LogScreen(navController, "Approved Requests", store) { it.type == NotificationType.CANCELLATION }
}

/**
 * Log of notifications that don't fit the request or approved logs.
 *
 * @param navController used for navigation
 */
@Composable
fun OtherLog(
    navController: NavController,
    store: CampStore? = null
) {
    LogScreen(navController, "Other", store) { true }
}

/**
 * Shared skeleton for the three log screens: a centered title above a list of
 * log entries (still a placeholder).
 *
 * @param navController used for navigation
 * @param title the heading shown at the top of the screen
 */
@Composable
private fun LogScreen(
    navController: NavController,
    title: String,
    store: CampStore?,
    include: (Notification) -> Boolean = { true }
) {

    val notes = store?.notifications.orEmpty()
        .filter(include) // filter by notification types
        .distinctBy { if (it.batchId.isBlank()) it.id else it.batchId } // Groups messages sent together
        .sortedByDescending { it.sentAt.toLongOrNull() ?: 0L }

    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }

        // Display notifications if the list is not empty
        if (notes.isEmpty())
            Text("No notifications found.")
        notes.forEach { note ->

            // Time sent
            val time = note.sentAt.toLongOrNull()?.let {
                // Format the time to display as "yyyy-MM-dd HH:mm"
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                    .format(java.util.Date(it))
            } ?: "Unknown Time" // display "Unknown Time" if it cannot be formatted.

            val sender = if (note.senderId.isBlank()) "System"
                else store?.userById(note.senderId)?.displayName() ?: note.senderId

            // Notification Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(note.message, style = MaterialTheme.typography.bodyLarge)
                    Text("To: ${note.recipientName}", style = MaterialTheme.typography.bodyMedium)
                    Text("From: $sender", style = MaterialTheme.typography.bodyMedium)   // new line
                    Text("$time · ${note.type}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview
@Composable
fun RequestLogPreview() {
    DasCampTheme {
        RequestLog(rememberNavController())
    }
}

/**
 * Screen listing campsites, with a search field. The list itself is still a
 * placeholder card that opens a campsite.
 *
 * @param navController used to navigate to a campsite
 */
@Composable
fun AdminCampsiteList(
    navController: NavController,
    repository: DasCampRepository? = null
) {
    var query by rememberSaveable { mutableStateOf("") } // campsite search
    val filteredCamps = repository?.campsites?.filter { site ->
        query.isBlank() ||
            site.name.contains(query, ignoreCase = true) ||
            site.location.contains(query, ignoreCase = true)
    }.orEmpty()
    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Campsites", style = MaterialTheme.typography.titleLarge) }

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Campsites") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        if (filteredCamps.isEmpty()) {
            Text("No campsites")
        } else {
            filteredCamps.forEach { campsite ->
                AdminCampsiteSummaryCard(
                    campsite = campsite,
                    onClick = {
                        navController.navigate(AdminRoutes.campsite(campsite.id))
                    }
                )
            }
        }
    }
}

/**
 * One campsite in the admin camplist. The name and place stay on the left.
 * The rating sits in its own chip on the right. US 03.04.01
 */

@Composable
private fun AdminCampsiteSummaryCard(campsite: Campsite, onClick: () -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = campsite.name.ifBlank { "Untitled campsite" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${campsite.location.ifBlank { "Location not set" }}  ·  up to ${campsite.maxOccupancy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "★ ${"%.1f".format(campsite.rating)}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Detail screen for a single campsite, as seen by an admin.
 *
 * Shows the image, main info, rules, geolocation, and availability legend
 * (all placeholders for now), plus actions to delete the image, view messages
 * with entrants, view the owner's profile, and remove the campsite. Destructive
 * actions ask for confirmation first.
 *
 * @param navController used for back navigation and for following actions
 */
@Composable
fun AdminViewCampsite(
    navController: NavController,
    repository: DasCampRepository? = null,
    campsiteId: String? = null,
    scheduleController: ScheduleController? = null
) {
    var pendingRemoval by remember { mutableStateOf<Pair<String, String>?>(null) }
    val admin = remember(repository) { repository?.let { AdminController(it) } }
    val campsite = repository?.campsites?.find { it.id == campsiteId }
    val reviews = repository?.reviews?.filter { it.campsiteId == campsiteId }.orEmpty()

    //if no campsite, show cant be found message
    if (campsite == null){
        AdminPage(navController = navController, showBack = true) {
            Text("Campsite not found")
        }
        return
    }


    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Campsite Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            StoredImage(
                path = campsite.imageUri,
                contentDescription = campsite.name,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No campsite photo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        //delete image option only if there is an image
        if (!campsite.imageUri.isNullOrBlank()) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = destructiveButtonColors(),
                onClick = rememberConfirmClick(
                    message = "Remove this image? This can't be undone.",
                    onConfirm = {
                        repository.updateCampsite(campsite.copy(imageUri = ""))
                    }
                )
            ) {
                Text("Delete Campsite Image") /*todo check this */
            }
        }

        Text(  //name
            text = campsite.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth()
        )
        Text(  //location
            text = campsite.location.ifBlank { "Location not set" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        //campsite details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CampsiteFact("Rating", "%.1f".format(campsite.rating))
                CampsiteFact("Reviews", campsite.ratingCount.toString())
                CampsiteFact("Guests", "Up to ${campsite.maxOccupancy}")
                CampsiteFact("Night", "$${formatPrice(campsite.pricePerNight)}")
            }
        }

        //tags
        if (campsite.tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                campsite.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        Text("Map", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clipToBounds()
                ) {
                    CampsiteMap(
                        latitude = campsite.latitude,
                        longitude = campsite.longitude,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = campsite.location.ifBlank { "Campsite pin" },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = readableCoordinates(campsite.latitude, campsite.longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        CampsiteSection("About", campsite.description)
        CampsiteSection("Rules and policies", campsite.rules)
        CampsiteSection("Cancellation", campsite.cancellationPolicy)

        //reviews
        Text("Reviews", style = MaterialTheme.typography.titleMedium)
        if (reviews.isEmpty()) {
            Text(
                "No reviews yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            reviews.forEach { review ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            review.entrantName.ifBlank { "Entrant" },
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (review.ratingRemoved) "Rating removed" else "${review.rating} / 5",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (review.body.isNotBlank()) {
                            Text(review.body, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            colors = destructiveButtonColors(),
                            enabled = !review.ratingRemoved,  //only enable button if rating not removed
                            onClick = {
                                pendingRemoval = review.id to "rating"
                            }
                        ) {
                            Text("Remove Rating")
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            colors = destructiveButtonColors(),
                            onClick = {
                                pendingRemoval = review.id to "review"
                            }
                        ) {
                            Text("Remove Review")
                        }
                    }
                }
                pendingRemoval?.let { (reviewId, kind) ->
                    Dialog(onDismissRequest = { pendingRemoval = null }) {
                        Column(
                            modifier = Modifier
                                .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                if (kind == "rating") {
                                    "Remove this rating? It will no longer count toward the campsite  and can't be undone."
                                } else {
                                    "Remove this review? This can't be undone."
                                },
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { pendingRemoval = null }) { Text("Cancel") }
                                TextButton(onClick = {
                                    pendingRemoval = null
                                    if (kind == "rating") admin?.removeRating(reviewId)
                                    else admin?.removeReview(reviewId)
                                }) { Text("Remove", color = Color.Red) }
                            }
                        }
                    }
                }
            }
        }

        Text("Availability", style = MaterialTheme.typography.titleMedium)
        if (campsite.availability.isEmpty()) {
            Text(
                "The owner has not posted open dates.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            campsite.availability.forEach { range ->
                Text(
                    "Open  ${range.start}  to  ${range.end}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        EntrantCalendarGrid(campsite, scheduleController)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendDot(Color(0xFF66BB6A), "Open")
            LegendDot(Color(0xFFC62828), "Booked")
            LegendDot(Color(0xFF9E9E9E), "Closed")
        }


        // view messages with entrants (same message list as from a campsite)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(AdminRoutes.MESSAGE_LIST) }
        ) { Text("View Messages between Owner and Entrants") }

        if (campsite.ownerId.isBlank()) {
            Text(
                "There is no owner for this campsite",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(AdminRoutes.profile(ProfileType.OWNER, campsite.ownerId))
                }
            ) {
                Text("View Owner Profile")
            }
        }

        // remove campsite
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = destructiveButtonColors(),
            onClick = rememberConfirmClick(
                message = "Remove this campsite? This can't be undone.",
                onConfirm = {
                    repository.deleteCampsite(campsite)
                    navController.navigate(AdminRoutes.CAMPSITE_LIST)
                }
            )
        ) {
            Text("Remove Campsite")
        }
    }
}

/**
 * Admin gallery of uploaded images. The list is still a placeholder card.
 *
 * @param navController used for navigation
 */
@Composable
fun ImageGallery(navController: NavController) {
    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Image Gallery", style = MaterialTheme.typography.titleLarge) }
        EmptyCard {
            navController.navigate(AdminRoutes.campsite("123"))
        }
    }
}

@Preview
@Composable
fun ImageGalleryPreview() {
    DasCampTheme {
        RequestLog(rememberNavController())
    }/*todo*/
}

/** One row in the admin message log: an owner/entrant pair and their newest message. */
private data class AdminThread(
    val ownerId: String,
    val entrantId: String,
    val title: String,  //heading/title text for the message view cards
    val latestMessage: String  //for the message preview
)
/**
 * Screen listing message conversations between owners and entrants, with a search field.
 * @param navController used to navigate to a message
 */
@Composable
fun AdminMessageList(navController: NavController, repository: DasCampRepository? = null) {
    var query by rememberSaveable { mutableStateOf("") }
    val threads = repository?.messages.orEmpty()
        .groupBy{it.ownerId to it.entrantId}  //group messages by conversation (owner and entrant), only 1 list item for group
        .map{(ids,msgs) ->  //map data into 1 item/conversation in the view message list
            val owner = repository?.userById(ids.first)?.displayName().orEmpty().ifBlank { "N/A" }  //if no name found, name shown will be unnamed
            val entrant = repository?.userById(ids.second)?.displayName().orEmpty().ifBlank { "N/A" }
            AdminThread(ids.first, ids.second, "Owner: $owner\nEntrant: $entrant", msgs.last().body)  //build thread preview info
        }
        .filter { thread ->
        query.isBlank() ||  //search filter for title or message preview
                thread.title.contains(query, ignoreCase = true) ||
                thread.latestMessage.contains(query, ignoreCase = true)
    }
    AdminPage(navController, showBack = true) {
        Text("Message Logs", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Messages") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (threads.isEmpty()) Text("No conversations yet")
        threads.forEach { thread ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        repository?.openMessageOwnerId = thread.ownerId
                        repository?.openMessageEntrantId = thread.entrantId
                        repository?.openMessageLotteryId = null
                        repository?.openReservationId = ""
                        navController.navigate(AdminRoutes.message(thread.entrantId))
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp).weight(1f)
                    ) {
                        Text(thread.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            thread.latestMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open conversation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AdminMessageListPreview() {
    DasCampTheme {
        RequestLog(rememberNavController())
    }
}

/**
 * Screen showing a single conversation between an entrant and a campsite owner,
 * with the entrant's message on top and the owner's reply below. The message
 * content is hardcoded placeholder text for now.
 *
 * @param navController used for back navigation
 * @param repository used for message source and profile names
 */
@Composable
fun AdminViewMessage(navController: NavController, repository: DasCampRepository? = null) {
    val view = remember(
        repository,
        repository?.openReservationId,
        repository?.openMessageOwnerId,
        repository?.openMessageEntrantId
    ) {
        repository?.let { MessageView(it) }
    }
    DisposableEffect(view) { onDispose { view?.remove() } }

    val ownerId = repository?.openMessageOwnerId.orEmpty()
    val entrantId = repository?.openMessageEntrantId.orEmpty()
    val ownerName = repository?.userById(ownerId)?.displayName().orEmpty().ifBlank {"Unnamed"}
    val entrantName = repository?.userById(entrantId)?.displayName().orEmpty().ifBlank {"Unnamed"}

    val messageScrollState = rememberScrollState()
    LaunchedEffect(view?.messages?.size) {
        messageScrollState.animateScrollTo(messageScrollState.maxValue)
    }

    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Owner: $ownerName",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Entrant: $entrantName",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if(ownerId.isBlank() || entrantId.isBlank()){
            Text("Open a conversation from a campsite, profile, or message logs.")
        } else if(view == null || view.messages.isEmpty()){
            Text("No messages")
        }else{
            view.messages.forEach { message ->
                val ownerMessage = if(ownerId != entrantId){
                    message.senderId == ownerId  //owner and entrant are different people
                }else{
                    message.fromOwner  //owner and entrant are the same person
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if(ownerMessage){
                        Alignment.Start
                    }else{
                        Alignment.End
                    }
                ) {
                    Text(
                        text = if (ownerMessage) ownerName else entrantName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (ownerMessage) 4.dp else 16.dp,  //owner on the left side
                            bottomEnd = if (ownerMessage) 16.dp else 4.dp  //entrant on the right side
                        ),
                        color = if (ownerMessage) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary,  //entrant gets primary colour
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = message.body,
                            modifier = Modifier.padding(12.dp),
                            color = if (ownerMessage) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AdminViewMessagePreview() {
    DasCampTheme {
        RequestLog(rememberNavController())
    }
}

/**
 * Creates a click handler that shows a confirmation dialog before running an action.
 *
 * Call this where you'd normally pass an `onClick`. The returned lambda opens a
 * dialog with [message]; [onConfirm] runs only if the user taps the confirm button.
 *
 * @param message the question shown in the dialog
 * @param confirmText label of the confirm button; defaults to "Remove"
 * @param dismissText label of the cancel button; defaults to "Cancel"
 * @param onConfirm the action to run once the user confirms
 * @return a click handler that opens the dialog
 */
@Composable
fun rememberConfirmClick(
    message: String,
    confirmText: String = "Remove",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit
): () -> Unit {
    var show by remember { mutableStateOf(false) }

    if (show) {
        Dialog(onDismissRequest = { show = false }) {
            Column(
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(message, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { show = false }) { Text(dismissText) }
                    TextButton(onClick = { show = false; onConfirm() }) {
                        Text(confirmText, color = Color.Red)
                    }
                }
            }
        }
    }

    return { show = true }
}