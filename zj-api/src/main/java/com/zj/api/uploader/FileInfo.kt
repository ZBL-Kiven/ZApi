package com.zj.api.uploader

import java.io.File
import java.util.*

@Suppress("unused")
data class FileInfo(val name: String, val paramName: String, val file: File, val path: String = file.path, val fileId: String = UUID.randomUUID().toString()) {

    constructor(name: String, paramName: String, path: String, fileId: String) : this(name, paramName, File(path), fileId)

}