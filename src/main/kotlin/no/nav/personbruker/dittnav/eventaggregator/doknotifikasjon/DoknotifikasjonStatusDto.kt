package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import java.time.LocalDateTime

data class DoknotifikasjonStatusDto(
    val eventId: String,
    val bestillerAppnavn: String,
    val status: String,
    val melding: String,
    val distribusjonsId: Long?,
    val kanal: String?,
    val tidspunkt: LocalDateTime
)
