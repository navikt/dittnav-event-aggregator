package no.nav.personbruker.dittnav.eventaggregator.doknotifikasjon

import no.nav.doknotifikasjon.schemas.DoknotifikasjonStatus
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

    suspend fun updateStatusForBeskjed(dokStatus: List<DoknotifikasjonStatus>): UpdateStatusResult {
        val eventIdRef = dokStatus.map { it.getBestillingsId() }

        val beskjedCandidates = beskjedRepository.getBeskjedWithEksternVarslingForEventIds(eventIdRef)

        val matchingStatuses = matchBeskjedWithDokStatus(beskjedCandidates, dokStatus)

        val persistResult = doknotifikasjonRepository.updateStatusesForBeskjed(matchingStatuses)

        val unmatchedStatuses = dokStatus - matchingStatuses

        return UpdateStatusResult(
            updatedStatuses = persistResult.getPersistedEntitites(),
            unchangedStatuses = persistResult.getUnalteredEntities(),
            unmatchedStatuses = unmatchedStatuses
        )
    }

    suspend fun updateStatusForOppgave(dokStatus: List<DoknotifikasjonStatus>): UpdateStatusResult {
        val bestillingsIdsToMatch = dokStatus.map { it.getBestillingsId() }

        val oppgaveCandidates = oppgaveRepository.getOppgaveWithEksternVarslingForEventIds(bestillingsIdsToMatch)

        val matchingStatuses = matchOppgaveWithDokStatus(oppgaveCandidates, dokStatus)

        val persistResult = doknotifikasjonRepository.updateStatusesForOppgave(matchingStatuses)

        val unmatchedStatuses = dokStatus - matchingStatuses

        return UpdateStatusResult(
            updatedStatuses = persistResult.getPersistedEntitites(),
            unchangedStatuses = persistResult.getUnalteredEntities(),
            unmatchedStatuses = unmatchedStatuses
        )
    }

    suspend fun updateStatusForInnboks(dokStatus: List<DoknotifikasjonStatus>): UpdateStatusResult {
        val bestillingsIdsToMatch = dokStatus.map { it.getBestillingsId() }

        val innboksCandidates = innboksRepository.getInnboksWithEksternVarslingForEventIds(bestillingsIdsToMatch)

        val matchingStatuses = matchInnboksWithDokStatus(innboksCandidates, dokStatus)

        val persistResult = doknotifikasjonRepository.updateStatusesForInnboks(matchingStatuses)

        val unmatchedStatuses = dokStatus - matchingStatuses

        return UpdateStatusResult(
            updatedStatuses = persistResult.getPersistedEntitites(),
            unchangedStatuses = persistResult.getUnalteredEntities(),
            unmatchedStatuses = unmatchedStatuses
        )
    }

    private fun matchBeskjedWithDokStatus(beskjedCandidates: List<Beskjed>,
                                          dokStatus: List<DoknotifikasjonStatus>): List<DoknotifikasjonStatus> {

        val appnavnEventIds = beskjedCandidates.map { it.appnavn to it.eventId }

        return dokStatus.filter { appnavnEventIds.contains(it.getBestillerId() to it.getBestillingsId()) }
    }

    private fun matchOppgaveWithDokStatus(oppgaveCandidates: List<Oppgave>,
                                          dokStatus: List<DoknotifikasjonStatus>): List<DoknotifikasjonStatus> {

        val appnavnEventIds = oppgaveCandidates.map { it.appnavn to it.eventId }

        return dokStatus.filter { appnavnEventIds.contains(it.getBestillerId() to it.getBestillingsId()) }
    }

    private fun matchInnboksWithDokStatus(innboksCandidates: List<Innboks>,
                                          dokStatus: List<DoknotifikasjonStatus>): List<DoknotifikasjonStatus> {

        val appnavnEventIds = innboksCandidates.map { it.appnavn to it.eventId }

        return dokStatus.filter { appnavnEventIds.contains(it.getBestillerId() to it.getBestillingsId()) }
    }
}
