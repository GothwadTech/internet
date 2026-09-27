package com.gothwad.internet.ui.components.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.gothwad.internet.data.BookmarkEntity
import com.gothwad.internet.ui.components.bookmarks.BookmarksContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksBottomSheet(
    onDismissRequest: () -> Unit,
    bookmarks: List<BookmarkEntity>,
    onSelectBookmark: (String) -> Unit,
    onDeleteBookmark: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        BookmarksContent(
            bookmarks = bookmarks,
            onSelectBookmark = onSelectBookmark,
            onDeleteBookmark = onDeleteBookmark
        )
    }
}
