package com.gothwad.internet.ui.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gothwad.internet.ui.TabInfo

@Composable
fun TabsManagementContent(
    tabs: List<TabInfo>,
    activeTabId: String?,
    onSwitchTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onCloseAllTabs: (Boolean) -> Unit
) {
    var isIncognitoModeSelected by remember { mutableStateOf(false) }
    
    val filteredTabs = tabs.filter { it.isIncognito == isIncognitoModeSelected }
    
    val isDark = isIncognitoModeSelected
    val containerBg = if (isDark) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface
    val textPrimary = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val textSecondary = if (isDark) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .background(containerBg)
            .padding(16.dp)
    ) {
        // Mode Selector: Normal vs Incognito
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDark) Color(0xFF2E2E2E) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(24.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Normal tab button
            val normalCount = tabs.count { !it.isIncognito }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (!isIncognitoModeSelected) (if (isDark) Color.DarkGray else MaterialTheme.colorScheme.primary) else Color.Transparent)
                    .clickable { isIncognitoModeSelected = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = if (!isIncognitoModeSelected) (if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary) else textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Standard ($normalCount)",
                        color = if (!isIncognitoModeSelected) (if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary) else textSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Incognito tab button
            val incognitoCount = tabs.count { it.isIncognito }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isIncognitoModeSelected) Color(0xFF3C4043) else Color.Transparent)
                    .clickable { isIncognitoModeSelected = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🕶️ Incognito ($incognitoCount)",
                        color = if (isIncognitoModeSelected) Color.White else textSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header Row with title and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isIncognitoModeSelected) "Incognito Tabs" else "Standard Tabs",
                color = textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (filteredTabs.isNotEmpty()) {
                    IconButton(
                        onClick = { onCloseAllTabs(isIncognitoModeSelected) },
                        modifier = Modifier.testTag("btn_close_all_tabs")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Close All Tabs",
                            tint = if (isDark) Color(0xFFF28B82) else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                IconButton(
                    onClick = { onNewTab(isIncognitoModeSelected) },
                    modifier = Modifier.testTag("btn_new_tab_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab",
                        tint = if (isDark) Color(0xFF8AB4F8) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (filteredTabs.isEmpty()) {
            // Show empty placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isIncognitoModeSelected) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("🕶️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You've gone Incognito",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Now you can browse privately, and other people who use this device won't see your activity. However, downloads, bookmarks and reading list items will still be saved.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onNewTab(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8AB4F8), contentColor = Color.Black)
                        ) {
                            Text("Create Incognito Tab", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "No standard tabs open",
                            color = textSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onNewTab(false) }) {
                            Text("Create Tab")
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTabs) { tab ->
                    val isActive = tab.id == activeTabId
                    TabItemView(
                        tab = tab,
                        isActive = isActive,
                        onSwitchTab = onSwitchTab,
                        onCloseTab = onCloseTab
                    )
                }
            }
        }
    }
}
