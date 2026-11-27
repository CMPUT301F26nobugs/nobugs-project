package com.example.dascamp.screens.ownerScreens

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.controllers.LotteryNotificationAudience
import com.example.dascamp.controllers.LotteryProposalController
import com.example.dascamp.controllers.MessageController
import com.example.dascamp.controllers.NotificationsController
import com.example.dascamp.controllers.QrCodeController
import com.example.dascamp.controllers.ReservationController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.controllers.copyPickedImage
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.CampsiteFormState
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.DateRange
import com.example.dascamp.model.LotteryFormState
import com.example.dascamp.model.LotteryProposal
import com.example.dascamp.model.LotteryProposalAction
import com.example.dascamp.model.LotteryProposalStatus
import com.example.dascamp.model.LotteryStatus
import com.example.dascamp.model.NotificationType
import com.example.dascamp.model.OwnerApprovalStatus
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.ReservationStatus
import com.example.dascamp.model.formatPrice
import com.example.dascamp.model.label
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.CampsiteMap
import com.example.dascamp.screens.EmptyValue
import com.example.dascamp.screens.LocalCurrentUser
import com.example.dascamp.screens.DatePickerField
import com.example.dascamp.screens.ProfileAction
import com.example.dascamp.screens.StoredImage
import com.example.dascamp.screens.entrantScreens.EntrantCalendarGrid
import com.example.dascamp.views.MessageView
import com.example.dascamp.views.ReservationView
import com.google.zxing.common.BitMatrix
import java.util.UUID
import com.example.dascamp.controllers.copyPickedImage

/**
 * Owner home. The two large buttons open the campsite list and the lottery list.
 * The three wide buttons open alerts, message logs, and reservations.
 */
