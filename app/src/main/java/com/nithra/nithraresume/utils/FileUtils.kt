package com.nithra.nithraresume.utils

import java.text.DecimalFormat

object FileUtils {

    private val decimalFormat = DecimalFormat("#.##")

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> "${decimalFormat.format(mb)} MB"
            kb >= 1 -> "${decimalFormat.format(kb)} KB"
            else    -> "$bytes B"
        }
    }

    fun formatFileModified(millis: Long): String =
        DateTimeUtils.formatTimestamp(millis, "EEE, dd MMM yyyy, hh:mm a")
}
