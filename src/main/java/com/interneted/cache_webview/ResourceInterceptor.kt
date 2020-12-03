package com.interneted.cache_webview

/**
 * Created by yale on 2018/9/20.
 */
interface ResourceInterceptor {
    fun interceptor(url: String?): Boolean
}