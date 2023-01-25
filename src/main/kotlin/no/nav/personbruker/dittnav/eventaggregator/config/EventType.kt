package no.nav.personbruker.dittnav.eventaggregator.config

import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType.*

enum class EventType(val eventType: String) {
    OPPGAVE_INTERN("oppgave"),
    BESKJED_INTERN("beskjed"),
    INNBOKS_INTERN("innboks"),
    DONE_INTERN("done");

    fun toVarselType(): VarselType {
        return when (this) {
            OPPGAVE_INTERN -> OPPGAVE
            BESKJED_INTERN -> BESKJED
            INNBOKS_INTERN -> INNBOKS
            DONE_INTERN -> throw RuntimeException("DONE er ikke en varseltype")
        }
    }
}
