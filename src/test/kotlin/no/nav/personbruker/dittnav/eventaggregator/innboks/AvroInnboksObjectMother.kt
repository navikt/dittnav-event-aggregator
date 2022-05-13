package no.nav.personbruker.dittnav.eventaggregator.innboks

import no.nav.brukernotifikasjon.schemas.internal.InnboksIntern
import no.nav.brukernotifikasjon.schemas.internal.domain.PreferertKanal
import java.time.Instant

object AvroInnboksObjectMother {

    private val defaultLopenummer = 1
    private val defaultText = "Dette er innboksnotifikasjon til brukeren"
    private val defaultEksternVarsling = true
    private val defaultPrefererteKanaler = listOf(PreferertKanal.EPOST.toString(), PreferertKanal.SMS.toString())


    fun createInnboks(lopenummer: Int): InnboksIntern {
        return createInnboks(lopenummer, defaultText)
    }

    fun createInnboksWithText(text: String): InnboksIntern {
        return createInnboks(defaultLopenummer, text)
    }

    fun createInnboks(lopenummer: Int, text: String): InnboksIntern {
        return InnboksIntern(
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli(),
            text,
            "https://nav.no/systemX/$lopenummer",
            4,
            defaultEksternVarsling,
            defaultPrefererteKanaler,
            null,
            null,
            null
        )
    }

    fun createInnboksWithTidspunktAndBehandlet(tidspunkt: Long, behandlet: Long?): InnboksIntern {
        return InnboksIntern(
            tidspunkt,
            behandlet,
            defaultText,
            "https://nav.no/systemX/$defaultLopenummer",
            4,
            defaultEksternVarsling,
            defaultPrefererteKanaler,
            null,
            null,
            null
        )
    }

}
