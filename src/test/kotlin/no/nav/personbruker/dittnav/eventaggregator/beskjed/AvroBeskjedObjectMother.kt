package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import java.time.Instant

object AvroBeskjedObjectMother {

    fun createBeskjed(i: Int): Beskjed {
        return Beskjed(
                "DittNAV",
                Instant.now().toEpochMilli(),
                "12345",
                i.toString(),
                "100$i",
                "Dette er Beskjed til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
