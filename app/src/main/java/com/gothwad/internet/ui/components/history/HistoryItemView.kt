package com.gothwad.internet.ui.components.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.gothwad.internet.data.HistoryEntity

@Composable
fun HistoryItemView(
    item: HistoryEntity,
    onSelectHistory: (String) -> Unit,
    onDeleteHistory: (Int) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingContent = {
            Text(
                text = item.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History Item"
            )
        },
        trailingContent = {
            IconButton(onClick = { onDeleteHistory(item.id) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Item"
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectHistory(item.url) }
    )
}
