package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.common.LocalDateTimeHelper.nowAtUtc
import no.nav.personbruker.dittnav.eventaggregator.common.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.varsel.VarselType

class DonePersistingService(private val doneRepository: DoneRepository) {

    suspend fun updateVarselTables(doneEvents: List<Done>, varselType: VarselType) {
        doneRepository.updateVarselTables(doneEvents,varselType)
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
