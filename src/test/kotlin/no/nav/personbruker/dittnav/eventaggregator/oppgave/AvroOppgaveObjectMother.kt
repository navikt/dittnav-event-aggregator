package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.internal.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.internal.OppgaveIntern
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
            Instant.now().toEpochMilli(),
            Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli(),
            defaultTekst,
            "https://nav.no/systemX",
            4,
            eksternVarsling,
            prefererteKanaler
        )
    }

    fun createOppgave(
        lopenummer: Int,
        tekst: String,
        synligFremTil: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    ): OppgaveIntern {
        return OppgaveIntern(
                Instant.now().toEpochMilli(),
                synligFremTil.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
                tekst,
                "https://nav.no/systemX/$lopenummer",
                4,
                defaultEksternVarsling,
                defaultPrefererteKanaler)
    }
}
