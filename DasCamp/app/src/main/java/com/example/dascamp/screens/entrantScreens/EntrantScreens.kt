package com.example.dascamp.screens.entrantScreens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.ui.theme.DasCampTheme
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.example.dascamp.controllers.ProfileController
import com.example.dascamp.controllers.NotificationsController
import com.example.dascamp.controllers.LotteryController
import com.example.dascamp.model.DasCampRepository
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clipToBounds
import com.example.dascamp.controllers.CampsiteController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.formatPrice
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.CampsiteMap
import com.example.dascamp.views.ProfileView
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.WaitlistStatus
import com.example.dascamp.views.ReservationView
import com.example.dascamp.views.NotificationView

/**
 * Entrant home screen, with shortcuts to the campsite list, reservations,
 * notifications, messages, and the QR scanner.
 *
 * The greeting is hardcoded to "User" for now.
 *
 * @param navController used to navigate to the selected section
 */
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
            Text(text = "Welcome Back,", style = MaterialTheme.typography.titleLarge)
            Text(text = "User", style = MaterialTheme.typography.titleLarge)
        }

        // Campsite button & reservations button container
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Campsite list button
            BigButton(
                onClickAction = { navController.navigate(EntrantRoutes.CAMPSITE_LIST) },
                icon = Icons.Default.LocationOn,
                iconDesc = "campsites",
                label = "View\nCampsites"
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Reservation list button
            BigButton(
                onClickAction = { navController.navigate(EntrantRoutes.RESERVATION_LIST) },
                icon = Icons.Default.DateRange,
                iconDesc = "reservations",
                label = "View\nReservations"
            )
        }

        // Notifications, messages, and scan QR code buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Notification list button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(EntrantRoutes.NOTIFICATION_LIST) }
            ) { Text("Notifications") }

            // Message list button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(EntrantRoutes.MESSAGE_LIST) }
            ) { Text("Messages") }

            // Scan QR code button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(EntrantRoutes.QR) }
            ) { Text("Scan QR") }
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

/**
 * Screen listing campsites, with a search field and a collapsible filter section
 * (location, date, and max occupancy).
 *
 * Results come from [CampsiteController.searchCampsites]. The date filter is only
 * applied once it is a full `yyyy-MM-dd` date, and is used as both the start and
 * end date of the search. Tapping a result stores its id in
 * [DasCampRepository.openCampsiteId] and navigates to [EntrantRoutes.CAMPSITE].
 *
 * @param navController used to navigate to a campsite
 * @param repository where the opened campsite id is stored; if null, tapping a result
 * still navigates but no campsite is selected
 * @param campsiteController performs the search; if null, the list is always empty
 */
@Composable
fun EntrantCampsiteList(
    navController: NavController,
    repository: DasCampRepository? = null,
    campsiteController: CampsiteController? = null
) {
    var query by rememberSaveable { mutableStateOf("") }                // campsite search
    var isToggled by rememberSaveable { mutableStateOf(false) }         // bool for showing filters
    var locationFilter by rememberSaveable { mutableStateOf("") }       // location filter
    var dateFilter by rememberSaveable { mutableStateOf("") }           // date filter
    var occupancyFilter by rememberSaveable { mutableStateOf("") }      // occupancy filter

    // Only apply the date filter once it is a full yyyy-MM-dd date
    val date = dateFilter.trim()
        .takeIf { Regex("\\d{4}-\\d{2}-\\d{2}").matches(it) }
        .orEmpty()

    // Searching results
    val results = campsiteController?.searchCampsites(
        query = query,
        location = locationFilter,
        guests = occupancyFilter.toIntOrNull(),
        startDate = date,
        endDate = date,
    ) ?: repository?.campsites.orEmpty()

    EntrantPage(
        navController = navController,
        showBack = false
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
        if (repository != null && !repository.campsitesLoaded) {
            Text("Loading campsites…")
        } else if (results.isEmpty()) {
            Text("No campsites match")
        }
        results.forEach { campsite ->
            TextButton(onClick = {
                repository?.openCampsiteId = campsite.id
                navController.navigate(EntrantRoutes.CAMPSITE)
            }) {
                Text("${campsite.name} · ${campsite.location} · up to ${campsite.maxOccupancy} · ★ ${"%.1f".format(campsite.rating)}")
            }
        }
    }
}

