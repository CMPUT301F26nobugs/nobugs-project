package com.example.dascamp.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.controllers.LotteryNotificationAudience
import com.example.dascamp.controllers.NotificationsController
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.screens.EmptyValue
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.CampsiteFormState
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.DateRange
import com.example.dascamp.model.LotteryFormState
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.label
import com.example.dascamp.model.OwnerMessage
import com.example.dascamp.model.NotificationType
import java.util.UUID
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.formatPrice
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.CampsiteMap
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.screens.EmptyValue
import com.example.dascamp.screens.entrantScreens.EntrantCalendarGrid
import com.example.dascamp.views.ReservationView

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
    val shown = repository?.let { repo ->
        repo.campsites.filter { campsite ->
            campsite.ownerId == repo.uid &&
                campsite.name.contains(query, ignoreCase = true)
        }
    }.orEmpty()
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
        if (repository != null && !repository.campsitesLoaded) {
            Text("Loading campsites…")
        } else if (shown.isEmpty()) {
            Text("No campsites found")
        } else {
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
        Text("Map Location, Click on the map to choose a pin location", style = MaterialTheme.typography.titleMedium)
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
            modifier = Modifier.fillMaxWidth().height(180.dp),
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
                    ownerId = existing?.ownerId ?: repository?.uid.orEmpty(),
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
fun OwnerBookedDates(navController: NavController, repository: DasCampRepository? = null, scheduleController: ScheduleController? = null, reservationView: ReservationView? = null) {
    val campsite = repository?.campsites?.find { it.id == repository.openCampsiteId }

    // ask controller for booked reservation
    var booked = listOf<Reservation>()
    if (campsite != null && scheduleController != null && reservationView != null){
        booked = scheduleController.bookedReservations(campsite.id, reservationView.reservations)
    }
    OwnerPage(navController, showBack = true){
        Text("Booked dates", style = MaterialTheme.typography.titleLarge)
        Text(campsite?.name?: "Campsite")
        EntrantCalendarGrid(campsite, scheduleController)
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Open = ")
            Box(modifier = Modifier.size(20.dp).background(Color.Green))
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Booked = ")
            Box(modifier = Modifier.size(20.dp).background(Color.Red))
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Closed = ")
            Box(modifier = Modifier.size(20.dp).background(Color.LightGray))
        }
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

/** Campsite location on a map, to edit the location, the edit button goes to the edit campsite page */
@Composable
fun OwnerMapLocation(navController: NavController, repository: DasCampRepository? = null) {
    val campsite = repository?.campsites?.find{it.id == repository.openCampsiteId}

    OwnerPage(navController, showBack = true) {
        Text("Map location", style = MaterialTheme.typography.titleLarge)

        if(campsite == null){
            Text("Unable to find campsite")
        }else {
            Text("Campsite Name: " + campsite.name)
            Text("Location Name: " + campsite.location)

            //geolocation box
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).clipToBounds()){
                CampsiteMap(
                    latitude = campsite.latitude,
                    longitude = campsite.longitude,
                    modifier = Modifier.fillMaxWidth().height(300.dp)  //onLocationPicked = null -> view only, pin cannot be moved
                )
            }

            Text("Location Coordinates: " + campsite.latitude + ", " + campsite.longitude)

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(OwnerRoutes.EDIT) }) {
                Text("Edit Location")
            }
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
    val shown = repository?.let { repo ->
        repo.lotteries
            .filter { lottery -> repo.campsites.any { it.id in lottery.campsiteIds && it.ownerId == repo.uid } }
            .filter { it.name.contains(query, ignoreCase = true) }
    }.orEmpty()
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
fun OwnerLotteryDetails(navController: NavController, repository: DasCampRepository? = null) {
    val lottery = repository?.let { repo -> repo.lotteries.find { it.id == repo.openLotteryId } }
    OwnerPage(navController, showBack = true) {
        if (lottery == null) {
            Text("Lottery not found")
            return@OwnerPage
        }
        Text(lottery.name, style = MaterialTheme.typography.titleLarge)
        Text("Registration: ${lottery.registrationStart} to ${lottery.registrationEnd}")
        Text("Booking: ${lottery.bookingStart} to ${lottery.bookingEnd}")
        Text("Winner count: ${lottery.winnerCount} · ${lottery.status.label()}")
        Text("Campsites", style = MaterialTheme.typography.titleMedium)
        lottery.campsiteIds.forEach { siteId ->
            Text(repository?.campsites?.find { it.id == siteId }?.name ?: siteId)
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.EDIT_LOTTERY) }) { Text("Edit lottery") }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(OwnerRoutes.SEND) }) { Text("Notify the waiting list") }
        Text("Waitlist (${lottery.entrants.size})", style = MaterialTheme.typography.titleMedium)
        if (lottery.entrants.isEmpty()) Text("No entrants yet")
        lottery.entrants.forEach { entrant ->
            Text("${entrant.name} · ${entrant.status.label()}")
        }
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
        repository?.let { repo -> repo.campsites.filter { it.ownerId == repo.uid } }?.forEach { campsite ->
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
        FormField("Response window (hours)", form.responseWindowHours, onValueChange = { typed -> form = form.copy(responseWindowHours = typed) })
        FormField("Waitlist capacity, optional", form.waitlistCapacity, onValueChange = { typed -> form = form.copy(waitlistCapacity = typed) })
        error?.let { Text(it) }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val built = form.toLottery(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    status = existing?.status ?: LotteryStatus.OPEN,
                    entrants = existing?.entrants ?: emptyList(),
                    validCampsiteIds = repository?.let { repo -> repo.campsites.filter { it.ownerId == repo.uid }.map { it.id }.toSet() } ?: emptySet()
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
fun OwnerReservations(navController: NavController, repository: DasCampRepository? = null) {
    var selected by remember { mutableStateOf("Active") }
    val controller = remember(repository) {
        repository?.let { com.example.dascamp.controllers.ReservationController(it) }
    }
    val reservations = when (selected) {
        "Past" -> controller?.ownerPast().orEmpty()
        "Cancelled" -> controller?.ownerCancelled().orEmpty()
        else -> controller?.ownerActive().orEmpty()
    }
    OwnerPage(navController, showBack = false) {
        Text("Reservations", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Active", "Past", "Cancelled").forEach { label ->
                if (label == selected) Button(onClick = { selected = label }) { Text(label) }
                else OutlinedButton(onClick = { selected = label }) { Text(label) }
            }
        }
        if (reservations.isEmpty()) Text("No ${selected.lowercase()} reservations")
        reservations.forEach { reservation ->
            OutlinedButton(onClick = {
                repository?.openReservationId = reservation.id
                navController.navigate(OwnerRoutes.RESERVATION)
            },
            modifier = Modifier.fillMaxWidth()  )
            {Text("${reservation.campsiteName} · ${reservation.entrantName} · ${reservation.startDate} to ${reservation.endDate}")
            }
        }
    }
}

/**
 * One reservation at the owner's campsite (US 02.08.01).
 * Shows the booking details and whether the rules were confirmed.
 * The owner can reject an active reservation by giving a reason.
 *
 * @param navController used to go back, open messages, or view booked dates
 * @param repository source of the reservation the owner tapped
 */
@Composable
fun OwnerReservationDetails(navController: NavController, repository: DasCampRepository? = null) {
    var reservation: Reservation? = null
    if(repository != null){
        for (item in repository.reservations){
            if(item.id == repository.openReservationId){
                reservation = item
            }
        }
    }
    val controller = remember(repository){ repository?.let { ReservationController(it) }}
    var reason by remember {mutableStateOf("")}
    var message by remember {mutableStateOf("")}

    if (reservation == null){
        OwnerPage(navController, showBack = true){
            Text("Reservation not found")
        }
        return
    }
    OwnerPage(navController, showBack = true){
        Text("Reservation", style = MaterialTheme.typography.titleLarge)
        Text("Campsite: " + reservation.campsiteName)
        Text("Entrant: " + reservation.entrantName)
        Text("Dates: " + reservation.startDate + " to " + reservation.endDate)
        Text("Guests: " + reservation.guestCount)
        Text("Status: " + reservation.status)
        if(reservation.cancelReason != null){
            Text("Reason: " + reservation.cancelReason)
        }

        Row(verticalAlignment = Alignment.CenterVertically){
            Checkbox(checked = reservation.rulesAccepted, onCheckedChange = null)
            Text("Rules & Policies Confirmed")
        }

        if(reservation.status == ReservationStatus.ACTIVE){
            FormField("Reason for rejection", reason, onValueChange = {
                typed -> reason = typed
            })
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if(controller != null){
                        val error = controller.reject(reservation, reason)
                        if(error == null){
                            navController.popBackStack()
                        } else{
                            message = error
                        }
                    }
                }
            ) {Text("Reject Reservation")}
            Text(message)
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {navController.navigate(OwnerRoutes.MESSAGE)}){Text("Message")}
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                repository?.openCampsiteId = reservation.campsiteId
                navController.navigate(OwnerRoutes.BOOKED)
            }
        ) {Text("View Dates")}
    }

}

