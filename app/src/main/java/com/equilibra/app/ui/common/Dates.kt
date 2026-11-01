package com.equilibra.app.ui.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** A date in the phone's language and format, e.g. "Nov 1, 2026". */
fun LocalDate.toDisplayString(): String = format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
