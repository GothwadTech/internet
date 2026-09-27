package com.gothwad.internet.ui

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gothwad.internet.data.BookmarkEntity
import com.gothwad.internet.data.BrowserJavascriptInterface
import com.gothwad.internet.data.BrowserRepository
import com.gothwad.internet.data.HistoryEntity
import com.gothwad.internet.data.QuickAccessEntity
import com.gothwad.internet.ui.components.web.WebViewConfigurator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrowserViewModel(private val repository: BrowserRepository) : ViewModel() {

    private val _tabs = MutableStateFlow<List<TabInfo>>(listOf(TabInfo()))
    val tabs: StateFlow<List<TabInfo>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(_tabs.value.first().id)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    private val _addressBarText = MutableStateFlow("")
    val addressBarText: StateFlow<String> = _addressBarText.asStateFlow()

    private val _showTabsSheet = MutableStateFlow(false)
    val showTabsSheet: StateFlow<Boolean> = _showTabsSheet.asStateFlow()

    private val _showBookmarksSheet = MutableStateFlow(false)
    val showBookmarksSheet: StateFlow<Boolean> = _showBookmarksSheet.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    private val _showQrScanner = MutableStateFlow(false)
    val showQrScanner: StateFlow<Boolean> = _showQrScanner.asStateFlow()

    private val _triggerAddQaDialog = MutableStateFlow(false)
    val triggerAddQaDialog: StateFlow<Boolean> = _triggerAddQaDialog.asStateFlow()

    private val _focusAddressBarEvent = MutableSharedFlow<Unit>(replay = 0)
    val focusAddressBarEvent: SharedFlow<Unit> = _focusAddressBarEvent.asSharedFlow()

    private val webViewMap = mutableMapOf<String, WebView>()

    private val _appTheme = MutableStateFlow("system")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _webTheme = MutableStateFlow("system")
    val webTheme: StateFlow<String> = _webTheme.asStateFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .asStateFlowInViewModel(emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.allHistory
        .asStateFlowInViewModel(emptyList())

    init {
        viewModelScope.launch {
            _appTheme.value = repository.getPreference("app_theme", "system")
            _webTheme.value = repository.getPreference("web_theme", "system")
        }

        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getQuickAccessList()
            val hasGmail = list.any { it.id == "qa_gmail" }
            if (!hasGmail) {
                val defaults = listOf(
                    QuickAccessEntity(
                        id = "qa_google",
                        order = 0,
                        type = 0,
                        parent = "root",
                        title = "Google",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=google.com",
                        extraJson = """{"default_mark":"G", "background":"#4285F4", "url":"https://www.google.com"}"""
                    ),
                    QuickAccessEntity(
                        id = "qa_gmail",
                        order = 1,
                        type = 0,
                        parent = "root",
                        title = "Gmail",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=mail.google.com",
                        extraJson = """{"default_mark":"M", "background":"#EA4335", "url":"https://mail.google.com"}"""
                    ),
                    QuickAccessEntity(
                        id = "qa_github",
                        order = 2,
                        type = 0,
                        parent = "root",
                        title = "GitHub",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=github.com",
                        extraJson = """{"default_mark":"Git", "background":"#181717", "url":"https://github.com"}"""
                    ),
                    QuickAccessEntity(
                        id = "qa_youtube",
                        order = 3,
                        type = 0,
                        parent = "root",
                        title = "YouTube",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=youtube.com",
                        extraJson = """{"default_mark":"Y", "background":"#FF0000", "url":"https://www.youtube.com"}"""
                    ),
                    QuickAccessEntity(
                        id = "qa_drive",
                        order = 4,
                        type = 0,
                        parent = "root",
                        title = "Drive",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=drive.google.com",
                        extraJson = """{"default_mark":"D", "background":"#34A853", "url":"https://drive.google.com"}"""
                    ),
                    QuickAccessEntity(
                        id = "qa_notes",
                        order = 5,
                        type = 0,
                        parent = "root",
                        title = "Notes",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=keep.google.com",
                        extraJson = """{"default_mark":"N", "background":"#FBBC05", "url":"https://keep.google.com"}"""
                    ),
                    QuickAccessEntity(
                        id = "qa_wikipedia",
                        order = 6,
                        type = 0,
                        parent = "root",
                        title = "Wikipedia",
                        iconUri = "https://www.google.com/s2/favicons?sz=128&domain=wikipedia.org",
                        extraJson = """{"default_mark":"W", "background":"#000000", "url":"https://wikipedia.org"}"""
                    )
                )
                repository.addQuickAccessList(defaults)
            }
        }

        viewModelScope.launch {
            _webTheme.collect { theme ->
                webViewMap.values.forEach { webView ->
                    WebViewConfigurator.configureWebViewTheme(webView, theme, webView.context)
                }
            }
        }

        // Expose current tab URL to address bar
        viewModelScope.launch {
            _activeTabId.collect { activeId ->
                val activeTab = _tabs.value.find { it.id == activeId }
                _addressBarText.value = if (activeTab?.url == "file:///android_asset/start-page/index.html") {
                    ""
                } else {
                    activeTab?.url ?: ""
                }
            }
        }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.asStateFlowInViewModel(initialValue: T): StateFlow<T> {
        val state = MutableStateFlow(initialValue)
        viewModelScope.launch {
            collect { state.value = it }
        }
        return state.asStateFlow()
    }

    fun getActiveTab(): TabInfo? {
        return _tabs.value.find { it.id == _activeTabId.value }
    }

    fun getOrCreateWebView(tabId: String, context: Context): WebView {
        val tab = _tabs.value.find { it.id == tabId }
        val isIncognito = tab?.isIncognito == true

        return webViewMap.getOrPut(tabId) {
            WebView(context.applicationContext).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = !isIncognito
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.setSupportZoom(true)
                settings.textZoom = 100
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mediaPlaybackRequiresUserGesture = false

                if (isIncognito) {
                    settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                    settings.saveFormData = false
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }

                val cookieManager = android.webkit.CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    cookieManager.setAcceptThirdPartyCookies(this, !isIncognito)
                }

                WebViewConfigurator.configureWebViewTheme(this, _webTheme.value, context)

                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { 
                            updateTabUrl(tabId, it) 
                            if (tabId == _activeTabId.value) {
                                _addressBarText.value = if (it == "file:///android_asset/start-page/index.html") "" else it
                            }
                        }
                        updateTabLoading(tabId, true)
                        updateBackForwardState(tabId, view)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val pageTitle = view?.title ?: "Page"
                        url?.let { 
                            updateTabFinished(tabId, pageTitle, it) 
                            if (tabId == _activeTabId.value) {
                                _addressBarText.value = if (it == "file:///android_asset/start-page/index.html") "" else it
                            }
                        }
                        updateTabLoading(tabId, false)
                        updateBackForwardState(tabId, view)

                        if (url != null && !url.startsWith("file://") && !url.startsWith("about:") && !isIncognito) {
                            viewModelScope.launch {
                                repository.addHistory(pageTitle, url)
                            }
                        }
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                        val urlString = request?.url?.toString() ?: ""
                        if (urlString.startsWith("local://dlg.qa/new")) {
                            _triggerAddQaDialog.value = true
                            return true
                        }
                        return false
                    }

                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        updateBackForwardState(tabId, view)
                    }
                }

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        updateTabProgress(tabId, newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let { updateTabTitle(tabId, it) }
                    }

                    override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                        request?.let {
                            it.grant(it.resources)
                        }
                    }

                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: android.webkit.GeolocationPermissions.Callback?
                    ) {
                        callback?.invoke(origin, true, false)
                    }
                }

                addJavascriptInterface(
                    BrowserJavascriptInterface(
                        repository = repository,
                        onActiveSearchBar = {
                            triggerAddressBarFocus()
                        },
                        onLaunchQrScan = {
                            openQrScannerOverlay()
                        },
                        onClickQaItem = { itemId ->
                            viewModelScope.launch {
                                val list = repository.getQuickAccessList()
                                val entity = list.find { it.id == itemId }
                                if (entity != null) {
                                    val url = try {
                                        org.json.JSONObject(entity.extraJson).optString("url")
                                    } catch (e: Exception) {
                                        ""
                                    }
                                    val targetUrl = if (url.isNotEmpty()) url else {
                                        when (entity.id) {
                                            "qa_google" -> "https://www.google.com"
                                            "qa_gmail" -> "https://mail.google.com"
                                            "qa_github" -> "https://github.com"
                                            "qa_youtube" -> "https://www.youtube.com"
                                            "qa_drive" -> "https://drive.google.com"
                                            "qa_notes" -> "https://keep.google.com"
                                            "qa_wikipedia" -> "https://wikipedia.org"
                                            else -> ""
                                        }
                                    }
                                    if (targetUrl.isNotEmpty()) {
                                        navigateActiveTab(targetUrl)
                                    }
                                }
                            }
                        }
                    ),
                    "mbrowser"
                )

                val tab = _tabs.value.find { it.id == tabId }
                if (tab?.isDesktopMode == true) {
                    val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    settings.userAgentString = desktopUA
                } else {
                    settings.userAgentString = null
                }
                loadUrl(tab?.url ?: "file:///android_asset/start-page/index.html")
            }
        }
    }

    private fun updateBackForwardState(tabId: String, webView: WebView?) {
        webView ?: return
        val canBack = webView.canGoBack()
        val canForward = webView.canGoForward()
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) {
                it.copy(canGoBack = canBack, canGoForward = canForward)
            } else {
                it
            }
        }
    }

    private fun updateTabUrl(tabId: String, url: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(url = url) else it
        }
    }

    private fun updateTabTitle(tabId: String, title: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(title = title) else it
        }
    }

    private fun updateTabProgress(tabId: String, progress: Int) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(progress = progress) else it
        }
    }

    private fun updateTabLoading(tabId: String, isLoading: Boolean) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(isLoading = isLoading) else it
        }
    }

    private fun updateTabFinished(tabId: String, title: String, url: String) {
        _tabs.value = _tabs.value.map {
            if (it.id == tabId) it.copy(title = title, url = url, progress = 100, isLoading = false) else it
        }
    }

    fun addNewTab(url: String = "file:///android_asset/start-page/index.html", isIncognito: Boolean = false) {
        val newTab = TabInfo(
            url = url,
            title = if (url == "file:///android_asset/start-page/index.html") "Home" else "Page",
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        _showTabsSheet.value = false
    }

    fun closeAllTabs(isIncognito: Boolean) {
        val list = _tabs.value
        val toKeep = list.filter { it.isIncognito != isIncognito }
        val toClose = list.filter { it.isIncognito == isIncognito }
        
        toClose.forEach { removeWebView(it.id) }
        
        if (toKeep.isEmpty()) {
            val newTab = TabInfo(isIncognito = isIncognito)
            _tabs.value = listOf(newTab)
            _activeTabId.value = newTab.id
        } else {
            _tabs.value = toKeep
            val activeId = _activeTabId.value
            if (activeId == null || !toKeep.any { it.id == activeId }) {
                _activeTabId.value = toKeep.first().id
            }
        }
    }

    fun closeTab(tabId: String) {
        val list = _tabs.value
        if (list.size <= 1) {
            // Can't close last remaining tab, just reload it to start-page
            val lastId = list.first().id
            loadUrlInTab(lastId, "file:///android_asset/start-page/index.html")
            return
        }

        val index = list.indexOfFirst { it.id == tabId }
        val activeId = _activeTabId.value

        _tabs.value = list.filter { it.id != tabId }
        removeWebView(tabId)

        if (activeId == tabId) {
            val nextActiveIndex = if (index >= list.size - 1) index - 1 else index
            _activeTabId.value = _tabs.value[nextActiveIndex].id
        }
    }

    fun switchTab(tabId: String) {
        _activeTabId.value = tabId
        _showTabsSheet.value = false
    }

    fun navigateActiveTab(address: String) {
        val activeId = _activeTabId.value ?: return
        var targetUrl = address.trim()

        if (targetUrl.isEmpty()) return

        // Check if input is a valid URL or a search query
        val isUrl = (targetUrl.startsWith("http://") || targetUrl.startsWith("https://") || targetUrl.startsWith("file://") || targetUrl.startsWith("about:")) ||
                (targetUrl.contains(".") && !targetUrl.contains(" ") && targetUrl.indexOf(".") < targetUrl.length - 1)

        if (!isUrl) {
            // Treat as a search query via Google
            targetUrl = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(targetUrl, "UTF-8")
        } else if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://") && !targetUrl.startsWith("file://")) {
            targetUrl = "https://$targetUrl"
        }

        loadUrlInTab(activeId, targetUrl)
    }

    private fun loadUrlInTab(tabId: String, url: String) {
        val webView = webViewMap[tabId]
        if (webView != null) {
            webView.loadUrl(url)
        } else {
            // Update the tab state directly so it'll load when initialized
            _tabs.value = _tabs.value.map {
                if (it.id == tabId) it.copy(url = url, title = "Loading...") else it
            }
        }
    }

    fun goBackActiveTab() {
        val activeId = _activeTabId.value ?: return
        webViewMap[activeId]?.let {
            if (it.canGoBack()) {
                it.goBack()
            }
        }
    }

    fun goForwardActiveTab() {
        val activeId = _activeTabId.value ?: return
        webViewMap[activeId]?.let {
            if (it.canGoForward()) {
                it.goForward()
            }
        }
    }

    fun reloadActiveTab() {
        val activeId = _activeTabId.value ?: return
        webViewMap[activeId]?.reload()
    }

    fun stopActiveTab() {
        val activeId = _activeTabId.value ?: return
        webViewMap[activeId]?.stopLoading()
    }

    fun goHomeActiveTab() {
        val activeId = _activeTabId.value ?: return
        loadUrlInTab(activeId, "file:///android_asset/start-page/index.html")
    }

    fun setAppTheme(theme: String) {
        _appTheme.value = theme
        viewModelScope.launch {
            repository.putPreference("app_theme", theme)
        }
    }

    fun setWebTheme(theme: String) {
        _webTheme.value = theme
        viewModelScope.launch {
            repository.putPreference("web_theme", theme)
        }
    }

    fun setAddressBarText(text: String) {
        _addressBarText.value = text
    }

    fun toggleTabsSheet(show: Boolean) {
        _showTabsSheet.value = show
    }

    fun toggleBookmarksSheet(show: Boolean) {
        _showBookmarksSheet.value = show
    }

    fun toggleHistorySheet(show: Boolean) {
        _showHistorySheet.value = show
    }

    fun toggleQrScanner(show: Boolean) {
        _showQrScanner.value = show
    }

    fun addCurrentPageToBookmarks() {
        val activeTab = getActiveTab() ?: return
        if (activeTab.url.startsWith("file:///")) return // Don't bookmark start page
        viewModelScope.launch(Dispatchers.IO) {
            repository.addBookmark(activeTab.title, activeTab.url)
        }
    }

    fun addNewTabQuickAccess(title: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextOrder = (repository.getQuickAccessList().maxOfOrNull { it.order } ?: 0) + 1
            val item = com.gothwad.internet.data.QuickAccessEntity(
                id = java.util.UUID.randomUUID().toString(),
                order = nextOrder,
                type = 1, // regular link
                title = title,
                iconUri = "", // fallback icon will be loaded automatically on start-page
                extraJson = org.json.JSONObject().apply { put("url", url) }.toString()
            )
            repository.addQuickAccess(item)
        }
    }

    fun resetTriggerAddQaDialog() {
        _triggerAddQaDialog.value = false
    }

    fun removeBookmarkByUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeBookmarkByUrl(url)
        }
    }

    fun removeHistory(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeHistory(id)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    private fun triggerAddressBarFocus() {
        viewModelScope.launch {
            _focusAddressBarEvent.emit(Unit)
        }
    }

    private fun openQrScannerOverlay() {
        _showQrScanner.value = true
    }

    fun toggleDesktopModeActiveTab(context: Context) {
        val activeId = _activeTabId.value ?: return
        val currentTab = _tabs.value.find { it.id == activeId } ?: return
        val newDesktopMode = !currentTab.isDesktopMode
        
        _tabs.value = _tabs.value.map {
            if (it.id == activeId) it.copy(isDesktopMode = newDesktopMode) else it
        }

        val webView = webViewMap[activeId]
        if (webView != null) {
            val settings = webView.settings
            if (newDesktopMode) {
                val desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                settings.userAgentString = desktopUA
            } else {
                settings.userAgentString = null
            }
            webView.reload()
        }
    }

    fun clearBrowserData(context: Context) {
        viewModelScope.launch {
            repository.clearHistory()
            try {
                android.webkit.CookieManager.getInstance().removeAllCookies(null)
                android.webkit.WebStorage.getInstance().deleteAllData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            webViewMap.values.forEach { webView ->
                webView.clearCache(true)
                webView.clearHistory()
            }
        }
    }

    private fun removeWebView(tabId: String) {
        webViewMap.remove(tabId)?.let {
            it.stopLoading()
            it.clearHistory()
            it.destroy()
        }
    }

    override fun onCleared() {
        super.onCleared()
        webViewMap.values.forEach {
            it.stopLoading()
            it.clearHistory()
            it.destroy()
        }
        webViewMap.clear()
    }
}
