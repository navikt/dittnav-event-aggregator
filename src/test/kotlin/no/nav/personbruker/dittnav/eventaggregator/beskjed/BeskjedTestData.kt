package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import java.time.LocalDateTime

object BeskjedTestData {

    private const val defaultSystembruker = "systembruker"
    private const val defaultNamespace = "namespace"
    private const val defaultAppnavn = "appnavn"
    private const val defaultFodselsnummer = "123"
    private const val defaultEventId = "12345"
    private const val defaultGrupperingsId = "grupperingsId"
    private const val defaultTekst = "tekst"
    private const val defaultLink = "http://link"
    private const val defaultSikkerhetsnivaa = 4
    private const val defaultAktiv = true
    private const val defaultEksternVarsling = false


    fun aktivBeskjed(
        eventId: String = "b1",
        synligFremTil: LocalDateTime = nowTruncatedToMillis().plusDays(1),
        fodselsnummer: String = "123",
        systembruker: String = "dummySystembruker",
    ): Beskjed {
        return beskjed(
            eventId = eventId,
            fodselsnummer = fodselsnummer,
            synligFremTil = synligFremTil,
            systembruker = systembruker,
            aktiv = true,
            link = "https://nav.no/systemX/$eventId"
        )
    }

    fun inaktivBeskjed(eventId: String = "b-1", fodselsnummer: String = "123"): Beskjed {
        return beskjed(eventId = eventId, fodselsnummer = fodselsnummer, aktiv = false)
    }

    fun beskjedWithForstBehandlet(
        eventId: String = "b-1",
        fodselsnummer: String = "123",
        forstBehandlet: LocalDateTime
    ): Beskjed {
        return beskjed(eventId = eventId, fodselsnummer = fodselsnummer, forstBehandlet = forstBehandlet)
    }

    fun aktivBeskjedWithEksternVarslingAndPrefererteKanaler(
        eksternVarsling: Boolean,
        prefererteKanaler: List<String>
    ): Beskjed {
        val beskjed = beskjed(
            eventId = "B-3",
            fodselsnummer = "1234",
            systembruker = "dummySystembruker",
            link = "https://nav.no/systemX/"
        )
        return beskjed.copy(eksternVarsling = eksternVarsling, prefererteKanaler = prefererteKanaler)
    }

    fun beskjed(
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
        eksternVarsling: Boolean = defaultEksternVarsling
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
        eksternVarsling = eksternVarsling
    )
}
