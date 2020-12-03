package com.interneted.cache_webview.utils

import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.net.URLConnection

/**
 * Created by yale on 2018/1/9.
 */
object MimeTypeMapUtils {
    fun getFileExtensionFromUrl(url: String): String {

        return MimeTypeMap.getFileExtensionFromUrl(url)
    }

    fun getMimeTypeFromUrl(url: String): String {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtensionFromUrl(url))?:""
    }

    fun getMimeTypeFromExtension(extension: String?): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?:""
    }
}