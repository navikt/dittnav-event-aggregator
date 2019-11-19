package no.nav.personbruker.dittnav.eventaggregator.innboks

import java.time.LocalDateTime
import java.time.ZoneId

object InnboksObjectMother {

    fun createInnboks(eventId: String, aktorId: String): Innboks {
        return Innboks(
                "DittNav",
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId,
                eventId,
                "100$eventId",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/$aktorId",
                4,
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                true)
    }
}