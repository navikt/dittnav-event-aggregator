package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime
import java.time.ZoneId

object BeskjedObjectMother {

    fun createBeskjed(eventId: String, aktorId: String): Beskjed {
        return Beskjed(
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                aktorId = aktorId,
                eventId = eventId,
                dokumentId = "100$aktorId",
                tekst = "Dette er beskjed til brukeren",
                link = "https://nav.no/systemX/$aktorId",
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }
}
