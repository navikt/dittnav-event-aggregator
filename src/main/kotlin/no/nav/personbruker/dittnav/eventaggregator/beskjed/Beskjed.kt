package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime

data class Beskjed(
        val id: Int?,
        val produsent: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String,
        val tekst: String,
        val link: String,
        val sikkerhetsnivaa: Int,
        val sistOppdatert: LocalDateTime,
        val synligFremTil: LocalDateTime?,
        val aktiv: Boolean
) {
    constructor(produsent: String,
                eventId: String,
                eventTidspunkt: LocalDateTime,
                fodselsnummer: String,
                grupperingsId: String,
                tekst: String,
                link: String,
                sikkerhetsnivaa: Int,
                sistOppdatert: LocalDateTime,
                synligFremTil: LocalDateTime?,
                aktiv: Boolean
    ) : this(null,
            produsent,
            eventId,
            eventTidspunkt,
            fodselsnummer,
            grupperingsId,
            tekst,
            link,
            sikkerhetsnivaa,
            sistOppdatert,
            synligFremTil,
            aktiv
    )
}
