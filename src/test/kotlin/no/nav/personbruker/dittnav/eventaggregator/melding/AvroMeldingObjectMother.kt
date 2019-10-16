package no.nav.personbruker.dittnav.eventaggregator.melding

import no.nav.brukernotifikasjon.schemas.Melding
import java.time.Instant

object AvroMeldingObjectMother {

    fun createMelding(i: Int): Melding {
        return Melding(
                "DittNAV",
                Instant.now().toEpochMilli(),
                "12345",
                i.toString(),
                "100$i",
                "Dette er melding til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
