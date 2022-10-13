package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.Brukernotifikasjon

class DonePersistingService(private val doneRepository: DoneRepository) {

    suspend fun writeDoneEventsForBeskjedToCache(doneEvents: List<Done>) {
        doneRepository.writeDoneEventsForBeskjedToCache(doneEvents)
    }

    suspend fun writeDoneEventsForOppgaveToCache(doneEvents: List<Done>) {
        doneRepository.writeDoneEventsForOppgaveToCache(doneEvents)
    }

    suspend fun writeDoneEventsForInnboksToCache(doneEvents: List<Done>) {
        doneRepository.writeDoneEventsForInnboksToCache(doneEvents)
    }

    suspend fun fetchBrukernotifikasjonerFromViewForEventIds(eventIds: List<String>): List<Brukernotifikasjon> {
        return doneRepository.fetchBrukernotifikasjonerFromViewForEventIds(eventIds)
    }

    suspend fun fetchAllDoneEventsWithLimit(): List<Done> {
        return doneRepository.fetchAllDoneEventsWithLimit()
    }

    suspend fun deleteDoneEventsFromCache(doneEventsToDelete: List<Done>) {
        doneRepository.deleteDoneEventsFromCache(doneEventsToDelete)
    }

    suspend fun updateDoneSistBehandetForUnmatchedEvents(doneEvents: List<Done>) {
        doneRepository.updateDoneEventsSistBehandlet(doneEvents, nowAtUtc())
    }
}
