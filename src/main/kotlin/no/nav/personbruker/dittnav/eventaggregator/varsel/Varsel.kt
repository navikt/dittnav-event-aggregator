package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.done.Done

data class Varsel(
        val eventId: String,
        val systembruker: String,
        val type: VarselType,
        val fodselsnummer: String
) {

    fun isRepresentsSameEventAs(doneEvent: Done): Boolean {
        if (eventId != doneEvent.eventId) return false

        return true
    }

}
