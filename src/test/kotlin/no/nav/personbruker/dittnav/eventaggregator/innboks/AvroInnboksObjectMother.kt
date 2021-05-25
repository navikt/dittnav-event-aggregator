package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import java.time.Instant

object AvroInnboksObjectMother {

    private val defaultLopenummer = 1
    private val defaultText = "Dette er innboksnotifikasjon til brukeren"

    fun createInnboks(lopenummer: Int): InnboksIntern {
        return createInnboks(lopenummer, defaultText)
    }

    fun createInnboksWithText(text: String): InnboksIntern {
        return createInnboks(defaultLopenummer, text)
    }

    fun createInnboks(lopenummer: Int, text: String): InnboksIntern {
        return InnboksIntern(
                lopenummer.toString(),
                Instant.now().toEpochMilli(),
                "100$lopenummer",
                text,
                "https://nav.no/systemX/$lopenummer",
                4)
    }

}
