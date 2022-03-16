package com.interneted.cache_webview.config

import android.text.TextUtils
import java.util.*

/**
 * Created by yale on 2017/9/26.
 */
class CacheExtensionConfig {
    //单独webview实例的
    private val statics: HashSet<String> = HashSet(STATIC)
    private val no_cache: HashSet<String> = HashSet(NO_CACH)
    fun isMedia(extension: String): Boolean {
        if (TextUtils.isEmpty(extension)) {
            return false
        }
        return if (NO_CACH.contains(extension)) {
            true
        } else no_cache.contains(
            extension.lowercase(Locale.getDefault()).trim { it <= ' ' })
    }

    fun canCache(extension: String): Boolean {
        if (TextUtils.isEmpty(extension)) {
            return false
        }
        val extension2 = extension.lowercase(Locale.getDefault()).trim { it <= ' ' }
        return if (STATIC.contains(extension2)) {
            true
        } else statics.contains(extension2)
    }

    fun addExtension(extension: String): CacheExtensionConfig {
        add(statics, extension)
        return this
    }

    fun removeExtension(extension: String): CacheExtensionConfig {
        remove(statics, extension)
        return this
    }

    fun isHtml(extension: String): Boolean {
        if (TextUtils.isEmpty(extension)) {
            return false
        }
        val extensionName = extension.lowercase(Locale.getDefault())

        return extensionName.contains("html") ||
                extensionName.contains("htm")
    }

    fun clearAll() {
        clearDiskExtension()
    }

    fun clearDiskExtension() {
        statics.clear()
    }

    companion object {
        //全局默认的
        private val STATIC: HashSet<String> = object : HashSet<String>() {
            init {
                add("html")
                add("htm")
                add("js")
                add("ico")
                add("css")
                add("png")
                add("jpg")
                add("jpeg")
                add("gif")
                add("bmp")
                add("ttf")
                add("woff")
                add("woff2")
                add("otf")
                add("eot")
                add("svg")
                add("xml")
                add("swf")
                add("txt")
                add("text")
                add("conf")
                add("webp")
            }
        }
        private val NO_CACH: HashSet<String> = object : HashSet<String>() {
            init {
                add("mp4")
                add("mp3")
                add("ogg")
                add("avi")
                add("wmv")
                add("flv")
                add("rmvb")
                add("3gp")
            }
        }

        fun addGlobalExtension(extension: String) {
            add(STATIC, extension)
        }

        fun removeGlobalExtension(extension: String) {
            remove(STATIC, extension)
        }

        private fun add(set: HashSet<String>, extension: String) {
            if (TextUtils.isEmpty(extension)) {
                return
            }
            set.add(extension.replace(".", "").lowercase(Locale.getDefault()).trim { it <= ' ' })
        }

        private fun remove(set: HashSet<String>, extension: String) {
            if (TextUtils.isEmpty(extension)) {
                return
            }
            set.remove(extension.replace(".", "").lowercase(Locale.getDefault()).trim { it <= ' ' })
        }
    }
}