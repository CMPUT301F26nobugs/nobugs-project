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
import com.example.dascamp.model.EntrantProfile
import com.example.dascamp.model.Role
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.ownerScreens.OwnerRoutes
import com.example.dascamp.model.Session

// SIGN-UP SCREEN
@Composable
fun SignUpScreen(
    navController: NavController,
    repository: DasCampRepository? = null,
    session: Session? = null
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    // Returning users skip this screen
    LaunchedEffect(session?.activeRole) {
        val route = when (session?.activeRole) {
            Role.ENTRANT -> EntrantRoutes.DASHBOARD
            Role.OWNER -> OwnerRoutes.DASHBOARD
            Role.ADMIN -> AdminRoutes.DASHBOARD
            null -> return@LaunchedEffect
        }
        navController.navigate(route) { popUpTo("signUpScreen") { inclusive = true } }
    }

    // Still waiting for the first Firestore snapshots: don't flash the form
    val ready = session != null && repository != null &&
            session.uid.isNotEmpty() && repository.rolesLoaded
    if (session != null && !ready) {
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
                    enabled = session != null,
                    onClick = {
                        if (repository != null && session != null &&
                            firstName.isNotBlank() && email.isNotBlank()
                        ) {
                            repository.addEntrant(
                                EntrantProfile(
                                    id = session.uid,
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