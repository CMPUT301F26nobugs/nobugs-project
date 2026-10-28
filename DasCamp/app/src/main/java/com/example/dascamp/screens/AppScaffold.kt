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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.dascamp.R
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