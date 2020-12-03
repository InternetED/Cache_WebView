package com.interneted.cache_webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import java.io.File
import java.io.InputStream

/**
 * Created by yale on 2018/7/13.
 */
interface WebViewRequestInterceptor {
    fun interceptRequest(request: WebResourceRequest): WebResourceResponse?
    fun interceptRequest(url: String): WebResourceResponse?
    fun clearCache()
    fun enableForce(force: Boolean)
    fun getCacheFile(url: String): InputStream?
    fun initAssetsData()
}