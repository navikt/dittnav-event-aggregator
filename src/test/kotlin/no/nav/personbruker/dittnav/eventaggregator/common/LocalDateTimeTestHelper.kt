package no.nav.personbruker.dittnav.eventaggregator.common

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object LocalDateTimeTestHelper {
    fun nowTruncatedToMillis(): LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
}

