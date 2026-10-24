package com.example.dascamp.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.waterfallPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.dascamp.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

// Scaffold for screens, use this for screens w/ header and navbar!
@Composable
fun AppScaffold(
    onBackClick: (() -> Unit)? = null,
    showNavBar: Boolean = true,
    NavBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val appTitle = stringResource(R.string.app_name) // references: res/values/strings.xml

    Scaffold(
        topBar = { Header(appTitle, onBackClick) },
        bottomBar = { if (showNavBar) NavBar() },
        content = content
    )
}

// Header
@Composable
fun Header(
    appTitle: String,
    onBackClick: (() -> Unit)?
) {
    // Parent for header
    Surface(color = MaterialTheme.colorScheme.primary) {

        // Parent for header contents
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Text(
                text = appTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally as Alignment)
            )
        }
    }
}

@Preview
@Composable
fun HeaderPreview(){
    Header("DasCamp", onBackClick = {null})
}

// Navbar

// Sign-Up Screen
@Composable
fun SignUpScreen(navController: NavController) {

    // Screen Parent Container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Sign-up Info Container
        Card(
            modifier = Modifier
                .size(width = 350.dp, height = 600.dp)
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
                    var firstName by remember { mutableStateOf("") }
                    Text("First Name")
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                    )
                }

                // Parent container for last name text input + title
                Column () {
                    // First Name Input
                    var lastName by remember { mutableStateOf("") }
                    Text("Last Name")
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                    )
                }

                // Parent container for Email input + title
                Column () {
                    // Email input
                    var email by remember { mutableStateOf("") }
                    Text("Email")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                    )
                }

                // Parent container for Phone Number text input + title
                Column () {
                    // Phone number input
                    var phoneNumber by remember { mutableStateOf("") }
                    Text("Phone Number")
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it }, // Add validation for digits later!!
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                // Sign-Up Button
                Button (
                    onClick = { navController.navigate("EntrantDashboard") } // navigate to Entrant Dashboard
                ) {
                    Text("Sign-Up")
                }
            }
        }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    val mockNavController = rememberNavController() // for previewing
    SignUpScreen(mockNavController)
}