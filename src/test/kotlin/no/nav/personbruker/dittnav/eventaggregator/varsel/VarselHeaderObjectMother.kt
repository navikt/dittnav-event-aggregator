package no.nav.personbruker.dittnav.eventaggregator.varsel

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave

object VarselHeaderObjectMother {

    fun giveMeOneOfEachEventType(): List<VarselHeader> {
        return listOf(giveMeBeskjed(), giveMeInnboks(), giveMeOppgave())
    }

    fun giveMeBeskjed(): VarselHeader {
        return VarselHeader("b-1", VarselType.BESKJED, true, "123", "dummyNamespace", "dummyAppnavn")
    }

    private fun giveMeInnboks(): VarselHeader {
        return VarselHeader("i-1",  VarselType.INNBOKS, true, "123", "dummyNamespace", "dummyAppnavn")
    }

    private fun giveMeOppgave(): VarselHeader {
        return VarselHeader("o-1",  VarselType.OPPGAVE, true, "123", "dummyNamespace", "dummyAppnavn")
    }

    fun giveMeFor(beskjed: Beskjed): VarselHeader {
        return VarselHeader(beskjed.eventId, VarselType.BESKJED, beskjed.aktiv, beskjed.fodselsnummer, beskjed.namespace, beskjed.appnavn)
    }

    fun giveMeFor(innboks: Innboks): VarselHeader {
        return VarselHeader(innboks.eventId, VarselType.INNBOKS, innboks.aktiv, innboks.fodselsnummer, innboks.namespace, innboks.appnavn)
    }

    fun giveMeFor(oppgave: Oppgave): VarselHeader {
        return VarselHeader(oppgave.eventId, VarselType.OPPGAVE, oppgave.aktiv, oppgave.fodselsnummer, oppgave.namespace, oppgave.appnavn)
    }

}
