package no.nav.personbruker.dittnav.eventaggregator.done

import java.time.LocalDateTime

data class Done(
        val produsent: String,
        val eventId: String,
        val eventTidspunkt: LocalDateTime,
        val fodselsnummer: String,
        val grupperingsId: String
) {
    override fun toString(): String {
        return "Done(" +
                "produsent=$produsent, " +
                "eventId=$eventId, " +
                "eventTidspunkt=$eventTidspunkt, " +
                "fodselsnummer=***, " +
                "grupperingsId=$grupperingsId"
    }
}
