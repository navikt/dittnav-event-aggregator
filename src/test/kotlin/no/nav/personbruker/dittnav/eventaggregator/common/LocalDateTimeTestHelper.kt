package no.nav.personbruker.dittnav.eventaggregator.common

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object LocalDateTimeTestHelper {
    fun nowAtUtcTruncated(): LocalDateTime = LocalDateTimeHelper.nowAtUtc().truncatedTo(ChronoUnit.MILLIS)
}

