package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.LocalDateTime
import java.time.ZoneId

object InnboksObjectMother {

    fun giveMeAktivInnboks(): Innboks {
        return giveMeAktivInnboks("i-1", "123")
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String): Innboks {
        return giveMeAktivInnboks(eventId, fodselsnummer, "dummySystembruker")
    }

    fun giveMeAktivInnboks(eventId: String, fodselsnummer: String, systembruker: String): Innboks {
        return Innboks(
                systembruker,
                eventId,
                LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer,
                "76543",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/",
                4,
                LocalDateTime.now(ZoneId.of("UTC")),
                true)
    }

    fun giveMeInaktivInnboks(): Innboks {
        return Innboks(
                "dummySystembruker",
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
