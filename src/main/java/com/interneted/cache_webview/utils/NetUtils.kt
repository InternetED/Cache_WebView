package com.interneted.cache_webview.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import java.net.URL
import java.util.*

/**
 * Created by yale on 2017/9/15.
 */
object NetUtils {
    fun isConnected(context: Context): Boolean {
        val cm = context.applicationContext
            .getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        return info != null && info.isConnected
    }

    fun getOriginUrl(referer: String?): String? {
        var ou = referer
        if (TextUtils.isEmpty(ou)) {
            return ""
        }
        try {
            val url = URL(ou)
            val port = url.port
            ou = url.protocol + "://" + url.host + if (port == -1) "" else ":$port"
        } catch (e: Exception) {
        }
        return ou
    }

    fun multimapToSingle(maps: Map<String, List<String>?>): Map<String, String> {
        val sb = StringBuilder()
        val map: MutableMap<String, String> = HashMap()
        for ((key, values) in maps) {
            sb.delete(0, sb.length)
            if (values != null && values.size > 0) {
                for (v in values) {
                    sb.append(v)
                    sb.append(";")
                }
            }
            if (sb.length > 0) {
                sb.deleteCharAt(sb.length - 1)
            }
            map[key] = sb.toString()
        }
        return map
    }
}