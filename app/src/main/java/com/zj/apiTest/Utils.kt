package com.zj.apiTest

import java.security.MessageDigest

fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    val result = digest.digest(toByteArray())
    println("result${result.size}")
    return toHex(result)
}

fun toHex(byteArray: ByteArray): String {
    val result = with(StringBuilder()) {
        byteArray.forEach {
            val hex = it.toInt() and (0xFF)
            val hexStr = Integer.toHexString(hex)
            if (hexStr.length == 1) {
                this.append("0").append(hexStr)
            } else {
                this.append(hexStr)
            }
        }
        this.toString()
    }
    return result
}