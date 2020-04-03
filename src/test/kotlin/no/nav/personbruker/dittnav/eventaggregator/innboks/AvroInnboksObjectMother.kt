package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import java.time.Instant

object AvroInnboksObjectMother {

    fun createInnboks(i: Int): Innboks {
        return Innboks(
                Instant.now().toEpochMilli(),
                "12345",
                "100$i",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

    fun createInnboks(i: Int, fodselsnummer: String): Innboks {
        return Innboks(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$i",
                "Dette er innboksnotifikasjon til brukeren",
                "https://nav.no/systemX/$i",
                4)
    }

}
