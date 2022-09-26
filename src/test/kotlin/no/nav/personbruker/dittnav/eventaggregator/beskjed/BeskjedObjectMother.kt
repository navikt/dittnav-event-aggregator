package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowTruncatedToMillis
import java.time.LocalDateTime

object BeskjedObjectMother {

    const val defaultSystembruker = "systembruker"
    const val defaultNamespace = "namespace"
    const val defaultAppnavn = "appnavn"
    const val defaultFodselsnummer = "123"
    const val defaultEventId = "12345"
    const val defaultGrupperingsId = "grupperingsId"
    const val defaultTekst = "tekst"
    const val defaultLink = "http://link"
    const val defaultSikkerhetsnivaa = 4
    const val defaultAktiv = true
    const val defaultEksternVarsling = false

    fun giveMeTwoAktiveBeskjeder(): List<Beskjed> {
        return listOf(
                giveMeAktivBeskjed("b-1", "123"),
                giveMeAktivBeskjed("b-2", "123")
        )
    }

    fun giveMeAktivBeskjed(): Beskjed {
        return giveMeBeskjed(eventId = "b-1", fodselsnummer = "123")
    }

    fun giveMeInaktivBeskjed(): Beskjed {
        return giveMeBeskjed(eventId = "b-1", fodselsnummer = "123", aktiv = false)
    }

    fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        return giveMeBeskjed(eventId = eventId, fodselsnummer = fodselsnummer, systembruker = "dummySystembruker", link = "https://nav.no/systemX/$eventId")
    }

    fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String, systembruker: String): Beskjed {
        return giveMeBeskjed(eventId = eventId, fodselsnummer = fodselsnummer, systembruker =  systembruker, link = "https://nav.no/systemX/$eventId")
    }

    fun giveMeBeskjedWithForstBehandlet(forstBehandlet: LocalDateTime): Beskjed {
        return giveMeBeskjed(forstBehandlet = forstBehandlet)
    }

    fun giveMeBeskjedWithForstBehandlet(eventId: String, fodselsnummer: String, forstBehandlet: LocalDateTime): Beskjed {
        return giveMeBeskjed(eventId = eventId, fodselsnummer = fodselsnummer, forstBehandlet = forstBehandlet)
    }

    fun giveMeAktivBeskjedWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Beskjed {
        val beskjed = giveMeBeskjed(eventId = "B-3", fodselsnummer = "1234", systembruker = "dummySystembruker", link = "https://nav.no/systemX/")
        return beskjed.copy(eksternVarsling = eksternVarsling, prefererteKanaler = prefererteKanaler)
    }

    fun giveMeBeskjedWithEventIdAndAppnavn(eventId: String, appnavn: String): Beskjed {
        return giveMeBeskjed(
            eventId = eventId,
            appnavn = appnavn
        )
    }

    fun giveMeBeskjed(
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
