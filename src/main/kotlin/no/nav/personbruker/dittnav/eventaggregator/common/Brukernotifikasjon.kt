package no.nav.personbruker.dittnav.eventaggregator.common

import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.done.Done

data class Brukernotifikasjon(
        val eventId: String,
        val systembruker: String,
        val type: EventType,
        val fodselsnummer: String
) {

    fun isRepresentsSameEventAs(doneEvent: Done): Boolean {
        if (eventId != doneEvent.eventId) return false

        return true
    }

}
