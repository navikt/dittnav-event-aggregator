package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjederAktivflagg
import no.nav.personbruker.dittnav.eventaggregator.common.database.BrukernotifikasjonRepository
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.ListPersistActionResult
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getBrukernotifikasjonFromViewForEventIds
import no.nav.personbruker.dittnav.eventaggregator.common.database.util.persistEachIndividuallyAndAggregateResults
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksEventerAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaverAktivFlag
import java.time.LocalDateTime

class DoneRepository(private val database: Database) : BrukernotifikasjonRepository<Done> {

    override suspend fun createInOneBatch(entities: List<Done>): ListPersistActionResult<Done> {
        return database.queryWithExceptionTranslation {
            createDoneEvents(entities)
        }
    }

    override suspend fun createOneByOneToFilterOutTheProblematicEvents(entities: List<Done>): ListPersistActionResult<Done> {
        return database.queryWithExceptionTranslation {
            entities.persistEachIndividuallyAndAggregateResults { doneEvent ->
                createDoneEvent(doneEvent)
            }
        }
    }

    suspend fun writeDoneEventsForBeskjedToCache(doneEvents: List<Done>) {
        if (doneEvents.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            setBeskjederAktivflagg(doneEvents, false)
        }
    }

    suspend fun writeDoneEventsForOppgaveToCache(doneEvents: List<Done>) {
        if (doneEvents.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            setOppgaverAktivFlag(doneEvents, false)
        }
    }

    suspend fun writeDoneEventsForInnboksToCache(doneEvents: List<Done>) {
        if (doneEvents.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            setInnboksEventerAktivFlag(doneEvents, false)
        }
    }

    suspend fun fetchBrukernotifikasjonerFromViewForEventIds(eventIds: List<String>): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewForEventIds(eventIds)
        }
        return resultat
    }

    suspend fun fetchAllDoneEventsWithLimit(): List<Done> {
        var resultat = emptyList<Done>()
        database.queryWithExceptionTranslation {
            resultat = getAllDoneEventWithLimit(2500)
        }
        return resultat
    }

    suspend fun deleteDoneEventsFromCache(doneEventsToDelete: List<Done>) {
        database.queryWithExceptionTranslation {
            deleteDoneEvents(doneEventsToDelete)
        }
    }

    suspend fun updateDoneEventsSistBehandlet(doneEvents: List<Done>, sistBehandlet: LocalDateTime) {
        database.queryWithExceptionTranslation {
            updateDoneSistbehandlet(doneEvents, sistBehandlet)
        }
    }
}
