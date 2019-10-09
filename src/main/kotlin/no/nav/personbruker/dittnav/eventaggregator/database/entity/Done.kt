package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.time.LocalDateTime

data class Done(
        val eventId: String,
        val produsent: String,
        val eventTidspunkt: LocalDateTime,
        val aktorId: String,
        val dokumentId: String
)
