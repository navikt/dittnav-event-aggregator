package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

object BeskjedObjectMother {

    fun giveMeBeskjed(): Beskjed {
        return giveMeBeskjed("b-1", "123")
    }

    fun giveMeBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        val produsent = "DittNAV"
        return giveMeBeskjed(eventId, fodselsnummer, produsent)
    }

    fun giveMeBeskjed(eventId: String, fodselsnummer: String, produsent: String): Beskjed {
        return Beskjed(
                uid = Random.nextInt(1,100).toString(),
                produsent = produsent,
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
