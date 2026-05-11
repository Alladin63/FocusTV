package com.focustv.app.core

import android.net.Uri

object SourceDetector {
    fun detect(rawUrl: String, username: String = "", password: String = ""): SourceType {
        val url = rawUrl.trim().lowercase()
        if (url.contains("get.php") || url.contains("player_api.php")) return SourceType.XTREAM
        if (url.endsWith(".m3u") || url.endsWith(".m3u8") || url.contains("#extm3u")) return SourceType.M3U
        if (url.contains("/c/") || url.contains("stalker") || url.contains("portal.php")) return SourceType.STALKER
        if (username.isNotBlank() && password.isNotBlank()) return SourceType.XTREAM
        return SourceType.AUTO
    }

    fun xtreamBaseUrl(input: String): String {
        val clean = input.trim().removeSuffix("/")
        if (clean.contains("player_api.php") || clean.contains("get.php")) {
            val uri = Uri.parse(clean)
            val scheme = uri.scheme ?: "http"
            val authority = uri.authority ?: return clean
            return "$scheme://$authority"
        }
        return clean
    }
}
