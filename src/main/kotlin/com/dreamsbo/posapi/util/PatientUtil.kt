package com.dreamsbo.posapi.util

import java.time.LocalDate
import java.time.Period

object PatientUtil {

    fun calculateAge(birthDate: LocalDate?): String {

        if (birthDate == null) {

            return ""
        }

        val currentDate = LocalDate.now()

        val period: Period = Period.between(birthDate, currentDate)

        return "${period.years}a ${period.months}m"
    }
}
