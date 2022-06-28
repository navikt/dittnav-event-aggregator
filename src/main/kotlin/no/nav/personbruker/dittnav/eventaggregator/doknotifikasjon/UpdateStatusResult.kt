package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus

data class UpdateStatusResult(
    val updatedStatuses: List<DoknotifikasjonStatus>,
    val unchangedStatuses: List<DoknotifikasjonStatus>,
    val unmatchedStatuses: List<DoknotifikasjonStatus>
)
