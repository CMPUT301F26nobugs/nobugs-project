package com.example.dascamp.screens.ownerScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.screens.EmptyValue


/**
 * Owner home. The two large buttons open the campsite list and the lottery list.
 * The three wide buttons open alerts, message logs, and reservations.
 */
@Composable
fun OwnerDashboard(navController: NavController) {
    OwnerPage(navController, showBack = false, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Welcome Back,",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text("Owner", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BigButton(
                { navController.openOwnerTab(OwnerRoutes.CAMPSITES) },
                Icons.Default.Place,
                "My campsites",
                "My\nCampsites"
            )
            BigButton(
                { navController.navigate(OwnerRoutes.LOTTERIES) },
                Icons.Default.DateRange,
                "My lotteries",
                "My\nLotteries"
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.openOwnerTab(OwnerRoutes.ALERTS) }
        ) { Text("Notification Logs") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.MESSAGE_LIST) })
        { Text("Message Logs") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.openOwnerTab(OwnerRoutes.RESERVATIONS) })
        { Text("Reservations") }
    }
}

/** Campsite list. Search and Add are the controls. The blank card opens the details layout. */
@Composable
fun OwnerCampsitesList(navController: NavController) {
    var query by remember { mutableStateOf("") }
    OwnerPage(navController, showBack = false) {
        Text("My Campsites", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search campsites") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )
        TextButton(onClick = { navController.navigate(OwnerRoutes.ADD) }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add Campsite")
        }
        EmptyCard { navController.navigate(OwnerRoutes.CAMPSITE) }
    }
}

/**
 * Campsite details. Each button opens the next storyboard screen:
 * edit, booked dates, map, QR, or the create-lottery form.
 */
@Composable
fun OwnerCampsite(navController: NavController) {
    OwnerPage(navController, showBack = true) {
        BlockBox(140)
        EmptyValue(0.8f)
        EmptyValue(0.5f)
        EmptyValue(0.3f)
        Text("Description", style = MaterialTheme.typography.titleMedium)
        EmptyValue()
        Text("Rules", style = MaterialTheme.typography.titleMedium)
        EmptyValue()
        Text("Cancellation", style = MaterialTheme.typography.titleMedium)
        EmptyValue()
        Text("Availability", style = MaterialTheme.typography.titleMedium)
        EmptyValue()
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.EDIT) })
        {
            Text("Edit campsite")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.BOOKED) }) { Text("Booked dates") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.MAP) }) { Text("Map location") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.QR) }) { Text("QR code") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.CREATE) }) { Text("Create lottery") }
    }
}

/** Empty publish form. Publish opens the blank details page. The fields are not stored. */
@Composable
fun OwnerAddCampsite(navController: NavController) {
    CampsiteForm(navController, "Add campsite", "Publish") {
        navController.popBackStack()
        navController.navigate(OwnerRoutes.CAMPSITE)
    }
}

/** Same form as Add, still empty. Save returns to the previous page. */
@Composable
fun OwnerEditCampsite(navController: NavController) {
    CampsiteForm(navController, "Edit campsite", "Save") { navController.popBackStack() }
}

@Composable
private fun CampsiteForm(navController: NavController, title: String, action: String, onAction: () -> Unit) {
    OwnerPage(navController, showBack = true) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        FormField("Name")
        FormField("Location")
        FormField("Amenities")
        FormField("Maximum occupancy")
        FormField("Price per night")
        FormField("Rules")
        FormField("Cancellation policy")
        FormField("Tags")
        Button(onClick = {}) { Text("Upload photo") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.MAP) }) { Text("Set map location") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAction
        ) { Text(action) }
    }
}

/** Calendar layout. No days are marked, because no stays are loaded yet. */
@Composable
fun OwnerBookedDates(navController: NavController) {
    OwnerPage(navController, showBack = true) {
        Text("Booked dates", style = MaterialTheme.typography.titleLarge)
        EmptyValue(0.6f)
        CalendarGrid()
        Text("Stays", style = MaterialTheme.typography.titleMedium)
        EmptyCard { navController.navigate(OwnerRoutes.RESERVATION) }
    }
}