/** Alerts tab. The blank card is one sent notification. */
@Composable
fun OwnerNotifications(navController: NavController, repository: DasCampRepository? = null) {
    val sentGroups = repository?.let { repo ->
        repo.notifications
            .filter {
                it.senderId == repo.uid && it.type == NotificationType.OWNER_MESSAGE
            }
            .groupBy { it.batchId.ifBlank { it.id } }
            .values
            .sortedByDescending { group ->
                group.firstOrNull()?.sentAt?.toLongOrNull() ?: 0L
            }
    }.orEmpty()

    OwnerPage(navController, showBack = false) {
        Text("Sent notifications", style = MaterialTheme.typography.titleLarge)
        sentGroups.forEach { group ->
            val first = group.first()
            Text(first.message)
            Text("Sent to ${group.size} entrant(s): ${group.joinToString { it.recipientName }}")
        }
    }
}

/** Send page opened from a lottery. The message field is empty. */
@Composable
fun OwnerSendNotification(
    navController: NavController,
    notificationsController: NotificationsController,
    repository: DasCampRepository? = null
) {
    var send by remember {
        mutableStateOf(LotteryNotificationAudience.ALL_WAITLIST)
    }
    var message by remember { mutableStateOf("") }
    var sendError by remember { mutableStateOf<String?>(null) }
    OwnerPage(navController, showBack = true) {
        Text("Send notification", style = MaterialTheme.typography.titleLarge)
        Text("Send to", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LotteryNotificationAudience.values().forEach { audience ->
                if (audience == send)
                    Button(onClick = { send = audience }) {
                        Text(audience.label)
                    } else OutlinedButton(
                    onClick = { send = audience }
                ) {
                    Text(audience.label)
                }
            }
        }
        FormField("Message", message, onValueChange = { typed -> message = typed })
        sendError?.let { Text(it) }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val result = notificationsController.sendLotteryMessage(
                    lotteryId = repository?.openLotteryId.orEmpty(),
                    audience = send,
                    message = message
                )
                if (result.error != null) {
                    sendError = result.error
                } else {
                    navController.openOwnerTab(OwnerRoutes.ALERTS)
                }
            }
        ) { Text("Send") }
    }
}

