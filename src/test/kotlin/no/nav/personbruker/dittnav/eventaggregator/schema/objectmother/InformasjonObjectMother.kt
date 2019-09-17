package no.nav.personbruker.dittnav.eventaggregator.schema.objectmother

import no.nav.brukernotifikasjon.schemas.Informasjon
import java.time.Instant

object InformasjonObjectMother {

    fun createInformasjon(i: Int): Informasjon {
        return Informasjon(
                "DittNAV",
                Instant.now().toEpochMilli(),
                "12345",
                i.toString(),
                "100$i",
                "Dette er informasjon til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
