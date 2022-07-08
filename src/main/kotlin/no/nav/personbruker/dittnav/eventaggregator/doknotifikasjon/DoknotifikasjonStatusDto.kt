package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

data class DoknotifikasjonStatusDto(
    val eventId: String,
    val bestillerAppnavn: String,
    val status: String,
    val melding: String,
    val distribusjonsId: Long?,
    val kanaler: List<String>,
    val antallOppdateringer: Int = 1
)
