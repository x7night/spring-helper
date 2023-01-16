package dto

import java.io.File

data class DownloadResult(
    val isZip: Boolean,
    val tempFile: File,
    val filename: String
)
