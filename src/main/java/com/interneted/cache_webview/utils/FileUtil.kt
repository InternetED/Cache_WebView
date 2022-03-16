package com.interneted.cache_webview.utils

import android.text.TextUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by yale on 2017/9/18.
 */
object FileUtil {
    fun deleteDirs(path: String?, isDeleteDir: Boolean) {
        if (path == null || TextUtils.isEmpty(path)) {
            return
        }
        val dir = File(path)
        if (!dir.exists()) {
            return
        }
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                deleteDirs(file.absolutePath, isDeleteDir)
            } else {
                file.delete()
            }
        }
        if (isDeleteDir) {
            dir.delete()
        }
    }

    @Throws(IOException::class)
    fun copy(inputStream: InputStream, out: OutputStream) {
        val buf = ByteArray(512)
        var len: Int
        while (inputStream.read(buf).also { len = it } != -1) {
            out.write(buf, 0, len)
        }
        inputStream.close()
        out.close()
    }
}