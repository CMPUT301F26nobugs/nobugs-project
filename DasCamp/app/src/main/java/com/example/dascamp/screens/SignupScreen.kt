package com.example.dascamp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.screens.entrantScreens.EntrantRoutes
import com.example.dascamp.ui.theme.DasCampTheme
import com.example.dascamp.model.DasCampRepository
import com.example.dascamp.model.Role
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
    repository: DasCampRepository? = null
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign-Up Page",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

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
            errorText = "",
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            enabled = repository != null && repository.uid.isNotEmpty(),
            onClick = {
                val ready = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank()
                showErrors = !ready
                if (repository != null && ready) {
                    repository.addUser(
                        User(
                            id = repository.uid,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim(),
                            phoneNumber = phoneNumber.trim()
                        )
                    )
                    // no navigate: the role appears and the effect at the top takes over
                }
            }
        ) { Text("Sign-Up") }
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