@Composable
fun OwnerDashboard(
    navController: NavController,
    repository: DasCampRepository? = null
) {
    val firstName = LocalCurrentUser.current?.firstName
        ?.takeIf { it.isNotBlank() } ?: "User"

    OwnerPage(navController, showBack = false, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome Back, $firstName", style = MaterialTheme.typography.titleLarge)
        }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.openOwnerTab(OwnerRoutes.ALERTS) }
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notifications")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(OwnerRoutes.MESSAGE_LIST) }
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Messages")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.openOwnerTab(OwnerRoutes.RESERVATIONS) }
            ) {
                Icon(Icons.Default.EventAvailable, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reservations")
            }

            val pendingRequestCount = repository?.lotteryProposals.orEmpty().count { proposal ->
                proposal.status == LotteryProposalStatus.PENDING &&
                    proposal.ownerApprovals.any {
                        it.ownerId == repository?.uid &&
                            it.ownerId != proposal.creatorId &&
                            it.status == OwnerApprovalStatus.PENDING
                    }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(OwnerRoutes.APPROVALS) }
            ) {
                Icon(Icons.Default.FactCheck, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lottery approvals ($pendingRequestCount)")
            }
        }
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
        if(!campsite.imageUri.isNullOrBlank()){
            StoredImage(
                path = campsite.imageUri,
                contentDescription = campsite.name,
                modifier = Modifier.fillMaxWidth().height(400.dp),
                contentScale = ContentScale.Fit
            ) {}
        }

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
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {uri ->
        if (uri != null){
            val path = copyPickedImage(context, uri, "campsite-" + System.currentTimeMillis() + ".jpg")
            if (path == null){
                error = "Could not save that picture"
            } else{
                form = form.copy(imageUri = path)
            }
        }
    }
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
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
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
        DatePickerField(
            value = form.availability[0].start,
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { selected ->
                val oldRange = form.availability[0]
                form = form.copy(
                    availability = listOf(DateRange(selected, oldRange.end))
                )
            },
            label = { Text("Available from") }
        )
        DatePickerField(
            value = form.availability[0].end,
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { selected ->
                val oldRange = form.availability[0]
                form = form.copy(
                    availability = listOf(DateRange(oldRange.start, selected))
                )
            },
            label = { Text("Available to") }
        )
        error?.let { Text(it) }
        Text("Photo (optional)", style = MaterialTheme.typography.titleMedium)
        StoredImage(
            path = form.imageUri,
            contentDescription = "Campsite Photo",
            modifier = Modifier.fillMaxWidth().height(400.dp),
            contentScale = ContentScale.Fit
                ) {
            Text("No photo yet")
        }
        Button(onClick = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            if (form.imageUri.isNullOrBlank()){
                Text("Upload Photo")
            } else{
                Text("Change Photo")
            }
        }
        if (!form.imageUri.isNullOrBlank()){
            TextButton(onClick = { form = form.copy(imageUri = null) }) { Text("Remove photo") }
        }
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
            Box(modifier = Modifier
                .size(20.dp)
                .background(Color.Green))
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Booked = ")
            Box(modifier = Modifier
                .size(20.dp)
                .background(Color.Red))
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Closed = ")
            Box(modifier = Modifier
                .size(20.dp)
                .background(Color.LightGray))
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
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clipToBounds()){
                CampsiteMap(
                    latitude = campsite.latitude,
                    longitude = campsite.longitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)  //onLocationPicked = null -> view only, pin cannot be moved
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
* QR code for one campsite (the one the owner opened).
* Entrants scan it to open the campsite. The owner can save it as a picture or share it.
*
* @param navController used to go back
* @param repository source of the campsite the owner opened
*/
@Composable
fun OwnerQrCode(navController: NavController, repository: DasCampRepository? = null) {
    val context = LocalContext.current
    val qrController = remember { QrCodeController() }

    var campsite: Campsite? = null
    if (repository != null) {
        for (site in repository.campsites) {
            if (site.id == repository.openCampsiteId) {
                campsite = site
            }
        }
    }

    if (campsite == null) {
        OwnerPage(navController, showBack = true) {
            Text("Campsite not found")
        }
        return
    }

    val text = qrController.qrText(campsite)
    val bitmap = remember(text) { qrBitmap(qrController.makeQr(text, 600)) }

    val savePng = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
    }
    OwnerPage(navController, showBack = true, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("QR Code", style = MaterialTheme.typography.titleLarge)
        Text(campsite.name, style = MaterialTheme.typography.titleMedium)

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code for " + campsite.name,
            modifier = Modifier.size(240.dp)
        )
        Text("ID: " + campsite.id)
        Text("Entrants can scan this code to view details and join the lottery")

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { savePng.launch("dascamp-qr-" + campsite.id + ".png") }
        ) { Text("Download QR Code") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out " + campsite.name + " on DasCamp: " + text
                )
                context.startActivity(Intent.createChooser(share, "Share Campsite"))
            }
        ) { Text("Share Link") }

    }

}

    /** Turns the QR grid into a picture: true = black square, false = white square */
    fun qrBitmap(matrix: BitMatrix): Bitmap{
    val width = matrix.width
    val height = matrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for ( x in 0 until width){
        for (y in 0 until height) {
            if (matrix[x, y]) {
                bitmap.setPixel(x, y, android.graphics.Color.BLACK)
            } else {
                bitmap.setPixel(x, y, android.graphics.Color.WHITE)
            }
        }
    }
        return bitmap
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
    LotteryForm(
        navController,
        repository,
        existingId = null,
        title = "Create lottery",
        action = "Publish lottery"
    )
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
        Text("Campsites in this period", style = MaterialTheme.typography.titleMedium)
        lottery.campsiteIds.forEach { siteId ->
            val site = repository?.campsites?.find { it.id == siteId }
            val ownerName = repository?.users?.find { it.id == site?.ownerId }?.displayName().orEmpty()
            val label = site?.name ?: siteId
            Text(if (ownerName.isBlank()) label else "$label · $ownerName")
        }
        val ownerCount = lottery.campsiteIds.mapNotNull { siteId ->
            repository?.campsites?.find { it.id == siteId }?.ownerId
        }.distinct().size
        if (ownerCount > 1) {
            Text("Shared lottery: campsites from $ownerCount owners")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${entrant.name} · ${entrant.status.label()}",
                    modifier = Modifier.weight(1f)
                )
                if (entrant.entrantId.isNotBlank()) {
                    OutlinedButton(onClick = {
                        repository?.let { repo ->
                            repo.openMessageOwnerId = repo.uid
                            repo.openMessageEntrantId = entrant.entrantId
                            repo.openMessageLotteryId = lottery.id
                            repo.openReservationId = ""
                        }
                        navController.navigate(OwnerRoutes.MESSAGE)
                    }) { Text("Message") }
                }
            }
        }
    }
}

