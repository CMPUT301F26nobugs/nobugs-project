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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.screens.EmptyValue
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.CampsiteFormState
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.DateRange
import com.example.dascamp.model.LotteryFormState
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.OwnerMessage
import com.example.dascamp.model.Notification
import com.example.dascamp.model.OwnerProfile
import java.util.UUID
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.formatPrice
import com.example.dascamp.screens.CampsiteMap



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
fun OwnerCampsitesList(
    navController: NavController,
    repository: DasCampRepository? = null
) {
    var query by remember { mutableStateOf("") }
    val shown = repository?.campsites
        ?.filter { it.name.contains(query, ignoreCase = true) }
        .orEmpty()
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
        shown.forEach { campsite ->
            TextButton(
                onClick = {
                    repository?.openCampsiteId = campsite.id
                    navController.navigate(OwnerRoutes.CAMPSITE)
                }
            ) { Text(campsite.name.ifBlank { "Untitled campsite" }) }
        }
    }
}

/**
 * Campsite details. Each button opens the next storyboard screen:
 * edit, booked dates, map, QR, or the create-lottery form.
 */
@Composable
fun OwnerCampsite(
    navController: NavController,
    repository: DasCampRepository? = null
) {
    val campsite = repository?.campsites?.find { it.id == repository.openCampsiteId }

    if (campsite == null){
        OwnerPage(navController, showBack = true ) {
            Text("Campsite not found")
        }
        return
    }
    OwnerPage(navController, showBack = true) {
        Text(campsite.name, style = MaterialTheme.typography.titleLarge)

        //Location
        Text("Location", style = MaterialTheme.typography.titleMedium)
        Text(campsite.location)

        //Price
        Text("Price per Night", style = MaterialTheme.typography.titleMedium)
        Text("$" + formatPrice(campsite.pricePerNight))

        //Maximum guests
        Text("Maximum Guests", style = MaterialTheme.typography.titleMedium)
        Text(campsite.maxOccupancy.toString())

        //Tags
        Text("Tags", style = MaterialTheme.typography.titleMedium)
        Text(campsite.tags.joinToString(", "))

        //Description
        Text("Description", style = MaterialTheme.typography.titleMedium)
        Text(campsite.description)

        //Rules
        Text("Rules", style = MaterialTheme.typography.titleMedium)
        Text(campsite.rules)

        //Cancellation
        Text("Cancellation", style = MaterialTheme.typography.titleMedium)
        Text(campsite.cancellationPolicy)

        //Availability
        Text("Availability", style = MaterialTheme.typography.titleMedium)
        for (range in campsite.availability){
            Text(range.start + " to " + range.end)
        }

        //Button Edit
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.EDIT) }
        ) { Text("Edit campsite") }

        //Booked dates
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.BOOKED) }
        ) { Text("Booked dates") }

        //Map button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.MAP) }
        ) { Text("Map location") }

        //Qr code button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.QR) }
        ) { Text("QR code") }

        //Create lottery button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.CREATE) }
        ) { Text("Create lottery") }

        //Delete button
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                campsite?.let { repository.deleteCampsite(it) }
                navController.popBackStack()
            }
        ) { Text("Delete campsite") }
    }
}
/** Empty publish form. Publish opens the blank details page. The fields are not stored. */
@Composable
fun OwnerAddCampsite(navController: NavController, repository: DasCampRepository? = null) {
    CampsiteForm(navController, repository, existing = null, "Add campsite", "Publish")
}

/** Same form as Add, still empty. Save returns to the previous page. */
@Composable
fun OwnerEditCampsite(navController: NavController, repository: DasCampRepository? = null) {
    val existing = repository?.campsites?.find { it.id == repository.openCampsiteId }
    CampsiteForm(navController, repository, existing, "Edit campsite", "Save")
}

