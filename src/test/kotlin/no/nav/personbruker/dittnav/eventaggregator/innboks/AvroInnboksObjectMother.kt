package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import java.time.Instant

object AvroInnboksObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnummer = "12345"
    private val defaultText = "Dette er innboksnotifikasjon til brukeren"

    fun createInnboks(lopenummer: Int): Innboks {
        return createInnboks(lopenummer, defaultFodselsnummer)
    }

    fun createInnboks(lopenummer: Int, fodselsnummer: String): Innboks {
        return createInnboks(lopenummer, fodselsnummer, defaultText)
    }

    fun createInnboksWithText(text: String): Innboks {
        return createInnboks(defaultLopenummer, defaultFodselsnummer, text)
    }

    fun createInnboks(lopenummer: Int, fodselsnummer: String, text: String): Innboks {
        return Innboks(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer",
                text,
                "https://nav.no/systemX/$lopenummer",
                4)
    }

}
