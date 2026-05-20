package com.nithra.nithraresume.utils

import java.text.DecimalFormat

object FileUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val df = DecimalFormat("#.##")
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> "${df.format(mb)} MB"
            kb >= 1 -> "${df.format(kb)} KB"
            else    -> "$bytes B"
        }
    }

    fun formatFileModified(millis: Long): String =
        DateTimeUtils.formatTimestamp(millis, "EEE, dd MMM yyyy, hh:mm a")
}
