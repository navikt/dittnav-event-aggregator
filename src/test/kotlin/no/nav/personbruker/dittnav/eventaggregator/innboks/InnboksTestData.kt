package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowAtUtcTruncated
import java.time.LocalDateTime

object InnboksTestData {

    fun innboks(
        systembruker: String = "systembruker",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        eventTidspunkt: LocalDateTime = nowAtUtcTruncated(),
        forstBehandlet: LocalDateTime = nowAtUtcTruncated(),
        fodselsnummer: String = "123",
        eventId: String = "76543",
        grupperingsId: String = "grupperingsId",
        tekst: String = "tekst",
        link: String = "http://link",
        sistOppdatert: LocalDateTime = nowAtUtcTruncated(),
        sikkerhetsnivaa: Int = 4,
        aktiv: Boolean = true,
        eksternVarsling: Boolean = false,
        prefererteKanaler: List<String> = emptyList(),
        smsVarslingstekst: String = "smsVarslingstekst",
        epostVarslingstekst: String = "epostVarslingstekst",
        epostVarslingstittel: String = "epostVarslingstittel"
    ) = Innboks(
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
        aktiv = aktiv,
        eksternVarsling = eksternVarsling,
        prefererteKanaler = prefererteKanaler,
        smsVarslingstekst = smsVarslingstekst,
        epostVarslingstekst = epostVarslingstekst,
        epostVarslingstittel = epostVarslingstittel
    )

}
