package com.clj.blesample.utils

object ByteUtil {
    fun bytesToHexString(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) {
            return ""
        }
        val sb = StringBuilder()
        for (b in bytes) {
            val hex = String.format("%02X", b)
            sb.append(hex).append(" ")
        }
        return sb.toString().trim()
    }

    fun bytesToString(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) {
            return ""
        }
        return String(bytes)
    }
}