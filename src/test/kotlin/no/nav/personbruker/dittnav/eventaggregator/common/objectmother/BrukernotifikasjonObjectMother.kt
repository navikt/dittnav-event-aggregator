package no.nav.personbruker.dittnav.eventaggregator.common.objectmother

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.config.EventType
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave

object BrukernotifikasjonObjectMother {

    fun giveMeOneOfEachEventType(): List<Brukernotifikasjon> {
        return listOf(giveMeBeskjed(), giveMeInnboks(), giveMeOppgave())
    }

    fun giveMeBeskjed(): Brukernotifikasjon {
        return Brukernotifikasjon("b-1", "dummyProducer", EventType.BESKJED, "123")
    }

    fun giveMeInnboks(): Brukernotifikasjon {
        return Brukernotifikasjon("i-1", "dummyProducer", EventType.INNBOKS, "123")
    }

    fun giveMeOppgave(): Brukernotifikasjon {
        return Brukernotifikasjon("o-1", "dummyProducer", EventType.OPPGAVE, "123")
    }

    fun giveMeFor(beskjed: Beskjed): Brukernotifikasjon {
        return Brukernotifikasjon(beskjed.eventId, beskjed.produsent, EventType.BESKJED, beskjed.fodselsnummer)
    }

    fun giveMeFor(innboks: Innboks): Brukernotifikasjon {
        return Brukernotifikasjon(innboks.eventId, innboks.produsent, EventType.INNBOKS, innboks.fodselsnummer)
    }

    fun giveMeFor(oppgave: Oppgave): Brukernotifikasjon {
        return Brukernotifikasjon(oppgave.eventId, oppgave.produsent, EventType.OPPGAVE, oppgave.fodselsnummer)
    }

}
