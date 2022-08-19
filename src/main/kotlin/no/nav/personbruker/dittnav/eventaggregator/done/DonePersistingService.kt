package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper
import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonPersistingService
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import java.time.LocalDateTime
import java.time.ZoneId

class DonePersistingService(private val doneRepository: DoneRepository) : BrukernotifikasjonPersistingService<Done>(doneRepository) {

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
