package com.example.dascamp.screens.entrantScreens

import java.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.controllers.ScheduleController
import com.example.dascamp.model.Campsite
import com.example.dascamp.model.Role
import com.example.dascamp.screens.AppScaffold
import com.example.dascamp.screens.RoleMenu
import com.example.dascamp.screens.DatePickerField
import com.example.dascamp.screens.contentMaxWidth
import com.example.dascamp.screens.screenPadding

/**
 * Navigation routes for the entrant screens.
 *
 * Unlike the admin routes, none of these take arguments yet, so each constant
 * can be passed straight to `navigate`.
 */
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

/**
 * Common layout for entrant screens: the app scaffold with the entrant navigation
 * bar and role menu, wrapped around a scrollable, padded column.
 *
 * @param navController used for the back button and the navigation bar
 * @param showBack whether to show a back button that pops the back stack
 * @param horizontalAlignment how the children of the column are aligned horizontally;
 * defaults to [Alignment.Start]
 * @param content the screen's content, laid out inside the column
 */
@Composable
fun EntrantPage(
    navController: NavController,
    showBack: Boolean,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    bottomBar: (@Composable () -> Unit)? = null,
    topBar: (@Composable () -> Unit)? = null,
    scrollState: ScrollState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val pageScrollState = scrollState ?: rememberScrollState()

    AppScaffold(
        onBackClick = if (showBack) {
            { navController.popBackStack() }
        } else null,
        navBar = { EntrantNavBar(navController) },
        roleMenu = { RoleMenu(navController, Role.ENTRANT) }
    ) { innerPadding ->
        if (bottomBar == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .widthIn(max = contentMaxWidth)
                        .verticalScroll(pageScrollState)
                        .padding(screenPadding()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = horizontalAlignment,
                    content = content
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                topBar?.let {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                            .padding(horizontal = screenPadding(), vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        it()
                    }
                }
                if (topBar != null) {
                    HorizontalDivider(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                            .padding(horizontal = screenPadding()),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .widthIn(max = contentMaxWidth)
                        .verticalScroll(pageScrollState)
                        .padding(screenPadding()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = horizontalAlignment,
                    content = content
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .widthIn(max = contentMaxWidth)
                        .padding(horizontal = screenPadding(), vertical = 8.dp)
                ) {
                    bottomBar()
                }
            }
        }
    }
}

/**
 * Bottom navigation bar for entrant screens, with buttons for the dashboard,
 * campsites, notifications, messages, and profile.
 *
 * @param navController used to navigate to the selected destination
 */
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

/**
 * A "Filter By:" toggle row with a collapsible card of campsite filters
 * (location, date, and max occupancy) beneath it.
 *
 * This composable is stateless: the caller owns the expanded flag and the field
 * values, and is told about every change through the callbacks.
 *
 * @param expanded whether the filter card is currently shown
 * @param onToggle called when the toggle row is clicked
 * @param location current location filter text
 * @param onLocationChange called with the new location text
 * @param dateFrom start of the availability search window
 * @param dateTo end of the availability search window
 * @param minimumStay minimum number of consecutive inclusive dates required
 * @param occupancy current max occupancy filter text
 * @param onOccupancyChange called with the new occupancy text
 */
@Composable
fun FilterSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    dateFrom: String,
    onDateFromChange: (String) -> Unit,
    dateTo: String,
    onDateToChange: (String) -> Unit,
    minimumStay: String,
    onMinimumStayChange: (String) -> Unit,
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
                    Text("Availability window (optional)", style = MaterialTheme.typography.labelLarge)
                    DatePickerField(
                        value = dateFrom,
                        onDateSelected = onDateFromChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("From") },
                        clearable = true
                    )
                    DatePickerField(
                        value = dateTo,
                        onDateSelected = onDateToChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("To") },
                        clearable = true
                    )
                    if (dateFrom.isBlank() != dateTo.isBlank()) {
                        Text("Choose both dates to filter by an availability window.")
                    }
                    if (dateFrom.isNotBlank() && dateTo.isNotBlank() && dateFrom > dateTo) {
                        Text("From must be on or before To.")
                    }
                    FilterField(
                        label = "Min stay:",
                        value = minimumStay,
                        onValueChange = onMinimumStayChange,
                        keyboardType = KeyboardType.Number,
                        onFocusLost = {
                            if (minimumStay.isBlank()) onMinimumStayChange("1")
                        }
                    )
                    Text("Minimum stay is measured in inclusive calendar days and must be at least 1.")
                    if (minimumStay.toIntOrNull()?.let { it >= 1 } != true) {
                        Text("Minimum stay must be at least 1 day.")
                    }
                    FilterField("Max Occ:", occupancy, onOccupancyChange)
                }
            }
        }
    }
}

