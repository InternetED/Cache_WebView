package com.interneted.cache_webview.utils

import java.security.MessageDigest

/**
 * Created by yale on 2017/9/22.
 */
object MD5Utils {
    fun getMD5(message: String): String {
        return getMD5(message, true)
    }

    fun getMD5(message: String, upperCase: Boolean): String {
        var md5str = ""
        try {
            val md = MessageDigest.getInstance("MD5")
            val input = message.toByteArray()
            val buff = md.digest(input)
            md5str = bytesToHex(buff, upperCase)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return md5str
    }

    fun bytesToHex(bytes: ByteArray, upperCase: Boolean): String {
        val md5str = StringBuffer()
        var digital: Int
        for (i in bytes.indices) {
            digital = bytes[i].toInt()
            if (digital < 0) {
                digital += 256
            }
            if (digital < 16) {
                md5str.append("0")
            }
            md5str.append(Integer.toHexString(digital))
        }
        return if (upperCase) {
            md5str.toString().toUpperCase()
        } else md5str.toString().toLowerCase()
    }
}