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

    /**
     * Parse [dateStr] using [format] and return UTC-midnight millis suitable for
     * Material3 DatePicker. Returns null if [dateStr] is blank or doesn't match [format].
     */
    fun parseDateToUtcMillis(dateStr: String, format: String): Long? {
        if (dateStr.isBlank()) return null
        return runCatching {
            val sdf = SimpleDateFormat(format, Locale.getDefault()).apply { isLenient = false }
            val parsed = sdf.parse(dateStr) ?: return null
            val local = java.util.Calendar.getInstance().also { it.time = parsed }
            val year = local.get(java.util.Calendar.YEAR)
            if (year !in 1900..2100) return null
            java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                set(year,
                    local.get(java.util.Calendar.MONTH),
                    local.get(java.util.Calendar.DAY_OF_MONTH),
                    0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.getOrNull()
    }
}
