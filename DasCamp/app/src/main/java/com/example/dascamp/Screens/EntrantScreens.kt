package com.example.dascamp.Screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ENTRANT DASHBOARD SCREEN
@Composable
fun EntrantDashboard(navController: NavController) {

    // Screen Container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AppScaffold(
            onBackClick = { navController.popBackStack() },
            navBar = {
                EntrantNavBar(
                    onHomeClick = { navController.navigate("home") },
                    onCampsitesClick = { navController.navigate("campsites") },
                    onNotificationsClick = { navController.navigate("notifications") },
                    onMessagesClick = { navController.navigate("messages") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
        ) {

        }
    }
}

@Preview
@Composable
fun EntrantDashboardPreview() {
    val mockNavController = rememberNavController() // for previewing
    EntrantDashboard(mockNavController)
}