@Composable
private fun CampsiteForm(
    navController: NavController,
    repository: DasCampRepository?,
    existing: Campsite?,
    title: String,
    action: String
) {
    var form by remember(existing?.id) {
        mutableStateOf(
            if (existing != null) CampsiteFormState.from(existing)
            else CampsiteFormState(
                latitude = "53.55",
                longitude = "-113.49",
                availability = listOf(DateRange("2027-05-01", "2027-09-30"))
            )
        )
    }
    var error by remember { mutableStateOf<String?>(null) }
    OwnerPage(navController, showBack = true) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        FormField("Name", form.name, onValueChange = { typed -> form = form.copy(name = typed) })
        FormField("Location", form.location, onValueChange = { typed -> form = form.copy(location = typed) })
        Text("Map Location", style = MaterialTheme.typography.titleMedium)
        var startLatitude = form.latitude.toDoubleOrNull()
        if (startLatitude == null){
            startLatitude = 53.55
        }
        var startLongitude = form.longitude.toDoubleOrNull()
        if (startLongitude == null){
            startLongitude = -113.49
        }

        CampsiteMap(
            latitude = startLatitude,
            longitude = startLongitude,
            modifier = Modifier.fillMaxWidth().height(250.dp),
            onLocationPicked = {
                pickedLatitude, pickedLongitude ->
                form = form.copy(
                    latitude = pickedLatitude.toString(),
                    longitude = pickedLongitude.toString()
                )
            }
        )
        Text("Picked: " + form.latitude + ", " + form.longitude)

        FormField("Amenities", form.description, onValueChange = { typed -> form = form.copy(description = typed) })
        FormField("Maximum occupancy", form.maxOccupancy, onValueChange = { typed -> form = form.copy(maxOccupancy = typed) })
        FormField("Price per night", form.pricePerNight, onValueChange = { typed -> form = form.copy(pricePerNight = typed) })
        FormField("Rules", form.rules, onValueChange = { typed -> form = form.copy(rules = typed) })
        FormField("Cancellation policy", form.cancellationPolicy, onValueChange = { typed -> form = form.copy(cancellationPolicy = typed) })
        FormField("Tags", form.tags, onValueChange = { typed -> form = form.copy(tags = typed) })
        FormField(
            "Available from (yyyy-MM-dd)",
            form.availability[0].start,
            onValueChange = {typed ->
                val oldRange = form.availability[0]
                val newRange = DateRange(typed, oldRange.end)
                form = form.copy(availability = listOf(newRange))
            }
        )
        FormField(
            "Available to (yyyy-MM-dd)",
            form.availability[0].end,
            onValueChange = {typed ->
                val oldRange = form.availability[0]
                val newRange = DateRange(oldRange.start, typed)
                form = form.copy(availability = listOf(newRange))
            }
        )
        error?.let { Text(it) }
        Button(onClick = {}) { Text("Upload photo") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val built = form.toCampsite(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    ownerId = existing?.ownerId ?: "owner-1",
                    rating = existing?.rating ?: 0.0,
                    ratingCount = existing?.ratingCount ?: 0
                )
                val campsite = built.campsite
                if (campsite == null) {
                    error = built.error
                } else if (repository != null) {
                    if (existing == null) repository.addCampsite(campsite) else repository.updateCampsite(campsite)
                    repository.openCampsiteId = campsite.id
                    navController.popBackStack()
                }
            }
        ) { Text(action) }
    }
}