@Preview
@Composable
fun EntrantCampsiteListPreview() {
    DasCampTheme {
        EntrantCampsiteList(rememberNavController())
    }
}

/**
 * Detail screen for the campsite whose id is stored in
 * [DasCampRepository.openCampsiteId].
 *
 * Shows the campsite's summary, price, tags, description, rules, cancellation
 * policy, map, and availability (open ranges and booked reservations), then a
 * booking form (start date, end date, guests, rules checkbox). "Book Reservation"
 * calls [ScheduleController.reserveDates] and shows the returned error or a success
 * message. If the campsite can't be found, a "Campsite not found" message is
 * shown instead.
 *
 * The current user is read from the repository, falling back to a hardcoded test
 * user when there isn't one. The "Enter Lottery" button, pool size, and end date
 * are still placeholders.
 *
 * @param navController used for back navigation and to message the owner
 * @param repository source of the campsite, its lottery, reservations, and current user
 * @param scheduleController checks booked dates and creates reservations
 */
@Composable
fun EntrantCampsiteScreen(navController: NavController, repository: DasCampRepository? = null, scheduleController: ScheduleController? = null, reservationView: ReservationView? = null) {
    val lotteryController = remember(repository) { repository?.let { LotteryController(it) } }
    val reservationController = remember(repository) { repository?.let { ReservationController(it) } }

    // campsite entrant tapped
    var campsite: Campsite? = null
    if(repository != null){
        for (site in repository.campsites){
            if(site.id == repository.openCampsiteId){
                campsite = site
            }
        }
    }

    // if no cant be found show message
    if (campsite == null){
        EntrantPage(navController = navController, showBack = true) {
            Text("Campsite not found")
        }
        return
    }

    val winningLottery = reservationController?.wonLottery(campsite.id)
    val waitingLottery = repository?.let { repo ->
        repo.lotteries.firstOrNull { lottery ->
            campsite.id in lottery.campsiteIds && lottery.status == LotteryStatus.OPEN &&
                    lottery.entrants.any { it.entrantId == repo.uid && it.status == WaitlistStatus.WAITING }
        }
    }

    //lottery for campsite
    var lottery: Lottery? = null
    if(repository != null){
        for(item in repository.lotteries){
            if(item.campsiteIds.contains(campsite.id) && lotteryController?.registrationOpen(item) == true){
                lottery = item
            }
        }
    }

    // who is reserving and has todo not for halfway tho
    var entrantId = "test-entrant"
    var entrantName = "Test User"
    if(repository != null) {
        val user = repository.currentUser
        if(user != null){
            entrantId = user.id
            entrantName = user.firstName + " " + user.lastName
        }
    }

    //what entrant picks
    var startDate by remember {mutableStateOf("")}
    var endDate by remember {mutableStateOf("")}
    var guests by remember {mutableStateOf("1")}
    var rulesAccepted by remember {mutableStateOf(false)}
    var bookingMessage by remember {mutableStateOf("")}

    EntrantPage(
        navController = navController,
        showBack = true,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = campsite.name, style = MaterialTheme.typography.titleLarge) }

        // Image (PLACEHOLDER will generate real images later when models and DB)
        Card(
            modifier = Modifier.size(width = 400.dp, height = 200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // Background
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant   // Text/Icon color
            )
        ) { Icon(Icons.Default.Image, "na", Modifier.fillMaxSize()) }


        // Container for description + Rules & Policies
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Summary
            val ratingText = campsite.rating.toString() + " (" + campsite.ratingCount.toString() + " ratings)"
            val guestsText = "Up to " + campsite.maxOccupancy.toString() + " guests"
            Text(
                text = campsite.location + " | " + ratingText + " | " + guestsText,
                style = MaterialTheme.typography.labelLarge)

            // Price
            Text(
                text = "$" + formatPrice(campsite.pricePerNight) + " per night",
                style = MaterialTheme.typography.bodyLarge)


            //Tags
            Text(text = campsite.tags.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.padding(8.dp))

            // Description
            Text(text = "Description", style = MaterialTheme.typography.titleMedium)
            Text(text = campsite.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.padding(8.dp))

            // Rules & Policies
            Text(text = "Rules & Policies", style = MaterialTheme.typography.titleMedium)
            Text(text = campsite.rules, style = MaterialTheme.typography.bodyLarge )
            Spacer(modifier = Modifier.padding(8.dp))

            // Cancellation Policy
            Text(text = "Cancellation Policy", style = MaterialTheme.typography.titleMedium)
            Text(text = campsite.cancellationPolicy, style = MaterialTheme.typography.bodyLarge )
        }

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        // Container for geolocation
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Geolocation title
            Text(text = "Geolocation", style = MaterialTheme.typography.labelLarge,)

            //geolocation box
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).clipToBounds()){
                CampsiteMap(
                    latitude = campsite.latitude,
                    longitude = campsite.longitude,
                    modifier = Modifier.fillMaxWidth().height(300.dp)  //onLocationPicked = null -> view only, pin cannot be moved
                )
            }
            Text("Location: " + campsite.latitude + ", " + campsite.longitude)
        }

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        // Container for calendar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(text = "Availability", style = MaterialTheme.typography.labelLarge)

            // Dates owner is free
            for (range in campsite.availability){
                Text("Open: " + range.start + " to " + range.end)
            }

            //date is booked
            var booked = listOf<Reservation>()
            if (scheduleController != null && reservationView != null){
                booked = scheduleController.bookedReservations(campsite.id,  reservationView.reservations)
            }
            if(booked.isEmpty()){
                Text("No dates booked")
            }else{
                for (reservation in booked){
                    Text("Booked: " + reservation.startDate + " to " + reservation.endDate)
                }
            }

            // Entrant Calendar Grid
            EntrantCalendarGrid()

            // Legend
            Row() {
                // Available
                Text("Available = ")
                Box(modifier = Modifier.size(20.dp).background(Color.Green))

                Spacer(modifier = Modifier.padding(8.dp))

                // Unavailable
                Text("Unavailable = ")
                Box(modifier = Modifier.size(20.dp).background(Color.Red))
            }
            Spacer(modifier = Modifier.padding(8.dp))

            if (winningLottery != null) {
                Text("You were selected. Choose dates to book", style = MaterialTheme.typography.labelLarge)
                Text("Booking window: ${winningLottery.bookingStart} to ${winningLottery.bookingEnd}")
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { typed -> startDate = typed },
                    label = { Text("Start date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { typed -> endDate = typed },
                    label = { Text("End date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = guests,
                    onValueChange = { typed -> guests = typed },
                    label = { Text("Number of guests") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        // Waitlist interest and winner booking are separate steps.
        var lotteryMessage by remember(lottery?.id, winningLottery?.id) { mutableStateOf("") }
        if (winningLottery != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = rulesAccepted, onCheckedChange = { rulesAccepted = it })
                Text("I have read the rules and policies")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val guestCount = guests.toIntOrNull() ?: 0
                    val error = if (scheduleController == null) {
                        "Reservation service is unavailable"
                    } else {
                        scheduleController.reserveDates(
                            campsite = campsite,
                            lottery = winningLottery,
                            entrantId = entrantId,
                            entrantName = entrantName,
                            startDate = startDate,
                            endDate = endDate,
                            guestCount = guestCount,
                            rulesAccepted = rulesAccepted
                        )
                    }
                    bookingMessage = if (error == null) {
                        "Reserved $startDate to $endDate"
                    } else error
                }
            ) { Text("Book reservation") }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    reservationController?.decline(winningLottery)
                    bookingMessage = "Lottery spot declined"
                }
            ) { Text("Decline lottery spot") }
        } else if (lottery != null) {
            val activeLottery = lottery
            val currentEntry = activeLottery.entrants.firstOrNull { it.entrantId == repository?.uid }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    lotteryMessage = if (currentEntry == null) {
                        lotteryController?.joinWaitlist(activeLottery, entrantName) ?: "Joined ${activeLottery.name}"
                    } else {
                        lotteryController?.leaveWaitlist(activeLottery) ?: "Left ${activeLottery.name}"
                    }
                }) {
                    Text(if (currentEntry == null) "Join waiting list" else "Leave waiting list")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Text("${activeLottery.entrants.size}/${activeLottery.waitlistCapacity ?: "∞"} · ends ${activeLottery.registrationEnd}")
            }
            Text("Joining adds you to the waiting list. You can book only if selected after the draw.")
        } else if (waitingLottery != null) {
            Text("Registration has closed. You are on the waitlist and winners will be drawn after the period ends.")
        } else {
            Text("There is no lottery currently accepting entrants for this campsite.")
        }
        Text(lotteryMessage.ifBlank { bookingMessage })

        // Chat owner button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(EntrantRoutes.MESSAGE) }
        ) { Text("Message Owner") }
    }
}

