package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import java.time.Instant

object AvroOppgaveObjectMother {

    private val defaultLopenummer = 1
    private val defaultTekst = "Dette er oppgave til brukeren"
    private val defaultEksternVarsling = true

    fun createOppgave(lopenummer: Int): OppgaveIntern {
        return createOppgave(lopenummer, defaultTekst)
    }

    fun createOppgave(tekst: String): OppgaveIntern {
        return createOppgave(defaultLopenummer, tekst)
    }

    fun createOppgave(lopenummer: Int, tekst: String): OppgaveIntern {
        return OppgaveIntern(
                lopenummer.toString(),
                Instant.now().toEpochMilli(),
                "100$lopenummer",
                tekst,
                "https://nav.no/systemX/$lopenummer",
                4,
                defaultEksternVarsling)
    }

}