// Booked dates for one campsite. controller picks the active reservation
@Composable
fun OwnerBookedDates(navController: NavController, repository: DasCampRepository? = null, scheduleController: ScheduleController? = null) {
    val campsite = repository?.campsites?.find { it.id == repository.openCampsiteId }

    // ask controller for booked reservation
    var booked = listOf<Reservation>()
    if (campsite != null && scheduleController != null){
        booked = scheduleController.bookedReservations(campsite.id, repository.reservations)
    }
    OwnerPage(navController, showBack = true){
        Text("Booked dates", style = MaterialTheme.typography.titleLarge)
        Text(campsite?.name?: "Campsite")
        CalendarGrid()
        Text("Stays", style = MaterialTheme.typography.titleMedium)
        if(booked.isEmpty()){
            Text("No booked dates")
        }else{
            for (reservation in booked){
                Text(reservation.startDate + " to " + reservation.endDate + " · " + reservation.entrantName)
            }
        }
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
fun OwnerLotteryList(navController: NavController, repository: DasCampRepository? = null) {
    var query by remember { mutableStateOf("") }
    val shown = repository?.lotteries
        ?.filter { it.name.contains(query, ignoreCase = true) }
        .orEmpty()
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
        shown.forEach { lottery ->
            TextButton(onClick = {
                repository?.openLotteryId = lottery.id
                navController.navigate(OwnerRoutes.LOTTERY)
            }) { Text(lottery.name) }
        }
    }
}

/** Empty lottery form. Publish opens the blank lottery details page. */
@Composable
fun OwnerCreateLottery(navController: NavController, repository: DasCampRepository? = null) {
    LotteryForm(navController, repository, existingId = null, "Create lottery", "Publish lottery")
}

/** Same form as Create. Save returns to lottery details. */
@Composable
fun OwnerEditLottery(navController: NavController, repository: DasCampRepository? = null) {
    LotteryForm(navController, repository, repository?.openLotteryId, "Edit lottery", "Save")
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
private fun LotteryForm(
    navController: NavController,
    repository: DasCampRepository?,
    existingId: String?,
    title: String,
    action: String
) {
    val existing = repository?.lotteries?.find { it.id == existingId }
    var form by remember(existing?.id) {
        mutableStateOf(
            if (existing != null) LotteryFormState.from(existing)
            else LotteryFormState(
                registrationStart = "2027-05-01",
                registrationEnd = "2027-05-15",
                bookingStart = "2027-05-16",
                bookingEnd = "2027-09-30",
                winnerCount = "5"
            )
        )
    }
    var error by remember { mutableStateOf<String?>(null) }
    OwnerPage(navController, showBack = true) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        FormField("Lottery name", form.name, onValueChange = { typed -> form = form.copy(name = typed) })
        Text("Campsites", style = MaterialTheme.typography.titleMedium)
        repository?.campsites?.forEach { campsite ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = campsite.id in form.campsiteIds,
                    onCheckedChange = { checked ->
                        form = form.copy(
                            campsiteIds = if (checked) form.campsiteIds + campsite.id
                            else form.campsiteIds - campsite.id
                        )
                    }
                )
                Text(campsite.name)
            }
        }
        FormField("Registration start", form.registrationStart, onValueChange = { typed -> form = form.copy(registrationStart = typed) })
        FormField("Registration end", form.registrationEnd, onValueChange = { typed -> form = form.copy(registrationEnd = typed) })
        FormField("Booking start", form.bookingStart, onValueChange = { typed -> form = form.copy(bookingStart = typed) })
        FormField("Booking end", form.bookingEnd, onValueChange = { typed -> form = form.copy(bookingEnd = typed) })
        FormField("Number of winners", form.winnerCount, onValueChange = { typed -> form = form.copy(winnerCount = typed) })
        FormField("Waitlist capacity, optional", form.waitlistCapacity, onValueChange = { typed -> form = form.copy(waitlistCapacity = typed) })
        error?.let { Text(it) }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val built = form.toLottery(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    status = existing?.status ?: LotteryStatus.OPEN,
                    entrants = existing?.entrants ?: emptyList(),
                    validCampsiteIds = repository?.campsites?.map { it.id }?.toSet() ?: emptySet()
                )
                val lottery = built.lottery
                if (lottery == null) {
                    error = built.error
                } else if (repository != null) {
                    if (existing == null) repository.addLottery(lottery) else repository.updateLottery(lottery)
                    repository.openLotteryId = lottery.id
                    navController.popBackStack()
                }
            }
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
fun OwnerNotifications(navController: NavController, repository: DasCampRepository? = null) {
    OwnerPage(navController, showBack = false) {
        Text("Sent notifications", style = MaterialTheme.typography.titleLarge)
        repository?.notifications?.forEach { note ->
            Text("${note.recipientName}: ${note.message}")
        }
    }
}