@Preview
@Composable
fun CampsiteScreenPreview() {
    DasCampTheme() {
        EntrantCampsiteScreen(rememberNavController())
    }
}

/**
 * Screen listing the entrant's notifications. Each can be dismissed with its close
 * button, or tapped to navigate to the campsite screen.
 *
 * The notifications are hardcoded dummy strings for now.
 *
 * @param navController used to navigate when a notification is tapped
 */
@Composable
fun EntrantNotificationList(
    navController: NavController,
    notificationView: NotificationView? = null,
    notificationsController: NotificationsController? = null,
    onLotterySelected: (String) -> Unit = {}
) {
    val notifications = notificationsController?.inbox(notificationView?.notifications.orEmpty()).orEmpty()

    EntrantPage(
        navController = navController,
        showBack = false,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Notifications",
                style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Display the signed-in entrant's win/loss messages and other alerts.
        if (notifications.isEmpty()) {
            Text("No notifications yet")
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(notifications) { _, notification ->
                EntrantNotificationRow(
                    alert = notification.message,
                    onDismiss = { notificationsController?.dismiss(notification) },
                    onClick = { notification.lotteryId?.let(onLotterySelected) }
                )
            }
        }
    }
}

/**
 * A single notification card with a dismiss button, the notification text, and a
 * chevron indicating it can be opened.
 *
 * @param alert the notification text to show
 * @param onDismiss called when the close button is tapped
 * @param onClick called when the card is tapped
 */
