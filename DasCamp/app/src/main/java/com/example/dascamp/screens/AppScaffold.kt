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

// REUSABLE SCREEN LAYOUT CALL W/ HEADER AND NAVBAR (MAKE YOUR OWN NAVBAR)
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

// REUSABLE HEADER
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

val LocalCurrentUser = compositionLocalOf<User?> { null }

/**
 * Header toggle for switching between the roles this user holds
 * Hidden when the user has only one role
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