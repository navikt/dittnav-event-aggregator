package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.done.Done
import java.time.LocalDateTime

data class Oppgave(
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
        val aktiv: Boolean
) {
    constructor(
            produsent: String,
            eventId: String,
            eventTidspunkt: LocalDateTime,
            fodselsnummer: String,
            grupperingsId: String,
            tekst: String,
            link: String,
            sikkerhetsinvaa: Int,
            sistOppdatert: LocalDateTime,
            aktiv: Boolean
    ) : this(null,
            produsent,
            eventId,
            eventTidspunkt,
            fodselsnummer,
            grupperingsId,
            tekst,
            link,
            sikkerhetsinvaa,
            sistOppdatert,
            aktiv
    )

    override fun toString(): String {
        return "Oppgave(" +
                "id=$id, " +
                "produsent=$produsent, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId, " +
                "tekst=***, " +
                "link=***, " +
                "sikkerhetsnivaa=$sikkerhetsnivaa, " +
                "sistOppdatert=$sistOppdatert, " +
                "aktiv=$aktiv"
    }

    fun isRepresentsSameEvent(doneEvent: Done): Boolean {
        return (eventId == doneEvent.eventId &&
                produsent == doneEvent.produsent &&
                fodselsnummer == doneEvent.fodselsnummer)
    }

}
