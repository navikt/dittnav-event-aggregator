package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import java.time.Instant

object AvroBeskjedObjectMother {

    fun createBeskjed(i: Int): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                "12345",
                "100$i",
                "Dette er Beskjed til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

    fun createBeskjed(i: Int, fodselsnummer: String): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$i",
                "Dette er Beskjed til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
