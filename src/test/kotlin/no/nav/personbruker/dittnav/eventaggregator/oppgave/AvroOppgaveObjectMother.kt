package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import java.time.Instant

object AvroOppgaveObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnr = "12345"
    private val defaultTekst = "Dette er oppgave til brukeren"
    private val defaultEksternVarsling = true
    private val defaultPrefererteKanaler = listOf(PreferertKanal.EPOST.toString(), PreferertKanal.SMS.toString())

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
                4,
                defaultEksternVarsling,
                defaultPrefererteKanaler)
    }
}
