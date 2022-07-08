package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

data class UpdateStatusResult(
    val updatedStatuses: List<DoknotifikasjonStatusDto>,
    val unmatchedStatuses: List<DoknotifikasjonStatusDto>
)
