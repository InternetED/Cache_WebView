package com.interneted.cache_webview

import android.annotation.TargetApi
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.InputStream

/**
 * Created by yale on 2018/7/16.
 */
class WebViewCacheInterceptorInst : WebViewRequestInterceptor {
    private var mInterceptor: WebViewRequestInterceptor? = null
    fun init(builder: WebViewCacheInterceptor.Builder?) {
        if (builder != null) {
            mInterceptor = builder.build()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun interceptRequest(request: WebResourceRequest): WebResourceResponse? {
        return if (mInterceptor == null) {
            null
        } else mInterceptor!!.interceptRequest(request)
    }

    override fun interceptRequest(url: String): WebResourceResponse? {
        return if (mInterceptor == null) {
            null
        } else mInterceptor!!.interceptRequest(url)
    }


    override fun clearCache() {
        if (mInterceptor == null) {
            return
        }
        mInterceptor!!.clearCache()
    }

    override fun enableForce(force: Boolean) {
        if (mInterceptor == null) {
            return
        }
        mInterceptor!!.enableForce(force)
    }

    override fun getCacheFile(url: String): InputStream? {
        return if (mInterceptor == null) {
            null
        } else mInterceptor!!.getCacheFile(url)
    }

    override fun initAssetsData() {
        AssetsLoader.instance!!.initData()
    }


    companion object {
        private val webViewCacheInterceptorInst: WebViewCacheInterceptorInst by lazy { WebViewCacheInterceptorInst() }

        fun getInstance(): WebViewCacheInterceptorInst {
            return webViewCacheInterceptorInst
        }
    }
}