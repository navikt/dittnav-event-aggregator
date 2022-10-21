package no.nav.personbruker.dittnav.eventaggregator.oppgave

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import java.time.LocalDateTime

object OppgaveTestData {

    fun oppgave(
        systembruker: String = "systembruker",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        eventTidspunkt: LocalDateTime = nowTruncatedToMillis(),
        forstBehandlet: LocalDateTime = nowTruncatedToMillis(),
        synligFremTil: LocalDateTime = nowTruncatedToMillis().plusDays(1),
        fodselsnummer: String = "123",
        eventId: String = "o-123",
        grupperingsId: String = "Dok12345",
        tekst: String = "tekst",
        link: String = "https://link",
        sistOppdatert: LocalDateTime = nowTruncatedToMillis(),
        sikkerhetsnivaa: Int = 4,
        aktiv: Boolean = true,
        eksternVarsling: Boolean = false,
        prefererteKanaler: List<String> = emptyList()
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
