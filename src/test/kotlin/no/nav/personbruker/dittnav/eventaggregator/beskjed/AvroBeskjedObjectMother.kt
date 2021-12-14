package no.nav.personbruker.dittnav.eventaggregator.beskjed

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object AvroBeskjedObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnr = "12345"
    private val defaultText = "Dette er Beskjed til brukeren"
    private val defaultEksternVarsling = true
    private val defaultPrefererteKanaler = listOf(PreferertKanal.EPOST.toString(), PreferertKanal.SMS.toString())

    fun createBeskjedWithText(text: String): Beskjed {
        return createBeskjed(defaultLopenummer, defaultFodselsnr, text)
    }

    fun createBeskjed(lopenummer: Int): Beskjed {
        return createBeskjed(lopenummer, defaultFodselsnr, defaultText)
    }

    fun createBeskjedWithFodselsnummer(fodselsnummer: String): Beskjed {
        return createBeskjed(defaultLopenummer, fodselsnummer, defaultText)
    }

    fun createBeskjedWithEksternVarslingAndPrefererteKanaler(eksternVarsling: Boolean, prefererteKanaler: List<String>): Beskjed {
        return Beskjed(
            Instant.now().toEpochMilli(),
            Instant.now().toEpochMilli(),
            defaultFodselsnr,
            "100$defaultLopenummer",
            defaultText,
            "https://nav.no/systemX/$defaultLopenummer",
            4,
            eksternVarsling,
            prefererteKanaler
        )
    }

    fun createBeskjed(
        lopenummer: Int,
        fodselsnummer: String,
        text: String,
        tidspunkt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")),
        synligFremTil: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    ): Beskjed {
        return Beskjed(
            tidspunkt.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
            synligFremTil.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli(),
            fodselsnummer,
            "100$lopenummer",
            text,
            "https://nav.no/systemX/$lopenummer",
            4,
            defaultEksternVarsling,
            defaultPrefererteKanaler
        )
    }

    fun createBeskjedWithoutSynligFremTilSatt(): Beskjed {
        return Beskjed(
                Instant.now().toEpochMilli(),
                null,
                defaultFodselsnr,
                "100$defaultLopenummer",
                defaultText,
                "https://nav.no/systemX/$defaultLopenummer",
                4,
                defaultEksternVarsling,
                defaultPrefererteKanaler)
    }
}
