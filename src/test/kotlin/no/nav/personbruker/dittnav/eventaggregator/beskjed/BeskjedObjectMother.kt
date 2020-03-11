package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

object BeskjedObjectMother {

    fun createBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        return Beskjed(
                uid = Random.nextInt(1,100).toString(),
                produsent = "DittNAV",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "100$fodselsnummer",
                tekst = "Dette er beskjed til brukeren",
                link = "https://nav.no/systemX/$fodselsnummer",
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }
}
