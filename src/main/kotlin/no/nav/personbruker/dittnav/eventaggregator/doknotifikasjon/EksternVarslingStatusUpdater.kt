package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository

class EksternVarslingStatusUpdater(
    private val eksternVarslingStatusRepository: EksternVarslingStatusRepository,
    private val varselRepository: VarselRepository,
) {

    suspend fun insertOrUpdateStatus(newStatus: DoknotifikasjonStatusDto) {
        val varsel = varselRepository.getVarsel(newStatus.eventId)

        if (varsel == null) {
            return
        }

        val existingStatus = eksternVarslingStatusRepository.getStatusIfExists(newStatus.eventId, varsel.type)

        val statusToPersist = existingStatus?.let {
            mergeStatuses(it, newStatus)
        } ?: newStatus

        eksternVarslingStatusRepository.updateStatus(statusToPersist, varsel.type)
    }

    private fun mergeStatuses(oldStatus: DoknotifikasjonStatusDto, newStatus: DoknotifikasjonStatusDto): DoknotifikasjonStatusDto {
        val kanaler = (oldStatus.kanaler + newStatus.kanaler).distinct()

        return newStatus.copy(
            kanaler = kanaler,
            antallOppdateringer = oldStatus.antallOppdateringer + 1
        )
    }
}
