package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import java.time.LocalDateTime

data class DoknotStatusDTO(
    val eventId: String,
    val status: String,
    val melding: String,
    val distribusjonsId: Long,
    val antallOppdateringer: Int,
    val tidspunkt: LocalDateTime
)
