package com.gothwad.internet.data

import android.webkit.JavascriptInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class BrowserJavascriptInterface(
    private val repository: BrowserRepository,
    private val onActiveSearchBar: () -> Unit,
    private val onLaunchQrScan: () -> Unit,
    private val onClickQaItem: (String) -> Unit
) {

    @JavascriptInterface
    fun clickQaItem(itemId: String) {
        onClickQaItem(itemId)
    }

    @JavascriptInterface
    fun getStringResource(name: String): String {
        return when (name) {
            "web_str_save" -> "Save"
            "web_str_delete" -> "Delete"
            "web_str_cancel" -> "Cancel"
            else -> ""
        }
    }

    @JavascriptInterface
    fun activeSearchBar() {
        onActiveSearchBar()
    }

    @JavascriptInterface
    fun launchQrScan() {
        onLaunchQrScan()
    }

    @JavascriptInterface
    fun getStartPageLogo(): String {
        return runBlocking(Dispatchers.IO) {
            repository.getPreference("start_page_logo", "")
        }
    }

    @JavascriptInterface
    fun getBooleanPreference(key: String): Boolean {
        val defaultVal = when (key) {
            "show-qa-icons" -> "true"
            "hide-start-page-logo" -> "false"
            "hide-add-qa-btn" -> "false"
            else -> "false"
        }
        val value = runBlocking(Dispatchers.IO) {
            repository.getPreference(key, defaultVal)
        }
        return value.lowercase() == "true"
    }

    @JavascriptInterface
    fun getStartPageBg(): String {
        return runBlocking(Dispatchers.IO) {
            repository.getPreference("start_page_bg", "")
        }
    }

    @JavascriptInterface
    fun getUserQaItems(parent: String): String {
        val parentId = if (parent.isEmpty()) "root" else parent
        return runBlocking(Dispatchers.IO) {
            val items = repository.getQuickAccessByParent(parentId)
            val jsonArray = JSONArray()
            for (item in items) {
                val jsonObject = JSONObject()
                jsonObject.put("id", item.id)
                jsonObject.put("order", item.order)
                jsonObject.put("type", item.type)
                jsonObject.put("parent", item.parent)
                jsonObject.put("title", item.title)
                jsonObject.put("icon_uri", item.iconUri)
                if (item.extraJson.isNotEmpty()) {
                    jsonObject.put("extra", JSONObject(item.extraJson))
                }
                jsonArray.put(jsonObject)
            }
            jsonArray.toString()
        }
    }

    @JavascriptInterface
    fun addNewQaItem(jsonDataString: String) {
        runBlocking(Dispatchers.IO) {
            try {
                val json = JSONObject(jsonDataString)
                val id = json.optString("id")
                val order = json.optInt("order")
                val type = json.optInt("type")
                val parent = json.optString("parent", "root")
                val title = json.optString("title")
                val iconUri = json.optString("icon_uri")
                val extraJson = json.opt("extra")?.toString() ?: ""
                
                val entity = QuickAccessEntity(
                    id = id,
                    order = order,
                    type = type,
                    parent = parent,
                    title = title,
                    iconUri = iconUri,
                    extraJson = extraJson
                )
                repository.addQuickAccess(entity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun removeQaItem(id: String) {
        runBlocking(Dispatchers.IO) {
            repository.removeQuickAccess(id)
        }
    }

    @JavascriptInterface
    fun updateUserQaItems(jsonDataString: String) {
        runBlocking(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray(jsonDataString)
                val entities = mutableListOf<QuickAccessEntity>()
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val id = json.optString("id")
                    val order = json.optInt("order")
                    val type = json.optInt("type")
                    val parent = json.optString("parent", "root")
                    val title = json.optString("title")
                    val iconUri = json.optString("icon_uri")
                    val extraJson = json.opt("extra")?.toString() ?: ""
                    
                    entities.add(
                        QuickAccessEntity(
                            id = id,
                            order = order,
                            type = type,
                            parent = parent,
                            title = title,
                            iconUri = iconUri,
                            extraJson = extraJson
                        )
                    )
                }
                repository.addQuickAccessList(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun setStartPageLogo(base64Image: String) {
        runBlocking(Dispatchers.IO) {
            repository.putPreference("start_page_logo", base64Image)
        }
    }

    @JavascriptInterface
    fun cleanStartPageLogo() {
        runBlocking(Dispatchers.IO) {
            repository.deletePreference("start_page_logo")
        }
    }

    @JavascriptInterface
    fun setStartPageBg(base64Image: String) {
        runBlocking(Dispatchers.IO) {
            repository.putPreference("start_page_bg", base64Image)
        }
    }

    @JavascriptInterface
    fun cleanStartPageBg() {
        runBlocking(Dispatchers.IO) {
            repository.deletePreference("start_page_bg")
        }
    }

    @JavascriptInterface
    fun isLightStartPageTheme(): Boolean {
        val value = runBlocking(Dispatchers.IO) {
            repository.getPreference("light_theme", "true")
        }
        return value.lowercase() == "true"
    }
}
