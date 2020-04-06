package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import java.time.Instant

object AvroBeskjedObjectMother {

    fun createBeskjedWithText(text: String): Beskjed {
        return createBeskjed(1, "12345", text)
    }

    fun createBeskjed(i: Int): Beskjed {
        return createBeskjed(i, "12345", "Dette er Beskjed til brukeren")
    }

    fun createBeskjedWithFodselsnummer(fodselsnummer: String): Beskjed {
        return createBeskjed(1, fodselsnummer, "Dette er Beskjed til brukeren")
    }

    fun createBeskjed(i: Int, fodselsnummer: String, tekst: String): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$i",
                tekst,
                "https://nav.no/systemX/$i",
                4)
    }

}
