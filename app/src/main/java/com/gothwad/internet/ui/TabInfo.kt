package com.gothwad.internet.ui

data class TabInfo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Home",
    val url: String = "file:///android_asset/start-page/index.html",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false
)
