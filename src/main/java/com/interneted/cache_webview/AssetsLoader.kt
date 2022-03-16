package com.interneted.cache_webview

import android.content.Context
import android.text.TextUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by yale on 2018/7/16.
 */
internal class AssetsLoader {
    private var mContext: Context? = null
    private var mAssetResSet: CopyOnWriteArraySet<String>? = null
    private var mDir: String? = ""
    private var mCleared = false
    private var mIsSuffixMod = false
    fun isAssetsSuffixMod(suffixMod: Boolean): AssetsLoader {
        mIsSuffixMod = suffixMod
        return this
    }

    fun init(context: Context?): AssetsLoader {
        mContext = context
        mAssetResSet = CopyOnWriteArraySet()
        mCleared = false
        return this
    }

    private fun getUrlPath(url: String): String {
        var uPath = ""
        try {
            val u = URL(url)
            uPath = u.path
            if (uPath.startsWith("/")) {
                if (uPath.length == 1) {
                    return uPath
                }
                uPath = uPath.substring(1)
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return uPath
    }

    fun getResByUrl(url: String): InputStream? {
        val uPath = getUrlPath(url)
        if (TextUtils.isEmpty(uPath)) {
            return null
        }
        if (!mIsSuffixMod) {
            return if (TextUtils.isEmpty(mDir)) {
                getAssetFileStream(uPath)
            } else {
                getAssetFileStream(mDir + File.separator + uPath)
            }
        }
        if (mAssetResSet != null) {
            for (p in mAssetResSet!!) {
                if (uPath.endsWith(p)) {
                    return if (TextUtils.isEmpty(mDir)) {
                        getAssetFileStream(p)
                    } else {
                        getAssetFileStream(mDir + File.separator + p)
                    }
                }
            }
        }
        return null
    }

    fun setDir(dir: String?): AssetsLoader {
        mDir = dir
        return this
    }

    fun initData(): AssetsLoader {
        if (!mIsSuffixMod) {
            return this
        }
        if (mAssetResSet!!.size == 0) {
            Thread { initResourceNoneRecursion(mDir) }.start()
        }
        return this
    }

    fun clear() {
        mCleared = true
        if (mAssetResSet != null && mAssetResSet!!.size > 0) {
            mAssetResSet!!.clear()
        }
    }

    private fun addAssetsFile(file: String) {
        var newFile = file
        val flag = mDir + File.separator
        if (!TextUtils.isEmpty(mDir)) {
            val pos = newFile.indexOf(flag)
            if (pos >= 0) {
                newFile = newFile.substring(pos + flag.length)
            }
        }
        mAssetResSet!!.add(newFile)
    }

    private fun initResourceNoneRecursion(dir: String?): AssetsLoader {
        try {
            val list = LinkedList<String>()
            val resData = mContext!!.assets.list(dir!!)
            for (res in resData!!) {
                val sub = dir + File.separator + res
                val tmp = mContext!!.assets.list(sub)
                if (tmp!!.size == 0) {
                    addAssetsFile(sub)
                } else {
                    list.add(sub)
                }
            }
            while (!list.isEmpty()) {
                if (mCleared) {
                    break
                }
                val last = list.removeFirst()
                val tmp = mContext!!.assets.list(last)
                if (tmp!!.size == 0) {
                    addAssetsFile(last)
                } else {
                    for (sub in tmp) {
                        val tmp1 = mContext!!.assets.list(last + File.separator + sub)
                        if (tmp1!!.size == 0) {
                            addAssetsFile(last + File.separator + sub)
                        } else {
                            list.add(last + File.separator + sub)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            // e.printStackTrace();
        }
        return this
    }

    fun getAssetFileStream(path: String?): InputStream? {
        try {
            return mContext!!.assets.open(path!!)
        } catch (e: IOException) {
            //e.printStackTrace();
        }
        return null
    }

    companion object {
        @Volatile
        private var assetsLoader: AssetsLoader? = null
        val instance: AssetsLoader?
            get() {
                if (assetsLoader == null) {
                    synchronized(AssetsLoader::class.java) {
                        if (assetsLoader == null) {
                            assetsLoader = AssetsLoader()
                        }
                    }
                }
                return assetsLoader
            }
    }
}