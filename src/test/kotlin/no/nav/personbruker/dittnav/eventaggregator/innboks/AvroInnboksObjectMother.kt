package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.Innboks
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import java.time.Instant

object AvroInnboksObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnummer = "12345"
    private val defaultText = "Dette er innboksnotifikasjon til brukeren"
    private val defaultEksternVarsling = true
    private val defaultPrefererteKanaler = listOf(PreferertKanal.EPOST.toString(), PreferertKanal.SMS.toString())

    fun createInnboks(lopenummer: Int): Innboks {
        return createInnboks(lopenummer, defaultFodselsnummer)
    }

    fun createInnboks(lopenummer: Int, fodselsnummer: String): Innboks {
        return createInnboks(lopenummer, fodselsnummer, defaultText, defaultEksternVarsling, defaultPrefererteKanaler)
    }

    fun createInnboksWithText(text: String): Innboks {
        return createInnboks(defaultLopenummer, defaultFodselsnummer, text, defaultEksternVarsling, defaultPrefererteKanaler)
    }

    fun createInnboksWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Innboks {
        return createInnboks(defaultLopenummer, defaultFodselsnummer, defaultText, eksternVarsling, prefererteKanaler)
    }

    private fun createInnboks(lopenummer: Int, fodselsnummer: String, text: String, eksternVarsling: Boolean, prefererteKanaler: List<String>): Innboks {
        return Innboks(
                Instant.now().toEpochMilli(),
                fodselsnummer,
                "100$lopenummer",
                text,
                "https://nav.no/systemX/$lopenummer",
                4,
                eksternVarsling,
                prefererteKanaler)
    }
}
