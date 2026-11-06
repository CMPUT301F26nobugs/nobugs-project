package com.example.dascamp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.R
import com.example.dascamp.model.Role
import com.example.dascamp.model.User
import com.example.dascamp.screens.adminScreens.AdminRoutes
import com.example.dascamp.screens.entrantScreens.EntrantNavBar
import com.example.dascamp.screens.entrantScreens.EntrantRoutes
import com.example.dascamp.screens.ownerScreens.OwnerRoutes
import com.example.dascamp.ui.theme.DasCampTheme

/**
 * Reusable screen layout with the app header on top and a navigation bar on the bottom.
 * Each role's page (e.g. `EntrantPage`, `AdminPage`) builds on this, supplying its own
 * navigation bar.
 *
 * The header shows the app name (from `R.string.app_name`), an optional back arrow,
 * and an optional role switcher.
 *
 * @param onBackClick called when the back arrow is tapped; the arrow is only shown
 * when this is non-null
 * @param navBar the bottom navigation bar; empty by default
 * @param roleMenu the top-right slot, normally a [RoleMenu]; empty by default
 * @param content the screen's content, given the scaffold's [PaddingValues] (the space
 * taken by the header and navigation bar) so it can avoid drawing underneath them
 */
@Composable
fun AppScaffold(
    onBackClick: (() -> Unit)? = null,  // default is null, so arrow only shows when you pass a function
    navBar: @Composable () -> Unit = {}, // navbar slot, put your navbar function here
    roleMenu: @Composable () -> Unit = {}, // top-right slot, put RoleMenu here
    content: @Composable (PaddingValues) -> Unit // this is the content of the screen, it takes the scaffolds padding values (i.e. how much space the header/navbar takes)
) {
    val appTitle = stringResource(R.string.app_name) // references: res/values/strings.xml

    Scaffold(
        topBar = { Header(appTitle, onBackClick, roleMenu) },
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

/**
 * Reusable top bar: an optional back arrow on the left, the app title in the
 * center, and a slot for the role switcher on the right. It sits below the status bar.
 *
 * @param appTitle the title shown in the center
 * @param onBackClick called when the back arrow is tapped; the arrow is hidden if null
 * @param roleMenu content for the right-hand slot, normally a [RoleMenu]; empty by default
 */
@Composable
fun Header(
    appTitle: String,
    onBackClick: (() -> Unit)?,
    roleMenu: @Composable () -> Unit = {}
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

            // App title
            Text(
                text = appTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
            )

            // Container for role switcher
            Box(Modifier.align(Alignment.CenterEnd)) { roleMenu() }
        }
    }
}

/**
 * The signed-in [User], made available to any composable below the point where it is provided.
 *
 * Defaults to `null` (no user). [RoleMenu] reads this to decide which roles to offer, so it
 * must be supplied higher up with `CompositionLocalProvider(LocalCurrentUser provides user)`;
 * otherwise the role menu silently stays hidden.
 */
val LocalCurrentUser = compositionLocalOf<User?> { null }

/**
 * Header toggle for switching between the roles this user holds
 * Hidden when the user has only one role
 *
 * Reads the user from [LocalCurrentUser]. Tapping the icon opens a dropdown of the user's
 * roles, with the [current] one in bold. Choosing a different role navigates to that role's
 * dashboard and clears the back stack (`popUpTo(0)`), so back doesn't return to the old role.
 *
 * Nothing is shown if there is no user or the user holds fewer than two roles.
 *
 * @param navController used to navigate to the chosen role's dashboard
 * @param current the role of the screen this menu is shown on
 */
@Composable
fun RoleMenu(navController: NavController, current: Role) {
    val user = LocalCurrentUser.current ?: return
    val options = listOf(
        Triple(Role.ENTRANT, user.entrant, EntrantRoutes.DASHBOARD),
        Triple(Role.OWNER, user.owner, OwnerRoutes.DASHBOARD),
        Triple(Role.ADMIN, user.admin, AdminRoutes.DASHBOARD)
    ).filter { it.second }

    android.util.Log.d("RoleMenu", "user=${LocalCurrentUser.current}")

    if (options.size < 2) return

    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Switch role",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (role, _, route) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            role.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontWeight = if (role == current) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        open = false
                        if (role != current) {
                            navController.navigate(route) { popUpTo(0) }
                        }
                    }
                )
            }
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