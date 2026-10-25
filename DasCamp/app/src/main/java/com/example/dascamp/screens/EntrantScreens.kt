package com.example.dascamp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.ui.theme.DasCampTheme

// DASHBOARD SCREEN
@Composable
fun EntrantDashboard(navController: NavController) {

    // Screen Container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Skeleton Layout
        AppScaffold(
            navBar = { EntrantNavBar(navController) }
        ) { innerPadding ->

            // Parent container for content
            Column(
                modifier = Modifier
                    .padding(innerPadding) // scaffold's padding (header & navbar)
                    .fillMaxSize()
                    .padding(16.dp) // padding for content
            ) {

                // Title container
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back,",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "User",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }

                // Campsite button & reservations button container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Campsite list button
                    BigButton(
                        onClickAction = { navController.navigate("campsiteList") },
                        icon = Icons.Default.LocationOn,
                        iconDesc = "campsites",
                        label = "View \n Campsites"
                    )

                    Spacer(modifier = Modifier.padding(8.dp))

                    // Reservation list button
                    BigButton(
                        onClickAction = { navController.navigate("reservationList") },
                        icon = Icons.Default.DateRange,
                        iconDesc = "reservations",
                        label = "View Reservations"
                    )
                }

                // Notifications, messages, and scan QR code buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // Notification logs button
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("notificationList") }
                    ) {
                        Text("Notifications")
                    }

                    Spacer(modifier = Modifier.padding(4.dp))

                    // Message logs button
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("messageList") }
                    ) {
                        Text("Messages")
                    }

                    Spacer(modifier = Modifier.padding(4.dp))

                    // Scan QR code button
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("qrCodeScreen") }
                    ) {
                        Text("Scan QR")
                    }
                }
            }
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
fun CampsiteList(navController: NavController) {

}

// NOTIFICATION LIST SCREEN
@Composable
fun NotificationList(navController: NavController) {

}

// RESERVATION LIST SCREEN
@Composable
fun ReservationList(navController: NavController) {

}

// MESSAGE LIST SCREEN
@Composable
fun MessageList(navController: NavController) {

}

// PROFILE SCREEN
@Composable
fun ProfileScreen(navController: NavController) {

}

// QR CODE SCREEN
@Composable
fun QrCodeScreen(navController: NavController) {

}