@Composable
private fun LotteryCampsiteRow(
    name: String,
    ownerLabel: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text("$name · $ownerLabel")
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
    val proposalController = remember(repository) {
        repository?.let { LotteryProposalController(it) }
    }
    var approvalDeadline by remember(existing?.id) { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val selectedOwnerIds = form.campsiteIds.mapNotNull { campsiteId ->
        repository?.campsites?.firstOrNull { it.id == campsiteId }?.ownerId
    }.distinct()
    val needsApproval = existing == null && selectedOwnerIds.size > 1
    val defaultApprovalDeadline =
        proposalController?.defaultApprovalDeadline(form.registrationStart).orEmpty()
    OwnerPage(navController, showBack = true) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        FormField("Lottery name", form.name, onValueChange = { typed -> form = form.copy(name = typed) })
        Text("Lottery period", style = MaterialTheme.typography.titleMedium)
        Text("The lottery only runs between these dates. You can list several of your campsites together with campsites that belong to other owners.")
        Text("Your campsites", style = MaterialTheme.typography.titleMedium)
        val mine = repository?.campsites?.filter { it.ownerId == repository.uid }.orEmpty()
        val others = repository?.campsites?.filter { it.ownerId != repository.uid }.orEmpty()
        if (mine.isEmpty()) Text("Add one of your campsites before publishing a lottery.")
        mine.forEach { campsite ->
            LotteryCampsiteRow(campsite.name, "You", campsite.id in form.campsiteIds) { checked ->
                form = form.copy(
                    campsiteIds = if (checked) form.campsiteIds + campsite.id
                    else form.campsiteIds - campsite.id
                )
            }
        }
        Text("Other owners' campsites", style = MaterialTheme.typography.titleMedium)
        if (others.isEmpty()) Text("No other campsites are listed yet.")
        others.forEach { campsite ->
            val ownerName = repository?.users
                ?.find { it.id == campsite.ownerId }
                ?.displayName()
                ?.ifBlank { null }
                ?: "Another owner"
            LotteryCampsiteRow(campsite.name, ownerName, campsite.id in form.campsiteIds) { checked ->
                form = form.copy(
                    campsiteIds = if (checked) form.campsiteIds + campsite.id
                    else form.campsiteIds - campsite.id
                )
            }
        }
        DatePickerField(
            value = form.registrationStart,
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { form = form.copy(registrationStart = it) },
            label = { Text("Registration start") }
        )
        DatePickerField(
            value = form.registrationEnd,
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { form = form.copy(registrationEnd = it) },
            label = { Text("Registration end") }
        )
        DatePickerField(
            value = form.bookingStart,
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { form = form.copy(bookingStart = it) },
            label = { Text("Booking start") }
        )
        DatePickerField(
            value = form.bookingEnd,
            modifier = Modifier.fillMaxWidth(),
            onDateSelected = { form = form.copy(bookingEnd = it) },
            label = { Text("Booking end") }
        )
        if (needsApproval) {
            Text("The lottery will be created only after every other campsite owner approves.")
            DatePickerField(
                value = approvalDeadline.ifBlank { defaultApprovalDeadline },
                modifier = Modifier.fillMaxWidth(),
                onDateSelected = { approvalDeadline = it },
                label = { Text("Owner approval deadline") }
            )
        }
        FormField("Number of winners", form.winnerCount, onValueChange = { typed -> form = form.copy(winnerCount = typed) })
        FormField("Response window (hours)", form.responseWindowHours, onValueChange = { typed -> form = form.copy(responseWindowHours = typed) })
        FormField("Waitlist capacity, optional", form.waitlistCapacity, onValueChange = { typed -> form = form.copy(waitlistCapacity = typed) })
        error?.let { Text(it) }
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !submitting,
            onClick = {
                val built = form.toLottery(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    status = existing?.status ?: LotteryStatus.OPEN,
                    entrants = existing?.entrants ?: emptyList(),
                    validCampsiteIds = repository?.campsites?.map { it.id }?.toSet() ?: emptySet(),
                    organizerCampsiteIds = repository?.campsites
                        ?.filter { it.ownerId == repository.uid }
                        ?.map { it.id }
                        ?.toSet()
                        ?: emptySet()
                )
                val lottery = built.lottery
                if (lottery == null) {
                    error = built.error
                } else if (repository != null) {
                    val overlapError = proposalController
                        ?.validateNoOverlap(lottery, existing?.id)
                    if (overlapError != null) {
                        error = overlapError
                    } else if (existing != null) {
                        repository.updateLottery(lottery)
                        repository.openLotteryId = lottery.id
                        navController.popBackStack()
                    } else if (needsApproval) {
                        val submission = proposalController?.createProposal(
                            lottery,
                            approvalDeadline.ifBlank { defaultApprovalDeadline }
                        )
                        if (submission == null || submission.error != null) {
                            error = submission?.error ?: "Could not create the proposal"
                        } else if (submission.task == null) {
                            error = "Could not submit the proposal"
                        } else {
                            submitting = true
                            submission.task
                                .addOnSuccessListener {
                                    submitting = false
                                    navController.navigate(OwnerRoutes.APPROVALS)
                                }
                                .addOnFailureListener { exception ->
                                    submitting = false
                                    error = exception.localizedMessage
                                        ?: "Could not submit the proposal"
                                }
                        }
                    } else {
                        repository.addLottery(lottery)
                        repository.openLotteryId = lottery.id
                        navController.popBackStack()
                    }
                }
            }
        ) { Text(if (needsApproval) "Request owner approval" else action) }
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
            OutlinedButton(
                onClick = {
                    repository?.openReservationId = reservation.id
                    navController.navigate(OwnerRoutes.RESERVATION)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${reservation.campsiteName} · ${reservation.entrantName} · ${reservation.startDate} to ${reservation.endDate}")
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
            onClick = {
                repository?.let { repo ->
                    repo.openMessageOwnerId = repo.uid
                    repo.openMessageEntrantId = reservation.entrantId
                    repo.openMessageLotteryId = null
                    repo.openReservationId = reservation.id
                }
                navController.navigate(OwnerRoutes.MESSAGE)
            }) { Text("Message") }
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
fun OwnerNotifications(
    navController: NavController,
    repository: DasCampRepository? = null,
    notificationsController: NotificationsController? = null
) {
    var showingIncoming by rememberSaveable { mutableStateOf(false) }
    val ownerInboxController = remember(repository, notificationsController) {
        notificationsController ?: repository?.let { NotificationsController(it) }
    }
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
    val incoming = ownerInboxController
        ?.ownerInbox(repository?.notifications.orEmpty())
        .orEmpty()

    OwnerPage(navController, showBack = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!showingIncoming) Button(onClick = { showingIncoming = false }) {
                Text("Outgoing")
            } else OutlinedButton(onClick = { showingIncoming = false }) {
                Text("Outgoing")
            }
            if (showingIncoming) Button(onClick = { showingIncoming = true }) {
                Text("Incoming")
            } else OutlinedButton(onClick = { showingIncoming = true }) {
                Text("Incoming")
            }
        }
        if (showingIncoming) {
            Text("Incoming notifications", style = MaterialTheme.typography.titleLarge)
            if (repository?.currentUser?.notificationsOn == false) {
                Text("Notifications are turned off")
            } else if (incoming.isEmpty()) {
                Text("No incoming notifications")
            } else {
                incoming.forEach { notification -> Text(notification.message) }
            }
        } else {
            Text("Sent notifications", style = MaterialTheme.typography.titleLarge)
            if (sentGroups.isEmpty()) {
                Text("No sent notifications")
            } else {
                sentGroups.forEach { group ->
                    val first = group.first()
                    Text(first.message)
                    Text("Sent to ${group.size} entrant(s): ${group.joinToString { it.recipientName }}")
                }
            }
        }
    }
}

