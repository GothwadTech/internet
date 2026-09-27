package com.gothwad.internet.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SiteSettingsDialog(
    siteJsEnabled: Boolean,
    onJsEnabledChange: (Boolean) -> Unit,
    siteZoomEnabled: Boolean,
    onZoomEnabledChange: (Boolean) -> Unit,
    siteDomStorageEnabled: Boolean,
    onDomStorageEnabledChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onApplyAndReload: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Site Settings",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable JavaScript", color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = siteJsEnabled, onCheckedChange = onJsEnabledChange)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Zoom Support", color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = siteZoomEnabled, onCheckedChange = onZoomEnabledChange)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable DOM Storage", color = MaterialTheme.colorScheme.onSurface)
                    Switch(checked = siteDomStorageEnabled, onCheckedChange = onDomStorageEnabledChange)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onApplyAndReload) {
                        Text("Apply & Reload")
                    }
                }
            }
        }
    }
}
