package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import java.time.Instant

object AvroBeskjedObjectMother {

    private val defaultLopenummer = 1
    private val defaultText = "Dette er Beskjed til brukeren"
    private val defaultEksternVarsling = true

    fun createBeskjedWithText(text: String): BeskjedIntern {
        return createBeskjed(defaultLopenummer, text)
    }

    fun createBeskjed(lopenummer: Int): BeskjedIntern {
        return createBeskjed(lopenummer, defaultText)
    }

    fun createBeskjed(lopenummer: Int, text: String): BeskjedIntern {
        return BeskjedIntern(
                lopenummer.toString(),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                "100$lopenummer",
                text,
                "https://nav.no/systemX/$lopenummer",
                4,
                defaultEksternVarsling)
    }

    fun createBeskjedWithoutSynligFremTilSatt(): BeskjedIntern {
        return BeskjedIntern(
                defaultLopenummer.toString(),
                Instant.now().toEpochMilli(),
                null,
                "100$defaultLopenummer",
                defaultText,
                "https://nav.no/systemX/$defaultLopenummer",
                4,
                defaultEksternVarsling)
    }

}
