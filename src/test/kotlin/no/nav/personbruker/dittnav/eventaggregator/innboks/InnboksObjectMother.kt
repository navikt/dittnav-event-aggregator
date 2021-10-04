package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.LocalDateTime
import java.time.ZoneId

object InnboksObjectMother {

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
        return Innboks(
                systembruker,
                "dummyNamespace",
                "dummyAppnavn",
                eventId,
                LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer,
                "76543",
                "Dette er innboksnotifikasjon til brukeren",
                link,
                4,
                LocalDateTime.now(ZoneId.of("UTC")),
                true)
    }

    fun giveMeInaktivInnboks(): Innboks {
        return Innboks(
                "dummySystembruker",
                "dummyNamespace",
                "dummyAppnavn",
                "76543",
                LocalDateTime.now(ZoneId.of("UTC")),
                "123",
                "76543",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/",
                4,
                LocalDateTime.now(ZoneId.of("UTC")),
                false)
    }

}
