package no.nav.personbruker.dittnav.eventaggregator.common

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object LocalDateTimeTestHelper {
    fun LocalDateTime.toEpochMilli() = toInstant(ZoneOffset.UTC).toEpochMilli()

    fun nowTruncatedToMillis() = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
}