/**
 * A single labeled text field inside the filter card. The field is empty by
 * default and shows "Any" as its placeholder.
 *
 * Must be called from a [Row]-compatible context only in the sense that it
 * emits its own [Row]; it fills the available width, with the text field taking
 * whatever space the fixed-width label leaves.
 *
 * @param label the text shown to the left of the field, e.g. `"Location:"`
 * @param value the current text of the field
 * @param onValueChange called with the new text whenever the user edits the field
 */
@Composable
fun FilterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    onFocusLost: (() -> Unit)? = null
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = { Text("Any") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focus ->
                    if (!focus.isFocused) onFocusLost?.invoke()
                }
        )
    }
}

/**
 * Month calendar for one campsite (US 01.04.01, 01.01.05).
 * Red = booked, green = open (inside the owner's availability), grey = closed.
 * The arrows change the month. It starts on the campsite's first open month.
 *
 * @param campsite the campsite to show, or null for an empty calendar
 * @param scheduleController decides if each day is booked or open
 */
@Composable
fun EntrantCalendarGrid(
    campsite: Campsite? = null,
    scheduleController: ScheduleController? = null
) {
    val today = Calendar.getInstance()
    var startYear = today.get(Calendar.YEAR)
    var startMonth = today.get(Calendar.MONTH) + 1
    if (campsite != null && campsite.availability.isNotEmpty()) {
        val firstOpen = campsite.availability[0].start
        if (firstOpen.length == 10) {
            val y = firstOpen.substring(0, 4).toIntOrNull()
            val x = firstOpen.substring(5, 7).toIntOrNull()
            if (y != null && x != null) {
                startYear = y
                startMonth = x
            }
        }
    }
    var year by remember(campsite?.id) { mutableStateOf(startYear) }
    var month by remember(campsite?.id) { mutableStateOf(startMonth) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = {
                month = month - 1
                if (month < 1) {
                    month = 12
                    year = year - 1
                }
            }
        ) { Text("◀") }

        Text(year.toString() + "-" + twoDigits(month), style = MaterialTheme.typography.titleMedium)

        TextButton(
            onClick = {
                month = month + 1
                if (month > 12) {
                    month = 1
                    year = year + 1
                }
            }
        ) { Text("▶") }
    }

    Row(Modifier.fillMaxWidth()) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
            Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }

    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, 1)
    val daysinMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstWeekday = calendar.get(Calendar.DAY_OF_WEEK) - 1

    val slashColor = MaterialTheme.colorScheme.outline
    val weekCount = (firstWeekday + daysinMonth + 6) / 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.outlineVariant)
            .padding(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        repeat(weekCount) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(7) { weekday ->
                    val day = week * 7 + weekday - firstWeekday + 1
                    val hasDate = day in 1..daysinMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                if (hasDate) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasDate) {
                            val date =
                                year.toString() + "-" + twoDigits(month) + "-" + twoDigits(day)
                            var color = Color(0xFF9E9E9E)

                            if (campsite != null && scheduleController != null) {
                                if (scheduleController.isDateBooked(campsite.id, date)) {
                                    color = Color(0xFFC62828)
                                } else if (scheduleController.isInAvailability(campsite, date)) {
                                    color = Color(0xFF66BB6A)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .border(width = 1.dp, color = color, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(day.toString(), style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = slashColor,
                                    start = Offset(size.width * 0.22f, size.height * 0.78f),
                                    end = Offset(size.width * 0.78f, size.height * 0.22f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
fun twoDigits(number: Int): String {
    if (number < 10) {
        return "0" + number
    }
    return number.toString()
}
