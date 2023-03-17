package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDateTime

data class EksternVarslingStatus(
    val eventId: String,
    val eksternVarslingSendt: Boolean,
    val renotifikasjonSendt: Boolean,
    val kanaler: List<String>,
    val sistMottattStatus: String,
    val historikk: List<EksternVarslingHistorikkEntry>,
    val sistOppdatert: LocalDateTime
)

data class EksternVarslingHistorikkEntry(
    val melding: String,
    val status: EksternStatus,
    val distribusjonsId: Long?,
    val kanal: String?,
    val renotifikasjon: Boolean?,
    val tidspunkt: LocalDateTime
)

enum class EksternStatus {
    Feilet, Info, Bestilt, Sendt, Ferdigstilt;

    val lowercaseName = name.lowercase()

    @JsonValue
    fun toJson() = lowercaseName
}
