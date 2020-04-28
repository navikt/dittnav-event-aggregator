package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.LocalDateTime
import java.time.ZoneId

object OppgaveObjectMother {

    fun giveMeAktivOppgave(): Oppgave {
        return giveMeAktivOppgave("o-1", "123")
    }

    fun giveMeAktivOppgave(eventId: String, fodselsnummer: String): Oppgave {
        return giveMeAktivOppgave(eventId, fodselsnummer, "dummyProducer")
    }

    fun giveMeAktivOppgave(eventId: String, fodselsnummer: String, systembruker: String): Oppgave {
        return Oppgave(
                systembruker = systembruker,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = true)
    }

    fun giveMeInaktivOppgave(): Oppgave {
        return Oppgave(
                systembruker = "dummyProducer",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = "123",
                eventId = "o-2",
                grupperingsId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                aktiv = false)
    }

}
