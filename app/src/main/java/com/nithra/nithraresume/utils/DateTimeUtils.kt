package com.nithra.nithraresume.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    // SimpleDateFormat is not thread-safe; use one cache per thread.
    private val cache = ThreadLocal.withInitial { HashMap<String, SimpleDateFormat>() }
    private fun sdf(pattern: String): SimpleDateFormat =
        cache.get()!!.getOrPut(pattern) { SimpleDateFormat(pattern, Locale.getDefault()) }

    fun formatToday(pattern: String): String =
        runCatching { sdf(pattern).format(Date()) }.getOrDefault("")

    fun formatTimestamp(timestamp: Long, pattern: String): String =
        runCatching { sdf(pattern).format(Date(timestamp)) }.getOrDefault("")

    fun reformat(dateStr: String, fromPattern: String, toPattern: String): String {
        if (dateStr.isBlank()) return dateStr
        return runCatching {
            val parsed = sdf(fromPattern).parse(dateStr) ?: return dateStr
            sdf(toPattern).format(parsed)
        }.getOrDefault(dateStr)
    }

    fun isValidPattern(pattern: String): Boolean =
        runCatching { sdf(pattern) }.isSuccess

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
