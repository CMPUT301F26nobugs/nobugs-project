package com.example.dascamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dascamp.ui.theme.DasCampTheme

/**
 * The app's single activity. It draws edge to edge (behind the status and navigation
 * bars) and hosts the whole Compose UI: [DasCampTheme] wrapped around [DasCampApp],
 * which owns navigation for every screen.
 *
 * Insets are handled by the screens themselves (`Header` uses `statusBarsPadding()`
 * and the nav bars use `navigationBarsPadding()`).
 */
class MainActivity : ComponentActivity() {
    /**
     * Enables edge-to-edge drawing and sets the Compose content.
     *
     * @param savedInstanceState the saved state from a previous instance, if any
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DasCampTheme {
                DasCampApp()
            }
        }
    }
}
