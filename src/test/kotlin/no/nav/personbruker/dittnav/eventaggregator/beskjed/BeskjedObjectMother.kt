package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime
import java.time.ZoneId

object BeskjedObjectMother {

    fun createBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        return Beskjed(
                produsent = "DittNav",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                synligFremTil = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "100$fodselsnummer",
                tekst = "Dette er beskjed til brukeren",
                link = "https://nav.no/systemX/$fodselsnummer",
                sistOppdatert = LocalDateTime.now(ZoneId.of("Europe/Oslo")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }
}
