package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.DoknotifikasjonStatusEnum.*
import no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon.EksternStatus.*
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselHeader
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselRepository

class EksternVarslingStatusUpdater(
    private val eksternVarslingStatusRepository: EksternVarslingStatusRepository,
    private val varselRepository: VarselRepository,
    private val eksternVarslingOppdatertProducer: EksternVarslingOppdatertProducer
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

        eksternVarslingOppdatertProducer.eksternStatusOppdatert(mergeOppdatering(varsel, newStatus))
    }

    private fun mergeStatuses(oldStatus: DoknotifikasjonStatusDto, newStatus: DoknotifikasjonStatusDto): DoknotifikasjonStatusDto {
        val kanaler = (oldStatus.kanaler + newStatus.kanaler).distinct()

        return newStatus.copy(
            kanaler = kanaler,
            antallOppdateringer = oldStatus.antallOppdateringer + 1
        )
    }

    private fun mergeOppdatering(varsel: VarselHeader, statusDto: DoknotifikasjonStatusDto) = EksternStatusOppdatering(
        status = determineStatus(statusDto),
        kanal = statusDto.kanaler.firstOrNull(),
        varselType = varsel.type,
        eventId = varsel.eventId,
        namespace = varsel.namespace,
        appnavn = varsel.appnavn
    )

    private fun determineStatus(statusDto: DoknotifikasjonStatusDto): EksternStatus {
        return when(statusDto.status) {
            FERDIGSTILT.name -> if (statusDto.kanaler.isNotEmpty()) Sendt else Ferdigstilt
            INFO.name -> Info
            FEILET.name -> Feilet
            OVERSENDT.name -> Bestilt
            else -> throw IllegalArgumentException("Kjente ikke igjen doknotifikasjon status ${statusDto.status}.")
        }
    }
}
