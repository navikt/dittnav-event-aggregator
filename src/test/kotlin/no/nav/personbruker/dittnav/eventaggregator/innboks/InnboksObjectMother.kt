package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedObjectMother
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksObjectMother
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object InnboksObjectMother {

    const val defaultSystembruker = "systembruker"
    const val defaultNamespace = "namespace"
    const val defaultAppnavn = "appnavn"
    const val defaultFodselsnummer = "123"
    const val defaultEventId = "76543"
    const val defaultGrupperingsId = "grupperingsId"
    const val defaultTekst = "tekst"
    const val defaultLink = "http://link"
    const val defaultSikkerhetsnivaa = 4
    const val defaultAktiv = true
    const val defaultEksternVarsling = false

    fun giveMeAktivInnboks(): Innboks {
        return giveMeAktivInnboks("i-1", "123")
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String): Innboks {
        return giveMeAktivInnboks(eventId = eventId, fodselsnummer = fodselsnummer, "dummySystembruker", "https://nav.no/systemX/")
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String, systembruker: String): Innboks {
        return giveMeAktivInnboks(eventId = eventId, fodselsnummer = fodselsnummer, systembruker = systembruker, link = "https://nav.no/systemX/")
    }

    fun giveMeAktivInnboksWithLink(link: String): Innboks {
        return giveMeAktivInnboks(eventId = "i-2", fodselsnummer = "123", systembruker = "dummySystembruker", link = link)
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String, systembruker: String, link: String): Innboks {
        return giveMeInnboks(
            aktiv = true,
            eventId = eventId,
            fodselsnummer = fodselsnummer,
            systembruker = systembruker,
            link = link
        )
    }

    fun giveMeInaktivInnboks(): Innboks {
        return giveMeInnboks(aktiv = false)
    }

    fun giveMeInnboksWithForstBehandlet(eventId: String, fodselsnummer: String, forstBehandlet: LocalDateTime): Innboks {
        return giveMeInnboks(eventId = eventId, fodselsnummer = fodselsnummer, forstBehandlet = forstBehandlet)
    }

    fun giveMeAktivInnboksWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Innboks {
        val innboks = giveMeInnboks(
            eventId = "B-3",
            fodselsnummer = "1234",
            systembruker = "dummySystembruker",
            link = "https://nav.no/systemX/"
        )
        return innboks.copy(eksternVarsling = eksternVarsling, prefererteKanaler = prefererteKanaler)
    }

    fun giveMeInnboksWithEventIdAndAppnavn(eventId: String, appnavn: String): Innboks {
        return giveMeInnboks(eventId = eventId, appnavn = appnavn)
    }

    private fun giveMeInnboks(
        systembruker: String = defaultSystembruker,
        namespace: String = defaultNamespace,
        appnavn: String = defaultAppnavn,
        eventTidspunkt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        forstBehandlet: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        fodselsnummer: String = defaultFodselsnummer,
        eventId: String = defaultEventId,
        grupperingsId: String = defaultGrupperingsId,
        tekst: String = defaultTekst,
        link: String = defaultLink,
        sistOppdatert: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS),
        sikkerhetsnivaa: Int = defaultSikkerhetsnivaa,
        aktiv: Boolean = defaultAktiv,
        eksternVarsling: Boolean = defaultEksternVarsling
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
        eksternVarsling = eksternVarsling
    )

}
