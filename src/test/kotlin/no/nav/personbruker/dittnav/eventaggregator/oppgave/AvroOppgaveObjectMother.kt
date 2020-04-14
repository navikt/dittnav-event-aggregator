package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import java.time.Instant

object AvroOppgaveObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnr = "12345"
    private val defaultTekst = "Dette er oppgave til brukeren"

    fun createOppgave(lopenummer: Int): Oppgave {
        return createOppgave(lopenummer, defaultFodselsnr, defaultTekst)
    }

    fun createOppgave(lopenummer: Int, fodselsnummer: String): Oppgave {
        return createOppgave(lopenummer, fodselsnummer, defaultTekst)
    }

    fun createOppgave(tekst: String): Oppgave {
        return createOppgave(defaultLopenummer, defaultFodselsnr, tekst)
    }

    fun createOppgave(lopenummer: Int, fodselsnummer: String, tekst: String): Oppgave {
        return Oppgave(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer",
                tekst,
                "https://nav.no/systemX/$lopenummer",
                4)
    }

}
