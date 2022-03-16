package com.interneted.cache_webview

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.interneted.cache_webview.config.CacheExtensionConfig
import com.interneted.cache_webview.utils.FileUtil
import com.interneted.cache_webview.utils.MimeTypeMapUtils
import com.interneted.cache_webview.utils.NetUtils
import com.interneted.cache_webview.utils.OKHttpFile
import okhttp3.Cache
import okhttp3.CacheControl.Companion.FORCE_CACHE
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by yale on 2018/7/13.
 */
class WebViewCacheInterceptor(builder: Builder) : WebViewRequestInterceptor {
    private val mCacheFile: File
    private val mCacheSize: Long
    private val mConnectTimeout: Long
    private val mReadTimeout: Long
    private val mCacheExtensionConfig: CacheExtensionConfig
    private val mContext: Context
    private val mDebug: Boolean
    private var mCacheType: CacheType
    private var mAssetsDir: String? = null
    private var mTrustAllHostname = false
    private var mSSLSocketFactory: SSLSocketFactory? = null
    private var mX509TrustManager: X509TrustManager? = null
    private var mDns: Dns? = null
    private val mResourceInterceptor: ResourceInterceptor?
    private var mIsSuffixMod = false

    //==============
    private var mHttpClient: OkHttpClient? = null
    private var mOrigin = ""
    private var mReferer: String? = ""
    private var mUserAgent = ""
    private val isEnableAssets: Boolean
        get() = mAssetsDir != null

    private fun initAssetsLoader() {

        AssetsLoader.instance!!.init(mContext).setDir(mAssetsDir)
            .isAssetsSuffixMod(mIsSuffixMod)
    }

