package no.nav.personbruker.dittnav.eventaggregator.database.entity

import java.time.LocalDateTime

data class Oppgave (
        val id: Int?,
        val produsent: String,
        val eventTidspunkt: LocalDateTime,
        val aktoerId: String,
        val eventId: String,
        val dokumentId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsinvaa: Int
) {
    constructor(
            produsent: String,
            eventTidspunkt: LocalDateTime,
            aktoerId: String,
            eventId: String,
            dokumentId: String,
            tekst: String,
            link: String,
            sikkerhetsinvaa: Int
    ) : this(null,produsent,eventTidspunkt,aktoerId,eventId,dokumentId,tekst,link,sikkerhetsinvaa)
}