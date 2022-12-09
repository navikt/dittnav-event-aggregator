package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import java.time.LocalDateTime

object BeskjedTestData {

    fun beskjed(
        systembruker: String = "systembruker",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        eventTidspunkt: LocalDateTime = nowTruncatedToMillis(),
        forstBehandlet: LocalDateTime = nowTruncatedToMillis(),
        synligFremTil: LocalDateTime = nowTruncatedToMillis().plusDays(1),
        fodselsnummer: String = "123",
        eventId: String = "12345",
        grupperingsId: String = "grupperingsId",
        tekst: String = "tekst",
        link: String = "http://link",
        sistOppdatert: LocalDateTime = nowTruncatedToMillis(),
        sikkerhetsnivaa: Int = 4,
        aktiv: Boolean = true,
        eksternVarsling: Boolean = false,
        prefererteKanaler: List<String> = emptyList(),
        smsVarslingstekst: String = "smsVarslingstekst",
        epostVarslingstekst: String = "epostVarslingstekst",
        epostVarslingstittel: String = "epostVarslingstittel"
    ) = Beskjed(
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
        prefererteKanaler = prefererteKanaler,
        smsVarslingstekst = smsVarslingstekst,
        epostVarslingstekst = epostVarslingstekst,
        epostVarslingstittel = epostVarslingstittel
    )
}
