package no.nav.personbruker.dittnav.eventaggregator.beskjed

import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.random.Random

object BeskjedObjectMother {

    fun giveMeAktivBeskjed(): Beskjed {
        return giveMeAktivBeskjed("b-1", "123")
    }

    fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String): Beskjed {
        val produsent = "DittNAV"
        return giveMeAktivBeskjed(eventId, fodselsnummer, produsent)
    }

    fun giveMeAktivBeskjed(eventId: String, fodselsnummer: String, produsent: String): Beskjed {
        return Beskjed(
                uid = Random.nextInt(1,100).toString(),
                produsent = produsent,
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = fodselsnummer,
                eventId = eventId,
                grupperingsId = "systemA010",
                tekst = "Dette er beskjed til brukeren",
                link = "https://nav.no/systemX/$eventId",
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                sikkerhetsnivaa = 4,
                aktiv = true)
    }

    fun giveMeInaktivBeskjed(): Beskjed {
        return Beskjed(
                uid = Random.nextInt(1,100).toString(),
                produsent = "DittNAV",
                eventTidspunkt = LocalDateTime.now(ZoneId.of("UTC")),
                synligFremTil = LocalDateTime.now(ZoneId.of("UTC")),
                fodselsnummer = "123",
                eventId = "b-2",
                grupperingsId = "65432",
                tekst = "Dette er beskjed til brukeren",
                link = "https://nav.no/systemX/",
                sistOppdatert = LocalDateTime.now(ZoneId.of("UTC")),
                sikkerhetsnivaa = 4,
                aktiv = false)
    }

}
