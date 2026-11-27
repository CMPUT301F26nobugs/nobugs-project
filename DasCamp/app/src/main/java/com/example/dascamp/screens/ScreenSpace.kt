package com.example.dascamp.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Padding that grows on wider screens so phones stay compact and tablets are not cramped.
 */
@Composable
fun screenPadding(): Dp {
    val width = LocalConfiguration.current.screenWidthDp
    return when {
        width >= 840 -> 32.dp
        width >= 600 -> 24.dp
        else -> 16.dp
    }
}

/** Keeps a page from stretching edge to edge on a tablet. */
val contentMaxWidth: Dp = 720.dp