/** Map layout. The box is where the pin will be drawn from the campsite coordinates. */
@Composable
fun OwnerMapLocation(navController: NavController) {
    var query by remember { mutableStateOf("") }
    OwnerPage(navController, showBack = true) {
        Text("Map location", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Location") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BlockBox(220)
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Map pin",
                modifier = Modifier.size(48.dp)
            )
        }


        EmptyValue(0.5f)
        EmptyValue(0.5f)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.popBackStack() }) {
            Text("Confirm Location")
        }
    }
}

/**
 * QR layout. The square is the space for the code.
 * Jess replaces this box with the generated code. The route stays ownerQrCode.
 */
@Composable
fun OwnerQrCode(navController: NavController) {
    OwnerPage(navController, showBack = true, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("QR code", style = MaterialTheme.typography.titleLarge)
        Box(Modifier.size(220.dp)) { BlockBox(220) }
        Text("ID: CAMP-HTN")
        Text("Entrants can scan this code to view details and join the lottery")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {}) {
            Text("Download QR Code")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {}) {
            Text("Share link")
        }
    }
    }


/** Lottery list, opened from My Lotteries. The blank card opens one lottery's layout. */
@Composable
fun OwnerLotteryList(navController: NavController) {
    var query by remember { mutableStateOf("") }
    OwnerPage(navController, showBack = true) {
        Text("My Lotteries", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Lotteries") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )
        TextButton(onClick = { navController.navigate(OwnerRoutes.CREATE) }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Create Lottery")
        }
        EmptyCard { navController.navigate(OwnerRoutes.LOTTERY) }
    }
}

/** Empty lottery form. Publish opens the blank lottery details page. */
@Composable
fun OwnerCreateLottery(navController: NavController) {
    LotteryForm(navController, "Create lottery", "Publish lottery") {
        navController.popBackStack()
        navController.navigate(OwnerRoutes.LOTTERIES)
    }
}

/** Same form as Create. Save returns to lottery details. */
@Composable
fun OwnerEditLottery(navController: NavController) {
    LotteryForm(navController, "Edit lottery", "Save") { navController.popBackStack() }
}

/**
 * Lottery details layout: summary bars, the campsite link, the waiting list,
 * and the buttons for edit, draw, and notify.
 */
@Composable
fun OwnerLotteryDetails(navController: NavController) {
    OwnerPage(navController, showBack = true) {
        EmptyValue(0.8f)
        EmptyValue(0.3f)
        EmptyValue()
        EmptyValue()
        Text("Campsites", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { navController.navigate(OwnerRoutes.CAMPSITE) }) { Text("Campsite") }
        Text(
            "Winners are drawn automatically when registration ends.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.EDIT_LOTTERY) }) { Text("Edit lottery") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.SEND) }) { Text("Notify the waiting list") }
        Text("Waiting list", style = MaterialTheme.typography.titleMedium)
        EmptyCard {}
    }
}

@Composable
private fun LotteryForm(navController: NavController, title: String, action: String, onAction: () -> Unit) {
    OwnerPage(navController, showBack = true) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        FormField("Lottery name")
        Text("Campsites", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = false, onCheckedChange = {})
            EmptyValue(0.6f)
        }
        FormField("Registration start")
        FormField("Registration end")
        FormField("Booking start")
        FormField("Booking end")
        FormField("Number of winners")
        FormField("Waitlist capacity, optional")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAction
        ) { Text(action) }
    }
}

/**
 * Reservations tab. Active, Past, and Cancelled switch the selected tab.
 * The blank card opens the reservation details layout.
 */