/** Send page opened from a lottery. The message field is empty. */
@Composable
fun OwnerSendNotification(navController: NavController, repository: DasCampRepository? = null) {
    var send by remember { mutableStateOf("All Waitlist") }
    var message by remember { mutableStateOf("") }
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
        FormField("Message", message, onValueChange = { typed -> message = typed })
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (message.isNotBlank()) {
                    repository?.addNotification(
                        Notification(
                            id = UUID.randomUUID().toString(),
                            recipientName = send,
                            message = message.trim(),
                            sentAt = System.currentTimeMillis().toString()
                        )
                    )
                }
                navController.openOwnerTab(OwnerRoutes.ALERTS)
            }
        ) { Text("Send") }
    }
}

/** Message logs, opened from the home screen. The blank card opens a thread. */
@Composable
fun OwnerMessageList(navController: NavController, repository: DasCampRepository? = null) {
    OwnerPage(navController, showBack = true) {
        Text("Message logs", style = MaterialTheme.typography.titleLarge)
        repository?.messages?.forEach { message ->
            TextButton(onClick = { navController.navigate(OwnerRoutes.MESSAGE) }) {
                Text("${message.entrantName}: ${message.body}")
            }
        }
    }
}

/** One message thread. The list area and the write box are empty. */
@Composable
fun OwnerMessages(navController: NavController, repository: DasCampRepository? = null) {
    var body by remember { mutableStateOf("") }
    OwnerPage(navController, showBack = true) {
        Text("Message", style = MaterialTheme.typography.titleLarge)
        repository?.messages?.forEach { message ->
            Text(message.body)
        }
        EmptyValue(0.5f)
        BlockBox(80)
        FormField("Write a message", body, onValueChange = { typed -> body = typed })
        Button(onClick = {
            if (body.isNotBlank()) {
                repository?.addMessage(
                    OwnerMessage(
                        id = UUID.randomUUID().toString(),
                        reservationId = "",
                        entrantName = "Entrant",
                        body = body.trim(),
                        sentAt = System.currentTimeMillis().toString()
                    )
                )
                body = ""
            }
        }) { Text("Send") }
    }
}

/**
 * Profile tab. Edit only unlocks the fields on this screen.
 * Nothing here is written to OwnerProfile yet.
 */
/**
 * Profile tab. Edit unlocks the fields, Done saves them.
 */
@Composable
fun OwnerProfile(navController: NavController, repository: DasCampRepository? = null) {
    val saved = repository?.owners?.firstOrNull()
    var profileId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var notificationsOn by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    LaunchedEffect(saved?.id) {
        if (saved != null && !loaded) {
            profileId = saved.id
            firstName = saved.firstName
            lastName = saved.lastName
            email = saved.email
            phoneNumber = saved.phoneNumber
            organization = saved.organizationName
            notificationsOn = saved.notificationsOn
            loaded = true
        }
    }
    OwnerPage(navController, showBack = false) {
        Text("Profile", style = MaterialTheme.typography.titleLarge)
        FormField("First name", firstName, { firstName = it }, editing)
        FormField("Last name", lastName, { lastName = it }, editing)
        FormField("Email", email, { email = it }, editing)
        FormField("Phone", phoneNumber, { phoneNumber = it }, editing)
        FormField("Organization", organization, { organization = it }, editing)
        Button(onClick = {
            if (editing && repository != null) {
                val id = profileId.ifBlank { UUID.randomUUID().toString() }
                profileId = id
                val profile = OwnerProfile(
                    id = id,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    phoneNumber = phoneNumber.trim(),
                    notificationsOn = notificationsOn,
                    organizationName = organization.trim()
                )
                if (repository.owners.any { it.id == id }) repository.updateOwner(profile)
                else repository.addOwner(profile)
            }
            editing = !editing
        }) { Text(if (editing) "Done" else "Edit") }
        Button(onClick = {}) { Text("Change profile picture") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Turn on notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = notificationsOn,
                onCheckedChange = { notificationsOn = it },
                enabled = editing
            )
        }
        Button(onClick = {}) { Text("App policy and guidelines") }
        Button(onClick = {
            saved?.let { repository.deleteOwner(it) }
            profileId = ""
            firstName = ""
            lastName = ""
            email = ""
            phoneNumber = ""
            organization = ""
            loaded = false
        }) { Text("Delete profile") }
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
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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