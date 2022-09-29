package no.nav.personbruker.dittnav.eventaggregator.varsel.eksternvarslingstatus

import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusDto
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository

class EksternVarslingStatusUpdater(
    private val eksternVarslingStatusRepository: EksternVarslingStatusRepository,
    private val varselRepository: VarselRepository,
) {

    suspend fun insertOrUpdateStatus(newStatus: DoknotifikasjonStatusDto) {
        val varsler = varselRepository.getVarsel(newStatus.eventId)
        if (varsler.isEmpty()) return

        val varselType = varsler.first().type
        val existingStatus = eksternVarslingStatusRepository.getStatusIfExists(newStatus.eventId, varselType)

        val statusToPersist = existingStatus?.let {
            mergeStatuses(it, newStatus)
        } ?: newStatus

        eksternVarslingStatusRepository.updateStatus(statusToPersist, varselType)
    }

    private fun mergeStatuses(oldStatus: DoknotifikasjonStatusDto, newStatus: DoknotifikasjonStatusDto): DoknotifikasjonStatusDto {
        val kanaler = (oldStatus.kanaler + newStatus.kanaler).distinct()

        return newStatus.copy(
            kanaler = kanaler,
            antallOppdateringer = oldStatus.antallOppdateringer + 1
        )
    }
}