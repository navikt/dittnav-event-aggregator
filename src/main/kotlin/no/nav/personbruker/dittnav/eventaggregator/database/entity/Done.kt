package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.time.LocalDateTime

data class Done(
        val produsent: String,
        val eventTidspunkt: LocalDateTime,
        val aktorId: String,
        val eventId: String,
        val dokumentId: String
)