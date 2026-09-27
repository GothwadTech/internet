package com.gothwad.internet.ui.components.web

import android.content.Context
import android.webkit.WebView

object WebViewConfigurator {
    fun configureWebViewTheme(webView: WebView, webTheme: String, context: Context) {
        val settings = webView.settings
        val useDark = when (webTheme) {
            "dark" -> true
            "light" -> false
            else -> {
                val isDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                isDark
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            settings.isAlgorithmicDarkeningAllowed = useDark
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            settings.forceDark = if (useDark) android.webkit.WebSettings.FORCE_DARK_ON else android.webkit.WebSettings.FORCE_DARK_OFF
        }
    }
}
