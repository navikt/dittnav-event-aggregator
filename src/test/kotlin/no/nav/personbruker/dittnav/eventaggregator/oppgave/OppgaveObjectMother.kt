package no.nav.personbruker.dittnav.eventaggregator.oppgave

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object OppgaveObjectMother {

    private const val defaultSystembruker = "systembruker"
    private const val defaultNamespace = "namespace"
    private const val defaultAppnavn = "appnavn"
    private const val defaultFodselsnummer = "123"
    private const val defaultEventId = "o-123"
    private const val defaultGrupperingsId = "Dok12345"
    private const val defaultTekst = "tekst"
    private const val defaultLink = "https://link"
    private const val defaultSikkerhetsnivaa = 4
    private const val defaultAktiv = true
    private const val defaultEksternVarsling = false
    private val defaultPrefererteKanaler = listOf<String>()

    fun giveMeAktivOppgave(): Oppgave {
        return giveMeAktivOppgave("o-1", "123")
    }

    fun giveMeAktivOppgave(eventId: String, fodselsnummer: String): Oppgave {
        return giveMeAktivOppgave(eventId, fodselsnummer, "dummySystembruker")
    }

    fun giveMeAktivOppgaveWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Oppgave {
        return  giveMeOppgave(
            aktiv = true,
            eventId = "O-2",
            fodselsnummer = "123",
            systembruker = "dummySystembruker",
            eksternVarsling = eksternVarsling,
            prefererteKanaler = prefererteKanaler
        )
    }

    fun giveMeAktivOppgave(eventId: String, fodselsnummer: String, systembruker: String): Oppgave {
        return giveMeOppgave(
            aktiv = true,
            eventId = eventId,
            fodselsnummer = fodselsnummer,
            systembruker = systembruker
        )
    }

    fun giveMeInaktivOppgave(): Oppgave {
        return giveMeOppgave(aktiv = false, eventId = "o-2")
    }


    fun giveMeOppgaveWithForstBehandlet(eventId: String, fodselsnummer: String, forstBehandlet: LocalDateTime): Oppgave {
        return giveMeOppgave(eventId = eventId, fodselsnummer = fodselsnummer, forstBehandlet = forstBehandlet)
    }

    fun giveMeOppgaveWithEventIdAndAppnavn(eventId: String, appnavn: String): Oppgave {
        return giveMeOppgave(
            eventId = eventId,
            appnavn = appnavn
        )
    }

    private fun giveMeOppgave(
        systembruker: String = defaultSystembruker,
        namespace: String = defaultNamespace,
        appnavn: String = defaultAppnavn,
        eventTidspunkt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        forstBehandlet: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        synligFremTil: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).plusDays(1).truncatedTo(ChronoUnit.MILLIS),
        fodselsnummer: String = defaultFodselsnummer,
        eventId: String = defaultEventId,
        grupperingsId: String = defaultGrupperingsId,
        tekst: String = defaultTekst,
        link: String = defaultLink,
        sistOppdatert: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        sikkerhetsnivaa: Int = defaultSikkerhetsnivaa,
        aktiv: Boolean = defaultAktiv,
        eksternVarsling: Boolean = defaultEksternVarsling,
        prefererteKanaler: List<String> = defaultPrefererteKanaler
    ) = Oppgave(
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn,
        eventId = eventId,
        eventTidspunkt = eventTidspunkt,
        forstBehandlet = forstBehandlet,
        fodselsnummer = fodselsnummer,
        grupperingsId = grupperingsId,
        tekst = tekst,
        link = link,
        sikkerhetsnivaa = sikkerhetsnivaa,
        sistOppdatert = sistOppdatert,
        synligFremTil = synligFremTil,
        aktiv = aktiv,
        eksternVarsling = eksternVarsling,
        prefererteKanaler = prefererteKanaler
    )
}
