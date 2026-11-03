package com.equilibra.app.ui.common

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** A date in the phone's language and format, e.g. "Nov 1, 2026". */
fun LocalDate.toDisplayString(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

/** Minutes from midnight as a time in the phone's format, e.g. 450 -> "07:30" or "7:30 AM". */
fun minuteOfDayToDisplayString(minuteOfDay: Int): String =
    LocalTime.of(minuteOfDay / 60, minuteOfDay % 60).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
