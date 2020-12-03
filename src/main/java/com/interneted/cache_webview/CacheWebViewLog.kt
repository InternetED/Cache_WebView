package com.interneted.cache_webview

import android.util.Log

/**
 * Created by yale on 2017/9/15.
 */
internal object CacheWebViewLog {
    private const val TAG = "CacheWebView"
    fun d(log: String?) {
        Log.d(TAG, log!!)
    }

    fun d(log: String?, show: Boolean) {
        if (show) {
            d(log)
        }
    }
}