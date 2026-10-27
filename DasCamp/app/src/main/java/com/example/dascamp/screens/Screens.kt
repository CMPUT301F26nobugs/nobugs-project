package com.example.dascamp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.dascamp.ui.theme.DasCampTheme


// REUSABLE SCREEN LAYOUT CALL W/ HEADER AND NAVBAR (MAKE YOUR OWN NAVBAR)
@Composable
fun AppScaffold(
    onBackClick: (() -> Unit)? = null,  // default is null, so arrow only shows when you pass a function
    navBar: @Composable () -> Unit = {}, // navbar slot, put your navbar function here
    content: @Composable (PaddingValues) -> Unit // this is the content of the screen, it takes the scaffolds padding values (i.e. how much space the header/navbar takes)
) {
    val appTitle = stringResource(R.string.app_name) // references: res/values/strings.xml

    Scaffold(
        topBar = { Header(appTitle, onBackClick) },
        bottomBar = { navBar() },
        content = content
    )
}

@Preview
@Composable
fun AppScaffoldPreview() {
    DasCampTheme {
        AppScaffold(
            onBackClick = {},
            navBar = {
                EntrantNavBar(rememberNavController())
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Screen content")
            }
        }
    }
}

// REUSABLE HEADER
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
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    .align(Alignment.Center)
            )
        }
    }
}

@Preview
@Composable
fun HeaderPreview(){
    DasCampTheme {
        Header("DasCamp", onBackClick = {})
    }
}

// ENTRANT NAVBAR
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
                onClick = { navController.navigate("entrantDashboard") }
            ) {
                Icon(
                    imageVector = Icons.Default.Home, // takes home icon from default icon library
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Campsites Button
            IconButton(
                onClick = { navController.navigate("campsiteList") }
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Campsites",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Notifications Button
            IconButton(
                onClick = { navController.navigate("notificationList") }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Messages Button
            IconButton(
                onClick = { navController.navigate("messageList") }
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Messages",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Profile Button
            IconButton(
                onClick = { navController.navigate("profileScreen") }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

}

@Preview
@Composable
fun EntrantNavBarPreview() {
    DasCampTheme {
        EntrantNavBar(rememberNavController())
    }
}

// BIG BUTTON SKELETON (can be used for big buttons on dashboards)
@Composable
fun BigButton(
    onClickAction: () -> Unit,
    icon: ImageVector,
    iconDesc: String,
    label: String
) {
    Card(
        modifier = Modifier.size(136.dp),
        onClick = { onClickAction },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                imageVector = icon,
                contentDescription = iconDesc,
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun BigButtonPreview() {
    DasCampTheme {
        BigButton({}, Icons.Default.Home, "home", "Go Home")
    }
}

@Composable
fun EmptyValue(widthFraction: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(18.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
    )
}

@Composable
fun EmptyCard(onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EmptyValue(0.7f)
            EmptyValue(0.4f)
        }
    }
}


// SIGN-UP SCREEN
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
    DasCampTheme {
        val mockNavController = rememberNavController() // for previewing
        SignUpScreen(mockNavController)
    }
}

