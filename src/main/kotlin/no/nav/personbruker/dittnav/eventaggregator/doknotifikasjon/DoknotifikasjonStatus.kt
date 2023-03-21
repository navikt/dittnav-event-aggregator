package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import java.time.LocalDateTime

data class DoknotifikasjonStatusEvent(
    val eventId: String,
    val bestillerAppnavn: String,
    val status: String,
    val melding: String,
    val distribusjonsId: Long?,
    val kanal: String?,
    val tidspunkt: LocalDateTime
)

enum class DoknotifikasjonStatusEnum {
    FEILET, INFO, OVERSENDT, FERDIGSTILT;

    companion object {
        fun fromInternal(status: EksternStatus) = when(status) {
            EksternStatus.Feilet -> FEILET
            EksternStatus.Info -> INFO
            EksternStatus.Bestilt -> OVERSENDT
            EksternStatus.Sendt -> FERDIGSTILT
            EksternStatus.Ferdigstilt -> FERDIGSTILT
        }
    }
}