@Composable
fun EntrantNotificationRow(
    alert: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                text = alert
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go to details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Screen listing the entrant's reservations, split into "Active" and "Past" tabs.
 *
 * Active reservations show a "Cancel reservation" button, which calls
 * [ReservationController.cancel]. If no controller is passed, both lists are empty.
 *
 * @param navController used for the scaffold's navigation
 * @param reservationController supplies the active and past reservations and
 * handles cancellation
 */
@Composable
fun EntrantReservationList(
    navController: NavController,
    reservationController: ReservationController? = null
) {
    var showPast by rememberSaveable { mutableStateOf(false) }
    val shown = if (showPast) reservationController?.past().orEmpty()
                else reservationController?.active().orEmpty()

    EntrantPage(navController = navController, showBack = false) {

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Reservations", style = MaterialTheme.typography.titleLarge) }

        // Active / Past tabs
        // Display Reservations
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!showPast) {
                Button(onClick = {}) { Text("Active") }
            } else {
                OutlinedButton(onClick = { showPast = false }) { Text("Active") }
            }

            if (showPast) {
                Button(onClick = {}) { Text("Past") }
            } else {
                OutlinedButton(onClick = { showPast = true }) { Text("Past") }
            }
        }

        if (shown.isEmpty()) Text(if (showPast) "No past reservations" else "No active reservations")
        shown.forEach { reservation ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Text(reservation.campsiteName, style = MaterialTheme.typography.titleMedium)
                    Text("${reservation.startDate} to ${reservation.endDate}")
                    if (!showPast) {
                        Button(onClick = { reservationController?.cancel(reservation) }) {
                            Text("Cancel reservation")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen listing the entrant's message conversations with owners, with a search
 * field. Tapping a conversation opens [EntrantMessageScreen].
 *
 * The conversations are hardcoded dummy entries ("Owner 1" to "Owner 6"), and the
 * search text doesn't filter anything yet.
 *
 * @param navController used to navigate to a conversation
 */
@Composable
fun EntrantMessageList(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }
    // Dummy data
    val nums = (1..6).toList()

    EntrantPage(
        navController = navController,
        showBack = false,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Messages", style = MaterialTheme.typography.titleLarge) }

        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Messages") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Messages
        /*TODO("Implement message class logic")*/
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nums) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(EntrantRoutes.MESSAGE) },
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
                        Text(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            text = "Owner $it"
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go to details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chat screen for a conversation with a campsite owner: message bubbles (the
 * entrant's on the right, the owner's on the left) above a message field.
 *
 * The messages are hardcoded dummy data, and the send icon doesn't send anything yet.
 *
 * @param navController used for back navigation
 */
@Composable
fun EntrantMessageScreen(navController: NavController) {
    // Dummy data
    var newMessage by rememberSaveable { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            "Hi! I wanna buy this." to true,
            "Ok, sure" to false,
            "Okay thanks!" to true,
            "I like potatoes..." to true,
            "Alright" to false,
            "That's awesome" to false,
            "Yeah" to true
        )
    }

    EntrantPage(
        navController,
        showBack = true
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Owner Name",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(24.dp))

            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Display Messages (to be implemented)
        Column(modifier = Modifier.fillMaxSize().weight(1f)) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { (text, isSender) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isSender) 16.dp else 4.dp,
                                bottomEnd = if (isSender) 4.dp else 16.dp
                            ),
                            color = if (isSender) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.padding(12.dp),
                                color = if (isSender) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Message Bar
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Message") },
                trailingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send") },
                singleLine = true
            )
        }
    }
}


