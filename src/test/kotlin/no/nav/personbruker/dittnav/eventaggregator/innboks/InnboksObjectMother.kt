package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.LocalDateTime
import java.time.ZoneId

object InnboksObjectMother {

    fun giveMeInnboks(): Innboks {
        return giveMeInnboks("i-1", "123")
    }

    fun giveMeInnboks(eventId: String, fodselsnummer: String): Innboks {
        return giveMeInnboks(eventId, fodselsnummer, "DittNAV")
    }

    fun giveMeInnboks(eventId: String, fodselsnummer: String, produsent: String): Innboks {
        return Innboks(
                produsent,
                eventId,
                LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer,
                "100$eventId",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/$fodselsnummer",
                4,
                LocalDateTime.now(ZoneId.of("UTC")),
                true)
    }

}
