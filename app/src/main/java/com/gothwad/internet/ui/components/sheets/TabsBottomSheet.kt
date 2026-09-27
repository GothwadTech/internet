package com.gothwad.internet.ui.components.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gothwad.internet.ui.TabInfo
import com.gothwad.internet.ui.components.tabs.TabsManagementContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsBottomSheet(
    onDismissRequest: () -> Unit,
    tabs: List<TabInfo>,
    activeTabId: String?,
    onSwitchTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onCloseAllTabs: (Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        TabsManagementContent(
            tabs = tabs,
            activeTabId = activeTabId,
            onSwitchTab = onSwitchTab,
            onCloseTab = onCloseTab,
            onNewTab = onNewTab,
            onCloseAllTabs = onCloseAllTabs
        )
    }
}
