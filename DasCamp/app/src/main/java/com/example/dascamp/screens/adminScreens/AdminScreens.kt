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
import com.example.dascamp.screens.ProfileAction
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.screens.EmptyValue
import com.example.dascamp.ui.theme.DasCampTheme
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import com.example.dascamp.controllers.AdminController
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Policy
import com.example.dascamp.model.CampStore
import com.example.dascamp.model.Notification
import com.example.dascamp.model.NotificationType
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.User
import com.example.dascamp.screens.entrantScreens.EntrantCalendarGrid

/**
 * The kinds of profile an admin can browse.
 *
 * @property label the plural name shown on filter chips
 */
enum class ProfileType(val label: String) {
    ENTRANT("Entrants"),
    OWNER("Owners")
}

/**
 * Admin home screen, with shortcuts to the campsite list, the profile list,
 * and the notification logs.
 *
 * @param navController used to navigate to the selected section
 */
@Composable
fun AdminDashboard(navController: NavController, onSignOut: () -> Unit = {}) {
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
            Text(text = "Welcome Back,", style = MaterialTheme.typography.titleLarge)
            Text(text = "Admin", style = MaterialTheme.typography.titleLarge)
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

        // Notification Logs
        Text(
            "Signed in on this device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ProfileAction(
            icon = Icons.Default.Campaign,
            label = "Notification logs",
            onClick = { navController.navigate(AdminRoutes.NOTIFICATION_LOG) }
        )
        ProfileAction(
            icon = Icons.Default.HowToReg,
            label = "Owner applications",
            onClick = { navController.navigate(AdminRoutes.OWNER_APPLICATIONS) }
        )
        ProfileAction(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Sign out",
            onClick = onSignOut
        )
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
        val matchedTypeFilter = if(u.admin){
            false  //do not display admin
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
                        val type = if(user.owner){  //determine the user type of the clicked profile
                            ProfileType.OWNER
                        } else{
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

    AdminPage(
        navController = navController,
        showBack = true,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

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

            if (profileType == ProfileType.ENTRANT) {
                // Entrant only -> delete profile
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = destructiveButtonColors(),
                    onClick = rememberConfirmClick(
                        message = "Remove this profile? This can't be undone.",
                        onConfirm = {
                            campStore.deleteUser(user)
                            navController.navigate(AdminRoutes.PROFILE_LIST)
                        }
                    )
                ) { Text("Delete Profile") }
            } else {
                // Owner only -> view messages with entrants (same message list as from a campsite)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(AdminRoutes.MESSAGE_LIST) }
                ) { Text("View Messages with Entrants") }

                // Owner only -> remove campsite owner role
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = destructiveButtonColors(),
                    onClick = rememberConfirmClick(
                        message = "Remove this campsite owner? This can't be undone.",
                        onConfirm = { navController.navigate(AdminRoutes.CAMPSITE_LIST) }
                    ) /*todo: implement delete*/
                ) { Text("Revoke Campsite Owner Role") }
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
    val shown = repository?.campsites?.filter { site ->
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

        if (repository == null) {
            EmptyCard {
                navController.navigate(AdminRoutes.campsite("123"))
            }
        } else if (shown.isEmpty()) {
            Text("No campsites")
        } else {
            shown.forEach { site ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate(AdminRoutes.campsite(site.id)) }
                ) { Text("${site.name} · ${site.location}") }
            }
        }
    }
}
@Preview
@Composable
fun AdminCampsiteListPreview() {
    DasCampTheme {
        RequestLog(rememberNavController())
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
    campsiteId: String? = null
) {
    var pendingRemoval by remember { mutableStateOf<Pair<String, String>?>(null) }
    val admin = remember(repository) { repository?.let { AdminController(it) } }
    val campsite = repository?.campsites?.find { it.id == campsiteId }
    val reviews = repository?.reviews?.filter { it.campsiteId == campsiteId }.orEmpty()
    AdminPage(
        navController = navController,
        showBack = true,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = campsite?.name ?: "Campsite",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Campsite Image (PLACEHOLDER)
        Card(
            modifier = Modifier.height(200.dp)
                    .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { Icon(Icons.Default.Place, "Campsite Image", Modifier.fillMaxSize()) }
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = destructiveButtonColors(),
            onClick = rememberConfirmClick(
                message = "Remove this image? This can't be undone.",
                onConfirm =  {navController.navigate(AdminRoutes.campsite("123")) }
            )/*todo: implement delete*/
        ) { Text("Delete Campsite Image") }

        // Campsite Main info (PLACEHOLDER)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(campsite?.location ?: "Location", style = MaterialTheme.typography.labelLarge)
                Text(" | Max Occupancy | ", style = MaterialTheme.typography.labelLarge)
                Text(
                    campsite?.let { "${it.rating} (${it.ratingCount})" } ?: "Rating",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Campsite details and rules and policy (placeholder)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Here are more details of the Campsite", style = MaterialTheme.typography.bodyLarge)
            Text("Rules and Policies ", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campsite Geolocation placeholder
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Geolocation", style = MaterialTheme.typography.labelLarge)
        }

        Card(
            modifier = Modifier.height(200.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { Icon(Icons.Default.Place, "Geolocation", Modifier.fillMaxSize()) }

        Spacer(modifier = Modifier.height(16.dp))

        // Container for calendar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(text = "Availability", style = MaterialTheme.typography.labelLarge)

            Spacer(modifier = Modifier.height(10.dp))

            // Entrant Calendar Grid

            // Legend
            Row() {
                // Available
                Text("Available = ")
                Box(modifier = Modifier.size(20.dp).background(Color.Red))

                Spacer(modifier = Modifier.padding(8.dp))

                // Unavailable
                Text("Unavailable = ")
                Box(modifier = Modifier.size(20.dp).background(Color.Green))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Ratings and reviews", style = MaterialTheme.typography.titleMedium)
        Text(Policy.TEXT, style = MaterialTheme.typography.bodySmall)
        if (reviews.isEmpty()) {
            Text("No reviews on this campsite")
        }
        reviews.forEach { review ->
            Text("${review.entrantName}", style = MaterialTheme.typography.labelLarge)
            Text(
                if (review.ratingRemoved) "Rating removed for policy" else "Rating ${review.rating}/5"
            )
            if (review.body.isNotBlank()) Text(review.body)
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !review.ratingRemoved,
                colors = destructiveButtonColors(),
                onClick = { pendingRemoval = review.id to "rating" }
            ) { Text("Remove rating") }
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = destructiveButtonColors(),
                onClick = { pendingRemoval = review.id to "review" }
            ) { Text("Remove review") }
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
                            "Remove this rating? It will no longer count toward the campsite average."
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

        // view messages with entrants (same message list as from a campsite)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(AdminRoutes.MESSAGE_LIST) }
            ) { Text("View Messages with Entrants") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(AdminRoutes.profile( ProfileType.OWNER, ""))
                }
            ) { Text("View Owner Profile") }

        // remove campsite
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = destructiveButtonColors(),
                onClick = rememberConfirmClick(
                    message = "Remove this campsite? This can't be undone.",
                    onConfirm =  {navController.navigate(AdminRoutes.CAMPSITE_LIST) }
                )
            ) { Text("Remove Campsite") }

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
    }
}

/**
 * Screen listing message conversations between owners and entrants, with a
 * search field. The list itself is still a placeholder card that opens a message.
 *
 * @param navController used to navigate to a message
 */
@Composable
fun AdminMessageList(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }  // profile search
    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Message Log", style = MaterialTheme.typography.titleLarge) }

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Profiles") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )
        // Display Profiles (PLACEHOLDER)
        EmptyCard {
            navController.navigate(AdminRoutes.message("123"))
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
 */
@Composable
fun AdminViewMessage(navController: NavController) {
    AdminPage(
        navController = navController,
        showBack = true,
    ) { Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Message", style = MaterialTheme.typography.titleLarge) }

        // Entrant message
        Text("Entrant", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.End))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF5285F2)
        ) {
            Text("Hi I wanna book this",
                color = Color.White,
                modifier = Modifier.padding(14.dp))
        }

        // Owner message
        Text("Campsite Owner", style = MaterialTheme.typography.titleLarge)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE6E6E6)
        ) {
            Text("Ok, sure", modifier = Modifier.padding(14.dp))
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