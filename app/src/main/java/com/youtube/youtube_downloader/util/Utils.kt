package com.youtube.youtube_downloader.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

object Utils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertDateToYearsAndDays(dateString: String): String {
        // Parse the date string
        val offsetDateTime = OffsetDateTime.parse(dateString)

        // Get the current date and time
        val currentDateTime = OffsetDateTime.now()

        // Calculate the difference in years and days
        val years = ChronoUnit.YEARS.between(offsetDateTime, currentDateTime)
        val days = ChronoUnit.DAYS.between(offsetDateTime.plusYears(years), currentDateTime)
        return if (years > 0) {
            "$years years"
        } else {
            "$days days"
        }
    }


}