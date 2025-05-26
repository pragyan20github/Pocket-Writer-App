package com.pocketwriter.app.util

fun resolveImageUrl(url: String): String {
    return if (url.startsWith("http")) url else "http://10.0.2.2:8080$url"
}
