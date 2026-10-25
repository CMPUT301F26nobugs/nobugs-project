package com.example.dascamp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

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
                    Text(text = "Welcome Back,", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "User", style = MaterialTheme.typography.titleLarge)
                }

                // Campsite button & reservations button container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Card() {
                        // View campsites button
                        Button(
                            onClick = { navController.navigate("campsiteList") }
                        ) { }
                    }
                    // View reservations button
                    Button(
                        onClick = { navController.navigate("reservationList") }
                    ) { }
                }

                // Notifications, messages, and scan QR code buttons
                Column() {
                    // Notification logs button
                    Button(
                        onClick = { navController.navigate("notificationList") }
                    ) { }

                    // Message logs button
                    Button(
                        onClick = { navController.navigate("messageList") }
                    ) { }

                    // Scan QR code button
                    Button(
                        onClick = { navController.navigate("qrCodeScreen") }
                    ) { }
                }
            }
        }
    }
}

@Preview
@Composable
fun EntrantDashboardPreview() {
    EntrantDashboard(rememberNavController())
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