/**
 * The entrant's profile screen: name, email, phone, a "Change Profile Picture"
 * card, an edit button, a notifications toggle, the app policy, an "Apply to be a
 * Campsite Owner" card, and a "Delete Profile" card.
 *
 * All of the profile data is hardcoded, and the edit, picture, policy, apply, and
 * delete cards don't do anything yet. Only the notifications toggle changes its
 * own label.
 *
 * @param navController used for the scaffold's navigation
 */
@Composable
fun EntrantProfileScreen(navController: NavController, repository: DasCampRepository) {
    val entrantId = repository.uid

    // Dummy data
    var notifications by remember { mutableStateOf(false) }
    var showRules by rememberSaveable { mutableStateOf(false) } //app rules
    val view = remember(entrantId) { ProfileView(repository, entrantId) }
    val controller = remember { ProfileController(repository) }
    DisposableEffect(view) { onDispose { view.remove() } }

    val profile = view.profile
    var showEdit by rememberSaveable { mutableStateOf(false) }

    EntrantPage(
        navController = navController,
        showBack = false,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Profile", style = MaterialTheme.typography.titleLarge) }

        // Display Profile
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(vertical = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                "First Name: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text("Kerem")
                        }
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                "Last Name: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text("Santoso")
                        }
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                "Email: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text("ksantoso@ualberta.ca")
                        }
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                "Phone Number: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text("(123) 456-7890")
                        }
                    }
                    Card(
                        modifier = Modifier
                            .clickable {showEdit = true},
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                modifier = Modifier
                                    .padding(start = 16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                text = "Edit",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier.size(150.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(80.dp),
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )

                        Text(
                            text = "Change Profile Picture",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showEdit) { //profile edit
                profile?.let { p ->
                    EditProfile(
                        initialFirst = p.firstName,
                        initialLast = p.lastName,
                        initialEmail = p.email,
                        initialPhone = p.phoneNumber,
                        onDismiss = { showEdit = false },
                        onSave = { f, l, e, ph ->
                            controller.saveProfile(f, l, e, ph)
                            showEdit = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { notifications = !notifications },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        modifier = Modifier
                            .weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        text = "Turn ${if (notifications) "off" else "on"} notifications",
                        fontWeight = FontWeight.Bold

                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {showRules = true},
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        text = "App-Policy & Guidelines",
                        fontWeight = FontWeight.Bold

                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        text = "This is the app policy and guidelines"

                    )
                }
            }
            if (showRules) { //show app rules
                AppRules(onDismiss = { showRules = false })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {},
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E8FF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        text = "Apply to be a Campsite Owner",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B21A8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {},
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEE2E2)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        text = "Delete Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                }
            }



        }
    }
}

