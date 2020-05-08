package no.nav.personbruker.dittnav.eventaggregator.done

import no.nav.personbruker.dittnav.eventaggregator.beskjed.setBeskjedAktivFlag
import no.nav.personbruker.dittnav.eventaggregator.common.database.Database
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.Brukernotifikasjon
import no.nav.personbruker.dittnav.eventaggregator.common.database.entity.getBrukernotifikasjonFromViewByAktiv
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

    suspend fun fetchActiveBrukernotifikasjonerFromView(): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewByAktiv(true)
        }
        return resultat
    }

    suspend fun fetchInaktiveBrukernotifikasjonerFromView(): List<Brukernotifikasjon> {
        var resultat = emptyList<Brukernotifikasjon>()
        database.queryWithExceptionTranslation {
            resultat = getBrukernotifikasjonFromViewByAktiv(false)
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
