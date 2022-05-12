package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object StatusoppdateringObjectMother {

    private const val defaultSystembruker = "systembruker"
    private const val defaultNamespace = "namespace"
    private const val defaultAppnavn = "appnavn"
    private const val defaultFodselsnummer = "123"
    private const val defaultEventId = "12345"
    private const val defaultGrupperingsId = "grupperingsId"
    private const val defaultLink = "http://link"
    private const val defaultSikkerhetsnivaa = 4
    private const val defaultStatusGlobal = "SENDT"
    private const val defaultStatusIntern = "dummyStatusIntern"
    private const val defaultSakstema = "dummySakstema"

    fun giveMeStatusoppdatering(eventId: String, fodselsnummer: String): Statusoppdatering {
        return giveMeStatusoppdatering(eventId = eventId, fodselsnummer = fodselsnummer, systembruker = "dummySystembruker", link = "https://nav.no/systemX/$eventId")
    }

    fun giveMeStatusoppdateringWithLink(link: String): Statusoppdatering {
        return giveMeStatusoppdatering(eventId = "s-1", fodselsnummer = "1234", systembruker = "dummySystemUser", link = link)
    }

    fun giveMeStatusoppdateringWithForstBehandlet(eventId: String, fodselsnummer: String, forstbehandlet: LocalDateTime): Statusoppdatering {
        return giveMeStatusoppdatering(eventId = eventId, fodselsnummer = fodselsnummer, forstBehandlet = forstbehandlet)
    }


    fun giveMeStatusoppdatering(
        systembruker: String = defaultSystembruker,
        namespace: String = defaultNamespace,
        appnavn: String = defaultAppnavn,
        eventId: String = defaultEventId,
        eventTidspunkt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        forstBehandlet: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        fodselsnummer: String = defaultFodselsnummer,
        grupperingsId: String = defaultGrupperingsId,
        link: String = defaultLink,
        sikkerhetsnivaa: Int = defaultSikkerhetsnivaa,
        sistOppdatert: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        statusGlobal: String = defaultStatusGlobal,
        statusIntern: String = defaultStatusIntern,
        sakstema: String = defaultSakstema,
    ) = Statusoppdatering(
        systembruker = systembruker,
        namespace = namespace,
        appnavn = appnavn,
        eventId = eventId,
        eventTidspunkt = eventTidspunkt,
        forstBehandlet = forstBehandlet,
        fodselsnummer = fodselsnummer,
        grupperingsId = grupperingsId,
        link = link,
        sikkerhetsnivaa = sikkerhetsnivaa,
        sistOppdatert = sistOppdatert,
        statusGlobal = statusGlobal,
        statusIntern = statusIntern,
        sakstema = sakstema
    )

}
