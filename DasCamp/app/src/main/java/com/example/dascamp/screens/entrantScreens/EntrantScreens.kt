package com.example.dascamp.screens.entrantScreens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.dascamp.controllers.MessageController
import com.example.dascamp.controllers.ReviewController
import com.example.dascamp.controllers.CampImages
import com.example.dascamp.model.DasCampRepository
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import com.example.dascamp.controllers.CampsiteController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ReservationCsv
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.formatPrice
import com.example.dascamp.model.invalidDate
import com.example.dascamp.screens.ProfileAction
import com.example.dascamp.screens.StoredImage
import com.example.dascamp.screens.DatePickerField
import com.example.dascamp.views.MessageView
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.CampsiteMap
import com.example.dascamp.views.ProfileView
import com.example.dascamp.model.Lottery
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.OwnerApplicationStatus
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.WaitlistStatus
import com.example.dascamp.model.label
import com.example.dascamp.screens.LocalCurrentUser
import com.example.dascamp.views.ReservationView
import com.example.dascamp.views.NotificationView
import com.example.dascamp.screens.QrCameraPreview
import com.example.dascamp.controllers.QrCodeController

/**
 * Entrant home screen, with shortcuts to the campsite list, reservations,
 * notifications, messages, and the QR scanner.
 *
 * The greeting uses the signed-in user's first name.
 *
 * @param navController used to navigate to the selected section
 */
@Composable
fun EntrantDashboard(navController: NavController) {
    val firstName = LocalCurrentUser.current?.firstName
        ?.takeIf { it.isNotBlank() } ?: "User"

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
            Text(text = "Welcome Back, $firstName", style = MaterialTheme.typography.titleLarge)
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
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notifications")
            }

            // Message list button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(EntrantRoutes.MESSAGE_LIST) }
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Messages")
            }

            // Scan QR code button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(EntrantRoutes.QR) }
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
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

/**
 * Screen listing campsites, with a search field and a collapsible filter section
 * (location, availability window, minimum stay, and max occupancy).
 *
 * Results come from [CampsiteController.searchCampsites]. The availability filter
 * is applied once both date endpoints are valid and ordered. Tapping a result stores its id in
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
    var dateFromFilter by rememberSaveable { mutableStateOf("") }
    var dateToFilter by rememberSaveable { mutableStateOf("") }
    var minimumStayFilter by rememberSaveable { mutableStateOf("1") }
    var occupancyFilter by rememberSaveable { mutableStateOf("") }      // occupancy filter

    val dateRangeValid =
        dateFromFilter.isNotBlank() &&
            dateToFilter.isNotBlank() &&
            !invalidDate(dateFromFilter) &&
            !invalidDate(dateToFilter) &&
            dateFromFilter <= dateToFilter

    // Searching results
    val results = campsiteController?.searchCampsites(
        query = query,
        location = locationFilter,
        guests = occupancyFilter.toIntOrNull(),
        startDate = if (dateRangeValid) dateFromFilter else "",
        endDate = if (dateRangeValid) dateToFilter else "",
        minimumStayDays = minimumStayFilter.toIntOrNull() ?: 0,
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
            dateFrom = dateFromFilter,
            onDateFromChange = { dateFromFilter = it },
            dateTo = dateToFilter,
            onDateToChange = { dateToFilter = it },
            minimumStay = minimumStayFilter,
            onMinimumStayChange = { typed ->
                minimumStayFilter = typed.filter(Char::isDigit)
            },
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
            CampsiteSummaryCard(
                campsite = campsite,
                onClick = {
                    repository?.openCampsiteId = campsite.id
                    navController.navigate(EntrantRoutes.CAMPSITE)
                }
            )
        }
    }
}

/**
 * One campsite in the list. The name and place stay on the left.
 * The rating sits in its own chip on the right. US 01.01.04
 */
