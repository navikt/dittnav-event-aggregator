package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.internal.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import java.time.Instant

object AvroOppgaveObjectMother {

    private val defaultLopenummer = 1
    private val defaultTekst = "Dette er oppgave til brukeren"
    private val defaultEksternVarsling = true
    private val defaultPrefererteKanaler = listOf(PreferertKanal.EPOST.toString(), PreferertKanal.SMS.toString())

    fun createOppgave(lopenummer: Int): OppgaveIntern {
        return createOppgave(lopenummer, defaultTekst)
    }

    fun createOppgaveWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): OppgaveIntern {
        return OppgaveIntern(
            defaultLopenummer.toString(),
            Instant.now().toEpochMilli(),
            "100$defaultLopenummer",
            defaultTekst,
            "https://nav.no/systemX",
            4,
            eksternVarsling,
            prefererteKanaler
        )
    }

    fun createOppgave(lopenummer: Int, tekst: String): OppgaveIntern {
        return OppgaveIntern(
                lopenummer.toString(),
                Instant.now().toEpochMilli(),
                "100$lopenummer",
                tekst,
                "https://nav.no/systemX/$lopenummer",
                4,
                defaultEksternVarsling,
                defaultPrefererteKanaler)
    }
}
