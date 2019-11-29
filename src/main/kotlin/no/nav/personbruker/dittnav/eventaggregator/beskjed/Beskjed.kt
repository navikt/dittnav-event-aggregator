package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime

data class Beskjed(
        val id: Int?,
        val produsent: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val eventId: String,
        val grupperingsId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val aktiv: Boolean
) {
    constructor(produsent: String,
                eventTidspunkt: LocalDateTime,
                fodselsnummer: String,
                eventId: String,
                grupperingsId: String,
                tekst: String,
                link: String,
                sikkerhetsnivaa: Int,
                sistOppdatert: LocalDateTime,
                aktiv: Boolean) : this(null, produsent, eventTidspunkt, fodselsnummer, eventId, grupperingsId, tekst, link,
            sikkerhetsnivaa, sistOppdatert, aktiv)
}
