package no.nav.personbruker.dittnav.eventaggregator.common.database.entity

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.Done

data class Brukernotifikasjon(
        val eventId: String,
        val systembruker: String,
        val type: EventType,
        val fodselsnummer: String
) {

    fun representsSameEventAs(doneEvent: Done): Boolean {
        return eventId == doneEvent.eventId
                && systembruker == doneEvent.systembruker
                && fodselsnummer == doneEvent.fodselsnummer
    }

}
