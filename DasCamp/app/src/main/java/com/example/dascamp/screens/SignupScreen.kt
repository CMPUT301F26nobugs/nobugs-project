package com.example.dascamp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.screens.entrantScreens.EntrantRoutes
import com.example.dascamp.ui.theme.DasCampTheme
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.DeviceSession
import com.example.dascamp.model.User
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.ownerScreens.OwnerRoutes

/**
 * Sign-up form for a new user: first name, last name, email, and phone number.
 *
 * While the repository is still waiting for its first users snapshot
 * ([DasCampRepository.usersLoaded] is false), a loading spinner is shown instead of the
 * form, so it doesn't flash for users who already have an account.
 *
 * The "Sign-Up" button is enabled only when a repository is present and its
 * [DasCampRepository.uid] is not empty. First name, last name, and email are required.
 * If one of those is blank when Sign-Up is pressed, that box is outlined in red and
 * nothing is saved. Phone number is optional. A successful tap trims the inputs and
 * calls [DasCampRepository.addUser] with a new [User] whose id is the repository's uid.
 *
 * This screen does not navigate after signing up. It relies on logic elsewhere
 * observing the repository, so that once the new user appears, they are routed to their
 * dashboard.
 *
 * @param navController currently unused by this screen
 * @param repository used to check loading state and to save the new user; if null, the
 * form is shown but the button is disabled
 */
@Composable
fun SignUpScreen(
    navController: NavController,
    repository: DasCampRepository? = null,
    deviceId: String = "",
    onSignedIn: (String, Boolean) -> Unit = { _, _ -> }
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var rememberDevice by rememberSaveable { mutableStateOf(true) }

    if (repository != null && !repository.usersLoaded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { CircularProgressIndicator() }
        return
    }

    val firstMissing = showErrors && firstName.isBlank()
    val lastMissing = showErrors && lastName.isBlank()
    val emailMissing = showErrors && email.isBlank()
    val phoneNumberMissing = showErrors && phoneNumber.isBlank()
    val context = LocalContext.current
    val thisDevice = deviceId.ifBlank { repository?.uid.orEmpty() }
    val storedId = DeviceSession.rememberedUserId(context)
    val remembered = when {
        repository == null -> null
        storedId != null -> repository.users.find { it.id == storedId }
        else -> soleProfileOnDevice(repository.users, thisDevice)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (remembered == null) "Create your profile" else "Welcome back",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = rememberDevice, onCheckedChange = { rememberDevice = it })
            Text("Remember this device", style = MaterialTheme.typography.bodyLarge)
        }

        if (remembered != null) {
            val label = remembered.firstName.ifBlank { remembered.displayName() }.ifBlank { "Profile" }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    releaseDevice(repository, thisDevice, remembered.id)
                    onSignedIn(remembered.id, rememberDevice)
                }
            ) { Text("Continue as $label") }
            Text("Or create a new profile", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.padding(8.dp))

        SignupField(
            label = "First Name",
            value = firstName,
            onValueChange = { firstName = it },
            showError = firstMissing,
            errorText = "Enter your first name"
        )
        SignupField(
            label = "Last Name",
            value = lastName,
            onValueChange = { lastName = it },
            showError = lastMissing,
            errorText = "Enter your last name"
        )
        SignupField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            showError = emailMissing,
            errorText = "Enter your email",
            keyboardType = KeyboardType.Email
        )
        SignupField(
            label = "Phone Number",
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            showError = phoneNumberMissing,
            errorText = "Enter your phone number",
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            enabled = repository != null && repository.uid.isNotEmpty(),
            onClick = {
                val ready = firstName.isNotBlank() && lastName.isNotBlank() &&
                    email.isNotBlank()
                showErrors = !ready
                if (repository != null && ready) {
                    val taken = repository.users.any { it.id == thisDevice }
                    val id = if (taken) java.util.UUID.randomUUID().toString() else thisDevice
                    releaseDevice(repository, thisDevice, id)
                    repository.addUser(
                        User(
                            id = id,
                            deviceId = thisDevice,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            phoneNumber = phoneNumber.trim(),
                            entrant = true
                        )
                    )
                    onSignedIn(id, rememberDevice)
                }
            }
        ) { Text(if (remembered == null) "Sign-Up" else "Create profile") }
    }
    }
}

/**
 * The one profile still tied to this phone, when the phone has not stored a claim yet.
 * If a second profile was created on the same phone, that later profile is the one kept.
 */
private fun soleProfileOnDevice(users: List<User>, deviceId: String): User? {
    if (deviceId.isBlank()) return null
    val tied = users.filter { it.deviceId == deviceId || it.id == deviceId }
    if (tied.size <= 1) return tied.firstOrNull()
    val later = tied.filter { it.id != deviceId }
    return if (later.size == 1) later.first() else null
}

/** Drops every other profile's claim on this phone. */
private fun releaseDevice(repository: DasCampRepository?, deviceId: String, keepUserId: String) {
    if (repository == null || deviceId.isBlank()) return
    repository.users.filter { user ->
        user.id != keepUserId && user.deviceId == deviceId
    }.forEach { previous ->
        repository.updateUser(previous.copy(deviceId = ""))
    }
}

/**
 * One labeled signup box. [showError] turns the outline red until the user types something.
 */
@Composable
private fun SignupField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    errorText: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val textColor = MaterialTheme.colorScheme.onSurface
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = textColor)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    if (state.isFocused) keyboard?.show()
                },
            singleLine = true,
            isError = showError,
            supportingText = { if (showError) Text(errorText) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    DasCampTheme {
        val mockNavController = rememberNavController() // for previewing
        SignUpScreen(mockNavController)
    }
}