package no.nav.personbruker.dittnav.eventaggregator.common

import java.time.LocalDateTime
import java.time.ZoneOffset

fun LocalDateTime.toEpochMilli() = toInstant(ZoneOffset.UTC).toEpochMilli()
