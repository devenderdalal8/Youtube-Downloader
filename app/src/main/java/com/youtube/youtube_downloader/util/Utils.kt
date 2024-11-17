package com.youtube.youtube_downloader.util

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun String.convertDateToYearsAndDays(): String {
    val offsetDateTime = OffsetDateTime.parse(this)

    val currentDateTime = OffsetDateTime.now()
    val years = ChronoUnit.YEARS.between(offsetDateTime, currentDateTime)
    val days = ChronoUnit.DAYS.between(offsetDateTime.plusYears(years), currentDateTime)
    return if (years > 0) {
        "$years years"
    } else {
        "$days days"
    }
}

fun Long.getFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var fileSize = this.toDouble()
    var unitIndex = 0
    while (fileSize >= 1024 && unitIndex < units.size - 1) {
        fileSize /= 1024
        unitIndex++
    }
    return "%.2f %s".format(fileSize, units[unitIndex])
}

fun String.calculateYearsMonthsDaysString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val date: Date

    try {
        date = formatter.parse(this) ?: return "Invalid date"
    } catch (e: ParseException) {
        return "Error parsing date"
    }

    val calendar = Calendar.getInstance()
    val currentDate = calendar.time

    // Get the difference in milliseconds
    val differenceInMillis = currentDate.time - date.time

    // Calculate the difference in days
    val days = (differenceInMillis / (1000 * 60 * 60 * 24)).toInt()
    val years = days / 365
    val months = (days % 365) / 30 // Simplified month calculation
    val remainingDays = (days % 365) % 30

    return when {
        years > 0 -> "$years yr $months months, $remainingDays days"
        months > 0 -> "$months months, $remainingDays days"
        else -> "$remainingDays days ago"
    }
}

fun Long.convertIntoNumber(): String {
    return when {
        this >= 1_000_000_000 -> "%.2fB".format(this / 1000000000.0)
        this >= 1_000_000 -> "%.2fM".format(this / 1000000.0)
        this >= 1_000 -> "%.2fk".format(this / 1000.0)
        else -> this.toString()  // If the value is less than 1,000, return it as-is
    }
}


fun String.getTimeDifference() :String{
    return this.substring(0 , 10).split("-").reversed().joinToString("/")
}

fun String?.isUrlExpired(): Boolean {
    val url = URL(this)
    val queryParams = url.query.split("&")
    val expireParam = queryParams.find { it.startsWith("expire=") }

    expireParam?.let {
        val expireTime = it.substringAfter("expire=").toLong() * 1000L // Convert to milliseconds
        val expireDate = Date(expireTime)
        val currentDate = Calendar.getInstance().time

        return currentDate.after(expireDate)
    }

    return true // If no expire param is found, consider the URL expired
}

infix fun <T> MutableStateFlow<T>.value(value: T) {
    this.value = value
}

fun String.isShortVideo(): Boolean = this.contains("youtube.com/shorts/", ignoreCase = true)
