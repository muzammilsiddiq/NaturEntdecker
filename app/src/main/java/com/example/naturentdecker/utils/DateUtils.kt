package com.example.naturentdecker.utils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateUtils {

    private val isoParser: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

    private val shortFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())

    fun formatTourDateRange(startDate: String?, endDate: String?): String {
        val start = parseOrNull(startDate)
        val end = parseOrNull(endDate)

        return when {
            start != null && end != null -> {
                if (start.year == end.year) {
                    "${shortFormatter.format(start)} - ${displayFormatter.format(end)}"
                } else {
                    "${displayFormatter.format(start)} - ${displayFormatter.format(end)}"
                }
            }
            start != null -> displayFormatter.format(start)
            end != null -> displayFormatter.format(end)
            else -> "Date TBD"
        }
    }

    private fun parseOrNull(isoString: String?): OffsetDateTime? {
        if (isoString.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(isoString, isoParser)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
