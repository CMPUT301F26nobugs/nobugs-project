package com.example.dascamp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ENTRANT ROUTES
object EntrantRoutes {
    const val DASHBOARD = "entrantDashboard"
    const val CAMPSITE_LIST = "entrantCampsiteList"
    const val CAMPSITE = "entrantCampsiteScreen"
    const val NOTIFICATION_LIST = "entrantNotificationList"
    const val NOTIFICATION = "entrantNotificationScreen"
    const val MESSAGE_LIST = "entrantMessageList"
    const val MESSAGE = "entrantMessageScreen"
    const val PROFILE = "entrantProfileScreen"
    const val QR = "entrantQRCodeScreen"
    const val RESERVATION_LIST = "entrantReservations"
    const val RESERVATION = "entrantReservation"
}

// Entrant skeleton layout for the content
@Composable
fun EntrantPage(
    navController: NavController,
    showBack: Boolean,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    AppScaffold(
        onBackClick = if (showBack) {{ navController.popBackStack() }} else null,
        navBar = { EntrantNavBar(navController) }
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

// ENTRANT NAVBAR
@Composable
fun EntrantNavBar(navController: NavController) {
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
                onClick = { navController.navigate(EntrantRoutes.DASHBOARD) }
            ) { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onPrimary) }

            // Campsites Button
            IconButton(
                onClick = { navController.navigate(EntrantRoutes.CAMPSITE_LIST) }
            ) { Icon(imageVector = Icons.Default.Place, contentDescription = "Campsites", tint = MaterialTheme.colorScheme.onPrimary) }

            // Notifications Button
            IconButton(
                onClick = { navController.navigate(EntrantRoutes.NOTIFICATION_LIST) }
            ) { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onPrimary) }

            // Messages Button
            IconButton(
                onClick = { navController.navigate(EntrantRoutes.MESSAGE_LIST) }
            ) { Icon(imageVector = Icons.Default.Email, contentDescription = "Messages", tint = MaterialTheme.colorScheme.onPrimary) }

            // Profile Button
            IconButton(
                onClick = { navController.navigate(EntrantRoutes.PROFILE) }
            ) { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}

// FILTER SECTION (toggle row + collapsible card)
@Composable
fun FilterSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    date: String,
    onDateChange: (String) -> Unit,
    occupancy: String,
    onOccupancyChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Clickable filter icon & text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = null // the text beside it already says what this is
            )
            Text(
                text = "Filter By:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Filters card, only shown while expanded
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title
                    Text(text = "Filters", style = MaterialTheme.typography.labelLarge)

                    // Fields
                    FilterField("Location:", location, onLocationChange)
                    FilterField("Date:", date, onDateChange)
                    FilterField("Max Occ:", occupancy, onOccupancyChange)
                }
            }
        }
    }
}

// FILTER FIELD
@Composable
fun FilterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(88.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Any") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        )
    }
}

// ENTRANT CALENDAR GRID
@Composable
fun EntrantCalendarGrid() {
    Row(Modifier.fillMaxWidth()) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
            Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
    repeat(5) {
        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(7) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                }
            }
        }
    }
}