    private fun initHttpClient() {

        val cache = Cache(mCacheFile, mCacheSize)
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(mConnectTimeout, TimeUnit.SECONDS)
            .readTimeout(mReadTimeout, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpCacheInterceptor())
        if (mTrustAllHostname) {
            builder.hostnameVerifier({ _, _ -> true })
        }
        if (mSSLSocketFactory != null && mX509TrustManager != null) {
            builder.sslSocketFactory(mSSLSocketFactory!!, mX509TrustManager!!)
        }
        if (mDns != null) {
            builder.dns(mDns!!)
        }
        mHttpClient = builder.build()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun interceptRequest(request: WebResourceRequest): WebResourceResponse? {
        return interceptRequest(request.url.toString(), request.requestHeaders)
    }

    private fun buildHeaders(): MutableMap<String, String?> {
        val headers: MutableMap<String, String?> = HashMap()
        if (!TextUtils.isEmpty(mOrigin)) {
            headers["Origin"] = mOrigin
        }
        if (!TextUtils.isEmpty(mReferer)) {
            headers["Referer"] = mReferer
        }
        if (!TextUtils.isEmpty(mUserAgent)) {
            headers["User-Agent"] = mUserAgent
        }
        return headers
    }

    override fun interceptRequest(url: String): WebResourceResponse? {
        return interceptRequest(url, buildHeaders())
    }

    private fun checkUrl(url: String): Boolean {
        if (TextUtils.isEmpty(url)) {
            return false
        }
        //okhttp only deal with http[s]
        if (!url.startsWith("http")) {
            return false
        }
        if (mResourceInterceptor != null && !mResourceInterceptor.interceptor(url)) {
            return false
        }
        if (url.contains(Regex(".*.ettoday.net"))) {
            return true
        }

        val extension: String = MimeTypeMapUtils.getFileExtensionFromUrl(url)
        if (TextUtils.isEmpty(extension)) {
            return false
        }
        if (mCacheExtensionConfig.isMedia(extension)) {
            return false
        }
        return mCacheExtensionConfig.canCache(extension)
    }


    override fun clearCache() {
        FileUtil.deleteDirs(mCacheFile.absolutePath, false)
        AssetsLoader.instance!!.clear()
    }

    override fun enableForce(force: Boolean) {
        mCacheType = if (force) {
            CacheType.FORCE
        } else {
            CacheType.NORMAL
        }
    }

    override fun getCacheFile(url: String): InputStream? {
        return OKHttpFile.getCacheFile(mCacheFile, url)
    }

    override fun initAssetsData() {
        AssetsLoader.instance!!.initData()
    }


    fun addHeader(reqBuilder: Request.Builder, headers: MutableMap<String, String?>?) {
        if (headers == null) {
            return
        }
        for ((key, value) in headers) {
            if (value != null) {
                reqBuilder.addHeader(key, value)
            }
        }
    }

    private fun interceptRequest(
        url: String,
        headers: MutableMap<String, String?>
    ): WebResourceResponse? {
        if (mCacheType == CacheType.NORMAL) {
            return null
        }
        if (!checkUrl(url)) {
            return null
        }
        if (isEnableAssets) {
            val inputStream: InputStream? = AssetsLoader.instance!!.getResByUrl(url)
            if (inputStream != null) {
                CacheWebViewLog.d(String.format("from assets: %s", url), mDebug)
                val mimeType: String = MimeTypeMapUtils.getMimeTypeFromUrl(url)
                return WebResourceResponse(mimeType, "", inputStream)
            }
        }
        try {
            val reqBuilder: Request.Builder = Request.Builder()
                .url(url)
            val extension: String = MimeTypeMapUtils.getFileExtensionFromUrl(url)
            if (mCacheExtensionConfig.isHtml(extension)) {
                headers[KEY_CACHE] = mCacheType.ordinal.toString() + ""
            }
            addHeader(reqBuilder, headers)
            if (!NetUtils.isConnected(mContext)) {
                reqBuilder.cacheControl(FORCE_CACHE)
            }
            val request: Request = reqBuilder.build()
            val response = mHttpClient!!.newCall(request).execute()
            val cacheRes = response.cacheResponse
            if (cacheRes != null) {
                CacheWebViewLog.d(String.format("from cache: %s", url), mDebug)
            } else {
                CacheWebViewLog.d(String.format("from server: %s", url), mDebug)
            }
            val mimeType: String = MimeTypeMapUtils.getMimeTypeFromUrl(url).run {
                if (this.isEmpty()) {
                    val contentType = response.header("Content-Type", "") ?: ""
                    val split = contentType.split(";")
                    val mimeType =
                        if (split.isNotEmpty()) {
                            split[0]
                        } else {
                            ""
                        }
                    mimeType
                } else {
                    this
                }
            }
            val webResourceResponse =
                WebResourceResponse(mimeType, "", response.body!!.byteStream())
            if (response.code == 504 && !NetUtils.isConnected(mContext)) {
                return null
            }

            var message = response.message
            if (TextUtils.isEmpty(message)) {
                message = "OK"
            }
            try {
                webResourceResponse.setStatusCodeAndReasonPhrase(response.code, message)
            } catch (e: Exception) {
                return null
            }
            webResourceResponse.responseHeaders =
                NetUtils.multimapToSingle(response.headers.toMultimap())

            return webResourceResponse
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    class Builder(val mContext: Context) {
        var mCacheFile: File
        var mCacheSize = (100 * 1024 * 1024).toLong()
        var mConnectTimeout: Long = 20
        var mReadTimeout: Long = 20
        var mCacheExtensionConfig: CacheExtensionConfig
        var mDebug = true
        var mCacheType = CacheType.FORCE
        var mTrustAllHostname = false
        var mSSLSocketFactory: SSLSocketFactory? = null
        var mX509TrustManager: X509TrustManager? = null
        var mResourceInterceptor: ResourceInterceptor? = null
        var mAssetsDir: String? = null
        var mIsSuffixMod = false
        var mDns: Dns? = null
        fun setResourceInterceptor(resourceInterceptor: ResourceInterceptor?) {
            mResourceInterceptor = resourceInterceptor
        }

        fun setTrustAllHostname(): Builder {
            mTrustAllHostname = true
            return this
        }

        fun setSSLSocketFactory(
            sslSocketFactory: SSLSocketFactory?,
            trustManager: X509TrustManager?
        ): Builder {
            if (sslSocketFactory != null && trustManager != null) {
                mSSLSocketFactory = sslSocketFactory
                mX509TrustManager = trustManager
            }
            return this
        }

        fun setCachePath(file: File?): Builder {
            if (file != null) {
                mCacheFile = file
            }
            return this
        }

        fun setCacheSize(cacheSize: Long): Builder {
            if (cacheSize > 1024) {
                mCacheSize = cacheSize
            }
            return this
        }

        fun setReadTimeoutSecond(time: Long): Builder {
            if (time >= 0) {
                mReadTimeout = time
            }
            return this
        }

        fun setConnectTimeoutSecond(time: Long): Builder {
            if (time >= 0) {
                mConnectTimeout = time
            }
            return this
        }

        fun setCacheExtensionConfig(config: CacheExtensionConfig?): Builder {
            if (config != null) {
                mCacheExtensionConfig = config
            }
            return this
        }

        fun setDebug(debug: Boolean): Builder {
            mDebug = debug
            return this
        }

        fun setCacheType(cacheType: CacheType): Builder {
            mCacheType = cacheType
            return this
        }

        fun isAssetsSuffixMod(suffixMod: Boolean): Builder {
            mIsSuffixMod = suffixMod
            return this
        }

        fun setAssetsDir(dir: String?): Builder {
            if (dir != null) {
                mAssetsDir = dir
            }
            return this
        }

        fun setDns(dns: Dns?) {
            mDns = dns
        }

        fun build(): WebViewRequestInterceptor {
            return WebViewCacheInterceptor(this)
        }

        init {
            mCacheFile = File(mContext.cacheDir.toString(), "CacheWebViewCache")
            mCacheExtensionConfig = CacheExtensionConfig()
        }
    }

    fun isValidUrl(url: String?): Boolean {
        return URLUtil.isValidUrl(url)
    }

    companion object {
        const val KEY_CACHE = "WebResourceInterceptor-Key-Cache"
    }

    init {
        mCacheExtensionConfig = builder.mCacheExtensionConfig
        mCacheFile = builder.mCacheFile
        mCacheSize = builder.mCacheSize
        mCacheType = builder.mCacheType
        mConnectTimeout = builder.mConnectTimeout
        mReadTimeout = builder.mReadTimeout
        mContext = builder.mContext
        mDebug = builder.mDebug
        mAssetsDir = builder.mAssetsDir
        mX509TrustManager = builder.mX509TrustManager
        mSSLSocketFactory = builder.mSSLSocketFactory
        mTrustAllHostname = builder.mTrustAllHostname
        mResourceInterceptor = builder.mResourceInterceptor
        mIsSuffixMod = builder.mIsSuffixMod
        mDns = builder.mDns
        initHttpClient()
        if (isEnableAssets) {
            initAssetsLoader()
        }
    }
}