package no.nav.personbruker.dittnav.eventaggregator.melding

import java.time.LocalDateTime
import java.time.ZoneId

object MeldingObjectMother {

    fun createMelding(eventId: String, aktorId: String): Melding {
        return Melding(
                "DittNav",
                LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId,
                eventId,
                "100$eventId",
                "Dette er melding til brukeren",
                "https://nav.no/systemX/$aktorId",
                4,
                true)
    }
}