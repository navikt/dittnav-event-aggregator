package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveObjectMother {

    fun giveMeOppgave(): Oppgave {
        return giveMeOppgave("o-1", "123")
    }

    fun giveMeOppgave(eventId: String, fodselsnummer: String): Oppgave {
        return Oppgave(
                produsent = "DittNAV",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsinvaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = true)
    }

}
