package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.LocalDateTime
import java.time.ZoneId

object InnboksObjectMother {

    fun createInnboks(eventId: String, fodselsnummer: String): Innboks {
        return Innboks(
                "DittNav",
                eventId,
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                fodselsnummer,
                "100$eventId",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/$fodselsnummer",
                4,
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                true)
    }
}