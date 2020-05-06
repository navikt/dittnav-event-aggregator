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
        return Brukernotifikasjon("b-1", "dummySystembruker", EventType.BESKJED, "123")
    }

    fun giveMeBeskjed(fodselsnummer: String): Brukernotifikasjon {
        return Brukernotifikasjon("b-1", "dummySystembruker", EventType.BESKJED, fodselsnummer)
    }

    fun giveMeInnboks(): Brukernotifikasjon {
        return Brukernotifikasjon("i-1", "dummySystembruker", EventType.INNBOKS, "123")
    }

    fun giveMeInnboks(fodselsnummer: String): Brukernotifikasjon {
        return Brukernotifikasjon("i-1", "dummySystembruker", EventType.INNBOKS, fodselsnummer)
    }

    fun giveMeOppgave(): Brukernotifikasjon {
        return Brukernotifikasjon("o-1", "dummySystembruker", EventType.OPPGAVE, "123")
    }

    fun giveMeOppgave(fodselsnummer: String): Brukernotifikasjon {
        return Brukernotifikasjon("o-1", "dummySystembruker", EventType.OPPGAVE, fodselsnummer)
    }

    fun giveMeFor(beskjed: Beskjed): Brukernotifikasjon {
        return Brukernotifikasjon(beskjed.eventId, beskjed.systembruker, EventType.BESKJED, beskjed.fodselsnummer)
    }

    fun giveMeFor(innboks: Innboks): Brukernotifikasjon {
        return Brukernotifikasjon(innboks.eventId, innboks.systembruker, EventType.INNBOKS, innboks.fodselsnummer)
    }

    fun giveMeFor(oppgave: Oppgave): Brukernotifikasjon {
        return Brukernotifikasjon(oppgave.eventId, oppgave.systembruker, EventType.OPPGAVE, oppgave.fodselsnummer)
    }

}
