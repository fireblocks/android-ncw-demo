package com.fireblocks.sdkdemo.bl.core.extensions

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import com.fireblocks.sdkdemo.R
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit


/**
 * Created by Fireblocks Ltd. on 07/10/2020
 */

private val SECOND_MILLIS: Int = 1000
private val MINUTE_MILLIS = 60 * SECOND_MILLIS
private val HOUR_MILLIS = 60 * MINUTE_MILLIS
private val DAY_MILLIS = 24 * HOUR_MILLIS

fun Long.asDate(): Date {
    return Date(this)
}

fun Long.isDateAfter(other: Long): Boolean {
    return this.asDate().after(other.asDate())
}

fun Long.toFormattedTimestamp(context: Context, @StringRes format: Int, dateFormat: String = "MMM d", timeFormat: String = "h:mm a", useSpecificDays: Boolean = true, useTime: Boolean = true): String {
    val date = Date(this)
    var formattedDate = SimpleDateFormat(dateFormat, Locale.getDefault()).format(date).toString().replace(".","")
    if (useSpecificDays) {
        if (isToday()) {
            formattedDate = context.getString(R.string.today)
        }

        if (isYesterday()) {
            formattedDate = context.getString(R.string.yesterday)
        }
    }
    return if (useTime) {
        val formattedTime = SimpleDateFormat(timeFormat, Locale.getDefault()).format(date).toString().uppercase(Locale.getDefault())

        context.getString(format, formattedDate, formattedTime)
    } else {
        formattedDate
    }
}


fun Long.toFormattedTime(timeFormat: String = "mm:ss"): String {
    val date = Date(this)
    val df = SimpleDateFormat(timeFormat, Locale.getDefault())
    df.timeZone = TimeZone.getTimeZone("GMT")
    return df.format(date)
}

fun String.toFormatedDateTime(pattern: String = "MM/dd/yyyy h:mm a"): String {
    val parsedDate = ZonedDateTime.parse(this)
    val localDate = parsedDate.withZoneSameInstant(java.time.ZoneId.systemDefault())
    val formattedDate = localDate.format(DateTimeFormatter.ofPattern(pattern))
    return formattedDate
}

fun Long.getRelativeTimeSpanString(context: Context): String {

    val time = TimeUnit.SECONDS.toMillis(this)

    val now = System.currentTimeMillis()

    val diff: Long = now - time
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)


    return if (seconds < 60) {
        context.getString(R.string.seconds_ago, seconds)
    } else if (minutes < 2) {
        context.getString(R.string.minute_ago)
    } else if (minutes < 60) {
        context.getString(R.string.minutes_ago, minutes)
    } else if (hours < 2) {
        context.getString(R.string.hour_ago)
    } else if (hours < 24) {
        context.getString(R.string.hours_ago, hours)
    } else if (days < 2) {
        context.getString(R.string.day_ago)
    } else {
        context.getString(R.string.days_ago, days)
    }
}

fun Long.isToday(): Boolean {
    return DateUtils.isToday(this)
}

fun Long.isYesterday(): Boolean {
    return DateUtils.isToday(this + DateUtils.DAY_IN_MILLIS)
}

fun Date.getTimezone(): String {
    val tz = TimeZone.getDefault()
    val isDaylite = tz.inDaylightTime(this)
    val timezone = tz.getDisplayName(isDaylite, TimeZone.SHORT)
    return timezone
}