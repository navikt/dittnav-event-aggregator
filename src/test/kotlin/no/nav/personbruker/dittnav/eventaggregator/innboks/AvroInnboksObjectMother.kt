package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import java.time.Instant

object AvroInnboksObjectMother {

    fun createInnboks(i: Int): Innboks {
        return Innboks(
                "DittNAV",
                Instant.now().toEpochMilli(),
                "12345",
                i.toString(),
                "100$i",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
