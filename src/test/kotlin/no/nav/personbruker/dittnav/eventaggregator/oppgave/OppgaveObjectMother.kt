package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import java.time.LocalDateTime

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

    fun giveMeOppgaveWithForstBehandlet(forstBehandlet: LocalDateTime): Oppgave {
        return giveMeOppgave(forstBehandlet = forstBehandlet)
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
        eventTidspunkt: LocalDateTime = nowTruncatedToMillis(),
        forstBehandlet: LocalDateTime = nowTruncatedToMillis(),
        synligFremTil: LocalDateTime = nowTruncatedToMillis().plusDays(1),
        fodselsnummer: String = defaultFodselsnummer,
        eventId: String = defaultEventId,
        grupperingsId: String = defaultGrupperingsId,
        tekst: String = defaultTekst,
        link: String = defaultLink,
        sistOppdatert: LocalDateTime = nowTruncatedToMillis(),
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
