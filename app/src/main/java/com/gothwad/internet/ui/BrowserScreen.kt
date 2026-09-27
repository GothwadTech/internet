package com.gothwad.internet.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gothwad.internet.ui.components.topbar.TopBar
import com.gothwad.internet.ui.components.sheets.TabsBottomSheet
import com.gothwad.internet.ui.components.sheets.BookmarksBottomSheet
import com.gothwad.internet.ui.components.sheets.HistoryBottomSheet
import com.gothwad.internet.ui.components.dialogs.*
import com.gothwad.internet.ui.components.qrcode.QrScannerOverlay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val addressText by viewModel.addressBarText.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val webTheme by viewModel.webTheme.collectAsState()

    val showTabsSheet by viewModel.showTabsSheet.collectAsState()
    val showBookmarksSheet by viewModel.showBookmarksSheet.collectAsState()
    val showHistorySheet by viewModel.showHistorySheet.collectAsState()
    val showQrScanner by viewModel.showQrScanner.collectAsState()

    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()

    val activeTab = tabs.find { it.id == activeTabId }
    val focusRequester = remember { FocusRequester() }

    var isBookmarked by remember(activeTab?.url, bookmarks) {
        mutableStateOf(bookmarks.any { it.url == activeTab?.url })
    }

    val triggerAddQaDialog by viewModel.triggerAddQaDialog.collectAsState()
    var showTopMenu by remember { mutableStateOf(false) }

    // Intercept hardware/system back button for Chrome-like page navigation
    BackHandler(enabled = activeTab?.canGoBack == true) {
        viewModel.goBackActiveTab()
    }

    // Add to QA state
    var showAddQaDialog by remember { mutableStateOf(false) }
    var qaTitleInput by remember { mutableStateOf("") }
    var qaUrlInput by remember { mutableStateOf("") }

    LaunchedEffect(triggerAddQaDialog) {
        if (triggerAddQaDialog) {
            qaTitleInput = ""
            qaUrlInput = ""
            showAddQaDialog = true
            viewModel.resetTriggerAddQaDialog()
        }
    }

    // Find in page state
    var findInPageActive by remember { mutableStateOf(false) }
    var findInPageQuery by remember { mutableStateOf("") }

    // Site Settings state
    var showSiteSettingsDialog by remember { mutableStateOf(false) }
    var siteJsEnabled by remember { mutableStateOf(true) }
    var siteZoomEnabled by remember { mutableStateOf(true) }
    var siteDomStorageEnabled by remember { mutableStateOf(true) }

    // Media Sniffer state
    var showMediaSnifferDialog by remember { mutableStateOf(false) }
    var sniffedMediaList by remember { mutableStateOf<List<String>>(emptyList()) }

    // Page Resources state
    var showResourcesDialog by remember { mutableStateOf(false) }
    var resourcesList by remember { mutableStateOf<List<String>>(emptyList()) }

    // Source Code state
    var showSourceCodeDialog by remember { mutableStateOf(false) }
    var sourceCodeText by remember { mutableStateOf("") }

    // Developer Tools state
    var showDevTools by remember { mutableStateOf(false) }
    var devToolsLog by remember { mutableStateOf<List<String>>(listOf("Console initialized. Execute any JS command below.")) }
    var devToolsInput by remember { mutableStateOf("") }

    // Text To Speech state
    var showTtsControls by remember { mutableStateOf(false) }
    var isTtsPlaying by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }

    // QR Code Dialog state
    var showQrCodeDialog by remember { mutableStateOf(false) }

    // Settings Dialog state
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Initialize TextToSpeech safely
    LaunchedEffect(Unit) {
        val instance = android.speech.tts.TextToSpeech(context) { _ -> }
        ttsInstance = instance
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    // Listen to active search bar trigger event from Javascript start-page
    LaunchedEffect(Unit) {
        viewModel.focusAddressBarEvent.collectLatest {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    TopBar(
                        addressText = addressText,
                        onAddressTextChange = { viewModel.setAddressBarText(it) },
                        focusRequester = focusRequester,
                        onSearchSubmit = {
                            viewModel.navigateActiveTab(addressText)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        onHomeClick = { viewModel.goHomeActiveTab() },
                        onNewTabClick = { viewModel.addNewTab() },
                        onTabsClick = { viewModel.toggleTabsSheet(true) },
                        tabsCount = tabs.size,
                        onMoreClick = { showTopMenu = true },
                        activeTab = activeTab
                    )

                    // Find in Page row
                    if (findInPageActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = findInPageQuery,
                                onValueChange = { query ->
                                    findInPageQuery = query
                                    activeTabId?.let { id ->
                                        viewModel.getOrCreateWebView(id, context).findAllAsync(query)
                                    }
                                },
                                placeholder = { Text("Find on page...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("find_in_page_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    activeTabId?.let { id ->
                                        viewModel.getOrCreateWebView(id, context).findNext(false)
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous")
                            }
                            IconButton(
                                onClick = {
                                    activeTabId?.let { id ->
                                        viewModel.getOrCreateWebView(id, context).findNext(true)
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                            }
                            IconButton(
                                onClick = {
                                    findInPageActive = false
                                    findInPageQuery = ""
                                    activeTabId?.let { id ->
                                        viewModel.getOrCreateWebView(id, context).clearMatches()
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                }

                // Chrome 3-Dot Overflow Menu Dropdown
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp,
                            end = 8.dp
                        )
                ) {
                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .widthIn(min = 280.dp, max = 320.dp)
                    ) {
                        val activeUrl = activeTab?.url ?: ""
                        val isStartPage = activeUrl.startsWith("file:///")

                        // --- Quick Action Row: Back, Forward, Bookmark, Download, Reload ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Back
                            IconButton(
                                onClick = {
                                    showTopMenu = false
                                    viewModel.goBackActiveTab()
                                },
                                enabled = activeTab?.canGoBack == true,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("menu_action_back")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (activeTab?.canGoBack == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 2. Forward
                            IconButton(
                                onClick = {
                                    showTopMenu = false
                                    viewModel.goForwardActiveTab()
                                },
                                enabled = activeTab?.canGoForward == true,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("menu_action_forward")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Forward",
                                    tint = if (activeTab?.canGoForward == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 3. Bookmark
                            IconButton(
                                onClick = {
                                    showTopMenu = false
                                    if (isBookmarked) {
                                        activeTab?.url?.let { viewModel.removeBookmarkByUrl(it) }
                                    } else {
                                        viewModel.addCurrentPageToBookmarks()
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("menu_action_bookmark")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark Page",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 4. Download / Save Offline
                            IconButton(
                                onClick = {
                                    showTopMenu = false
                                    if (isStartPage) {
                                        android.widget.Toast.makeText(context, "Cannot save start page.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        activeTabId?.let { id ->
                                            val wv = viewModel.getOrCreateWebView(id, context)
                                            val safeTitle = (activeTab?.title ?: "SavedPage").replace(Regex("[^a-zA-Z0-9]"), "_")
                                            val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                                            val file = java.io.File(dir, "$safeTitle.mhtml")
                                            wv.saveWebArchive(file.absolutePath, false) { path ->
                                                if (path != null) {
                                                    android.widget.Toast.makeText(context, "Page saved offline!", android.widget.Toast.LENGTH_LONG).show()
                                                } else {
                                                    android.widget.Toast.makeText(context, "Failed to save page.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("menu_action_download")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Save Offline",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // 5. Reload / Stop (moved from top header to 3-dot menu)
                            IconButton(
                                onClick = {
                                    showTopMenu = false
                                    if (activeTab?.isLoading == true) {
                                        viewModel.stopActiveTab()
                                    } else {
                                        viewModel.reloadActiveTab()
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("menu_action_reload")
                            ) {
                                Icon(
                                    imageVector = if (activeTab?.isLoading == true) Icons.Default.Close else Icons.Default.Refresh,
                                    contentDescription = if (activeTab?.isLoading == true) "Stop" else "Reload",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // --- 1. New Tab ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("New tab", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                viewModel.addNewTab(isIncognito = false)
                            },
                            modifier = Modifier.testTag("chrome_menu_new_tab")
                        )

                        // --- 2. New Incognito Tab ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("New Incognito tab", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                viewModel.addNewTab(isIncognito = true)
                            },
                            modifier = Modifier.testTag("chrome_menu_new_incognito")
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // --- 3. History ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("History", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                viewModel.toggleHistorySheet(true)
                            },
                            modifier = Modifier.testTag("chrome_menu_history")
                        )

                        // --- 4. Downloads ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Downloads", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                try {
                                    val intent = android.content.Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS)
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Downloads directory", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("chrome_menu_downloads")
                        )

                        // --- 5. Bookmarks ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Bookmarks", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                viewModel.toggleBookmarksSheet(true)
                            },
                            modifier = Modifier.testTag("chrome_menu_bookmarks")
                        )

                        // --- 6. Recent Tabs ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tab,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Recent tabs", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                viewModel.toggleTabsSheet(true)
                            },
                            modifier = Modifier.testTag("chrome_menu_recent_tabs")
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // --- 7. Share... ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Share...", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                activeTab?.url?.let { url ->
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, url)
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, activeTab.title)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Share Link"))
                                }
                            },
                            modifier = Modifier.testTag("chrome_menu_share")
                        )

                        // --- 8. Find in Page ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Find in page", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                findInPageActive = true
                                findInPageQuery = ""
                            },
                            modifier = Modifier.testTag("chrome_menu_find_in_page")
                        )

                        // --- 9. Translate... ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Translate...", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                activeTab?.url?.let { url ->
                                    val translateUrl = "https://translate.google.com/translate?sl=auto&tl=en&u=${android.net.Uri.encode(url)}"
                                    viewModel.navigateActiveTab(translateUrl)
                                }
                            },
                            modifier = Modifier.testTag("chrome_menu_translate")
                        )

                        // --- 10. Listen to this page (Read Aloud) ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Listen to this page", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                activeTabId?.let { id ->
                                    val wv = viewModel.getOrCreateWebView(id, context)
                                    wv.evaluateJavascript("document.body.innerText") { textResult ->
                                        val text = if (textResult != null && textResult.startsWith("\"") && textResult.endsWith("\"")) {
                                            textResult.substring(1, textResult.length - 1)
                                                .replace("\\n", "\n")
                                                .replace("\\\"", "\"")
                                                .replace("\\\\", "\\")
                                        } else {
                                            textResult ?: ""
                                        }
                                        if (text.trim().isNotEmpty()) {
                                            ttsInstance?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                            isTtsPlaying = true
                                            showTtsControls = true
                                        } else {
                                            android.widget.Toast.makeText(context, "No readable text found.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("chrome_menu_read_aloud")
                        )

                        // --- 11. Add to Quick Access ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Add to Quick Access", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                qaTitleInput = activeTab?.title ?: ""
                                qaUrlInput = activeTab?.url ?: ""
                                showAddQaDialog = true
                            },
                            modifier = Modifier.testTag("chrome_menu_add_qa")
                        )

                        // --- 12. Desktop Site ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Desktop site", fontSize = 14.sp) },
                            trailingIcon = {
                                Checkbox(
                                    checked = activeTab?.isDesktopMode == true,
                                    onCheckedChange = {
                                        showTopMenu = false
                                        viewModel.toggleDesktopModeActiveTab(context)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showTopMenu = false
                                viewModel.toggleDesktopModeActiveTab(context)
                            },
                            modifier = Modifier.testTag("chrome_menu_desktop_site")
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // --- 13. Scan QR Code ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Scan QR Code", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                viewModel.toggleQrScanner(true)
                            },
                            modifier = Modifier.testTag("chrome_menu_scan_qr")
                        )

                        // --- 14. Generate QR Code ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Generate QR Code", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                showQrCodeDialog = true
                            },
                            modifier = Modifier.testTag("chrome_menu_generate_qr")
                        )

                        // --- 15. Developer Tools ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Developer Tools", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                showDevTools = true
                            },
                            modifier = Modifier.testTag("chrome_menu_dev_tools")
                        )

                        // --- 16. View Page Source ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("View Page Source", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                activeTabId?.let { id ->
                                    val wv = viewModel.getOrCreateWebView(id, context)
                                    wv.evaluateJavascript("(function() { return document.documentElement.outerHTML; })()") { jsonResult ->
                                        val cleaned = if (jsonResult != null && jsonResult.startsWith("\"") && jsonResult.endsWith("\"")) {
                                            jsonResult.substring(1, jsonResult.length - 1)
                                                .replace("\\u003C", "<")
                                                .replace("\\u003E", ">")
                                                .replace("\\u0022", "\"")
                                                .replace("\\u0027", "'")
                                                .replace("\\u0026", "&")
                                                .replace("\\n", "\n")
                                                .replace("\\r", "\r")
                                                .replace("\\t", "\t")
                                                .replace("\\\"", "\"")
                                                .replace("\\\\", "\\")
                                        } else {
                                            jsonResult ?: ""
                                        }
                                        sourceCodeText = cleaned
                                        showSourceCodeDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.testTag("chrome_menu_source_code")
                        )

                        // --- 17. Site Settings ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Site Settings", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                activeTabId?.let { id ->
                                    val wv = viewModel.getOrCreateWebView(id, context)
                                    siteJsEnabled = wv.settings.javaScriptEnabled
                                    siteZoomEnabled = wv.settings.supportZoom()
                                    siteDomStorageEnabled = wv.settings.domStorageEnabled
                                }
                                showSiteSettingsDialog = true
                            },
                            modifier = Modifier.testTag("chrome_menu_site_settings")
                        )

                        // --- 18. Sniff Media Resources ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text("Sniff Media Resources", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                activeTabId?.let { id ->
                                    val wv = viewModel.getOrCreateWebView(id, context)
                                    val script = """
                                        (function() {
                                            var media = [];
                                            var imgs = document.getElementsByTagName('img');
                                            for (var i = 0; i < imgs.length; i++) { if (imgs[i].src && imgs[i].src.startsWith('http')) media.push(imgs[i].src); }
                                            var videos = document.getElementsByTagName('video');
                                            for (var i = 0; i < videos.length; i++) { if (videos[i].src && videos[i].src.startsWith('http')) media.push(videos[i].src); }
                                            var audios = document.getElementsByTagName('audio');
                                            for (var i = 0; i < audios.length; i++) { if (audios[i].src && audios[i].src.startsWith('http')) media.push(audios[i].src); }
                                            var sources = document.getElementsByTagName('source');
                                            for (var i = 0; i < sources.length; i++) { if (sources[i].src && sources[i].src.startsWith('http')) media.push(sources[i].src); }
                                            return JSON.stringify(media);
                                        })()
                                    """.trimIndent()
                                    wv.evaluateJavascript(script) { jsonResult ->
                                        try {
                                            val cleaned = if (jsonResult != null && jsonResult.startsWith("\"") && jsonResult.endsWith("\"")) {
                                                jsonResult.substring(1, jsonResult.length - 1)
                                                    .replace("\\\"", "\"")
                                                    .replace("\\\\", "\\")
                                            } else {
                                                jsonResult ?: "[]"
                                            }
                                            val array = org.json.JSONArray(cleaned)
                                            val list = mutableListOf<String>()
                                            for (i in 0 until array.length()) {
                                                list.add(array.getString(i))
                                            }
                                            sniffedMediaList = list.distinct()
                                            showMediaSnifferDialog = true
                                        } catch (e: Exception) {
                                            sniffedMediaList = emptyList()
                                            showMediaSnifferDialog = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.testTag("chrome_menu_sniff_media")
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // --- 19. Settings ---
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            text = { Text("Settings", fontSize = 14.sp) },
                            onClick = {
                                showTopMenu = false
                                showSettingsDialog = true
                            },
                            modifier = Modifier.testTag("chrome_menu_settings")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (activeTabId != null) {
                AndroidView(
                    factory = { ctx ->
                        android.widget.FrameLayout(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { container ->
                        container.removeAllViews()
                        val activeId = activeTabId
                        if (activeId != null) {
                            val wv = viewModel.getOrCreateWebView(activeId, container.context)
                            wv.layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                            container.addView(wv)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Floating TTS media controls strip
            if (showTtsControls) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF23252E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "TTS Playing",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reading Webpage Content",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isTtsPlaying) "Playing..." else "Paused",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    if (isTtsPlaying) {
                                        ttsInstance?.stop()
                                        isTtsPlaying = false
                                    } else {
                                        activeTabId?.let { id ->
                                            val wv = viewModel.getOrCreateWebView(id, context)
                                            wv.evaluateJavascript("document.body.innerText") { textResult ->
                                                val text = if (textResult != null && textResult.startsWith("\"") && textResult.endsWith("\"")) {
                                                    textResult.substring(1, textResult.length - 1)
                                                        .replace("\\n", "\n")
                                                        .replace("\\\"", "\"")
                                                        .replace("\\\\", "\\")
                                                } else {
                                                    textResult ?: ""
                                                }
                                                if (text.trim().isNotEmpty()) {
                                                    ttsInstance?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                                    isTtsPlaying = true
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isTtsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {
                                    ttsInstance?.stop()
                                    isTtsPlaying = false
                                    showTtsControls = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sheets Section

    if (showTabsSheet) {
        TabsBottomSheet(
            onDismissRequest = { viewModel.toggleTabsSheet(false) },
            tabs = tabs,
            activeTabId = activeTabId,
            onSwitchTab = { viewModel.switchTab(it) },
            onCloseTab = { viewModel.closeTab(it) },
            onNewTab = { isIncognito ->
                viewModel.addNewTab(isIncognito = isIncognito)
                viewModel.toggleTabsSheet(false)
            },
            onCloseAllTabs = { isIncognito ->
                viewModel.closeAllTabs(isIncognito)
            }
        )
    }



    if (showBookmarksSheet) {
        BookmarksBottomSheet(
            onDismissRequest = { viewModel.toggleBookmarksSheet(false) },
            bookmarks = bookmarks,
            onSelectBookmark = { url ->
                viewModel.navigateActiveTab(url)
                viewModel.toggleBookmarksSheet(false)
            },
            onDeleteBookmark = { viewModel.removeBookmarkByUrl(it) }
        )
    }

    if (showHistorySheet) {
        HistoryBottomSheet(
            onDismissRequest = { viewModel.toggleHistorySheet(false) },
            history = history,
            onSelectHistory = { url ->
                viewModel.navigateActiveTab(url)
                viewModel.toggleHistorySheet(false)
            },
            onDeleteHistory = { viewModel.removeHistory(it) },
            onClearAll = { viewModel.clearSearchHistory() }
        )
    }

    // Dialogs Section
    if (showAddQaDialog) {
        AddQaDialog(
            qaTitleInput = qaTitleInput,
            onQaTitleChange = { qaTitleInput = it },
            qaUrlInput = qaUrlInput,
            onQaUrlChange = { qaUrlInput = it },
            onDismissRequest = { showAddQaDialog = false },
            onAddClick = {
                if (qaTitleInput.trim().isNotEmpty() && qaUrlInput.trim().isNotEmpty()) {
                    viewModel.addNewTabQuickAccess(qaTitleInput, qaUrlInput)
                    showAddQaDialog = false
                }
            }
        )
    }

    if (showSiteSettingsDialog) {
        SiteSettingsDialog(
            siteJsEnabled = siteJsEnabled,
            onJsEnabledChange = { siteJsEnabled = it },
            siteZoomEnabled = siteZoomEnabled,
            onZoomEnabledChange = { siteZoomEnabled = it },
            siteDomStorageEnabled = siteDomStorageEnabled,
            onDomStorageEnabledChange = { siteDomStorageEnabled = it },
            onDismissRequest = { showSiteSettingsDialog = false },
            onApplyAndReload = {
                activeTabId?.let { id ->
                    val wv = viewModel.getOrCreateWebView(id, context)
                    wv.settings.javaScriptEnabled = siteJsEnabled
                    wv.settings.setSupportZoom(siteZoomEnabled)
                    wv.settings.builtInZoomControls = siteZoomEnabled
                    wv.settings.displayZoomControls = false
                    wv.settings.domStorageEnabled = siteDomStorageEnabled
                    wv.reload()
                }
                showSiteSettingsDialog = false
            }
        )
    }

    if (showMediaSnifferDialog) {
        MediaSnifferDialog(
            sniffedMediaList = sniffedMediaList,
            onDismissRequest = { showMediaSnifferDialog = false },
            onCopyClick = { mediaUrl ->
                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clipData = android.content.ClipData.newPlainText("Media URL", mediaUrl)
                clipboardManager.setPrimaryClip(clipData)
                android.widget.Toast.makeText(context, "URL Copied!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showResourcesDialog) {
        PageResourcesDialog(
            resourcesList = resourcesList,
            onDismissRequest = { showResourcesDialog = false },
            onCopyClick = { resItem ->
                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clipData = android.content.ClipData.newPlainText("Resource URL", resItem.substringAfter(": "))
                clipboardManager.setPrimaryClip(clipData)
                android.widget.Toast.makeText(context, "URL Copied!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSourceCodeDialog) {
        SourceCodeDialog(
            sourceCodeText = sourceCodeText,
            onDismissRequest = { showSourceCodeDialog = false },
            onCopyClick = {
                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clipData = android.content.ClipData.newPlainText("Source Code", sourceCodeText)
                clipboardManager.setPrimaryClip(clipData)
                android.widget.Toast.makeText(context, "Entire Source Copied!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDevTools) {
        DevToolsDialog(
            devToolsLog = devToolsLog,
            devToolsInput = devToolsInput,
            onInputChange = { devToolsInput = it },
            onDismissRequest = { showDevTools = false },
            onRunClick = {
                if (devToolsInput.trim().isNotEmpty()) {
                    val cmd = devToolsInput
                    devToolsLog = devToolsLog + "> $cmd"
                    activeTabId?.let { id ->
                        viewModel.getOrCreateWebView(id, context).evaluateJavascript(cmd) { result ->
                            devToolsLog = devToolsLog + "< $result"
                        }
                    }
                    devToolsInput = ""
                }
            }
        )
    }

    if (showQrCodeDialog) {
        QrCodeDialog(
            activeTab = activeTab,
            onDismissRequest = { showQrCodeDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            appTheme = appTheme,
            onAppThemeChange = { viewModel.setAppTheme(it) },
            webTheme = webTheme,
            onWebThemeChange = { viewModel.setWebTheme(it) },
            onClearBrowserData = {
                viewModel.clearBrowserData(context)
                android.widget.Toast.makeText(context, "Browsing history and caches cleared!", android.widget.Toast.LENGTH_LONG).show()
            },
            onDismissRequest = { showSettingsDialog = false }
        )
    }

    if (showQrScanner) {
        QrScannerOverlay(
            onClose = { viewModel.toggleQrScanner(false) },
            onQrCodeDetected = { scannedUrl ->
                viewModel.navigateActiveTab(scannedUrl)
                viewModel.toggleQrScanner(false)
            }
        )
    }
}
