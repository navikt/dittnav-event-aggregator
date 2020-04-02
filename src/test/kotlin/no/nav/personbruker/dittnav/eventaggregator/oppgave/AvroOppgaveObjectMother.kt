package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import java.time.Instant

object AvroOppgaveObjectMother {

    fun createOppgave(i: Int): Oppgave {
        return Oppgave(
                Instant.now().toEpochMilli(),
                "12345",
                "100$i",
                "Dette er oppgave til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

    fun createOppgaveWithFodselsnummer(i: Int, fodselsnummer: String): Oppgave {
        return Oppgave(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$i",
                "Dette er oppgave til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
