package com.example.dascamp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * [DasCampRepository.uid] is not empty. When tapped, it does nothing unless first name,
 * last name, and email are all non-blank; otherwise it trims the inputs and calls
 * [DasCampRepository.addUser] with a new [User] whose id is the repository's uid.
 * The phone number is optional.
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
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Waiting for the first users snapshot: don't flash the form
    if (repository != null && !repository.usersLoaded) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { CircularProgressIndicator() }
        return
    }

    // Screen Parent Container
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Sign-up Info Container
        Card(
            modifier = Modifier
                .width(400.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // Background
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant   // Text/Icon color
            )
        ) {

            // Text-Fields and Button Container
            Column(
                modifier = Modifier
                    .fillMaxSize(), // fills max size of parent (i.e. card)
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically), // vertical spacing & arrangement for children
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Sign-Up Title
                Text(
                    text = "Sign-Up Page",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.padding(8.dp))

                // Parent container for first name text input + title
                Column () {
                    // First Name Input
                    Text("First Name")
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                    )
                }

                // Parent container for last name text input + title
                Column () {
                    // First Name Input
                    Text("Last Name")
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                    )
                }

                // Parent container for Email input + title
                Column () {
                    // Email input
                    Text("Email")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                    )
                }

                // Parent container for Phone Number text input + title
                Column () {
                    // Phone number input
                    Text("Phone Number")
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it }, // Add validation for digits later!!
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                // Sign-Up Button
                Button(
                    enabled = repository != null && repository.uid.isNotEmpty(),
                    onClick = {
                        if (repository != null && firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank()) {
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