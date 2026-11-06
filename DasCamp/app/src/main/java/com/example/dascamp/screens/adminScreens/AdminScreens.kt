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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.dascamp.ui.theme.DasCampTheme
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import com.example.dascamp.screens.entrantScreens.EntrantCalendarGrid


// 2 types of profiles that can be browsed
enum class ProfileType(val label: String) {
    ENTRANT("Entrants"),
    OWNER("Owners")
}

// ADMIN DASHBOARD SCREEN
@Composable
fun AdminDashboard(navController: NavController) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Notification log button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(AdminRoutes.NOTIFICATION_LOG) }
            ) {
                Text("Notification Logs")
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

// PROFILE LIST SCREEN
@Composable
fun AdminProfileList(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }                        // profile search
    var selectedType by rememberSaveable { mutableStateOf<ProfileType?>(null) } // null = show all

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

        // Display Profiles (PLACEHOLDER)
        EmptyCard {
            navController.navigate(AdminRoutes.profile(selectedType ?: ProfileType.ENTRANT))
        }
    }
}

@Preview
@Composable
fun AdminProfileListPreview() {
    DasCampTheme {
        AdminProfileList(rememberNavController())
    }
}

// VIEW PROFILE SCREEN -> entrant or owner
@Composable
fun AdminViewProfile(navController: NavController, profileType: ProfileType) {
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
                text = if (profileType == ProfileType.ENTRANT) "Entrant Profile" else "Owner Profile",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Profile picture (PLACEHOLDER)
        Card(
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) { Icon(Icons.Default.Person, "Profile picture", Modifier.fillMaxSize()) }

        // Profile info (PLACEHOLDER)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Name", style = MaterialTheme.typography.labelLarge)
            EmptyValue(0.6f)
            Text("Email", style = MaterialTheme.typography.labelLarge)
            EmptyValue(0.8f)
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
                    onConfirm =  {navController.navigate(AdminRoutes.PROFILE_LIST) }
                ) /*todo: implement delete*/
            ) { Text("Delete Profile") }
        } else {
            // Owner only -> view messages with entrants (same message list as from a campsite)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(AdminRoutes.MESSAGE_LIST) }
            ) { Text("View Messages with Entrants") }

            // Owner only -> remove campsite owner
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = destructiveButtonColors(),
                onClick = rememberConfirmClick(
                    message = "Remove this campsite owner? This can't be undone.",
                    onConfirm =  {navController.navigate(AdminRoutes.CAMPSITE_LIST) }
                ) /*todo: implement delete*/
            ) { Text("Remove Campsite Owner") }
        }
    }
}

@Composable
private fun destructiveButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.error,
    contentColor = MaterialTheme.colorScheme.onError
)

@Preview
@Composable
fun AdminViewEntrantProfilePreview() {
    DasCampTheme {
        AdminViewProfile(rememberNavController(), ProfileType.ENTRANT)
    }
}

@Preview
@Composable
fun AdminViewOwnerProfilePreview() {
    DasCampTheme {
        AdminViewProfile(rememberNavController(), ProfileType.OWNER)
    }
}

// NOTIFICATION LOG SCREEN -> lead to 3 different screens/log types
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

// REQUEST LOG SCREEN
@Composable
fun RequestLog(navController: NavController) {
    LogScreen(navController, "Campsite Requests")
}

// APPROVED LOG SCREEN
@Composable
fun ApprovedLog(navController: NavController) {
    LogScreen(navController, "Approved Requests")
}

// OTHER LOG SCREEN
@Composable
fun OtherLog(navController: NavController) {
    LogScreen(navController, "Other")
}

// Shared skeleton code for the three log screens
@Composable
private fun LogScreen(navController: NavController, title: String) {
    AdminPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = title, style = MaterialTheme.typography.titleLarge) }

        // Display log entries (to be implemented)
        EmptyCard { /*TODO("Open log entry")*/ }
    }
}

@Preview
@Composable
fun RequestLogPreview() {
    DasCampTheme {
        RequestLog(rememberNavController())
    }
}

@Composable
fun AdminCampsiteList(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") } // campsite search
    var selectedType by rememberSaveable { mutableStateOf<ProfileType?>(null) } // null = show all
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

        EmptyCard {
            navController.navigate(AdminRoutes.campsite("123"))
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

@Composable
fun AdminViewCampsite(navController: NavController) {
    var showConfirmation by remember { mutableStateOf(false) }
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
                text = "Campsite Name",
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
                Text("Location", style = MaterialTheme.typography.labelLarge)
                Text(" | Max Occupancy | ", style = MaterialTheme.typography.labelLarge)
                Text("Rating", style = MaterialTheme.typography.labelLarge)
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

        // view messages with entrants (same message list as from a campsite)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(AdminRoutes.MESSAGE_LIST) }
            ) { Text("View Messages with Entrants") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(AdminRoutes.profile( ProfileType.OWNER))
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

@Preview
@Composable
fun AdminViewCampsitePreview() {
    DasCampTheme {
        AdminViewProfile(rememberNavController(), ProfileType.ENTRANT)
    }
}

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

@Composable
fun AdminViewMessage(navController: NavController) {
    AdminPage(
        navController = navController,
        showBack = true,
    ) { Column(
        modifier = Modifier
            .fillMaxSize()
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
