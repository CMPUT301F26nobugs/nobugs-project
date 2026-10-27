package com.example.dascamp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.ui.theme.DasCampTheme

// ENTRANT SCREEN SKELETON
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

// DASHBOARD SCREEN
@Composable
fun EntrantDashboard(navController: NavController) {
    EntrantPage(
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
            Text(
                text = "Welcome Back,",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "User",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }

        // Campsite button & reservations button container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Campsite list button
            BigButton(
                onClickAction = { navController.navigate("campsiteList") },
                icon = Icons.Default.LocationOn,
                iconDesc = "campsites",
                label = "View \n Campsites"
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Reservation list button
            BigButton(
                onClickAction = { navController.navigate("reservationList") },
                icon = Icons.Default.DateRange,
                iconDesc = "reservations",
                label = "View Reservations"
            )
        }

        // Notifications, messages, and scan QR code buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Notification logs button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("notificationList") }
            ) {
                Text("Notifications")
            }

            Spacer(modifier = Modifier.padding(4.dp))

            // Message logs button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("messageList") }
            ) {
                Text("Messages")
            }

            Spacer(modifier = Modifier.padding(4.dp))

            // Scan QR code button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("qrCodeScreen") }
            ) {
                Text("Scan QR")
            }
        }
    }
}

@Preview
@Composable
fun EntrantDashboardPreview() {
    DasCampTheme {
        EntrantDashboard(rememberNavController())
    }
}

// CAMPSITE LIST SCREEN
@Composable
fun CampsiteList(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }                // campsite search
    var isToggled by rememberSaveable { mutableStateOf(false) }          // bool for showing filters
    var locationFilter by rememberSaveable { mutableStateOf("") }       // location filter
    var dateFilter by rememberSaveable { mutableStateOf("") }           // date filter
    var occupancyFilter by rememberSaveable { mutableStateOf("") }      // occupancy filter

    EntrantPage(
        navController = navController,
        showBack = false,
    ) {

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Campsites",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Campsites") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            singleLine = true
        )

        // Filter toggle row + filter card
        FilterSection(
            expanded = isToggled,
            onToggle = { isToggled = !isToggled },
            location = locationFilter,
            onLocationChange = { locationFilter = it },
            date = dateFilter,
            onDateChange = { dateFilter = it },
            occupancy = occupancyFilter,
            onOccupancyChange = { occupancyFilter = it }
        )

        // Display Campsites
        EmptyCard { navController.navigate("campsite") }
    }
}

@Preview
@Composable
fun CampsiteListPreview() {
    DasCampTheme {
        CampsiteList(rememberNavController())
    }
}

// FILTER SECTION (toggle row + collapsible card)
@Composable
private fun FilterSection(
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
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FilterField("Location:", location, onLocationChange)
                    FilterField("Date:", date, onDateChange)
                    FilterField("Max Occ:", occupancy, onOccupancyChange)
                }
            }
        }
    }
}

// One labelled filter input (label on the left, rounded text field on the right)
@Composable
private fun FilterField(
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

// NOTIFICATION LIST SCREEN
@Composable
fun NotificationList(navController: NavController) {

}

// RESERVATION LIST SCREEN
@Composable
fun ReservationList(navController: NavController) {

}

// MESSAGE LIST SCREEN
@Composable
fun MessageList(navController: NavController) {

}

// PROFILE SCREEN
@Composable
fun ProfileScreen(navController: NavController) {

}

// QR CODE SCREEN
@Composable
fun QrCodeScreen(navController: NavController) {

}