package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.personbruker.dittnav.eventaggregator.beskjed.Beskjed
import no.nav.personbruker.dittnav.eventaggregator.beskjed.BeskjedRepository
import no.nav.personbruker.dittnav.eventaggregator.innboks.Innboks
import no.nav.personbruker.dittnav.eventaggregator.innboks.InnboksRepository
import no.nav.personbruker.dittnav.eventaggregator.oppgave.Oppgave
import no.nav.personbruker.dittnav.eventaggregator.oppgave.OppgaveRepository

class DoknotifikasjonStatusUpdater(
    private val beskjedRepository: BeskjedRepository,
    private val oppgaveRepository: OppgaveRepository,
    private val innboksRepository: InnboksRepository,
    private val doknotifikasjonRepository: DoknotifikasjonStatusRepository) {


    suspend fun updateStatusForBeskjed(dokStatus: List<DoknotifikasjonStatusDto>): UpdateStatusResult {
        val eventIds = dokStatus.map { it.eventId }.distinct()

        val beskjedCandidates = beskjedRepository.getBeskjedWithEksternVarslingForEventIds(eventIds)
        val matchingStatuses = matchBeskjedWithDokStatus(beskjedCandidates, dokStatus)

        val existingStatuses = doknotifikasjonRepository.getStatusesForBeskjed(eventIds)
        val applyStatusResult = applyUpdatesInMemory(existingStatuses, matchingStatuses)

        val persistResult = doknotifikasjonRepository.updateStatusesForBeskjed(applyStatusResult)

        val unmatchedStatuses = dokStatus - matchingStatuses

        return UpdateStatusResult(
            updatedStatuses = persistResult.getPersistedEntitites(),
            unmatchedStatuses = unmatchedStatuses
        )
    }

    suspend fun updateStatusForOppgave(dokStatus: List<DoknotifikasjonStatusDto>): UpdateStatusResult {
        val eventIds = dokStatus.map { it.eventId }.distinct()

        val oppgaveCandidates = oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(eventIds)
        val matchingStatuses = matchOppgaveWithDokStatus(oppgaveCandidates, dokStatus)

        val existingStatuses = doknotifikasjonRepository.getStatusesForOppgave(eventIds)
        val applyStatusResult = applyUpdatesInMemory(existingStatuses, matchingStatuses)

        val persistResult = doknotifikasjonRepository.updateStatusesForOppgave(applyStatusResult)

        val unmatchedStatuses = dokStatus - matchingStatuses

        return UpdateStatusResult(
            updatedStatuses = persistResult.getPersistedEntitites(),
            unmatchedStatuses = unmatchedStatuses
        )
    }

    suspend fun updateStatusForInnboks(dokStatus: List<DoknotifikasjonStatusDto>): UpdateStatusResult {
        val eventIds = dokStatus.map { it.eventId }.distinct()

        val innboksCandidates = innboksRepository.getInnboksWithEksternVarslingForEventIds(eventIds)
        val matchingStatuses = matchInnboksWithDokStatus(innboksCandidates, dokStatus)

        val existingStatuses = doknotifikasjonRepository.getStatusesForInnboks(eventIds)
        val applyStatusResult = applyUpdatesInMemory(existingStatuses, matchingStatuses)

        val persistResult = doknotifikasjonRepository.updateStatusesForInnboks(applyStatusResult)

        val unmatchedStatuses = dokStatus - matchingStatuses

        return UpdateStatusResult(
            updatedStatuses = persistResult.getPersistedEntitites(),
            unmatchedStatuses = unmatchedStatuses
        )
    }

    private fun matchBeskjedWithDokStatus(beskjedCandidates: List<Beskjed>,
                                          dokStatus: List<DoknotifikasjonStatusDto>): List<DoknotifikasjonStatusDto> {

        val appnavnEventIds = beskjedCandidates.map { it.appnavn to it.eventId }

        return dokStatus.filter { appnavnEventIds.contains(it.bestillerAppnavn to it.eventId) }
    }

    private fun matchOppgaveWithDokStatus(oppgaveCandidates: List<Oppgave>,
                                          dokStatus: List<DoknotifikasjonStatusDto>): List<DoknotifikasjonStatusDto> {

        val appnavnEventIds = oppgaveCandidates.map { it.appnavn to it.eventId }

        return dokStatus.filter { appnavnEventIds.contains(it.bestillerAppnavn to it.eventId) }
    }

    private fun matchInnboksWithDokStatus(innboksCandidates: List<Innboks>,
                                          dokStatus: List<DoknotifikasjonStatusDto>): List<DoknotifikasjonStatusDto> {

        val appnavnEventIds = innboksCandidates.map { it.appnavn to it.eventId }

        return dokStatus.filter { appnavnEventIds.contains(it.bestillerAppnavn to it.eventId) }
    }

    private fun applyUpdatesInMemory(existingStatuses: List<DoknotifikasjonStatusDto>, toApply: List<DoknotifikasjonStatusDto>): List<DoknotifikasjonStatusDto> {
        val statusesByEventId = existingStatuses.associateBy { it.eventId }.toMutableMap()

        for (status in toApply) {
            statusesByEventId.merge(status.eventId, status, this::mergeStatuses)
        }

        return statusesByEventId.values.toList()
    }

    private fun mergeStatuses(oldStatus: DoknotifikasjonStatusDto, newStatus: DoknotifikasjonStatusDto): DoknotifikasjonStatusDto {
        val kanaler = (oldStatus.kanaler + newStatus.kanaler).distinct()

        return newStatus.copy(
            kanaler = kanaler,
            antallOppdateringer = oldStatus.antallOppdateringer + 1
        )
    }
}
