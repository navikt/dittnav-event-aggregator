package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeTestHelper.nowAtUtcTruncated
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.Companion.fromInternal
import java.time.LocalDateTime

object EksternVarslingStatusTestData {

    fun createEksternvarslingStatus(
        eventId: String,
        status: EksternStatus,
        kanal: String,
        renotifikasjon: Boolean = false,
        melding: String = "dummyMelding",
        distribusjonsId: Long = 1L,
        sistOppdatert: LocalDateTime =  nowAtUtcTruncated()
    ) = EksternVarslingStatus (
        eventId = eventId,
        kanaler = listOf(kanal),
        sistMottattStatus = fromInternal(status).name,
        eksternVarslingSendt = status == EksternStatus.Sendt,
        renotifikasjonSendt = renotifikasjon,
        historikk = listOf(
            EksternVarslingHistorikkEntry(
                melding = melding,
                status = status,
                distribusjonsId = distribusjonsId,
                kanal = kanal,
                renotifikasjon = renotifikasjon,
                tidspunkt = sistOppdatert
            )
        ),
        sistOppdatert = sistOppdatert
    )
}

