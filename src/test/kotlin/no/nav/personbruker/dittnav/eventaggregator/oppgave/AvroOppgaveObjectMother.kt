package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.brukernotifikasjon.schemas.Oppgave
import java.time.Instant

object AvroOppgaveObjectMother {

    fun createOppgave(i: Int): Oppgave {
        return Oppgave(
                "DittNAV",
                Instant.now().toEpochMilli(),
                "12345",
                i.toString(),
                "100$i",
                "Dette er oppgave til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