/** Owner view of shared-lottery proposals created by or sent to this owner. */
@Composable
fun OwnerLotteryApprovals(
    navController: NavController,
    repository: DasCampRepository,
    controller: LotteryProposalController
) {
    var showingRequests by rememberSaveable { mutableStateOf(true) }
    var confirm by remember { mutableStateOf<Pair<LotteryProposal, Boolean>?>(null) }
    var confirmCancel by remember { mutableStateOf<LotteryProposal?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val uid = repository.uid
    val incoming = repository.lotteryProposals.filter { proposal ->
        proposal.creatorId != uid && proposal.ownerApprovals.any { it.ownerId == uid }
    }
    val outgoing = repository.lotteryProposals.filter { it.creatorId == uid }

    OwnerPage(navController, showBack = true) {
        Text("Lottery approvals", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showingRequests) Button(
                modifier = Modifier.weight(1f),
                onClick = { showingRequests = true }
            ) {
                Text("Incoming", maxLines = 1)
            } else OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { showingRequests = true }
            ) {
                Text("Incoming", maxLines = 1)
            }
            if (!showingRequests) Button(
                modifier = Modifier.weight(1f),
                onClick = { showingRequests = false }
            ) {
                Text("My proposals", maxLines = 1)
            } else OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { showingRequests = false }
            ) {
                Text("My proposals", maxLines = 1)
            }
        }
        error?.let { Text(it) }

        if (showingRequests) {
            if (incoming.isEmpty()) Text("No campsite-use requests")
            incoming.forEach { proposal ->
                val approval = proposal.ownerApprovals.first { it.ownerId == uid }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(proposal.lotteryDraft.name)
                    Text(
                        "Campsites: " + approval.campsiteIds.joinToString { campsiteId ->
                            repository.campsites.firstOrNull { it.id == campsiteId }?.name
                                ?: campsiteId
                        }
                    )
                    Text("From ${proposal.creatorName}")
                    val status = when {
                        proposal.status == LotteryProposalStatus.REJECTED &&
                            approval.status == OwnerApprovalStatus.DECLINED -> "Declined"
                        proposal.status == LotteryProposalStatus.REJECTED -> "Rejected"
                        proposal.status == LotteryProposalStatus.PUBLISHED -> "Published"
                        proposal.status == LotteryProposalStatus.EXPIRED -> "Expired"
                        proposal.status == LotteryProposalStatus.CANCELLED -> "Cancelled"
                        approval.status == OwnerApprovalStatus.DECLINED -> "Declined"
                        approval.status == OwnerApprovalStatus.APPROVED -> "Approved"
                        else -> "Pending"
                    }
                    Text(status)
                    if (proposal.status == LotteryProposalStatus.PENDING &&
                        approval.status == OwnerApprovalStatus.PENDING
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { confirm = proposal to true }) {
                                Text("Approve")
                            }
                            OutlinedButton(onClick = { confirm = proposal to false }) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
        } else {
            if (outgoing.isEmpty()) Text("No proposals created")
            outgoing.forEach { proposal ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(proposal.lotteryDraft.name)
                    Text(proposal.status.name.lowercase().replaceFirstChar { it.uppercase() })
                    if (proposal.status == LotteryProposalStatus.PENDING) {
                        proposal.ownerApprovals.forEach { approval ->
                            Text(
                                "${approval.ownerName}: " +
                                    approval.status.name.lowercase()
                                        .replaceFirstChar { it.uppercase() }
                            )
                        }
                        OutlinedButton(onClick = { confirmCancel = proposal }) {
                            Text("Cancel proposal")
                        }
                    }
                }
            }
        }
    }

    confirm?.let { (proposal, approve) ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(if (approve) "Approve request?" else "Decline request?") },
            text = { Text("This decision cannot be changed.") },
            confirmButton = {
                TextButton(onClick = {
                    confirm = null
                    controller.respond(proposal.id, uid, approve)
                        .addOnSuccessListener { result ->
                            if (result != LotteryProposalAction.UPDATED) {
                                error = "This request has already been handled or is unavailable."
                            }
                        }
                        .addOnFailureListener {
                            error = it.localizedMessage ?: "Could not save your response"
                        }
                }) { Text(if (approve) "Approve" else "Decline") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = null }) { Text("Keep pending") }
            }
        )
    }

    confirmCancel?.let { proposal ->
        AlertDialog(
            onDismissRequest = { confirmCancel = null },
            title = { Text("Cancel proposal?") },
            text = { Text("All involved owners will be notified.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = null
                    controller.cancel(proposal.id, uid)
                        .addOnSuccessListener { result ->
                            if (result != LotteryProposalAction.UPDATED) {
                                error = "This proposal is no longer pending."
                            }
                        }
                        .addOnFailureListener {
                            error = it.localizedMessage ?: "Could not cancel the proposal"
                        }
                }) { Text("Cancel proposal") }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = null }) { Text("Keep pending") }
            }
        )
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LotteryNotificationAudience.values().forEach { audience ->
                if (audience == send)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { send = audience }
                    ) {
                        Text(audience.label)
                    } else OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
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

