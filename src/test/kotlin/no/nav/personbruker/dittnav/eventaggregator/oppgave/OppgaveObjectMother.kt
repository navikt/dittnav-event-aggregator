package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object OppgaveObjectMother {

    fun giveMeAktivOppgave(): Oppgave {
        return giveMeAktivOppgave("o-1", "123")
    }

    fun giveMeAktivOppgave(eventId: String, fodselsnummer: String): Oppgave {
        return giveMeAktivOppgave(eventId, fodselsnummer, "dummySystembruker")
    }

    fun giveMeAktivOppgaveWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Oppgave {
        val oppgave = giveMeAktivOppgave(eventId = "O-2", fodselsnummer = "123", systembruker = "dummySystembruker")
        return oppgave.copy(eksternVarsling = eksternVarsling, prefererteKanaler = prefererteKanaler)
    }

    fun giveMeAktivOppgave(eventId: String, fodselsnummer: String, systembruker: String): Oppgave {
        return Oppgave(
                systembruker = systembruker,
                namespace = "dummyNamespace",
                appnavn = "dummyAppnavn",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
                aktiv = true,
                eksternVarsling = false)
    }

    fun giveMeInaktivOppgave(): Oppgave {
        return Oppgave(
                systembruker = "dummySystembruker",
                namespace = "dummyNamespace",
                appnavn = "dummyAppnavn",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
                fodselsnummer = "123",
                eventId = "o-2",
                grupperingsId = "Dok12345",
                tekst = "Dette er en oppgave til brukeren",
                link = "https://nav.no/systemX/",
                sikkerhetsnivaa = 4,
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
                aktiv = false,
                eksternVarsling = false)
    }
}
