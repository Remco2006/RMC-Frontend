package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.compose.components.AuthHeader
import com.example.rmcfrontend.compose.components.GradientBackground

@Composable
fun MapScreen() {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AuthHeader(
                title = "Map",
                subtitle = "This tab is ready for a real map integration.",
                icon = Icons.Outlined.Map
            )

            Spacer(Modifier.height(16.dp))

            ElevatedCard(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Next step: choose a map provider",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Options: Google Maps Compose (API key) or OpenStreetMap. Tell me which one you want and I will implement it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AssistChip(onClick = { }, label = { Text("Google Maps") })
                        AssistChip(onClick = { }, label = { Text("OpenStreetMap") })
                    }
                }
            }
        }
    }
}
