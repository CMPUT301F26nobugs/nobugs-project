package com.example.dascamp.screens.entrantScreens

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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dascamp.controllers.CampsiteController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Reservation
import com.example.dascamp.model.formatPrice
import com.example.dascamp.screens.BigButton
import com.example.dascamp.screens.CampsiteMap
import com.example.dascamp.screens.EmptyCard
import com.example.dascamp.model.Lottery

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

// CAMPSITE LIST SCREEN
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
    ).orEmpty() // or return empty if campsiteController is null

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
        if (results.isEmpty()) { Text("No campsites match") }
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

// CAMPSITE SCREEN
@Composable
fun EntrantCampsiteScreen(navController: NavController, repository: DasCampRepository? = null, scheduleController: ScheduleController? = null) {

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

    //lottery for campsite
    var lottery: Lottery? = null
    if(repository != null){
        for(item in repository.lotteries){
            if(item.campsiteIds.contains(campsite.id)){
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
            if (scheduleController != null && repository != null){
                booked = scheduleController.bookedReservations(campsite.id, repository.reservations)
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

            //Dates Selected
            Text(text = "Select your dates", style = MaterialTheme.typography.labelLarge )
            if(lottery != null){
                Text("Book between " + lottery.bookingStart + " and " + lottery.bookingEnd)
            }
            OutlinedTextField(
                value = startDate,
                onValueChange = {typed -> startDate = typed},
                label = {Text("Start date (yyyy-mm-dd)")},
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = endDate,
                onValueChange = {typed -> endDate = typed},
                label = {Text("End date (yyyy-mm-dd)")},
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = guests,
                onValueChange = {typed -> guests = typed},
                label = {Text("Number of guests")},
                modifier = Modifier.fillMaxWidth()
            )

        }

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        // Container for booking/entering lottery buttons
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enter lottery button
            Button(
                onClick = {/*TODO("Implement booking logic")*/}
            ) { Text("Enter Lottery") }

            Spacer(modifier = Modifier.padding(20.dp))

            // Waiting list display
            Column() {
                Text("Current Pool: 5/10")
                Text("End Date: May 10, 2026")
            }
        }

        //Confirm rules
        Row(verticalAlignment = Alignment.CenterVertically){
            Checkbox(
                checked = rulesAccepted,
                onCheckedChange = { checked -> rulesAccepted = checked}
            )
            Text("I have read the rules and policies")
        }
        //Book reservation
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                var guestCount = guests.toIntOrNull()
                if(guestCount == null){
                    guestCount = 0
                }
                if(scheduleController != null){
                    val error = scheduleController.reserveDates(
                        campsite = campsite,
                        lottery = lottery,
                        entrantId = entrantId,
                        entrantName = entrantName,
                        startDate = startDate,
                        endDate = endDate,
                        guestCount = guestCount,
                        rulesAccepted = rulesAccepted
                    )
                    if(error == null){
                        bookingMessage = "Reserved " + startDate + " to " + endDate
                    }else{
                        bookingMessage = error
                    }
                }
            }
        ){Text("Book Reservation")}
        Text(bookingMessage)

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


// NOTIFICATION LIST SCREEN
@Composable
fun EntrantNotificationList(navController: NavController) {
    // Dummy data
    val notifications = remember {
        mutableStateListOf(
            "You won the lottery for Campsite 1!",
            "You have lost the lottery for Campsite 2 :(",
            "Reservation Confirmation",
            "Reservation Cancellation Confirmation"
            )
    }

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

        // Display Notifications
        /*TODO("Implement notification class logic")*/
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(notifications) { index, alert ->
                EntrantNotificationRow(
                    alert = alert,
                    onDismiss = { notifications.remove( alert) },
                    onClick = {
                        // Route to a specific existing screen based on the message content
                        when (alert) {
                            "You won the lottery for Campsite 1!" -> navController.navigate(EntrantRoutes.CAMPSITE)
                            "You have lost the lottery for Campsite 2 :(" -> navController.navigate(EntrantRoutes.CAMPSITE)
                            "Reservation Confirmation" -> navController.navigate(EntrantRoutes.CAMPSITE)
                            "Reservation Cancellation Confirmation" -> navController.navigate(EntrantRoutes.CAMPSITE)
                        }
                    }
                )
            }
        }
    }
}

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

// RESERVATION LIST SCREEN
@Composable
fun EntrantReservationList(navController: NavController) {
    EntrantPage(
        navController = navController,
        showBack = false
    ) {

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) { Text(text = "Reservations", style = MaterialTheme.typography.titleLarge) }

        // Display Reservations
        EmptyCard {
            navController.navigate(
                EntrantRoutes.RESERVATION
            )
        }
    }
}

// MESSAGE LIST SCREEN
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

// MESSAGE SCREEN
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


// PROFILE SCREEN
@Composable
fun EntrantProfileScreen(navController: NavController) {
    // Dummy data
    var notifications by remember { mutableStateOf(false) }

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
                            .clickable {},
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
                    .clickable {},
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

// QR CODE SCREEN
@Composable
fun EntrantQrCodeScreen(navController: NavController) {
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.size(215.dp),
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )

                    Text(
                        text = "QR CODE",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(EntrantRoutes.CAMPSITE) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    text = "SCAN"
                )
            }
        }
    }
}