@Composable
private fun CampsiteSummaryCard(campsite: Campsite, onClick: () -> Unit) {
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
 * Shows a short summary, the map, then the written details, calendar, and the
 * next step. Joining a waiting list does not reserve the campsite. Booking
 * opens only after the entrant is selected. "Book reservation" calls
 * [ScheduleController.reserveDates]. If the campsite can't be found, a
 * "Campsite not found" message is shown instead.
 *
 * The current user is read from the repository, falling back to a hardcoded test
 * user when there isn't one.
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
            campsite.id in lottery.campsiteIds && lottery.status != LotteryStatus.COMPLETED &&
                    lottery.entrants.any { it.entrantId == repo.uid && it.status == WaitlistStatus.WAITING }
        }
    }

    //lottery for campsite
    var lottery: Lottery? = null
    if(repository != null){
        for(item in repository.lotteries){
            if(item.campsiteIds.contains(campsite.id) && lotteryController?.waitlistOpen(item) == true){
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
    val context = LocalContext.current
    var confirmed by remember { mutableStateOf<Pair<Reservation, Long>?>(null) }
    val saveCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val c = confirmed
        if (uri != null && c != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(ReservationCsv.build(c.first, "confirmed", c.second).toByteArray())
            }
        }
    }
    var reviewStars by remember { mutableStateOf(5) }
    var reviewBody by remember { mutableStateOf("") }
    var reviewMessage by remember { mutableStateOf("") }
    val reviewController = remember(repository) { repository?.let { ReviewController(it) } }

    val campsiteReviews = repository?.reviews
        ?.filter { it.campsiteId == campsite.id }
        .orEmpty()
    val booked = if (scheduleController != null && reservationView != null) {
        scheduleController.bookedReservations(campsite.id, reservationView.reservations)
    } else {
        emptyList()
    }
    val myReservation = repository?.reservations?.firstOrNull { reservation ->
        reservation.campsiteId == campsite.id &&
            reservation.entrantId == entrantId &&
            reservation.status != ReservationStatus.CANCELLED
    }

    EntrantPage(
        navController = navController,
        showBack = true,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            StoredImage(
                path = campsite.imageUri,
                contentDescription = campsite.name,
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                contentScale = ContentScale.Fit
            ) {
                // No photo: grey box with an icon
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No campsite photo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = campsite.name,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = campsite.location.ifBlank { "Location not set" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

        Text("Reviews", style = MaterialTheme.typography.titleMedium)
        if (campsiteReviews.isEmpty()) {
            Text(
                "No reviews yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            campsiteReviews.forEach { review ->
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
        if (booked.isEmpty()) {
            Text("No dates booked", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            booked.forEach { reservation ->
                Text(
                    "Booked  ${reservation.startDate}  to  ${reservation.endDate}",
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

        var lotteryMessage by remember(lottery?.id, winningLottery?.id) { mutableStateOf("") }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (winningLottery != null) {
                    Text("You can reserve now", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Choose a campsite for this reservation",
                        style = MaterialTheme.typography.titleSmall
                    )
                    repository?.campsites
                        ?.filter { it.id in winningLottery.campsiteIds }
                        .orEmpty()
                        .forEach { option ->
                            if (option.id == campsite.id) {
                                Text("Selected: ${option.name} · ${option.location}")
                            } else {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        repository?.openCampsiteId = option.id
                                        navController.navigate(EntrantRoutes.CAMPSITE)
                                    }
                                ) {
                                    Text("Choose ${option.name}")
                                }
                            }
                        }
                    Text(
                        "You were selected. Choose dates from ${winningLottery.bookingStart} to ${winningLottery.bookingEnd}, accept the rules, then book. A saved reservation lets you post a review and message the owner.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    winningLottery.entrants.firstOrNull { it.entrantId == entrantId }
                        ?.responseDeadlineAt?.let { deadline ->
                            Text(
                                "Respond by ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(deadline))}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    DatePickerField(
                        value = startDate,
                        onDateSelected = { startDate = it },
                        label = { Text("Start date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DatePickerField(
                        value = endDate,
                        onDateSelected = { endDate = it },
                        label = { Text("End date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = guests,
                        onValueChange = { typed -> guests = typed },
                        label = { Text("Number of guests") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                confirmed = repository?.reservations?.firstOrNull {
                                    it.campsiteId == campsite.id && it.entrantId == entrantId &&
                                        it.startDate == startDate && it.endDate == endDate &&
                                        it.status == ReservationStatus.ACTIVE
                                }?.let { it to System.currentTimeMillis() }
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
                    ) { Text("Decline this spot") }
                } else if (lottery != null) {
                    val activeLottery = lottery
                    val currentEntry = activeLottery.entrants.firstOrNull { it.entrantId == repository?.uid }
                    val waitingCount = activeLottery.entrants.count { it.status == WaitlistStatus.WAITING }
                    val capacity = activeLottery.waitlistCapacity?.toString() ?: "no limit"
                    if (currentEntry == null) {
                        Text("Join the waiting list", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Joining does not reserve the campsite. Registration closes ${activeLottery.registrationEnd}. Winners are drawn after that, and you get a notification if you are selected. Booking then opens from ${activeLottery.bookingStart} to ${activeLottery.bookingEnd}. Reviews and messages unlock only after you reserve.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (currentEntry.status == WaitlistStatus.WAITING) {
                        Text("You're on the waiting list", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "You cannot reserve yet. Registration closes ${activeLottery.registrationEnd}, then winners are drawn. Check Notifications. If you are selected, come back here to book dates between ${activeLottery.bookingStart} and ${activeLottery.bookingEnd}.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "You are ${currentEntry.status.label()} in this lottery.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Booking stays closed unless you are selected. A replacement draw can still pick someone who is waiting.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "$waitingCount waiting · list $capacity",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (currentEntry == null || currentEntry.status == WaitlistStatus.WAITING) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                lotteryMessage = if (currentEntry == null) {
                                    lotteryController?.joinWaitlist(activeLottery, entrantName)
                                        ?: "You're on the waiting list. Watch Notifications after ${activeLottery.registrationEnd}."
                                } else {
                                    lotteryController?.leaveWaitlist(activeLottery)
                                        ?: "You left the waiting list"
                                }
                            }
                        ) {
                            Text(if (currentEntry == null) "Join waiting list" else "Leave waiting list")
                        }
                    }
                } else if (waitingLottery != null) {
                    Text("You're still waiting", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Selected entrants are choosing dates. You may be drawn if a spot opens before ${waitingLottery.bookingEnd}. You cannot reserve until you are selected.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("No open lottery", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "This campsite is not taking waiting-list entries. An owner has to publish a lottery before anyone can be selected and reserve.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                val statusLine = lotteryMessage.ifBlank { bookingMessage }
                if (statusLine.isNotBlank()) {
                    Text(statusLine, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        confirmed?.let { (r, _) ->
            val off = LocalCurrentUser.current?.notificationsOn == false
            AlertDialog(
                onDismissRequest = { confirmed = null },
                title = { Text("Reservation confirmed") },
                text = {
                    Text(
                        "${r.campsiteName}, ${r.startDate} to ${r.endDate}. " +
                            if (off) "Download your confirmation below."
                            else "A confirmation was sent to your notifications."
                    )
                },
                confirmButton = {
                    Button(onClick = { saveCsv.launch("reservation-${r.id}.csv") }) { Text("Download CSV") }
                },
                dismissButton = { TextButton(onClick = { confirmed = null }) { Text("Close") } }
            )
        }

        if (myReservation != null) {
            Text("Your stay", style = MaterialTheme.typography.titleMedium)
            Text(
                "Reserved ${myReservation.startDate} to ${myReservation.endDate}. You can rate the campsite and message the owner.",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    if (star == reviewStars) {
                        Button(onClick = { reviewStars = star }) { Text("$star") }
                    } else {
                        OutlinedButton(onClick = { reviewStars = star }) { Text("$star") }
                    }
                }
            }
            OutlinedTextField(
                value = reviewBody,
                onValueChange = { reviewBody = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Review") },
                placeholder = { Text("How was your stay?") }
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val error = reviewController?.postReview(campsite.id, reviewStars, reviewBody)
                    reviewMessage = error ?: "Review posted"
                    if (error == null) reviewBody = ""
                }
            ) { Text("Post review") }
            if (reviewMessage.isNotBlank()) Text(reviewMessage)
        }
        val participantLottery = repository?.lotteries?.firstOrNull { lottery ->
            campsite.id in lottery.campsiteIds &&
                lottery.entrants.any { it.entrantId == entrantId }
        }
        if (repository != null && (myReservation != null || participantLottery != null)) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    repository.openMessageOwnerId = campsite.ownerId
                    repository.openMessageEntrantId = entrantId
                    repository.openMessageLotteryId = participantLottery?.id
                    repository.openReservationId = myReservation?.id.orEmpty()
                    navController.navigate(EntrantRoutes.MESSAGE)
                }
            ) { Text("Message owner") }
        }
    }
}

/** One number in the campsite summary row. */
@Composable
fun RowScope.CampsiteFact(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A titled block. Blank text is skipped so empty policies do not leave a heading. */
@Composable
fun CampsiteSection(title: String, body: String) {
    if (body.isBlank()) return
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A colored dot and a short calendar key. */
@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

/** Short map caption. Four decimal places is enough to find the pin. */
fun readableCoordinates(latitude: Double, longitude: Double): String {
    if (latitude == 0.0 && longitude == 0.0) return "The owner has not set a map pin"
    return "${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}"
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
    val user = LocalCurrentUser.current
    val notificationsOff = user?.notificationsOn == false
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
        if (notificationsOff) {
            Text("Notifications are turned off. Turn them on in your profile settings to see them.")
        } else if (notifications.isEmpty()) {
            Text("No notifications yet")
        }

        notifications.forEach { notification ->
            EntrantNotificationRow(
                alert = notification.message,
                onDismiss = { notificationsController?.dismiss(notification) },
                onClick = { notification.lotteryId?.let(onLotterySelected) }
            )
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
    reservationController: ReservationController? = null,
    repository: DasCampRepository? = null
) {
    var showPast by rememberSaveable { mutableStateOf(false) }
    var rateMessage by remember {mutableStateOf("")}
    val shown = if (showPast) reservationController?.past().orEmpty()
                else reservationController?.active().orEmpty()
    val context = LocalContext.current
    var cancelled by remember { mutableStateOf<Pair<Reservation, Long>?>(null) }
    val saveCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val c = cancelled
        if (uri != null && c != null) {
            val r = c.first.copy(status = ReservationStatus.CANCELLED)  // the copy captured is still ACTIVE
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(ReservationCsv.build(r, "cancelled", c.second).toByteArray())
            }
        }
    }

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
Button(onClick = {
                            reservationController?.cancel(reservation)
                            cancelled = reservation to System.currentTimeMillis()
                        }) { Text("Cancel reservation") }
                    } else{
                        if(reservation.rating == 0){
                            Text("Rate your stay:")
                            Row{
                                for (stars in 1..5){
                                    TextButton(onClick = {
                                        if(reservationController != null){
                                            val error = reservationController.rate(reservation, stars)
                                            if (error == null){
                                                rateMessage = "Thanks. You rated " + reservation.campsiteName + " " + stars + "★"
                                            } else{
                                                rateMessage = error
                                            }
                                        }
                                    }) {Text(stars.toString() + "★")}
                                }
                            }
                        }else {
                            Text("You rated this stay " + reservation.rating + "★")
                        }
                    }
                    Button(onClick = {
                        repository?.let { repo ->
                            repo.openReservationId = reservation.id
                            repo.openMessageOwnerId = repo.campsites
                                .find { it.id == reservation.campsiteId }?.ownerId.orEmpty()
                            repo.openMessageEntrantId = reservation.entrantId
                            repo.openMessageLotteryId = null
                        }
                        navController.navigate(EntrantRoutes.MESSAGE)
                    }) { Text("Message owner") }
                }
            }
        }
        Text(rateMessage)
        cancelled?.let { (r, _) ->
            val off = LocalCurrentUser.current?.notificationsOn == false
            AlertDialog(
                onDismissRequest = { cancelled = null },
                title = { Text("Reservation cancelled") },
                text = {
                    Text(
                        "${r.campsiteName}, ${r.startDate} to ${r.endDate}. " +
                                if (off) "Download your confirmation below."
                                else "A confirmation was sent to your notifications."
                    )
                },
                confirmButton = {
                    Button(onClick = { saveCsv.launch("cancellation-${r.id}.csv") }) { Text("Download CSV") }
                },
                dismissButton = { TextButton(onClick = { cancelled = null }) { Text("Close") } }
            )
        }    }
}

/**
 * Existing owner/entrant conversations for the signed-in entrant. US 01.14.01
 */
@Composable
fun EntrantMessageList(
    navController: NavController,
    repository: DasCampRepository? = null
) {
    var query by rememberSaveable { mutableStateOf("") }
    val controller = remember(repository) { repository?.let { MessageController(it) } }
    val threads = controller?.entrantConversations().orEmpty().filter { thread ->
        query.isBlank() ||
            thread.otherName.contains(query, ignoreCase = true) ||
            thread.latestMessage.body.contains(query, ignoreCase = true)
    }

    EntrantPage(
        navController = navController,
        showBack = false,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Messages", style = MaterialTheme.typography.titleLarge) }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Messages") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            singleLine = true
        )

        if (threads.isEmpty()) {
            Text("No conversations yet. Message an owner from a reservation or lottery.")
        }
        threads.forEach { thread ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        repository?.openMessageOwnerId = thread.ownerId
                        repository?.openMessageEntrantId = thread.entrantId
                        repository?.openMessageLotteryId = null
                        repository?.openReservationId = ""
                        navController.navigate(EntrantRoutes.MESSAGE)
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
                        Text(thread.otherName, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            thread.latestMessage.body,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
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

/**
 * Shared conversation with one campsite owner. The entrant's messages sit on
 * the right. A lottery or reservation must authorize the first message.
 * US 01.14.01
 */
@Composable
fun EntrantMessageScreen(
    navController: NavController,
    repository: DasCampRepository? = null
) {
    var newMessage by rememberSaveable { mutableStateOf("") }
    var sendError by remember { mutableStateOf<String?>(null) }
    val controller = remember(repository) { repository?.let { MessageController(it) } }
    val view = remember(
        repository,
        repository?.openReservationId,
        repository?.openMessageOwnerId,
        repository?.openMessageEntrantId
    ) {
        repository?.let { MessageView(it) }
    }
    DisposableEffect(view) { onDispose { view?.remove() } }
    val messageScrollState = rememberScrollState()
    LaunchedEffect(view?.messages?.size) {
        messageScrollState.animateScrollTo(messageScrollState.maxValue)
    }

    val reservation = repository?.reservations?.find { it.id == repository.openReservationId }
    val campsite = repository?.campsites?.find { it.id == reservation?.campsiteId }
    val ownerId = repository?.openMessageOwnerId
        ?.ifBlank { campsite?.ownerId.orEmpty() }
        .orEmpty()
    val entrantId = repository?.openMessageEntrantId
        ?.ifBlank { repository?.uid.orEmpty() }
        .orEmpty()
    val owner = repository?.users?.find { it.id == ownerId }
    val title = owner?.displayName()?.ifBlank { null } ?: "Campsite owner"
    val sendMessage = {
        val error = controller?.send(
            ownerId = ownerId,
            entrantId = entrantId,
            body = newMessage,
            lotteryId = repository?.openMessageLotteryId,
            reservationId = repository?.openReservationId
                ?.takeIf { it.isNotBlank() }
        )
        sendError = error
        if (error == null) newMessage = ""
    }

    EntrantPage(
        navController,
        showBack = true,
        topBar = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        scrollState = messageScrollState,
        bottomBar = if (ownerId.isNotBlank() && entrantId.isNotBlank()) {{
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                sendError?.let { Text(it) }
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Message") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.clickable(onClick = sendMessage)
                        )
                    },
                    singleLine = true
                )
            }
        }} else null
    ) {
        if (ownerId.isBlank() || entrantId.isBlank()) {
            Text("Open a conversation from a campsite, lottery, or reservation.")
        } else {
            view?.messages.orEmpty().forEach { message ->
                val isSender = if(ownerId != entrantId){  //messages show on diff sides if same account is sending the messages
                        message.senderId == repository?.uid
                    }else{
                        !message.fromOwner
                    }
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
                            text = message.body,
                            modifier = Modifier.padding(12.dp),
                            color = if (isSender) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

        }
    }
}


/**
 * The entrant's profile screen: name, email, phone, a "Change Profile Picture"
 * card, an edit button, a notifications toggle, the app policy, sign out,
 * and a "Delete Profile" card.
 *
 * Edit saves the name, email, and phone. Delete asks for confirmation, then
 * removes the profile. The notifications toggle saves that preference.
 *
 * @param navController used for the scaffold's navigation
 */
@Composable
fun EntrantProfileScreen(
    navController: NavController,
    repository: DasCampRepository,
    onSignOut: () -> Unit = {}
) {
    val entrantId = repository.uid

    var showRules by rememberSaveable { mutableStateOf(false) } //app rules
    var showDelete by rememberSaveable {mutableStateOf(false) }
    val view = remember(entrantId) { ProfileView(repository, entrantId) }
    val controller = remember { ProfileController(repository) }
    DisposableEffect(view) { onDispose { view.remove() } }

    val profile = view.profile
    val notificationsOn = profile?.notificationsOn ?: true
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var imageMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Photo picker. The picture is shrunk, then stored in Firestore.
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            imageMessage = "Uploading photo…"
            val marker = withContext(Dispatchers.IO) {
                CampImages.upload(context, uri, CampImages.profilePath(entrantId))
            }
            if (marker == null) {
                imageMessage = "Could not save that picture"
            } else {
                controller.setProfileImage(marker)
                imageMessage = null
            }
        }
    }

    EntrantPage(
        navController = navController,
        showBack = false,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    imagePicker.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            StoredImage(
                path = profile?.profileImageUri,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Add a profile picture",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = profile?.displayName()?.ifBlank { "Your name" } ?: "Your name",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            "Signed in on this device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ProfileDetail(Icons.Default.Email, profile?.email?.ifBlank { "No email" } ?: "No email")
        ProfileDetail(Icons.Default.Phone, profile?.phoneNumber?.ifBlank { "No phone number" } ?: "No phone number")

        OutlinedButton(onClick = { showEdit = true }) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Edit details")
        }
        TextButton(onClick = {
            imagePicker.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) {
            Text(if (profile?.profileImageUri.isNullOrBlank()) "Add photo" else "Change photo")
        }
        if (!profile?.profileImageUri.isNullOrBlank()) {
            TextButton(onClick = {
                val path = profile?.profileImageUri
                scope.launch {
                    withContext(Dispatchers.IO) {
                        CampImages.delete(CampImages.profilePath(entrantId))
                        if (path != null && !path.startsWith("firestore:") && !path.startsWith("http")) {
                            File(path).delete()
                        }
                    }
                    controller.setProfileImage(null)
                    imageMessage = null
                }
            }) { Text("Remove photo") }
        }
        imageMessage?.let { Text(it) }

        if (showEdit) {
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Notifications", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = notificationsOn,
                    onCheckedChange = { controller.setNotifications(it) }
                )
            }
        }
        ProfileAction(
            icon = Icons.Default.Policy,
            label = "App policy and guidelines",
            onClick = { showRules = true }
        )
        if (showRules) {
            AppRules(onDismiss = { showRules = false })
        }
        if (profile?.owner != true) {
            val applyLabel = when (profile?.ownerApplication) {
                OwnerApplicationStatus.PENDING -> "Application pending"
                OwnerApplicationStatus.DENIED -> "Application denied. Apply again"
                else -> "Apply to be a Campsite Owner"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = controller.canApply(profile)) {
                        controller.applyToBeOwner()
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = applyLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B21A8)
                )
            }
        }
        ProfileAction(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Sign out",
            onClick = onSignOut
        )
        ProfileAction(
            icon = Icons.Default.Delete,
            label = "Delete profile",
            onClick = { showDelete = true },
            destructive = true
        )

            if (showDelete){
                AlertDialog(
                    onDismissRequest = {showDelete = false},
                    title = {Text("Delete Profile?")},
                    text = {
                        Text("This removes your profile and cancels your active reservations. This cant be undone")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                controller.deleteProfile()
                                showDelete = false
                                onSignOut()
                            }
                        ) { Text("Delete", color = Color(0xFF991B1B))}
                    },
                    dismissButton = {
                        TextButton(onClick = {showDelete = false}) {
                            Text("Cancel")
                        }
                    }
                )
            }
    }
}

/** One contact line under a profile name. */
@Composable
private fun ProfileDetail(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
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


/**
 * QR code scanner screen for entrants.
 *
 * Asks for camera permission, then shows the camera. When a DasCamp QR code is read,
 * it opens that campsite (it sets [DasCampRepository.openCampsiteId] and goes to
 * [EntrantRoutes.CAMPSITE], the same way the campsite list does).
 * Any other QR code shows "Not a DasCamp QR Code".
 *
 * The camera reads one code per visit, so the entrant reopens the screen to scan again.
 *
 * @param navController used to open the scanned campsite
 * @param repository where the scanned campsite id is stored; if null, every scan
 * shows the "Not a DasCamp QR Code" message
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntrantQrCodeScreen(navController: NavController, repository: DasCampRepository? = null) {
    val context = LocalContext.current
    val qrController =  remember { QrCodeController() }
    var scanMessage by remember {mutableStateOf("")}
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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(300.dp),
            ) {
                if (hasCameraPermission) {
                    QrCameraPreview(
                        modifier = Modifier.fillMaxSize()
                    ) { scannedValue ->
                       val id = qrController.campsiteIdFrom(scannedValue)
                        if (id != null && repository != null){
                            repository.openCampsiteId = id
                            navController.navigate(EntrantRoutes.CAMPSITE)
                        } else {
                            scanMessage = "Not a DasCamp QR Code"
                        }
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
            Text (scanMessage)
        }
    }
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