@Composable
fun OwnerReservations(navController: NavController) {
    var selected by remember { mutableStateOf("Active") }
    OwnerPage(navController, showBack = false) {
        Text("Reservations", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Active", "Past", "Cancelled").forEach { label ->
                if (label == selected) Button(onClick = { selected = label }) { Text(label) }
                else OutlinedButton(onClick = { selected = label }) { Text(label) }
            }
        }
        EmptyCard { navController.navigate(OwnerRoutes.RESERVATION) }
    }
}

/** One reservation. Reject is present for the layout. Message opens the thread. */
@Composable
fun OwnerReservationDetails(navController: NavController) {
    OwnerPage(navController, showBack = true) {
        EmptyValue(0.8f)
        EmptyValue(0.3f)
        EmptyValue(0.5f)
        EmptyValue(0.6f)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = true, onCheckedChange = null)
            Text("Rules & Policies Confirmed")
        }
        FormField("Reason for rejection")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.popBackStack() }) { Text("Reject reservation") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.MESSAGE) }) { Text("Message") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.BOOKED) }) { Text("View Dates") }
    }
}

/** Alerts tab. The blank card is one sent notification. */
@Composable
fun OwnerNotifications(navController: NavController) {
    OwnerPage(navController, showBack = false) {
        Text("Sent notifications", style = MaterialTheme.typography.titleLarge)
        EmptyCard {}
    }
}

/** Send page opened from a lottery. The message field is empty. */
@Composable
fun OwnerSendNotification(navController: NavController) {
    var send by remember {mutableStateOf("All Waitlist")}
    OwnerPage(navController, showBack = true) {
        Text("Send notification", style = MaterialTheme.typography.titleLarge)
        Text("Send to", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All Waitlist", "Winners", "Not Selected").forEach { label ->
                if (label == send)
                    Button(onClick = { send = label }) {
                        Text(label)
                    } else OutlinedButton(
                    onClick = {
                        send = label
                    }
                ) {
                    Text(label)
                }
            }
        }
        FormField("Message")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.openOwnerTab(OwnerRoutes.ALERTS) }) { Text("Send") }
    }
}

/** Message logs, opened from the home screen. The blank card opens a thread. */
@Composable
fun OwnerMessageList(navController: NavController) {
    OwnerPage(navController, showBack = true) {
        Text("Message logs", style = MaterialTheme.typography.titleLarge)
        EmptyCard { navController.navigate(OwnerRoutes.MESSAGE) }
    }
}

/** One message thread. The list area and the write box are empty. */
@Composable
fun OwnerMessages(navController: NavController) {
    OwnerPage(navController, showBack = true) {
        Text("Message", style = MaterialTheme.typography.titleLarge)
        EmptyValue(0.5f)
        BlockBox(80)
        FormField("Write a message")
        Button(onClick = {}) { Text("Send") }
    }
}

/**
 * Profile tab. Edit only unlocks the fields on this screen.
 * Nothing here is written to OwnerProfile yet.
 */
@Composable
fun OwnerProfile(navController: NavController) {
    var editing by remember { mutableStateOf(false) }
    var notificationsOn by remember { mutableStateOf(true) }
    OwnerPage(navController, showBack = false) {
        Text("Profile", style = MaterialTheme.typography.titleLarge)
        FormField("Contact name", editing)
        FormField("Email", editing)
        FormField("Phone", editing)
        FormField("Organization", editing)
        Button(onClick = { editing = !editing }) { Text(if (editing) "Done" else "Edit") }
        Button(onClick = {}) { Text("Change profile picture") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Turn on notifications", modifier = Modifier.weight(1f))
            Switch(checked = notificationsOn, onCheckedChange = { notificationsOn = it })
        }
        Button(onClick = {}) { Text("App policy and guidelines") }
        Button(onClick = {}) { Text("Delete profile") }
    }
}

@Composable
private fun FormField(label: String, enabled: Boolean = true) {
    var value by remember { mutableStateOf("") }
    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        enabled = enabled,
        singleLine = true
    )
}

@Composable
private fun CalendarGrid() {
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
