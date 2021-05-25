package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.internal.StatusoppdateringIntern
import java.time.Instant

object AvroStatusoppdateringObjectMother {

    private val defaultLopenummer = 1
    private val defaultLink = "http://dummyLink"
    private val defaultSikkerhetsnivaa = 4
    private val defaultStatusGlobal = "SENDT"
    private val defaultStatusIntern = "dummyStatusIntern"
    private val defaultSakstema = "dummySakstema"

    fun createStatusoppdatering(lopenummer: Int): StatusoppdateringIntern {
        return createStatusoppdatering(lopenummer, defaultStatusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithStatusGlobal(statusGlobal: String): StatusoppdateringIntern {
        return createStatusoppdatering(defaultLopenummer, statusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithStatusIntern(statusIntern: String?): StatusoppdateringIntern {
        return createStatusoppdatering(defaultLopenummer, defaultStatusGlobal, statusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithSakstema(sakstema: String): StatusoppdateringIntern {
        return createStatusoppdatering(defaultLopenummer, defaultStatusGlobal, defaultStatusIntern, sakstema)
    }

    fun createStatusoppdatering(lopenummer: Int, statusGlobal: String, statusIntern: String?, sakstema: String): StatusoppdateringIntern {
        return StatusoppdateringIntern(
                lopenummer.toString(),
                Instant.now().toEpochMilli(),
                "100$lopenummer",
                defaultLink,
                defaultSikkerhetsnivaa,
                statusGlobal,
                statusIntern,
                sakstema)
    }

}
