package com.example.dascamp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dascamp.model.Role
import com.example.dascamp.model.User
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.entrantScreens.EntrantRoutes
import com.example.dascamp.screens.ownerScreens.OwnerRoutes
import com.example.dascamp.ui.theme.DasCampTheme

/**
 * A column of buttons for jumping to the other dashboards this user is allowed to use.
 *
 * One full-width "Switch to <role> view" button is shown for each role the user holds
 * other than [current]. Tapping one navigates to that role's dashboard and pops any
 * earlier copy of that dashboard off the back stack. Nothing is shown if [user] is null
 * or holds no other roles.
 *
 * @param navController used to navigate to the chosen dashboard
 * @param user the signed-in user, or null if there isn't one
 * @param current the role of the screen this is shown on, which is left out of the buttons
 */
@Composable
fun RoleSwitcher(navController: NavController, user: User?, current: Role) {
    if (user == null) return
    val targets = listOf(
        Triple(Role.ENTRANT, user.entrant, EntrantRoutes.DASHBOARD),
        Triple(Role.OWNER, user.owner, OwnerRoutes.DASHBOARD),
        Triple(Role.ADMIN, user.admin, AdminRoutes.DASHBOARD)
    )
    targets.forEach { (role, has, route) ->
        if (has && role != current) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(route) { popUpTo(route) { inclusive = true } } }
            ) { Text("Switch to ${role.name.lowercase()} view") }
        }
    }
}

/**
 * A large square card button with an icon above a centered label, intended for the
 * main actions on dashboards.
 *
 * @param onClickAction called when the card is tapped
 * @param icon the icon shown above the label
 * @param iconDesc accessibility description of the icon
 * @param label the text under the icon; use `\n` for a manual line break
 */
@Composable
fun BigButton(
    onClickAction: () -> Unit,
    icon: ImageVector,
    iconDesc: String,
    label: String
) {
    Card(
        modifier = Modifier.size(150.dp),
        onClick = onClickAction,
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

/**
 * A rounded gray bar used as a placeholder for text that is empty or still loading.
 *
 * @param widthFraction fraction of the available width the bar fills, from 0 to 1;
 * defaults to the full width
 */
@Composable
fun EmptyValue(widthFraction: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(18.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
    )
}

/**
 * A clickable placeholder card made of two [EmptyValue] bars, used where a list item
 * will eventually go.
 *
 * @param onClick called when the card is tapped
 */
@Composable
fun EmptyCard(onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EmptyValue(0.7f)
            EmptyValue(0.4f)
        }
    }
}