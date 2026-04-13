package com.nithra.nithraresume.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    /**
     * Format today's date using the given [pattern].
     * Returns an empty string if the pattern is invalid.
     */
    fun formatToday(pattern: String): String =
        runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
        }.getOrDefault("")

    /**
     * Format a [timestamp] (millis since epoch) using the given [pattern].
     * Returns an empty string if the pattern is invalid.
     */
    fun formatTimestamp(timestamp: Long, pattern: String): String =
        runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
        }.getOrDefault("")

    /**
     * Reformat a date string from one pattern to another.
     * Returns [dateStr] unchanged if either pattern is invalid or the input doesn't parse.
     */
    fun reformat(dateStr: String, fromPattern: String, toPattern: String): String {
        if (dateStr.isBlank()) return dateStr
        return runCatching {
            val parsed = SimpleDateFormat(fromPattern, Locale.getDefault()).parse(dateStr)
                ?: return dateStr
            SimpleDateFormat(toPattern, Locale.getDefault()).format(parsed)
        }.getOrDefault(dateStr)
    }

    /**
     * Returns true if [pattern] is a valid [SimpleDateFormat] pattern.
     */
    fun isValidPattern(pattern: String): Boolean =
        runCatching { SimpleDateFormat(pattern, Locale.getDefault()) }.isSuccess
}
// update 171