/**
 * QR code scanner screen. Currently a placeholder: it shows a QR icon and a
 * "SCAN" button that navigates to the campsite screen.
 *
 * @param navController used to navigate after "scanning"
 */
//edit profile
@Composable
fun EditProfile(
    initialFirst: String,
    initialLast: String,
    initialEmail: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var first by rememberSaveable { mutableStateOf(initialFirst) }
    var last by rememberSaveable { mutableStateOf(initialLast) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var phone by rememberSaveable { mutableStateOf(initialPhone) }

    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val canSave = first.isNotBlank() && last.isNotBlank() && emailValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = first,
                    onValueChange = { first = it },
                    label = { Text("First name") },
                    singleLine = true,
                    isError = first.isBlank()
                )
                OutlinedTextField(
                    value = last,
                    onValueChange = { last = it },
                    label = { Text("Last name") },
                    singleLine = true,
                    isError = last.isBlank()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = !emailValid,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone number (optional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onSave(first.trim(), last.trim(), email.trim(), phone.trim()) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

//app rules
@Composable
fun AppRules(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Guide & Policy") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "How to use the app",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Scan a campsite QR code from the QR Code Scanner screen to check in.", style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text(
                        text = "Guidelines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Be respectful of other users and follow campsite rules.", style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text(
                        text = "Privacy policy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = "The camera is only used to scan QR codes. No images are stored.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Confirm") }
        }
    )
}


//campsite rules
@Composable
fun CampsiteRules(rulesText: String,
                         onDismiss: () -> Unit,
                         onConfirm: () -> Unit
    ) {
    var hasRead by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Guide & Policy") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                ) {
                    Text(rulesText, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasRead = !hasRead },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = hasRead, onCheckedChange = { hasRead = it })
                    Text("I have read and agree to the campsite rules and policies")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = hasRead) {
                Text("Confirm reservation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


// QR CODE SCREEN
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntrantQrCodeScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    EntrantPage(
        navController = navController,
        showBack = false,
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "QR Code Scanner", style = MaterialTheme.typography.titleLarge) }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(300.dp),
            ) {
                if (hasCameraPermission) {
                    QrCameraPreview(
                        modifier = Modifier.fillMaxSize()
                    ) { scannedValue ->
                        navController.navigate(EntrantRoutes.CAMPSITE)
                    }
                } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Camera permission is needed to scan QR codes.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }) { Text("Grant permission") }
                }
            }
            }
        }
    }
}

//camera for qr code scanning
@Composable
fun QrCameraPreview(
    modifier: Modifier = Modifier,
    onQrScanned: (String) -> Unit
) {
}

//Rules and policies
private const val SAMPLE_RULES = """
1. Check-in is after 2:00 PM and check-out is before 11:00 AM.

2. Quiet hours are from 10:00 PM to 7:00 AM. Please respect other campers.

3. Do not exceed the maximum occupancy listed for your campsite.

4. Fires are only allowed in designated fire pits. Never leave a fire unattended and fully extinguish it before leaving.

5. Pack out everything you bring in. Dispose of garbage and recyclables properly.

6. Pets must be leashed at all times and cleaned up after.

7. Do not damage trees, plants, or campsite property.

8. Follow all local wildlife and safety regulations. Store food securely.

9. The campsite owner may cancel a reservation if these rules are violated.
"""
