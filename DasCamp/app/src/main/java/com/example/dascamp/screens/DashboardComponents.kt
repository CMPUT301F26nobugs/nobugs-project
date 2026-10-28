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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dascamp.ui.theme.DasCampTheme


// BIG BUTTON SKELETON (can be used for big buttons on dashboards)
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

// PLACEHOLDER BAR FOR EMPTY/LOADING STATES
@Composable
fun EmptyValue(widthFraction: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(18.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
    )
}

// PLACEHOLDER CARD BUILT FROM EmptyValue BARS
@Composable
fun EmptyCard(onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EmptyValue(0.7f)
            EmptyValue(0.4f)
        }
    }
}