/** Message logs, opened from the home screen. The blank card opens a thread. */
@Composable
fun OwnerMessageList(navController: NavController, repository: DasCampRepository? = null) {
    OwnerPage(navController, showBack = true) {
        Text("Message logs", style = MaterialTheme.typography.titleLarge)
    }
}

/** One message thread. The list area and the write box are empty. */
@Composable
fun OwnerMessages(navController: NavController, repository: DasCampRepository? = null) {
    var body by remember { mutableStateOf("") }
    OwnerPage(navController, showBack = true) {
        Text("Message", style = MaterialTheme.typography.titleLarge)
        EmptyValue(0.5f)
        BlockBox(80)
        FormField("Write a message", body, onValueChange = { typed -> body = typed })
        Button(onClick = {
            // TODO implement messaging w/ firestore
            }
        ) { Text("Send") }
    }
}

/**
 * Profile tab. Edit unlocks the fields, Done saves them.
 */
@Composable
fun OwnerProfile(
    navController: NavController,
    repository: DasCampRepository? = null,
) {
    val saved = repository?.currentUser
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var notificationsOn by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    LaunchedEffect(saved?.id) {
        if (saved != null && !loaded) {
            firstName = saved.firstName
            lastName = saved.lastName
            email = saved.email
            phoneNumber = saved.phoneNumber
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
        Button(onClick = {
            if (editing && repository != null && saved != null) {
                repository.updateUser(
                    saved.copy(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        email = email.trim(),
                        phoneNumber = phoneNumber.trim(),
                        notificationsOn = notificationsOn
                    )
                )
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
            if (repository != null && saved != null) { repository.deleteUser(saved) }
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
