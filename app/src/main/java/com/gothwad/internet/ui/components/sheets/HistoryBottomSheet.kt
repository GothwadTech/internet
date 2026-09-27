package com.gothwad.internet.ui.components.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gothwad.internet.data.HistoryEntity
import com.gothwad.internet.ui.components.history.HistoryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    onDismissRequest: () -> Unit,
    history: List<HistoryEntity>,
    onSelectHistory: (String) -> Unit,
    onDeleteHistory: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        HistoryContent(
            history = history,
            onSelectHistory = onSelectHistory,
            onDeleteHistory = onDeleteHistory,
            onClearAll = onClearAll
        )
    }
}
