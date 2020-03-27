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
        if (eventId != doneEvent.eventId) return false
        if (fodselsnummer != doneEvent.fodselsnummer) return false
        if (produsent != doneEvent.produsent) return false

        return true
    }

}
