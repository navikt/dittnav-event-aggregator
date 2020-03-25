package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.Done

data class Brukernotifikasjon(
        val eventId: String,
        val produsent: String,
        val type: EventType,
        val fodselsnummer: String
) {

    fun isRepresentsSameEventAs(doneEvent: Done): Boolean {
        return (eventId == doneEvent.eventId &&
                produsent == doneEvent.produsent &&
                fodselsnummer == doneEvent.fodselsnummer)
    }

}
