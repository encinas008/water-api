package com.dreamsbo.posapi.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


object DateUtil {

    fun simpleFormat(date: OffsetDateTime): String {

        val locale = Locale("es", "ES", "ES")

        val zoneId = ZoneId.of("America/La_Paz")

        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a", locale).withZone(zoneId)

        return date.format(formatter)
    }

    fun fromDate(dateInMilliseconds: Long): OffsetDateTime {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(dateInMilliseconds), ZoneId.of("America/La_Paz")).withHour(0)
            .withMinute(0).withSecond(0).withNano(0)
    }

    fun toDate(dateInMilliseconds: Long): OffsetDateTime {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(dateInMilliseconds), ZoneId.of("America/La_Paz")).plusDays(1)
            .withHour(0).withMinute(0).withSecond(0).withNano(0)
    }
}
