package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.LocalDateTime

data class Innboks (
        val id: Int?,
        val produsent: String,
        val eventTidspunkt: LocalDateTime,
        val aktorId: String,
        val eventId: String,
        val dokumentId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val aktiv: Boolean
) {
    constructor(
            produsent: String,
            eventTidspunkt: LocalDateTime,
            aktorId: String,
            eventId: String,
            dokumentId: String,
            tekst: String,
            link: String,
            sikkerhetsnivaa: Int,
            sistOppdatert: LocalDateTime,
            aktiv: Boolean
    ) : this(
            null,
            produsent,
            eventTidspunkt,
            aktorId,
            eventId,
            dokumentId,
            tekst,
            link,
            sikkerhetsnivaa,
            sistOppdatert,
            aktiv
    )
}