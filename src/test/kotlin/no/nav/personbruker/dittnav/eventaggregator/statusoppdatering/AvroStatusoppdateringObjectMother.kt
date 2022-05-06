package no.nav.personbruker.dittnav.eventaggregator.statusoppdatering

import no.nav.brukernotifikasjon.schemas.internal.StatusoppdateringIntern
import java.time.Instant

object AvroStatusoppdateringObjectMother {

    private val defaultLink = "http://dummyLink"
    private val defaultSikkerhetsnivaa = 4
    private val defaultStatusGlobal = "SENDT"
    private val defaultStatusIntern = "dummyStatusIntern"
    private val defaultSakstema = "dummySakstema"

    fun createStatusoppdatering(): StatusoppdateringIntern {
        return createStatusoppdatering(defaultStatusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithStatusGlobal(statusGlobal: String): StatusoppdateringIntern {
        return createStatusoppdatering(statusGlobal, defaultStatusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithStatusIntern(statusIntern: String?): StatusoppdateringIntern {
        return createStatusoppdatering(defaultStatusGlobal, statusIntern, defaultSakstema)
    }

    fun createStatusoppdateringWithSakstema(sakstema: String): StatusoppdateringIntern {
        return createStatusoppdatering(defaultStatusGlobal, defaultStatusIntern, sakstema)
    }

    fun createStatusoppdatering(statusGlobal: String, statusIntern: String?, sakstema: String): StatusoppdateringIntern {
        return StatusoppdateringIntern(
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                defaultLink,
                defaultSikkerhetsnivaa,
                statusGlobal,
                statusIntern,
                sakstema)
    }

    fun createStatusoppdateringWithTidspunktAndBehandlet(tidspunkt: Long, behandlet: Long?): StatusoppdateringIntern {
        return StatusoppdateringIntern(
            tidspunkt,
            behandlet,
            defaultLink,
            defaultSikkerhetsnivaa,
            defaultStatusGlobal,
            defaultStatusIntern,
            defaultSakstema
        )
    }

}
