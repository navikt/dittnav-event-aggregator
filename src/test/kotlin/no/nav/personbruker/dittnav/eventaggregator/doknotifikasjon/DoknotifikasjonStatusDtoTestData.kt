package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowAtUtcTruncated
import java.time.LocalDateTime

object DoknotifikasjonStatusDtoTestData {

    fun createDoknotifikasjonStatusDto(
        eventId: String,
        bestillerAppnavn: String = "dummyBestiller",
        status: String =  "INFO",
        melding: String = "dummyMelding",
        distribusjonsId: Long = 1L,
        kanal: String? = null,
        tidspunkt: LocalDateTime = nowAtUtcTruncated()
    ): DoknotifikasjonStatusDto {
        return DoknotifikasjonStatusDto(
            eventId = eventId,
            bestillerAppnavn = bestillerAppnavn,
            status = status,
            melding = melding,
            distribusjonsId = distribusjonsId,
            kanal = kanal,
            tidspunkt = tidspunkt
        )
    }
}

