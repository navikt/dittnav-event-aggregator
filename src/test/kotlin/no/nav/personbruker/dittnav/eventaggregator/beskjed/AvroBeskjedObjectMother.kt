package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.internal.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.internal.BeskjedIntern
import java.time.Instant

object AvroBeskjedObjectMother {

    private val defaultLopenummer = 1
    private val defaultText = "Dette er Beskjed til brukeren"
    private val defaultEksternVarsling = true
    private val defaultPrefererteKanaler = listOf(PreferertKanal.EPOST.toString(), PreferertKanal.SMS.toString())

    fun createBeskjed(lopenummer: Int): BeskjedIntern {
        return createBeskjed(lopenummer, defaultText)
    }

    fun createBeskjedWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): BeskjedIntern {
        return BeskjedIntern(
            defaultLopenummer.toString(),
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli(),
            "100$defaultLopenummer",
            defaultText,
            "https://nav.no/systemX/$defaultLopenummer",
            4,
            eksternVarsling,
            prefererteKanaler
        )
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
                defaultEksternVarsling,
                defaultPrefererteKanaler)
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
                defaultEksternVarsling,
                defaultPrefererteKanaler)
    }
}
