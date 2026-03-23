package com.example.naturentdecker.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun formatIsoDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val date = isoFormat.parse(isoString)
            if (date != null) displayFormat.format(date) else isoString
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatTourDateRange(startDate: String?, endDate: String?): String {
        val start = formatIsoDate(startDate)
        val end = formatIsoDate(endDate)
        return when {
            start.isNotEmpty() && end.isNotEmpty() -> "$start - $end"
            start.isNotEmpty() -> start
            end.isNotEmpty() -> end
            else -> "Date TBD"
        }
    }
}
