package no.nav.personbruker.dittnav.eventaggregator.statusOppdatering

import no.nav.brukernotifikasjon.schemas.StatusOppdatering
import java.time.Instant

object AvroStatusOppdateringObjectMother {

    private val defaultLopenummer = 1
    private val defaultFodselsnr = "12345"
    private val defaultLink = "dummyLink"
    private val defaultSikkerhetsnivaa = 4
    private val defaultStatusGlobal = "SENDT"
    private val defaultStatusIntern = "dummyStatusIntern"
    private val defaultSakstema = "dummySakstema"

    fun createStatusOppdatering(lopenummer: Int): StatusOppdatering {
        return createStatusOppdatering(lopenummer, defaultFodselsnr, defaultStatusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusOppdateringWithFodselsnummer(fodselsnummer: String): StatusOppdatering {
        return createStatusOppdatering(defaultLopenummer, fodselsnummer, defaultStatusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusOppdateringWithStatusGlobal(statusGlobal: String): StatusOppdatering {
        return createStatusOppdatering(defaultLopenummer, defaultFodselsnr, statusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusOppdateringWithStatusIntern(statusIntern: String?): StatusOppdatering {
        return createStatusOppdatering(defaultLopenummer, defaultFodselsnr, defaultStatusGlobal, statusIntern, defaultSakstema)
    }

    fun createStatusOppdateringWithSakstema(sakstema: String): StatusOppdatering {
        return createStatusOppdatering(defaultLopenummer, defaultFodselsnr, defaultStatusGlobal, defaultStatusIntern, sakstema)
    }

    fun createStatusOppdatering(lopenummer: Int, fodselsnummer: String, statusGlobal: String, statusIntern: String?, sakstema: String): StatusOppdatering {
        return StatusOppdatering(
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
