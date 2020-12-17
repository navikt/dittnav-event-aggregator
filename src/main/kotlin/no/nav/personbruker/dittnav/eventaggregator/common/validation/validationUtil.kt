package no.nav.personbruker.dittnav.eventaggregator.common.validation

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun timestampToUTCDateOrNull(timestamp: Long?): LocalDateTime? {
    return timestamp?.let { datetime ->
        LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC"))
    }
}