/** Shared conversations with entrants who contacted this owner. */
@Composable
fun OwnerMessageList(navController: NavController, repository: DasCampRepository? = null) {
    var query by rememberSaveable { mutableStateOf("") }
    val controller = remember(repository) { repository?.let { MessageController(it) } }
    val threads = controller?.ownerConversations().orEmpty().filter { thread ->
        query.isBlank() ||
            thread.otherName.contains(query, ignoreCase = true) ||
            thread.latestMessage.body.contains(query, ignoreCase = true)
    }
    OwnerPage(navController, showBack = true) {
        Text("Messages", style = MaterialTheme.typography.titleLarge)
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
                        navController.navigate(OwnerRoutes.MESSAGE)
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
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
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
                        contentDescription = "Go to conversation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/** Shared owner/entrant conversation. The owner can reply to the entrant. */
@Composable
fun OwnerMessages(navController: NavController, repository: DasCampRepository? = null) {
    var body by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
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
    val ownerId = repository?.openMessageOwnerId?.ifBlank {
        reservation?.let { selected ->
            repository?.campsites?.find { it.id == selected.campsiteId }?.ownerId
        }.orEmpty()
    }.orEmpty()
    val entrantId = repository?.openMessageEntrantId
        ?.ifBlank { reservation?.entrantId.orEmpty() }
        .orEmpty()
    val profileName = repository?.users?.find { it.id == entrantId }
        ?.displayName().orEmpty()
    val storedName = view?.messages?.lastOrNull { it.entrantName.isNotBlank() }
        ?.entrantName.orEmpty()
    val entrantName = profileName.ifBlank { storedName }
        .ifBlank { reservation?.entrantName.orEmpty() }
        .ifBlank { "Entrant" }
    val sendMessage = {
        val result = controller?.send(
            ownerId = ownerId,
            entrantId = entrantId,
            body = body,
            lotteryId = repository?.openMessageLotteryId,
            reservationId = repository?.openReservationId
                ?.takeIf { it.isNotBlank() }
        )
        error = result
        if (result == null) body = ""
    }
    OwnerPage(
        navController,
        showBack = true,
        topBar = {
            Text(entrantName, style = MaterialTheme.typography.titleLarge)
        },
        scrollState = messageScrollState,
        bottomBar = if (ownerId == repository?.uid && entrantId.isNotBlank()) {{
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                error?.let { Text(it) }
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
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
        if (ownerId != repository?.uid || entrantId.isBlank()) {
            Text("Open a participant conversation from a lottery or reservation.")
        } else {
            if (view == null || view.messages.isEmpty()) Text("No messages yet")
            view?.messages?.forEach { message ->
                val mine = message.senderId == repository?.uid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (mine) 16.dp else 4.dp,
                            bottomEnd = if (mine) 4.dp else 16.dp
                        ),
                        color = if (mine) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = message.body,
                            modifier = Modifier.padding(12.dp),
                            color = if (mine) MaterialTheme.colorScheme.onPrimary
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
 * Profile tab. Edit unlocks the fields, Done saves them.
 */
@Composable
fun OwnerProfile(
    navController: NavController,
    repository: DasCampRepository? = null,
    onSignOut: () -> Unit = {}
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

    OwnerPage(navController, showBack = false, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile", style = MaterialTheme.typography.titleLarge)
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .padding(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            saved?.displayName()?.ifBlank { "Owner" } ?: "Owner",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            "Signed in on this device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!editing) {
            OwnerDetail(Icons.Default.Email, email.ifBlank { "No email" })
            OwnerDetail(Icons.Default.Phone, phoneNumber.ifBlank { "No phone number" })
        } else {
            FormField("First name", firstName, { firstName = it }, true)
            FormField("Last name", lastName, { lastName = it }, true)
            FormField("Email", email, { email = it }, true)
            FormField("Phone", phoneNumber, { phoneNumber = it }, true)
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
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
            }
        ) {
            Icon(
                if (editing) Icons.Default.Edit else Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (editing) "Save" else "Edit details")
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
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text("Notifications", modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsOn,
                    onCheckedChange = { notificationsOn = it },
                    enabled = editing
                )
            }
        }
        ProfileAction(Icons.AutoMirrored.Filled.Logout, "Sign out", onSignOut)
        ProfileAction(
            icon = Icons.Default.Delete,
            label = "Delete profile",
            destructive = true,
            onClick = {
                if (repository != null && saved != null) repository.deleteUser(saved)
                onSignOut()
            }
        )
    }
}

@Composable
private fun OwnerDetail(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
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
