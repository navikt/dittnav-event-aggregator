package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.Statusoppdatering
import java.time.Instant

object AvroStatusoppdateringObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnr = "12345"
    private val defaultLink = "dummyLink"
    private val defaultSikkerhetsnivaa = 4
    private val defaultStatusGlobal = "SENDT"
    private val defaultStatusIntern = "dummyStatusIntern"
    private val defaultSakstema = "dummySakstema"

    fun createStatusoppdatering(lopenummer: Int): Statusoppdatering {
        return createStatusoppdatering(lopenummer, defaultFodselsnr, defaultStatusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithFodselsnummer(fodselsnummer: String): Statusoppdatering {
        return createStatusoppdatering(defaultLopenummer, fodselsnummer, defaultStatusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithStatusGlobal(statusGlobal: String): Statusoppdatering {
        return createStatusoppdatering(defaultLopenummer, defaultFodselsnr, statusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithStatusIntern(statusIntern: String?): Statusoppdatering {
        return createStatusoppdatering(defaultLopenummer, defaultFodselsnr, defaultStatusGlobal, statusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithSakstema(sakstema: String): Statusoppdatering {
        return createStatusoppdatering(defaultLopenummer, defaultFodselsnr, defaultStatusGlobal, defaultStatusIntern, sakstema)
    }

    fun createStatusoppdatering(lopenummer: Int, fodselsnummer: String, statusGlobal: String, statusIntern: String?, sakstema: String): Statusoppdatering {
        return Statusoppdatering(
                Instant.now().toEpochMilli(),
                "100$lopenummer",
                defaultLink,
                defaultSikkerhetsnivaa,
                statusGlobal,
                statusIntern,
                sakstema,
                fodselsnummer)
    }

}
