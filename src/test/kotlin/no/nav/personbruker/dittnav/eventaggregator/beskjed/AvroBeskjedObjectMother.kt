package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import java.time.Instant

object AvroBeskjedObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnr = "12345"
    private val defaultText = "Dette er Beskjed til brukeren"

    fun createBeskjedWithText(text: String): Beskjed {
        return createBeskjed(defaultLopenummer, defaultFodselsnr, text)
    }

    fun createBeskjed(lopenummer: Int): Beskjed {
        return createBeskjed(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createBeskjedWithFodselsnummer(fodselsnummer: String): Beskjed {
        return createBeskjed(defaultLopenummer, fodselsnummer, defaultText)
    }

    fun createBeskjed(lopenummer: Int, fodselsnummer: String, text: String): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer",
                text,
                "https://nav.no/systemX/$lopenummer",
                4)
    }

    fun createBeskjedWithotSynligFremTilSatt(): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                null,
                defaultFodselsnr,
                "100$defaultLopenummer",
                defaultText,
                "https://nav.no/systemX/$defaultLopenummer",
                4)
    }

}
