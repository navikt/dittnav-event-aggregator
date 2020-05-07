package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjedAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getBrukernotifikasjonFromViewForEventIds
import no.nav.personbruker.dittnav.eventaggregator.innboks.setInnboksAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.oppgave.setOppgaveAktivFlag

class DoneRepository(private val database: Database) {

    suspend fun writeDoneEventsForBeskjedToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                setBeskjedAktivFlag(entity.eventId, entity.systembruker, entity.fodselsnummer, false)
            }
        }
    }

    suspend fun writeDoneEventsForOppgaveToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                setOppgaveAktivFlag(entity.eventId, entity.systembruker, entity.fodselsnummer, false)
            }
        }
    }

    suspend fun writeDoneEventsForInnboksToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                setInnboksAktivFlag(entity.eventId, entity.systembruker, entity.fodselsnummer, false)
            }
        }
    }

    suspend fun writeDoneEventToCache(entities: List<Done>) {
        if (entities.isEmpty()) {
            return
        }
        database.queryWithExceptionTranslation {
            entities.forEach { entity ->
                createDoneEvent(entity)
            }
        }
    }

    suspend fun fetchBrukernotifikasjonerFromViewForEventIds(eventIds: List<String>): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewForEventIds(eventIds)
        }
        return resultat
    }

    suspend fun fetchAllDoneEvents(): List<Done> {
        var resultat = emptyList<Done>()
        database.queryWithExceptionTranslation {
            resultat = getAllDoneEvent()
        }
        return resultat
    }

    suspend fun deleteDoneEventFromCache(doneEventsToDelete: List<Done>) {
        database.queryWithExceptionTranslation {
            doneEventsToDelete.forEach { doneEvent ->
                deleteDoneEvent(doneEvent)
            }
        }
    }

}
